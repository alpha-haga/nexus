package nexus.bff.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nexus.core.region.Region
import nexus.core.region.RegionContext
import nexus.infrastructure.db.CorporationContext
import nexus.infrastructure.db.DomainAccount
import nexus.infrastructure.db.DomainAccountContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * P1-1: BFF 認可 + Context set フィルター
 */
@Component
class NexusAuthorizationContextFilter(
    private val environment: Environment,
) : OncePerRequestFilter() {

    private val logger: Logger = LoggerFactory.getLogger(NexusAuthorizationContextFilter::class.java)

    companion object {
        private const val API_PREFIX = "/api/v1/"

        private const val REGION_HEADER = "X-NEXUS-REGION"
        private const val CORP_HEADER = "X-NEXUS-CORP"

        private const val INTEGRATION_ROLE = "integration__ALL__GROUP"
        private const val SEP = "__"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: return true
        return !path.startsWith(API_PREFIX)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val path = request.requestURI ?: ""

            val route = determineRouteOrRespond404(path, response) ?: return
            val roles = extractDbAccessRolesOrRespond403(response) ?: return

            when (route) {
                is Route.Integration -> authorizeIntegrationOrRespond403(roles, response) ?: return
                is Route.Region -> authorizeRegionOrRespond403(route.domainAccount, roles, request, response) ?: return
            }

            filterChain.doFilter(request, response)
        } finally {
            RegionContext.clear()
            CorporationContext.clear()
            DomainAccountContext.clear()
        }
    }

    private sealed class Route {
        data object Integration : Route()
        data class Region(val domainAccount: DomainAccount) : Route()
    }

    private fun determineRouteOrRespond404(path: String, response: HttpServletResponse): Route? {
        return when {
            path.startsWith("/api/v1/gojo/") -> Route.Region(DomainAccount.GOJO)
            path.startsWith("/api/v1/funeral/") -> Route.Region(DomainAccount.FUNERAL)
            path.startsWith("/api/v1/group/") -> Route.Integration
            path.startsWith("/api/v1/identity/") -> Route.Integration
            path.startsWith("/api/v1/household/") -> Route.Integration
            else -> {
                response.sendError(HttpStatus.NOT_FOUND.value())
                null
            }
        }
    }

    private fun extractDbAccessRolesOrRespond403(response: HttpServletResponse): List<String>? {
        val authentication = SecurityContextHolder.getContext()?.authentication
        val jwt = when (authentication) {
            is JwtAuthenticationToken -> authentication.token
            else -> null
        }

        if (jwt == null) {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        val roles = runCatching { extractDbAccessRolesOrFail(jwt) }.getOrElse {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }
        return roles
    }

    private fun authorizeIntegrationOrRespond403(roles: List<String>, response: HttpServletResponse): Unit? {
        // case-insensitive 比較で integration__ALL__GROUP を判定
        val hasIntegrationRole = roles.any { it.equals(INTEGRATION_ROLE, ignoreCase = true) }
        if (!hasIntegrationRole) {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        RegionContext.set(Region.INTEGRATION)
        if (logger.isDebugEnabled) logger.debug("Authorized integration access, RegionContext=INTEGRATION")
        return Unit
    }

    private fun authorizeRegionOrRespond403(
        domainAccount: DomainAccount,
        roles: List<String>,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Unit? {
        val isLocal = environment.activeProfiles.any { it.equals("local", ignoreCase = true) }
        val headerRegion = request.getHeader(REGION_HEADER)
        val headerCorp = request.getHeader(CORP_HEADER)

        // local かつ両ヘッダーがある場合: ヘッダーで一意化（token の複数候補チェックを先にしない）
        if (isLocal && headerRegion != null && headerCorp != null) {
            val region = runCatching { Region.fromStringOrThrow(headerRegion) }.getOrElse {
                response.sendError(HttpStatus.FORBIDDEN.value())
                return null
            }
            if (region == Region.INTEGRATION) {
                response.sendError(HttpStatus.FORBIDDEN.value())
                return null
            }

            // requiredRole は raw role 形式で構築（DomainAccount.name は大文字固定）
            val requiredRole = "${region.name.lowercase()}$SEP${headerCorp.lowercase()}$SEP${domainAccount.name}"
            // case-insensitive 比較で判定
            val hasRequiredRole = roles.any { it.equals(requiredRole, ignoreCase = true) }
            if (!hasRequiredRole) {
                response.sendError(HttpStatus.FORBIDDEN.value())
                return null
            }

            RegionContext.set(region)
            CorporationContext.set(headerCorp.lowercase())
            DomainAccountContext.set(domainAccount)

            if (logger.isDebugEnabled) {
                logger.debug(
                    "Authorized region access (header-based). RegionContext=$region, CorporationContext=$headerCorp, DomainAccountContext=$domainAccount"
                )
            }

            return Unit
        }

        // ヘッダー無し or 片方欠け: token から決定し、複数候補チェックを行う
        val parsedForDomain = roles.mapNotNull { parseRoleOrNull(it) }
            .filter { it.domainAccount.equals(domainAccount.name, ignoreCase = true) }

        val regions = parsedForDomain.map { it.region.lowercase() }.distinct()
        val corps = parsedForDomain.map { it.corporation.lowercase() }.distinct()
        if (regions.size > 1 || corps.size > 1) {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        val sole = parsedForDomain.firstOrNull() ?: run {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        val region = runCatching { Region.fromStringOrThrow(sole.region) }.getOrElse {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }
        if (region == Region.INTEGRATION) {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        // requiredRole は raw role 形式で構築（DomainAccount.name は大文字固定）
        val requiredRole = "${region.name.lowercase()}$SEP${sole.corporation.lowercase()}$SEP${domainAccount.name}"
        // case-insensitive 比較で判定
        val hasRequiredRole = roles.any { it.equals(requiredRole, ignoreCase = true) }
        if (!hasRequiredRole) {
            response.sendError(HttpStatus.FORBIDDEN.value())
            return null
        }

        RegionContext.set(region)
        CorporationContext.set(sole.corporation.lowercase())
        DomainAccountContext.set(domainAccount)

        if (logger.isDebugEnabled) {
            logger.debug(
                "Authorized region access (token-based). RegionContext=$region, CorporationContext=${sole.corporation}, DomainAccountContext=$domainAccount"
            )
        }

        return Unit
    }

    private data class ParsedRole(
        val region: String,
        val corporation: String,
        val domainAccount: String,
    )

    private fun parseRoleOrNull(raw: String): ParsedRole? {
        val parts = raw.split(SEP)
        if (parts.size != 3) return null
        val (region, corp, domain) = parts
        if (region.isBlank() || corp.isBlank() || domain.isBlank()) return null
        return ParsedRole(region, corp, domain)
    }
}
package nexus.bff.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nexus.core.region.Region
import nexus.core.region.RegionContext
import nexus.infrastructure.db.CorporationContext
import nexus.infrastructure.db.DomainAccount
import nexus.infrastructure.db.DomainAccountContext
import org.slf4j.LoggerFactory
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * P1-1: BFF 認可 + Context set フィルター
 *
 * - request path から DomainAccount / integration API を判定
 * - access token の claim（nexus_db_access: List<String>）を取得
 * - 必要 role を照合し、未許可は 403 / 未定義APIは 404
 * - 許可された場合のみ Region / Corporation / DomainAccount Context を set
 * - finally で必ず clear（ThreadLocal リーク防止）
 *
 * local プロファイルのみ、検証用ヘッダーを併用可能:
 * - X-NEXUS-REGION
 * - X-NEXUS-CORP
 */
@Component
class NexusAuthorizationContextFilter(
    private val environment: Environment,
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(NexusAuthorizationContextFilter::class.java)

    companion object {
        private const val CLAIM_DB_ACCESS = "nexus_db_access"

        private const val HEADER_REGION = "X-NEXUS-REGION"
        private const val HEADER_CORP = "X-NEXUS-CORP"

        private const val PREFIX_GOJO = "/api/v1/gojo/"
        private const val PREFIX_FUNERAL = "/api/v1/funeral/"
        private const val PREFIX_GROUP = "/api/v1/group/"
        private const val PREFIX_IDENTITY = "/api/v1/identity/"
        private const val PREFIX_HOUSEHOLD = "/api/v1/household/"

        private const val ROLE_SEPARATOR = "__"
        private const val INTEGRATION_ROLE = "integration__ALL__GROUP"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        // securityFilterChain で permitAll にしているものはここでも素通し
        val path = request.requestURI
        return path.startsWith("/actuator/") || path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val scope = resolveRequestScope(request)
                ?: return respondNotFound(response)

            val dbAccessRoles = extractDbAccessRolesOrFail()

            if (dbAccessRoles.isEmpty()) {
                return respondForbidden(response, "Missing or empty claim: $CLAIM_DB_ACCESS")
            }

            when (scope) {
                is RequestScope.Integration -> {
                    authorizeIntegration(dbAccessRoles)
                    RegionContext.set(Region.INTEGRATION)
                }

                is RequestScope.Region -> {
                    val region = resolveRegion(request, dbAccessRoles, scope.domainAccount)
                    val corp = resolveCorporation(request, dbAccessRoles, region, scope.domainAccount)

                    val requiredRole = "${region.toRolePart()}${ROLE_SEPARATOR}${corp}${ROLE_SEPARATOR}${scope.domainAccount.name}"
                    if (!dbAccessRoles.contains(requiredRole)) {
                        return respondForbidden(response, "Missing required role: $requiredRole")
                    }

                    RegionContext.set(region)
                    CorporationContext.set(corp)
                    DomainAccountContext.set(scope.domainAccount)
                }
            }

            filterChain.doFilter(request, response)
        } catch (e: ForbiddenException) {
            return respondForbidden(response, e.message ?: "Forbidden")
        } finally {
            // clear は必ず finally
            RegionContext.clear()
            CorporationContext.clear()
            DomainAccountContext.clear()
        }
    }

    private fun extractDbAccessRolesOrFail(): Set<String> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw IllegalStateException("Authentication is missing")

        val jwt = (authentication as? JwtAuthenticationToken)?.token
            ?: throw IllegalStateException("JwtAuthenticationToken is missing")

        val claim = jwt.claims[CLAIM_DB_ACCESS]
            ?: return emptySet()

        val roles = when (claim) {
            is Collection<*> -> claim.filterIsInstance<String>()
            else -> emptyList()
        }

        val normalized = roles.map { it.trim() }.filter { it.isNotEmpty() }

        if (normalized.isEmpty()) {
            // P04-5: claim 不在/空は 403
            return emptySet()
        }

        return normalized.toSet()
    }

    private fun authorizeIntegration(dbAccessRoles: Set<String>) {
        if (!dbAccessRoles.contains(INTEGRATION_ROLE)) {
            throw ForbiddenException("Missing required role: $INTEGRATION_ROLE")
        }
    }

    private fun resolveRegion(
        request: HttpServletRequest,
        dbAccessRoles: Set<String>,
        domainAccount: DomainAccount
    ): Region {
        // local: header 優先（検証用）
        if (isLocalProfile()) {
            val regionHeader = request.getHeader(HEADER_REGION)?.trim()
            if (!regionHeader.isNullOrEmpty()) {
                return Region.fromStringOrThrow(regionHeader)
            }
        }

        val parsed = dbAccessRoles.mapNotNull { DbAccessRole.parseOrNull(it) }
        val candidates = parsed
            .filter { it.region != Region.INTEGRATION }
            .filter { it.domainAccount == domainAccount }
            .map { it.region }
            .distinct()

        if (candidates.isEmpty()) {
            throw ForbiddenException("No region role found for domainAccount=${domainAccount.name}")
        }
        if (candidates.size > 1) {
            throw ForbiddenException("Ambiguous region in token. Provide only one region or use local header: $HEADER_REGION")
        }
        return candidates.single()
    }

    private fun resolveCorporation(
        request: HttpServletRequest,
        dbAccessRoles: Set<String>,
        region: Region,
        domainAccount: DomainAccount
    ): String {
        // local: header 優先（検証用）
        if (isLocalProfile()) {
            val corpHeader = request.getHeader(HEADER_CORP)?.trim()
            if (!corpHeader.isNullOrEmpty()) {
                return corpHeader
            }
        }

        val parsed = dbAccessRoles.mapNotNull { DbAccessRole.parseOrNull(it) }
        val candidates = parsed
            .filter { it.region == region }
            .filter { it.domainAccount == domainAccount }
            .map { it.corporation }
            .distinct()

        if (candidates.isEmpty()) {
            throw ForbiddenException("No corporation role found for region=${region.toRolePart()}, domainAccount=${domainAccount.name}")
        }
        if (candidates.size > 1) {
            throw ForbiddenException("Ambiguous corporation in token. Provide only one corporation or use local header: $HEADER_CORP")
        }
        return candidates.single()
    }

    private fun resolveRequestScope(request: HttpServletRequest): RequestScope? {
        val path = request.requestURI
        return when {
            path.startsWith(PREFIX_GOJO) -> RequestScope.Region(DomainAccount.GOJO)
            path.startsWith(PREFIX_FUNERAL) -> RequestScope.Region(DomainAccount.FUNERAL)
            path.startsWith(PREFIX_GROUP) -> RequestScope.Integration
            path.startsWith(PREFIX_IDENTITY) -> RequestScope.Integration
            path.startsWith(PREFIX_HOUSEHOLD) -> RequestScope.Integration
            else -> null
        }
    }

    private fun respondNotFound(response: HttpServletResponse) {
        response.status = HttpStatus.NOT_FOUND.value()
    }

    private fun respondForbidden(response: HttpServletResponse, message: String) {
        if (logger.isDebugEnabled) {
            logger.debug("Forbidden: {}", message)
        }
        response.status = HttpStatus.FORBIDDEN.value()
    }

    private fun isLocalProfile(): Boolean {
        return environment.acceptsProfiles(Profiles.of("local"))
    }

    private fun Region.toRolePart(): String = name.lowercase()

    private sealed interface RequestScope {
        data object Integration : RequestScope
        data class Region(val domainAccount: DomainAccount) : RequestScope
    }

    private data class DbAccessRole(
        val region: Region,
        val corporation: String,
        val domainAccount: DomainAccount,
    ) {
        companion object {
            fun parseOrNull(raw: String): DbAccessRole? {
                val parts = raw.split(ROLE_SEPARATOR)
                if (parts.size != 3) return null

                val regionStr = parts[0].trim()
                val corp = parts[1].trim()
                val domainAccountStr = parts[2].trim()

                if (regionStr.isEmpty() || corp.isEmpty() || domainAccountStr.isEmpty()) return null

                val region = Region.fromString(regionStr) ?: return null
                val domainAccount = try {
                    DomainAccount.fromStringOrThrow(domainAccountStr)
                } catch (e: IllegalArgumentException) {
                    return null
                }
                return DbAccessRole(region = region, corporation = corp, domainAccount = domainAccount)
            }
        }
    }

    private class ForbiddenException(message: String) : RuntimeException(message)
}

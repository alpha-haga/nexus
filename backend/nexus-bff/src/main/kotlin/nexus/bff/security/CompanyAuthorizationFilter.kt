package nexus.bff.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nexus.core.auth.AccessDeniedException
import nexus.core.auth.CompanyNotAvailableException
import nexus.bff.security.CompanyResolverService
import nexus.core.exception.AuthorizationException
import nexus.core.exception.ValidationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 認可フィルター（cmpCd 検証）
 * 
 * すべてのAPIリクエストで X-Company-Code を受け取り、
 * ドメイン判定を行い、cmpCd の扱いを分岐する
 * 
 * 注意: SecurityFilterChain で明示的に登録されるため、@Component は使用しない
 */
class CompanyAuthorizationFilter(
    private val companyResolverService: CompanyResolverService,
) : OncePerRequestFilter() {

    private val logger: Logger = LoggerFactory.getLogger(CompanyAuthorizationFilter::class.java)

    companion object {
        private const val API_PREFIX = "/api/v1/"
        private const val COMPANY_CODE_HEADER = "X-Company-Code"
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI ?: return true
        
        // /api/v1/ 以外は除外
        if (!path.startsWith(API_PREFIX)) {
            return true
        }
        
        // 認証系APIは絶対に除外（二重ガード）
        if (path.startsWith("/api/v1/auth/")) {
            return true
        }
        
        return false
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        if (logger.isDebugEnabled) {
            logger.debug("CompanyAuthorizationFilter: processing request path=${request.requestURI}")
        }
        val path = request.requestURI ?: ""
        val handling = determineCmpCdHandling(path)

        // 認証不要パスはスキップ
        if (handling == CmpCdHandling.NOT_REQUIRED) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication = SecurityContextHolder.getContext().authentication
        val jwt = when (authentication) {
            is JwtAuthenticationToken -> authentication.token
            else -> null
        }

        if (jwt == null) {
            throw AuthorizationException("authenticate", "request", "Authentication required")
        }

        val roles = jwt.getClaimAsStringList("nexus_db_access") ?: emptyList()
        val cmpCd = request.getHeader(COMPANY_CODE_HEADER)

        when (handling) {
            CmpCdHandling.IGNORE -> {
                // cmpCd を無視（group/integration系）
                // 監査ログ用に記録するだけ
                if (cmpCd != null) {
                    MDC.put("cmpCd", cmpCd)
                    MDC.put("cmpCdHandling", "IGNORE")
                }
                filterChain.doFilter(request, response)
            }
            CmpCdHandling.VALIDATE_AND_USE -> {
                // cmpCd 検証必須
                if (cmpCd == null || cmpCd.isBlank()) {
                    throw ValidationException("X-Company-Code header is required", "X-Company-Code", "required")
                }

                val domain = extractDomainFromPath(path)

                try {
                    companyResolverService.validateAndResolve(roles, cmpCd, domain)

                    // MDC に設定
                    MDC.put("cmpCd", cmpCd)
                    MDC.put("cmpCdHandling", "VALIDATE_AND_USE")

                    if (logger.isDebugEnabled) {
                        logger.debug("Authorized company access. cmpCd=$cmpCd, domain=$domain")
                    }
                } catch (e: AccessDeniedException) {
                    // ロールに含まれない法人へのアクセスは拒否
                    throw e
                } catch (e: CompanyNotAvailableException) {
                    // is_active = '0' の場合は 503
                    throw e
                }

                filterChain.doFilter(request, response)
            }
            CmpCdHandling.NOT_REQUIRED -> {
                // 到達しない（上で早期リターン）
                filterChain.doFilter(request, response)
            }
        }
    }

    /**
     * ドメイン別 cmpCd 処理判定
     */
    private fun determineCmpCdHandling(uri: String): CmpCdHandling {
        return when {
            // 認証系: cmpCd 不要
            uri.startsWith("/api/v1/auth/") -> CmpCdHandling.NOT_REQUIRED
            
            // Integration 固定: cmpCd 無視
            uri.startsWith("/api/v1/group/") -> CmpCdHandling.IGNORE
            uri.startsWith("/api/v1/identity/") -> CmpCdHandling.IGNORE
            uri.startsWith("/api/v1/household/") -> CmpCdHandling.IGNORE
            
            // Point DB 固定: cmpCd 無視
            uri.startsWith("/api/v1/point/") -> CmpCdHandling.IGNORE
            
            // その他（gojo, funeral 等）: cmpCd 検証必須
            else -> CmpCdHandling.VALIDATE_AND_USE
        }
    }

    private fun extractDomainFromPath(uri: String): String? {
        return when {
            uri.startsWith("/api/v1/gojo/") -> "GOJO"
            uri.startsWith("/api/v1/funeral/") -> "FUNERAL"
            uri.startsWith("/api/v1/point/") -> "POINT"
            uri.startsWith("/api/v1/group/") -> "GROUP"
            else -> null
        }
    }

    /**
     * ドメイン別 cmpCd 処理判定
     */
    private enum class CmpCdHandling {
        VALIDATE_AND_USE,  // 検証し、法人スキーマへ接続
        IGNORE,            // 無視（Integration/Point 等の固定接続先）
        NOT_REQUIRED,      // 不要（認証系 API）
    }
}

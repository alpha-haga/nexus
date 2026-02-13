package nexus.bff.controller.auth

import nexus.bff.controller.auth.dto.AvailableCompanyDto
import nexus.bff.controller.auth.dto.BootstrapResponse
import nexus.bff.controller.auth.dto.UserInfoDto
import nexus.bff.security.CompanyResolverService
import nexus.core.auth.RoleParser
import nexus.core.exception.AuthorizationException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 認証コントローラー
 */
@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val companyResolverService: CompanyResolverService,
) {

    private val logger: Logger = LoggerFactory.getLogger(AuthController::class.java)

    /**
     * 初期化用API（1回で必要な情報を全て返す）
     * 
     * ログイン後にフロントエンドから最初に呼び出し、
     * ユーザー情報・利用可能法人を一括取得する。
     */
    @GetMapping("/bootstrap")
    fun bootstrap(
        @AuthenticationPrincipal jwt: Jwt
    ): BootstrapResponse {
        // 調査用ログ: JWT claims の確認（デバッグ専用）
        logger.info("jwt claims keys={}", jwt.claims.keys)
        val scope = jwt.getClaimAsString("scope")
        logger.info("jwt scope={}", scope)
        val azp = jwt.getClaimAsString("azp")
        val subClaim = jwt.getClaimAsString("sub")
        logger.info(
            "jwt debug: sub={}, subject={}, issuer={}, typ={}, azp={}, audience={}",
            subClaim,
            jwt.subject,
            jwt.issuer,
            jwt.getClaimAsString("typ"),
            azp,
            jwt.audience
        )
        logger.info(
            "jwt debug: preferred_username={}, email={}, iss={}, aud={}",
            jwt.getClaimAsString("preferred_username"),
            jwt.getClaimAsString("email"),
            jwt.getClaimAsString("iss"),
            jwt.getClaimAsString("aud")
        )

        // sub claim を取得（暫定回避: null 許容）
        val sub = jwt.subject ?: subClaim
        if (sub == null) {
            logger.warn("JWT missing 'sub' claim. This is unexpected for OIDC tokens. azp=$azp, scope=$scope")
            // 暫定回避: sub が null でも業務を継続（500 エラーを避ける）
            // 代替で preferred_username を sub に詰めない（意味が違うため）
        }

        val roles = jwt.getClaimAsStringList("nexus_db_access") ?: emptyList()
        val companies = companyResolverService.resolveAvailableCompanies(roles)

        return BootstrapResponse(
            user = UserInfoDto(
                sub = sub,
                username = jwt.getClaimAsString("preferred_username"),
                email = jwt.getClaimAsString("email"),
            ),
            roles = roles,
            availableCompanies = companies.map { cmp ->
                AvailableCompanyDto(
                    cmpCd = cmp.cmpCd,
                    companyName = cmp.companyName,
                    companyNameShort = cmp.companyNameShort,
                    availableDomains = cmp.availableDomains.toList(),
                )
            },
            hasIntegrationAccess = RoleParser.hasIntegrationAccess(roles),
        )
    }
}

package nexus.bff.controller.auth

import nexus.bff.controller.auth.dto.AvailableCompanyDto
import nexus.bff.controller.auth.dto.BootstrapResponse
import nexus.bff.controller.auth.dto.UserInfoDto
import nexus.bff.security.CompanyResolverService
import nexus.core.auth.RoleParser
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
        val roles = jwt.getClaimAsStringList("nexus_db_access") ?: emptyList()
        val companies = companyResolverService.resolveAvailableCompanies(roles)

        return BootstrapResponse(
            user = UserInfoDto(
                sub = jwt.subject,
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

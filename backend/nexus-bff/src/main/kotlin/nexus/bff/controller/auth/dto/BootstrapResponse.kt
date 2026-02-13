package nexus.bff.controller.auth.dto

data class BootstrapResponse(
    val user: UserInfoDto,
    val roles: List<String>,
    val availableCompanies: List<AvailableCompanyDto>,
    val hasIntegrationAccess: Boolean,
)

data class UserInfoDto(
    val sub: String?,  // 暫定回避: null 許容（OIDC 的には異常だが業務継続のため）
    val username: String?,
    val email: String?,
)

data class AvailableCompanyDto(
    val cmpCd: String,
    val companyName: String,
    val companyNameShort: String?,
    val availableDomains: List<String>,
)

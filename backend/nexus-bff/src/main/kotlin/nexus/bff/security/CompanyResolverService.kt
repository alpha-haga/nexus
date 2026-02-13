package nexus.bff.security

import nexus.core.auth.AccessDeniedException
import nexus.core.auth.CompanyNotAvailableException
import nexus.core.auth.RoleParser
import nexus.identity.query.CompanyMasterQueryService
import org.springframework.stereotype.Service

/**
 * 法人解決サービス
 * 
 * ロールから利用可能法人を解決し、cmpCd の検証を行う
 */
@Service
class CompanyResolverService(
    private val companyMasterQueryService: CompanyMasterQueryService,
) {

    /**
     * 利用可能法人情報
     */
    data class AvailableCompany(
        val cmpCd: String,
        val companyName: String,
        val companyNameShort: String?,
        val regionCd: String,
        val companyCd: String,
        val availableDomains: Set<String>,
        val displayOrder: Int,
    )

    /**
     * ロールから利用可能法人を解決
     * is_active = '1' の法人のみ返す
     */
    fun resolveAvailableCompanies(roles: List<String>): List<AvailableCompany> {
        val companyDomains = RoleParser.getAvailableCompanies(roles)

        return companyDomains.mapNotNull { (key, domains) ->
            val (region, company) = key.split("__")
            companyMasterQueryService.findByRegionAndCompany(region, company)?.let { cmp ->
                // is_active = '0' の法人は除外
                if (cmp.isActive != "1") {
                    return@let null
                }

                // マスタで定義されているドメインとロールの交差
                val masterDomains = cmp.availableDomains
                val effectiveDomains = domains.intersect(masterDomains)

                if (effectiveDomains.isNotEmpty()) {
                    AvailableCompany(
                        cmpCd = cmp.cmpCd,
                        companyName = cmp.companyName,
                        companyNameShort = cmp.companyNameShort,
                        regionCd = cmp.regionCd,
                        companyCd = cmp.companyCd,
                        availableDomains = effectiveDomains,
                        displayOrder = cmp.displayOrder,
                    )
                } else {
                    null
                }
            }
        }.sortedBy { it.displayOrder }
    }

    /**
     * cmpCd からアクセス可能か検証
     * 検証順序: master取得 → role検証(403) → is_active検証(503) → domain交差(403)
     * @throws AccessDeniedException ロールに含まれない場合、またはドメイン不一致（403）
     * @throws CompanyNotAvailableException is_active = '0' の場合（503）
     */
    fun validateAndResolve(roles: List<String>, cmpCd: String, domain: String? = null) {
        // 1. master取得
        val companyMaster = companyMasterQueryService.findByCmpCd(cmpCd)
            ?: throw AccessDeniedException("Company master not found: $cmpCd")

        // 2. role検証（403）
        val companyDomains = RoleParser.getAvailableCompanies(roles)
        val roleKey = "${companyMaster.regionCd.lowercase()}__${companyMaster.companyCd.lowercase()}"
        val roleDomains = companyDomains[roleKey]
            ?: throw AccessDeniedException("Access denied to company: $cmpCd")

        // 3. is_active検証（503）- ロールがあるがinactiveの場合は必ず503
        if (companyMaster.isActive != "1") {
            throw CompanyNotAvailableException("Company is not available: $cmpCd")
        }

        // 4. domain交差検証（403）
        val masterDomains = companyMaster.availableDomains
        val effectiveDomains = roleDomains.intersect(masterDomains)
        if (effectiveDomains.isEmpty()) {
            throw AccessDeniedException("Access denied to company: $cmpCd (no matching domains)")
        }

        // 5. ドメイン指定がある場合はドメインもチェック（403）
        if (domain != null && !effectiveDomains.contains(domain.uppercase())) {
            throw AccessDeniedException("Access denied to domain: $domain in company: $cmpCd")
        }
    }
}

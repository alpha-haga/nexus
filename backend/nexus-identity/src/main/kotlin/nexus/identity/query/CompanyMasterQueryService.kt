package nexus.identity.query

import nexus.core.auth.Company

/**
 * 法人マスタ QueryService インターフェース
 * 
 * 実装は infrastructure 層に配置
 * Integration DB の NXCM_COMPANY テーブルから取得
 */
interface CompanyMasterQueryService {
    /**
     * region_cd と company_cd で法人を検索
     */
    fun findByRegionAndCompany(regionCd: String, companyCd: String): Company?

    /**
     * cmp_cd で法人を検索
     */
    fun findByCmpCd(cmpCd: String): Company?

    /**
     * is_active = '1' の法人一覧を取得
     */
    fun findAllActive(): List<Company>
}

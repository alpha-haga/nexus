package nexus.group.query

/**
 * 法人マスタ一覧のクエリサービス（インターフェース）
 *
 * Read 導線の入口として定義。
 * 実装は infrastructure 層に配置（JdbcCompanyQueryService）
 */
interface CompanyQueryService {
    /**
     * 法人マスタ一覧を取得
     *
     * @return 法人一覧（CompanyDto のリスト）
     */
    fun findAll(): List<CompanyDto>
}

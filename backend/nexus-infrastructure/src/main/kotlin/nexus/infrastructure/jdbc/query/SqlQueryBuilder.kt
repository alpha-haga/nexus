package nexus.infrastructure.jdbc.query

import nexus.infrastructure.jdbc.SqlLoader

/**
 * SQL クエリ構築ビルダー
 *
 * P2-5: target/search の2段階SQL構造に対応
 * - COUNT: target SQL を wrap して生成
 * - SELECT_*: search SQL を wrap + where + orderBy + offset/fetch
 * - RAW: SQL をそのまま返す（加工禁止）
 *
 * @param C 検索条件の型
 * @param sqlLoader SQL ローダー
 * @param targetSqlPath target SQL のパス（COUNT用、例: "group/group_contract_target.sql"）
 * @param searchSqlPath search SQL のパス（SELECT_*用、例: "group/group_contract_search.sql"）
 * @param rawSqlPath raw SQL のパス（RAW用、オプショナル）
 * @param conditionApplier 検索条件を WHERE 句に適用する関数
 * @param orderByBuilder ORDER BY 句構築ビルダー
 */
class SqlQueryBuilder<C>(
    private val sqlLoader: SqlLoader,
    private val targetSqlPath: String,
    private val searchSqlPath: String,
    private val rawSqlPath: String? = null,
    private val conditionApplier: ConditionApplier<C>,
    private val orderByBuilder: OrderByBuilder
) {
    /**
     * クエリを構築
     *
     * @param mode クエリ実行モード
     * @param condition 検索条件
     * @param sortKey ソートキー（null の場合はデフォルトソート）
     * @param sortDir ソート方向（"asc"/"desc"、null の場合は ASC）
     * @param page ページネーション仕様（SELECT_PAGED の場合は必須）
     * @return 構築されたクエリ
     * @throws IllegalArgumentException SELECT_PAGED で page が null の場合
     * @throws IllegalArgumentException RAW で rawSqlPath が null の場合
     */
    fun build(
        mode: QueryMode,
        condition: C,
        sortKey: String?,
        sortDir: String?,
        page: PageSpec?
    ): BuiltQuery {
        // WHERE 句を構築
        val where = WhereBuilder()
        conditionApplier.apply(condition, where)
        val whereSql = where.buildWhere()

        // パラメータを取得
        val params = where.params().toMutableMap()

        // モードに応じて SQL を構築
        val sql = when (mode) {
            QueryMode.COUNT -> {
                // COUNT: target SQL を wrap して生成（ORDER BY / OFFSET-FETCH は付けない）
                val targetSql = sqlLoader.load(targetSqlPath)
                "SELECT COUNT(1) FROM (\n$targetSql\n$whereSql\n) t"
            }

            QueryMode.SELECT_ALL -> {
                // SELECT_ALL: search SQL を wrap + where + orderBy
                val searchSql = sqlLoader.load(searchSqlPath)
                val orderBy = orderByBuilder.build(sortKey, sortDir)
                "SELECT * FROM (\n$searchSql\n) s\n$whereSql\n$orderBy"
            }

            QueryMode.SELECT_PAGED -> {
                // SELECT_PAGED: search SQL を wrap + where + orderBy + offset/fetch
                require(page != null) { "page must be specified for SELECT_PAGED mode" }
                val searchSql = sqlLoader.load(searchSqlPath)
                val orderBy = orderByBuilder.build(sortKey, sortDir)
                params["offset"] = page.offset
                params["limit"] = page.limit
                "SELECT * FROM (\n$searchSql\n) s\n$whereSql\n$orderBy\nOFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY"
            }

            QueryMode.RAW -> {
                // RAW: SQL をそのまま返す（where/order/offset 付与禁止）
                require(rawSqlPath != null) { "rawSqlPath must be specified for RAW mode" }
                sqlLoader.load(rawSqlPath)
            }
        }

        return BuiltQuery(sql, params.toMap())
    }
}

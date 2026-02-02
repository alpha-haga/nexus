package nexus.infrastructure.jdbc.query

import nexus.infrastructure.jdbc.SqlLoader

/**
 * SQL クエリ構築ビルダー
 *
 * base SQL（FROM/JOIN）と WHERE 句を構造で一致させ、
 * count/search の条件不一致事故を防ぐ。
 *
 * @param C 検索条件の型
 * @param sqlLoader SQL ローダー
 * @param baseSqlPath base SQL のパス（FROM/JOIN + alias を含む想定、例: "group/group_contract_base.sql"）
 * @param selectColumnsSqlPath SELECT 列定義 SQL のパス（例: "group/group_contract_select_columns.sql"）
 * @param conditionApplier 検索条件を WHERE 句に適用する関数
 * @param orderByBuilder ORDER BY 句構築ビルダー
 */
class SqlQueryBuilder<C>(
    private val sqlLoader: SqlLoader,
    private val baseSqlPath: String,
    private val selectColumnsSqlPath: String,
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
     */
    fun build(
        mode: QueryMode,
        condition: C,
        sortKey: String?,
        sortDir: String?,
        page: PageSpec?
    ): BuiltQuery {
        // base SQL と SELECT 列を読み込み
        val baseSql = sqlLoader.load(baseSqlPath)
        val selectCols = sqlLoader.load(selectColumnsSqlPath)

        // WHERE 句を構築
        val where = WhereBuilder()
        conditionApplier.apply(condition, where)
        val whereSql = where.buildWhere()

        // パラメータを取得
        val params = where.params().toMutableMap()

        // ORDER BY 句を生成（SELECT_ALL と SELECT_PAGED で使用）
        // COUNT では不要なので生成しない
        val orderBy = if (mode != QueryMode.COUNT) {
            orderByBuilder.build(sortKey, sortDir)
        } else {
            null
        }

        // モードに応じて SQL を構築
        // 注意: baseSql は FROM/JOIN + エイリアスを含む想定のため、subquery wrap は行わない。
        // これにより ORDER BY が baseSql 内のエイリアス（例: contract_search.xxx）を参照しても壊れない。
        // count/search の一致は「同一 baseSql + 同一 whereSql を用いる」ことで担保される。
        val sql = when (mode) {
            QueryMode.COUNT -> {
                // COUNT: SELECT COUNT(1) + base + where（wrap なし）
                "SELECT COUNT(1)\n$baseSql\n$whereSql"
            }

            QueryMode.SELECT_ALL -> {
                // SELECT_ALL: SELECT ... + base + where + ORDER BY（wrap なし）
                "$selectCols\n$baseSql\n$whereSql\n$orderBy"
            }

            QueryMode.SELECT_PAGED -> {
                // SELECT_PAGED: SELECT_ALL + OFFSET ... FETCH ...
                require(page != null) { "page must be specified for SELECT_PAGED mode" }
                params["offset"] = page.offset
                params["limit"] = page.limit
                "$selectCols\n$baseSql\n$whereSql\n$orderBy\nOFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY"
            }
        }

        return BuiltQuery(sql, params.toMap())
    }
}

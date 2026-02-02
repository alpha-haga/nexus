package nexus.infrastructure.jdbc.query

/**
 * ORDER BY 句構築ビルダー
 *
 * 許可されたソートキーのみを受け付け、列名の直結を禁止する。
 * stableSecondaryOrderBy により順序安定化を保証する。
 *
 * @param allowed 許可されたソートキーと列式のマップ（sortKey -> "safe_column_expr"）
 * @param defaultOrderBy デフォルト ORDER BY 句（sortKey が null/blank の場合に使用）
 *   "ORDER BY ..." を含む安全な ORDER BY 断片であること（例: "ORDER BY contract_search.contract_receipt_ymd DESC, contract_search.contract_no"）
 * @param stableSecondaryOrderBy 順序安定化用の2次ソート ORDER BY 断片（例: "contract_search.contract_no ASC"）
 *   列名ではなく、安全な ORDER BY 断片として指定すること（", column" ではなく "column ASC" 形式）
 */
class OrderByBuilder(
    private val allowed: Map<String, String>,
    private val defaultOrderBy: String,
    private val stableSecondaryOrderBy: String? = null
) {
    /**
     * ORDER BY 句を構築
     *
     * @param sortKey ソートキー（null/blank の場合は defaultOrderBy を使用）
     * @param sortDirectionRaw ソート方向（"asc"/"desc"、null の場合は ASC）
     * @return ORDER BY 句文字列
     * @throws IllegalArgumentException 許可されていない sortKey または不正な sortDirectionRaw の場合
     */
    fun build(sortKey: String?, sortDirectionRaw: String?): String {
        // sortKey が null/blank の場合はデフォルトを使用
        if (sortKey.isNullOrBlank()) {
            return defaultOrderBy
        }

        // 許可されていない sortKey は例外
        val columnExpr = allowed[sortKey]
            ?: throw IllegalArgumentException("Sort key '$sortKey' is not allowed. Allowed keys: ${allowed.keys.joinToString()}")

        // ソート方向を解決
        val direction = SortDirection.fromString(sortDirectionRaw) ?: SortDirection.ASC

        // ORDER BY 句を構築
        val orderBy = "ORDER BY $columnExpr ${direction.sql}"

        // stableSecondaryOrderBy があれば2次ソートとして追加
        // 注意: stableSecondaryOrderBy は「ORDER BY 断片」として扱う（", column" ではなく "column ASC" 形式）
        return if (stableSecondaryOrderBy != null) {
            "$orderBy, $stableSecondaryOrderBy"
        } else {
            orderBy
        }
    }
}

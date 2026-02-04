package nexus.infrastructure.jdbc.query

/**
 * WHERE 句構築ビルダー
 *
 * AND を基本に条件を積み、OR グループをサポートする。
 * NULL吸収目的の OR（`:p IS NULL OR col = :p`）は禁止。
 * 値がある条件のみを追加する（andIfNotNull / orIfNotNull を使用）。
 */
class WhereBuilder {
    private val conditions = mutableListOf<String>()
    private val params = linkedMapOf<String, Any?>()

    /**
     * 生の SQL 断片を AND 条件として追加
     *
     * **注意**: 必要最小限の使用に留める。通常は andIfNotNull を使用すること。
     *
     * @param sqlFragment SQL 断片（例: "col = :param"）
     */
    fun andRaw(sqlFragment: String) {
        conditions.add(sqlFragment)
    }

    /**
     * 値が non-null の場合のみ AND 条件を追加
     *
     * @param name パラメータ名
     * @param value 値（null の場合は何もしない）
     * @param predicate 値が non-null の場合に SQL 断片を生成する関数
     */
    fun <T : Any> andIfNotNull(name: String, value: T?, predicate: (String) -> String) {
        if (value != null) {
            conditions.add(predicate(name))
            params[name] = value
        }
    }

    /**
     * OR グループを追加
     *
     * OR グループ内で1件も条件が積まれない場合は、WHERE 句に追加しない。
     * OR グループは括弧で囲んで AND に結合される: `AND (a OR b OR c)`
     *
     * @param block OR グループ構築ブロック
     */
    fun orGroup(block: OrGroupBuilder.() -> Unit) {
        val orGroup = OrGroupBuilder()
        orGroup.block()
        val orConditions = orGroup.build()
        if (orConditions.isNotEmpty()) {
            conditions.add("(${orConditions.joinToString(" OR ")})")
            params.putAll(orGroup.params())
        }
    }

    /**
     * WHERE 句を構築
     *
     * @return WHERE 句文字列（条件が0件の場合は空文字、1件以上ある場合は "WHERE\n    ...\n    AND ..." 形式）
     */
    fun buildWhere(): String {
        if (conditions.isEmpty()) {
            return ""
        }
        return "WHERE\n    ${conditions.joinToString("\n    AND ")}"
    }

    /**
     * バインドパラメータを取得
     *
     * @return パラメータ Map
     */
    fun params(): Map<String, Any?> = params.toMap()

    /**
     * OR グループ構築ビルダー
     */
    inner class OrGroupBuilder {
        private val orConditions = mutableListOf<String>()
        private val orParams = linkedMapOf<String, Any?>()

        /**
         * 値が non-null の場合のみ OR 条件を追加
         *
         * @param name パラメータ名
         * @param value 値（null の場合は何もしない）
         * @param predicate 値が non-null の場合に SQL 断片を生成する関数
         */
        fun <T : Any> orIfNotNull(name: String, value: T?, predicate: (String) -> String) {
            if (value != null) {
                orConditions.add(predicate(name))
                orParams[name] = value
            }
        }

        /**
         * 生の SQL 断片を OR 条件として追加
         *
         * **注意**: 必要最小限の使用に留める。通常は orIfNotNull を使用すること。
         * パラメータは既に追加済みの場合に使用する。
         *
         * @param sqlFragment SQL 断片（例: "col = :param"）
         */
        fun orRaw(sqlFragment: String) {
            orConditions.add(sqlFragment)
        }

        internal fun build(): List<String> = orConditions.toList()

        internal fun params(): Map<String, Any?> = orParams.toMap()
    }
}

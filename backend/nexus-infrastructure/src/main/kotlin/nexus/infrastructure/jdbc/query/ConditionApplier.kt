package nexus.infrastructure.jdbc.query

/**
 * 検索条件を WHERE 句に適用する関数型インターフェース
 *
 * @param C 検索条件の型
 */
fun interface ConditionApplier<C> {
    /**
     * 検索条件を WhereBuilder に適用する
     *
     * @param condition 検索条件
     * @param where WhereBuilder インスタンス
     */
    fun apply(condition: C, where: WhereBuilder)
}

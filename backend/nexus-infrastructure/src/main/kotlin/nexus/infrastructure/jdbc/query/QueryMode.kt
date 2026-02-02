package nexus.infrastructure.jdbc.query

/**
 * クエリ実行モード
 *
 * - COUNT: 件数取得のみ
 * - SELECT_ALL: 全件取得（ページングなし）
 * - SELECT_PAGED: ページング付き取得
 */
enum class QueryMode {
    COUNT,
    SELECT_ALL,
    SELECT_PAGED
}

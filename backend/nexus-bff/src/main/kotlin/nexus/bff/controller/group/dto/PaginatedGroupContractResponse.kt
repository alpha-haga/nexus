package nexus.bff.controller.group.dto

/**
 * 法人横断契約検索API レスポンスDTO（ページネーション結果）
 *
 * frontend がそのまま使える名前で定義
 */
data class PaginatedGroupContractResponse(
    val content: List<GroupContractSearchResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

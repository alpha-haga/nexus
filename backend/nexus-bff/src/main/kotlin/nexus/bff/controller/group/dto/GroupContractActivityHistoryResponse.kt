package nexus.bff.controller.group.dto

/**
 * 対応履歴（TODO カード: 対応履歴）
 *
 * P2-6：API 分離設計のためのレスポンス骨格。
 * 項目は P2-7 以降で確定し、段階的に拡張する。
 */
data class GroupContractActivityHistoryResponse(
    val cmpCd: String,
    val contractNo: String,
    val activities: List<Activity> = emptyList(),
) {
    data class Activity(
        val id: String,
    )
}

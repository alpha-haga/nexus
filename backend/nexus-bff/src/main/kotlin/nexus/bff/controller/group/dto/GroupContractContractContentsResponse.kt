package nexus.bff.controller.group.dto

/**
 * 契約内容（TODO カード: 契約内容）
 *
 * P2-6：API 分離設計のためのレスポンス骨格。
 * 項目は P2-7 以降で確定し、段階的に拡張する。
 */
data class GroupContractContractContentsResponse(
    val cmpCd: String,
    val contractNo: String,
    val attributes: Map<String, String?> = emptyMap(),
)

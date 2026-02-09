package nexus.bff.controller.group.dto

/**
 * 入金情報（TODO カード: 入金情報）
 *
 * P2-6：API 分離設計のためのレスポンス骨格。
 * 項目は P2-7 以降で確定し、段階的に拡張する。
 */
data class GroupContractPaymentsResponse(
    val cmpCd: String,
    val contractNo: String,
    val payments: List<Payment> = emptyList(),
) {
    data class Payment(
        val id: String,
    )
}

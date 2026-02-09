package nexus.group.query

/**
 * 入金情報（サブリソース）
 *
 * P2-6：API 分離設計のための DTO 骨格。
 * P2-7 以降で項目を確定し、必要なフィールドを追加する。
 */
data class GroupContractPaymentDto(
    val cmpCd: String,
    val contractNo: String,
    val payments: List<Payment> = emptyList(),
) {
    data class Payment(
        /** 監査/追跡のための識別子（採番規則は P2-7 以降） */
        val id: String,
    )
}

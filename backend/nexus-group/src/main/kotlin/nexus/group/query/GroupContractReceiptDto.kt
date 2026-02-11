package nexus.group.query

/**
 * 入金情報（サブリソース）
 *
 * P2-9: 項目確定
 * - SQL の列をそのまま反映（取得できない項目は null）
 * - 1契約に対して複数行（履歴）を返す
 * - ORDER BY は SQL 側で保証
 */
data class GroupContractReceiptDto(
    val cmpCd: String,
    val contractNo: String,
    val receipts: List<Receipt>
) {
    data class Receipt(
        val listNo: Long?,
        val ym: String?,
        val dmdMethodKbn: String?,
        val dmdRsltKbn: String?,
        val dmdMethodName: String?,
        val dmdRsltName: String?,
        val clientConsignorKbn: String?,
        val clientConsignorName: String?,
        val discountGaku: Long?,
        val shareNum: Long?,
        val courseMonthlyPremium: Long?,
        val receiptReceiptMethodKbn: String?,
        val receiptReceiptMethodName: String?,
        val receiptReceiptYmd: String?,
        val receiptReceiptGaku: Long?,
        val receiptNum: Long?,
        val pekeReceiptMethodKbn: String?,
        val pekeReceiptReasonKbn: String?,
        val pekeReceiptReasonName: String?,
        val pekeReceiptYmd: String?,
        val pekeReceiptGaku: Long?,
        val pekeNum: Long?,
        val refundReasonKbn: String?,
        val refundReasonName: String?,
        val refundGaku: Long?,
        val refundYmd: String?,
        val count: Long?,
        val paymentRec: Long?,
        val refundCount: Long?,
        val refundPayment: Long?,
        val pekeReceiptReasonKbnCd: String?,
        val opeRecFlg: String?,
        val opeUsageKbn: String?,
        val opeUsageName: String?,
        val opeUsagePurposeKbn: String?,
        val opeUsagePurposeName: String?,
        val partUsageGaku: Long?,
        val opeYmd: String?,
    )
}

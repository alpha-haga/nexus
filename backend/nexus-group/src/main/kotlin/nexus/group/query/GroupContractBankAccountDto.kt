package nexus.group.query

/**
 * 口座情報（サブリソース）
 *
 * P2-8: 項目確定
 * - SQL の列をそのまま反映（取得できない項目は null）
 * - 1契約に対して1口座情報を返す（SQL は 1行想定）
 */
data class GroupContractBankAccountDto(
    val cmpCd: String,
    val contractNo: String,
    // 支払方法
    val debitMethodKbn: String?,
    val debitMethodName: String?,
    // 積立方法
    val saveMethodKbn: String?,
    val saveMethodName: String?,
    // 銀行情報
    val bankCd: String?,
    val bankName: String?,
    val bankBranchCd: String?,
    val bankBranchName: String?,
    // 口座情報
    val depositorName: String?,
    val accTypeKbn: String?,
    val accNo: String?,
    val accStatusKbn: String?,
    val registrationUpdateYmd: String?,
    // その他
    val abolishFlg: String?,
    val compelMonthPayFlg: String?,
    val monthlyPremium: Long?,
    val remainingSaveNum: Long?,
    val remainingReceiptGaku: Long?,
    val discountGaku: Long?,
    val viewFlg: Int?,
)

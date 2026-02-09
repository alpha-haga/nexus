package nexus.bff.controller.group.dto

/**
 * 口座情報（TODO カード: 口座情報）
 *
 * P2-6：API 分離設計のためのレスポンス骨格。
 * 項目は P2-7 以降で確定し、段階的に拡張する。
 */
data class GroupContractBankAccountResponse(
    val cmpCd: String,
    val contractNo: String,
    val accounts: List<Account> = emptyList(),
) {
    data class Account(
        val id: String,
    )
}

package nexus.group.query

/**
 * 口座情報（サブリソース）
 *
 * P2-6：API 分離設計のための DTO 骨格。
 * P2-7 以降で項目を確定し、必要なフィールドを追加する。
 */
data class GroupContractBankAccountDto(
    val cmpCd: String,
    val contractNo: String,
    /**
     * 口座情報は個人情報を含む可能性があるため、
     * 表示項目・マスキング規則は P2-7 以降で確定する。
     */
    val accounts: List<Account> = emptyList(),
) {
    data class Account(
        /** 監査/追跡のための識別子（採番規則は P2-7 以降） */
        val id: String,
    )
}

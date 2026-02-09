package nexus.group.query

/**
 * 契約対応履歴（サブリソース）
 *
 * P2-6：API 分離設計のための DTO 骨格。
 * P2-7 以降で項目を確定し、必要なフィールドを追加する。
 */
data class GroupContractActivityHistoryDto(
    val cmpCd: String,
    val contractNo: String,
    /**
     * 表示項目は P2-7 以降で確定する。
     * 未確定のため、現時点では空のまま扱う。
     */
    val activities: List<Activity> = emptyList(),
) {
    data class Activity(
        /** 監査/追跡のための識別子（採番規則は P2-7 以降） */
        val id: String,
    )
}

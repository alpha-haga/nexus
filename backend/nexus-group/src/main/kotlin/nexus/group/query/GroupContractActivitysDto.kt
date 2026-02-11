package nexus.group.query

/**
 * 対応履歴（サブリソース）
 *
 * P2-10: 項目確定
 * - SQL の列をそのまま反映（取得できない項目は null）
 * - 1契約に対して複数行（履歴）を返す
 * - ORDER BY は SQL 側で保証
 */
data class GroupContractActivitysDto(
    val cmpCd: String,
    val contractNo: String,
    val activities: List<Activity> = emptyList(),
) {
    data class Activity(
        val recNo: String?,
        val serviceYmd: String?,
        val serviceKbn: String?,
        val serviceName: String?,
        val serviceMethod: String?,
        val serviceMethodName: String?,
        val visitReasonKbn: String?,
        val visitReasonName: String?,
        val callStatusKbn: String?,
        val callStatusName: String?,
        val receptionPsnNm: String?,
        val freeComment: String?,
        val responsibleFamilyName: String?,
        val responsibleFirstName: String?,
        val responsibleSectName: String?,
    )
}

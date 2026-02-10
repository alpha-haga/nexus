package nexus.group.query

/**
 * 担当者情報（サブリソース）
 *
 * P2-7: 項目確定
 * - bosyu/entry/restore/taking を UNION ALL で 4行必ず返す
 * - SQL の role / role_label / bosyu_cd / staff_name をそのまま反映
 */
data class GroupContractStaffDto(
    val cmpCd: String,
    val contractNo: String,
    val staffs: List<Staff> = emptyList(),
) {
    data class Staff(
        /** 役割（bosyu/entry/restore/taking） */
        val role: String,
        /** 役割ラベル（募集担当者/加入担当者/復活担当者/引継担当者） */
        val roleLabel: String,
        /** 募集コード（SQL: bosyu_cd） */
        val bosyuCd: String?,
        /** 担当者名（SQL: staff_name、SQL 側で結合済み） */
        val staffName: String?,
    )
}

package nexus.bff.controller.group.dto

/**
 * 担当者情報（TODO カード: 担当者情報）
 *
 * P2-6：API 分離設計のためのレスポンス骨格。
 * 項目は P2-7 以降で確定し、段階的に拡張する。
 */
data class GroupContractStaffResponse(
    val cmpCd: String,
    val contractNo: String,
    val staffs: List<Staff> = emptyList(),
) {
    data class Staff(
        val id: String,
        val displayName: String? = null,
    )
}

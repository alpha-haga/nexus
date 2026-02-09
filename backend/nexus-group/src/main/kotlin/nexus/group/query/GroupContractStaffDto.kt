package nexus.group.query

/**
 * 担当者情報（サブリソース）
 *
 * P2-6：API 分離設計のための DTO 骨格。
 * P2-7 以降で項目を確定し、必要なフィールドを追加する。
 */
data class GroupContractStaffDto(
    val cmpCd: String,
    val contractNo: String,
    val staffs: List<Staff> = emptyList(),
) {
    data class Staff(
        /** 社員/担当者を特定するキー（実際のキー設計は P2-7 以降） */
        val id: String,
        /** 表示名（不足時はフロントで補完しない） */
        val displayName: String? = null,
    )
}

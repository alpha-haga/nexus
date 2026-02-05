package nexus.group.query

/**
 * 法人マスタDTO（Read Only）
 *
 * SQL alias に完全に一致。
 * integration DB (zgom_cmp) から直接マッピング。
 *
 * 命名ルール: Kotlin DTO は lowerCamelCase
 */
data class CompanyDto(
    val cmpCd: String,              // cmp_cd
    val cmpNm: String?,             // cmp_nm
    val cmpShortNm: String,          // cmp_short_nm
    val regionCd: String?            // region_cd
)

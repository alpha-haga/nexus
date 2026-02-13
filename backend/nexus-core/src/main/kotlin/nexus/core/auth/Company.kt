package nexus.core.auth

/**
 * 法人情報（Domain Model）
 * 
 * 表示・運用フラグ中心に限定（DB接続情報は持たない）
 */
data class Company(
    val cmpCd: String,
    val companyName: String,
    val companyNameShort: String?,
    val regionCd: String,
    val companyCd: String,
    val availableDomains: Set<String>,
    val displayOrder: Int,
    val isActive: String,  // '1' or '0'
)

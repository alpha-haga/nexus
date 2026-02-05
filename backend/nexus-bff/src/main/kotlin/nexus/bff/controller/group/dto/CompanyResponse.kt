package nexus.bff.controller.group.dto

/**
 * 法人マスタ一覧API レスポンスDTO
 *
 * frontend がそのまま使える名前で定義
 */
data class CompanyResponse(
    val cmpCd: String,
    val cmpShortNm: String,
    val regionCd: String?
)

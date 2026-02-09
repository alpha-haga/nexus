package nexus.bff.controller.group.dto

/**
 * 法人横断契約詳細API レスポンスDTO
 *
 * 詳細トップ表示に必要な最小限の情報を含む
 * フィールド順: SQL SELECT 順に準拠
 */
data class GroupContractDetailResponse(
    // 基本識別情報
    val cmpCd: String,
    val cmpShortName: String?,
    val contractNo: String,
    val familyNo: String,
    val houseNo: String?,
    
    // 契約者情報
    val familyNameGaiji: String?,
    val firstNameGaiji: String?,
    val familyNameKana: String?,
    val firstNameKana: String?,
    val contractReceiptYmd: String?,
    val birthday: String?,
    
    // 契約状態（表示用材料）
    val contractStatusKbn: String?,
    val contractStatusName: String?,
    val dmdStopReasonKbn: String?,
    val dmdStopReasonName: String?,
    val cancelReasonKbn: String?,
    val cancelReasonName: String?,
    val zashuReasonKbn: String?,
    val zashuReasonName: String?,
    val anspApproveKbn: String?,
    val anspApproveName: String?,
    val torikeshiReasonKbn: String?,
    val torikeshiReasonName: String?,
    val ecApproveKbn: String?,
    val ecApproveName: String?,
    val cancelStatusKbn: String?,
    val cancelStatusName: String?,
    val contractStatus: String?,
    
    // コース情報
    val courseCd: String?,
    val courseName: String?,
    
    // 連絡先
    val telNo: String?,
    val mobileNo: String?,
    
    // 住所
    val prefName: String?,
    val cityTownName: String?,
    val addr1: String?,
    val addr2: String?
)

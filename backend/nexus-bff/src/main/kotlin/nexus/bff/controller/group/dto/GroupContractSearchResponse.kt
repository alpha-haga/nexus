package nexus.bff.controller.group.dto

/**
 * 法人横断契約検索API レスポンスDTO（個別契約）
 *
 * frontend がそのまま使える名前で定義
 * フィールド順: SQL SELECT 順に準拠
 */
data class GroupContractSearchResponse(
    // 基本情報
    val cmpCd: String,
    val cmpShortName: String?,
    val contractNo: String,
    val familyNo: String,
    val houseNo: String?,
    val familyNameGaiji: String?,
    val firstNameGaiji: String?,
    val familyNameKana: String?,
    val firstNameKana: String?,
    val contractReceiptYmd: String?,
    val birthday: String?,
    
    // 契約状態（SQL SELECT 順）
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
    val taskName: String?,
    val statusUpdateYmd: String?,
    
    // コース・保障内容
    val courseCd: String?,
    val courseName: String?,
    val shareNum: Int?,
    val monthlyPremium: Long?,
    val contractGaku: Long?,
    val totalSaveNum: Long?,
    val totalGaku: Long?,
    
    // 住所
    val zipCd: String?,
    val prefName: String?,
    val cityTownName: String?,
    val oazaTownName: String?,
    val azaChomeName: String?,
    val addr1: String?,
    val addr2: String?,
    
    // 連絡先
    val telNo: String?,
    val mobileNo: String?,
    
    // ポイント
    val saPoint: Int?,
    val aaPoint: Int?,
    val aPoint: Int?,
    val newPoint: Int?,
    val addPoint: Int?,
    val noallwPoint: Int?,
    val ssPoint: Int?,
    val upPoint: Int?,
    
    // 募集
    val entryKbnName: String?,
    val recruitRespBosyuCd: String?,
    val bosyuFamilyNameKanji: String?,
    val bosyuFirstNameKanji: String?,
    val entryRespBosyuCd: String?,
    val entryFamilyNameKanji: String?,
    val entryFirstNameKanji: String?,
    
    // 供給ランク / 部門
    val motoSupplyRankOrgCd: String?,
    val motoSupplyRankOrgName: String?,
    val supplyRankOrgCd: String?,
    val supplyRankOrgName: String?,
    val sectCd: String?,
    val sectName: String?,
    
    // その他
    val anspFlg: String?,
    val agreementKbn: String?,
    val collectOfficeCd: String?,
    val foreclosureFlg: String?,
    val registYmd: String?,
    val receptionNo: String?
)

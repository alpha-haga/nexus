package nexus.group.query

/**
 * 法人横断契約詳細DTO（Read Only）
 *
 * SQL alias に完全に一致。
 * integration DB (zgot_contract_search_key) から直接マッピング。
 *
 * 命名ルール: Kotlin DTO は lowerCamelCase
 * フィールド順: SQL SELECT 順に準拠
 */
data class GroupContractDetailDto(
    // 基本識別情報
    val cmpCd: String,                        // cmp_cd
    val cmpShortName: String?,                // cmp_short_name
    val contractNo: String,                   // contract_no
    val familyNo: String,                     // family_no
    val houseNo: String?,                     // house_no
    
    // 契約者情報
    val familyNameGaiji: String?,             // family_name_gaiji
    val firstNameGaiji: String?,              // first_name_gaiji
    val familyNameKana: String?,              // family_name_kana
    val firstNameKana: String?,               // first_name_kana
    val contractReceiptYmd: String?,          // contract_receipt_ymd (YYYYMMDD format)
    val birthday: String?,                    // birthday (YYYYMMDD format)
    
    // 契約状態（表示用材料）
    override val contractStatusKbn: String?,           // contract_status_kbn
    override val contractStatusName: String?,          // contract_status_name
    override val dmdStopReasonKbn: String?,           // dmd_stop_reason_kbn
    override val dmdStopReasonName: String?,           // dmd_stop_reason_name
    override val cancelReasonKbn: String?,             // cancel_reason_kbn
    override val cancelReasonName: String?,            // cancel_reason_name
    override val zashuReasonKbn: String?,              // zashu_reason_kbn
    override val zashuReasonName: String?,             // zashu_reason_name
    override val anspApproveKbn: String?,              // ansp_approve_kbn
    override val anspApproveName: String?,            // ansp_approve_name
    override val torikeshiReasonKbn: String?,         // torikeshi_reason_kbn
    override val torikeshiReasonName: String?,        // torikeshi_reason_name
    override val ecApproveKbn: String?,                 // ec_approve_kbn
    override val ecApproveName: String?,               // ec_approve_name
    override val cancelStatusKbn: String?,             // cancel_status_kbn
    override val cancelStatusName: String?,            // cancel_status_name
    val contractStatus: String?,              // contract_status
    
    // コース情報
    val courseCd: String?,                    // course_cd
    val courseName: String?,                   // course_name
    
    // 連絡先
    val telNo: String?,                       // tel_no
    val mobileNo: String?,                     // mobile_no
    
    // 住所
    val prefName: String?,                     // pref_name
    val cityTownName: String?,                 // city_town_name
    val addr1: String?,                        // addr1
    val addr2: String?                         // addr2
) : ContractStatusMaterials

package nexus.group.query

import java.time.LocalDate

/**
 * 法人横断契約一覧検索結果DTO（Read Only）
 *
 * SQL alias に完全に一致。
 * integration DB (zgot_contract_search_key) から直接マッピング。
 *
 * 命名ルール: Kotlin DTO は lowerCamelCase
 * フィールド順: SQL SELECT 順に準拠
 */
data class GroupContractSearchDto(
    // 基本情報
    val cmpCd: String,                        // cmp_cd
    val cmpShortName: String?,                // cmp_short_name
    val contractNo: String,                   // contract_no
    val familyNo: String,                     // family_no
    val houseNo: String?,                     // house_no
    val familyNameGaiji: String?,             // family_name_gaiji
    val firstNameGaiji: String?,              // first_name_gaiji
    val familyNameKana: String?,              // family_name_kana
    val firstNameKana: String?,               // first_name_kana
    val contractReceiptYmd: String?,          // contract_receipt_ymd (YYYYMMDD format)
    val birthday: String?,                    // birthday (YYYYMMDD format)
    
    // 契約状態（SQL SELECT 順）
    override val contractStatusKbn: String?,           // contract_status_kbn
    override val contractStatusName: String?,          // contract_status_name
    override val dmdStopReasonKbn: String?,             // dmd_stop_reason_kbn
    override val dmdStopReasonName: String?,            // dmd_stop_reason_name
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
    val contractStatus: String?,              // contract_status (SQL では NULL)
    val taskName: String?,                    // task_name
    val statusUpdateYmd: String?,             // status_update_ymd (YYYYMMDD format)
    
    // コース・保障内容
    val courseCd: String?,                    // course_cd
    val courseName: String?,                  // course_name
    val shareNum: Int?,                       // share_num
    val monthlyPremium: Long?,                // monthly_premium
    val contractGaku: Long?,                  // contract_gaku
    val totalSaveNum: Long?,                  // total_save_num
    val totalGaku: Long?,                     // total_gaku
    
    // 住所
    val zipCd: String?,                       // zip_cd
    val prefName: String?,                    // pref_name
    val cityTownName: String?,                // city_town_name
    val oazaTownName: String?,                // oaza_town_name
    val azaChomeName: String?,                // aza_chome_name
    val addr1: String?,                       // addr1
    val addr2: String?,                       // addr2
    
    // 連絡先
    val telNo: String?,                       // tel_no
    val mobileNo: String?,                    // mobile_no
    
    // ポイント
    val saPoint: Int?,                        // sa_point
    val aaPoint: Int?,                        // aa_point
    val aPoint: Int?,                         // a_point
    val newPoint: Int?,                       // new_point
    val addPoint: Int?,                       // add_point
    val noallwPoint: Int?,                    // noallw_point
    val ssPoint: Int?,                        // ss_point
    val upPoint: Int?,                        // up_point
    
    // 募集
    val entryKbnName: String?,                // entry_kbn_name
    val recruitRespBosyuCd: String?,          // recruit_resp_bosyu_cd
    val bosyuFamilyNameKanji: String?,        // bosyu_family_name_kanji
    val bosyuFirstNameKanji: String?,         // bosyu_first_name_kanji
    val entryRespBosyuCd: String?,            // entry_resp_bosyu_cd
    val entryFamilyNameKanji: String?,        // entry_family_name_kanji
    val entryFirstNameKanji: String?,         // entry_first_name_kanji
    
    // 供給ランク / 部門
    val motoSupplyRankOrgCd: String?,         // moto_supply_rank_org_cd
    val motoSupplyRankOrgName: String?,       // moto_supply_rank_org_name
    val supplyRankOrgCd: String?,             // supply_rank_org_cd
    val supplyRankOrgName: String?,           // supply_rank_org_name
    val sectCd: String?,                      // sect_cd
    val sectName: String?,                    // sect_name
    
    // その他
    val anspFlg: String?,                     // ansp_flg
    val agreementKbn: String?,                // agreement_kbn
    val collectOfficeCd: String?,             // collect_office_cd
    val foreclosureFlg: String?,              // foreclosure_flg
    val registYmd: String?,                   // regist_ymd (YYYYMMDD format)
    val receptionNo: String?                  // reception_no
) : ContractStatusMaterials

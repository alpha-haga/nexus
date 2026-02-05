package nexus.group.query

import java.time.LocalDate

/**
 * 法人横断契約検索条件
 *
 * SQL WHERE 句のバインドパラメータに対応。
 * 全てのフィールドが nullable（未指定は検索条件に含めない）。
 *
 * 命名ルール: SQL bind parameter に合わせて lowerCamelCase
 */
data class GroupContractSearchCondition(
    // 契約受付年月日範囲
    val contractReceiptYmdFrom: String? = null,   // YYYYMMDD形式、null で条件除外
    val contractReceiptYmdTo: String? = null,     // YYYYMMDD形式、null で条件除外
    
    // 契約番号（前方一致）
    val contractNo: String? = null,
    
    // 契約者氏名（中間一致、family_name_gaiji/first_name_gaiji/family_name_kana/first_name_kana のいずれかに一致）
    val contractorName: String? = null,
    
    // 担当者氏名（中間一致、募集担当/登録担当の氏名（漢字・カナ）のいずれかに一致）
    val staffName: String? = null,
    
    // 電話番号（中間一致）
    val telNo: String? = null,
    
    // 募集責任者コード（完全一致）
    val bosyuCd: String? = null,
    
    // コースコード（完全一致）
    val courseCd: String? = null,
    
    // コース名（中間一致）
    val courseName: String? = null,
    
    // 契約状態区分（完全一致）
    val contractStatusKbn: String? = null
)
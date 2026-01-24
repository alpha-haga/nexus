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
    
    // 家族名かな（中間一致）
    val familyNmKana: String? = null,
    
    // 電話番号（中間一致）
    val telNo: String? = null,
    
    // 募集責任者コード（完全一致）
    val bosyuCd: String? = null,
    
    // コースコード（完全一致）
    val courseCd: String? = null,
    
    // 契約状態区分（完全一致）
    val contractStatusKbn: String? = null
)
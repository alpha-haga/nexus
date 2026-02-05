package nexus.infrastructure.group.query

import nexus.group.query.GroupContractSearchCondition
import nexus.infrastructure.jdbc.query.ConditionApplier
import nexus.infrastructure.jdbc.query.WhereBuilder

/**
 * Group Contract 検索条件を WHERE 句に適用する ConditionApplier
 *
 * NULL吸収目的の OR（`:p IS NULL OR ...`）を廃止し、
 * 値がある条件のみを動的に WHERE 句に追加する。
 */
class GroupContractConditionApplier : ConditionApplier<GroupContractSearchCondition> {
    override fun apply(condition: GroupContractSearchCondition, where: WhereBuilder) {
        // 契約受付年月日範囲（開始）
        where.andIfNotNull("contractReceiptYmdFrom", condition.contractReceiptYmdFrom) { paramName ->
            "contract_receipt_ymd >= :$paramName"
        }

        // 契約受付年月日範囲（終了）
        where.andIfNotNull("contractReceiptYmdTo", condition.contractReceiptYmdTo) { paramName ->
            "contract_receipt_ymd <= :$paramName"
        }

        // 契約番号（前方一致）
        where.andIfNotNull("contractNo", condition.contractNo) { paramName ->
            "contract_no LIKE :$paramName || '%'"
        }

        // 契約者氏名（中間一致）
        // family_name_gaiji, first_name_gaiji, family_name_kana, first_name_kana のいずれかに一致（OR条件）
        if (condition.contractorName != null) {
            where.orGroup {
                orIfNotNull("contractorName", condition.contractorName) { paramName ->
                    "family_name_gaiji LIKE '%' || :$paramName || '%'"
                }
                orRaw("first_name_gaiji LIKE '%' || :contractorName || '%'")
                orRaw("family_name_kana LIKE '%' || :contractorName || '%'")
                orRaw("first_name_kana LIKE '%' || :contractorName || '%'")
            }
        }

        // 担当者氏名（中間一致）
        // 募集担当（bosyu_family_name_kanji/bosyu_first_name_kanji/bosyu_family_name_kana/bosyu_first_name_kana）
        // または登録担当（entry_family_name_kanji/entry_first_name_kanji/entry_family_name_kana/entry_first_name_kana）
        // のいずれかに一致（OR条件）
        if (condition.staffName != null) {
            where.orGroup {
                orIfNotNull("staffName", condition.staffName) { paramName ->
                    "bosyu_family_name_kanji LIKE '%' || :$paramName || '%'"
                }
                orRaw("bosyu_first_name_kanji LIKE '%' || :staffName || '%'")
                orRaw("bosyu_family_name_kana LIKE '%' || :staffName || '%'")
                orRaw("bosyu_first_name_kana LIKE '%' || :staffName || '%'")
                orRaw("entry_family_name_kanji LIKE '%' || :staffName || '%'")
                orRaw("entry_first_name_kanji LIKE '%' || :staffName || '%'")
                orRaw("entry_family_name_kana LIKE '%' || :staffName || '%'")
                orRaw("entry_first_name_kana LIKE '%' || :staffName || '%'")
            }
        }

        // 電話番号（中間一致）
        // 電話番号（tel_no）または携帯番号（mobile_no）のいずれかにマッチしたらヒット（OR条件）
        if (condition.telNo != null) {
            where.orGroup {
                // パラメータは1回だけ追加
                orIfNotNull("telNo", condition.telNo) { paramName ->
                    "tel_no LIKE '%' || :$paramName || '%'"
                }
                // 2つ目の条件は同じパラメータを使用（orRawで追加）
                orRaw("mobile_no LIKE '%' || :telNo || '%'")
            }
        }

        // 募集責任者コード（完全一致）
        where.andIfNotNull("bosyuCd", condition.bosyuCd) { paramName ->
            "recruit_resp_bosyu_cd = :$paramName"
        }

        // コースコード（完全一致）
        where.andIfNotNull("courseCd", condition.courseCd) { paramName ->
            "course_cd = :$paramName"
        }
        
        // コース名（中間一致）
        where.andIfNotNull("courseName", condition.courseName) { paramName ->
            "course_name LIKE '%' || :$paramName || '%'"
        }

        // 契約状態区分（完全一致）
        where.andIfNotNull("contractStatusKbn", condition.contractStatusKbn) { paramName ->
            "contract_status_kbn = :$paramName"
        }
    }
}

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

        // 家族名かな（中間一致）
        where.andIfNotNull("familyNmKana", condition.familyNmKana) { paramName ->
            "family_name_kana LIKE '%' || :$paramName || '%'"
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

        // 契約状態区分（完全一致）
        where.andIfNotNull("contractStatusKbn", condition.contractStatusKbn) { paramName ->
            "contract_status_kbn = :$paramName"
        }
    }
}

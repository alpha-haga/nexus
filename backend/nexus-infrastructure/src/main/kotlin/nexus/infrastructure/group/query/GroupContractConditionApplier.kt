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
        // 旧SQL（group_contract_search.sql, group_contract_count.sql）に合わせて
        // contract_search.search_tel_no を使用（旧SQLを正とする）
        where.andIfNotNull("telNo", condition.telNo) { paramName ->
            "search_tel_no LIKE '%' || :$paramName || '%'"
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

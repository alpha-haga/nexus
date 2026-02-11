package nexus.infrastructure.group.query

import nexus.group.query.GroupContractBankAccountDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractBankAccountDto の RowMapper
 *
 * P2-8: SQL から口座情報を取得
 * - SQL alias (snake_case) を直接読む
 * - 取得できない項目は null として扱う
 * - SQL の列順に準拠
 */
class GroupContractBankAccountRowMapper : RowMapper<GroupContractBankAccountDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractBankAccountDto {
        val cmpCd = requireNotNull(rs.getString("cmp_cd")) {
            "cmp_cd is null (row=$rowNum)"
        }
        val contractNo = requireNotNull(rs.getString("contract_no")) {
            "contract_no is null (row=$rowNum)"
        }

        return GroupContractBankAccountDto(
            cmpCd = cmpCd,
            contractNo = contractNo,
            // 支払方法
            debitMethodKbn = rs.getString("debit_method_kbn"),
            debitMethodName = rs.getString("debit_method_name"),
            // 積立方法
            saveMethodKbn = rs.getString("save_method_kbn"),
            saveMethodName = rs.getString("save_method_name"),
            // 銀行情報
            bankCd = rs.getString("bank_cd"),
            bankName = rs.getString("bank_name"),
            bankBranchCd = rs.getString("bank_branch_cd"),
            bankBranchName = rs.getString("bank_branch_name"),
            // 口座情報
            depositorName = rs.getString("depositor_name"),
            accTypeKbn = rs.getString("acc_type_kbn"),
            accNo = rs.getString("acc_no"),
            accStatusKbn = rs.getString("acc_status_kbn"),
            registrationUpdateYmd = rs.getString("registration_update_ymd"),
            // その他
            abolishFlg = rs.getString("abolish_flg"),
            compelMonthPayFlg = rs.getString("compel_month_pay_flg"),
            monthlyPremium = rs.getLong("monthly_premium").takeIf { !rs.wasNull() },
            remainingSaveNum = rs.getLong("remaining_save_num").takeIf { !rs.wasNull() },
            remainingReceiptGaku = rs.getLong("remaining_receipt_gaku").takeIf { !rs.wasNull() },
            discountGaku = rs.getLong("discount_gaku").takeIf { !rs.wasNull() },
            viewFlg = rs.getInt("view_flg").takeIf { !rs.wasNull() },
        )
    }
}

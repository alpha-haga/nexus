package nexus.infrastructure.group.query

import nexus.group.query.GroupContractReceiptDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractReceiptDto.Receipt の RowMapper
 *
 * P2-9: SQL から入金情報を取得
 * - SQL alias (snake_case) を直接読む
 * - 取得できない項目は null として扱う
 * - SQL の列順に準拠
 */
class GroupContractReceiptRowMapper : RowMapper<GroupContractReceiptDto.Receipt> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractReceiptDto.Receipt {
        return GroupContractReceiptDto.Receipt(
            listNo = rs.getLong("list_no").takeIf { !rs.wasNull() },
            ym = rs.getString("ym"),
            dmdMethodKbn = rs.getString("dmd_method_kbn"),
            dmdRsltKbn = rs.getString("dmd_rslt_kbn"),
            dmdMethodName = rs.getString("dmd_method_name"),
            dmdRsltName = rs.getString("dmd_rslt_name"),
            clientConsignorKbn = rs.getString("client_consignor_kbn"),
            clientConsignorName = rs.getString("client_consignor_name"),
            discountGaku = rs.getLong("discount_gaku").takeIf { !rs.wasNull() },
            shareNum = rs.getLong("share_num").takeIf { !rs.wasNull() },
            courseMonthlyPremium = rs.getLong("course_monthly_premium").takeIf { !rs.wasNull() },
            receiptReceiptMethodKbn = rs.getString("receipt_receipt_method_kbn"),
            receiptReceiptMethodName = rs.getString("receipt_receipt_method_name"),
            receiptReceiptYmd = rs.getString("receipt_receipt_ymd"),
            receiptReceiptGaku = rs.getLong("receipt_receipt_gaku").takeIf { !rs.wasNull() },
            receiptNum = rs.getLong("receipt_num").takeIf { !rs.wasNull() },
            pekeReceiptMethodKbn = rs.getString("peke_receipt_method_kbn"),
            pekeReceiptReasonKbn = rs.getString("peke_receipt_reason_kbn"),
            pekeReceiptReasonName = rs.getString("peke_receipt_reason_name"),
            pekeReceiptYmd = rs.getString("peke_receipt_ymd"),
            pekeReceiptGaku = rs.getLong("peke_receipt_gaku").takeIf { !rs.wasNull() },
            pekeNum = rs.getLong("peke_num").takeIf { !rs.wasNull() },
            refundReasonKbn = rs.getString("refund_reason_kbn"),
            refundReasonName = rs.getString("refund_reason_name"),
            refundGaku = rs.getLong("refund_gaku").takeIf { !rs.wasNull() },
            refundYmd = rs.getString("refund_ymd"),
            count = rs.getLong("count").takeIf { !rs.wasNull() },
            paymentRec = rs.getLong("payment_rec").takeIf { !rs.wasNull() },
            refundCount = rs.getLong("refund_count").takeIf { !rs.wasNull() },
            refundPayment = rs.getLong("refund_payment").takeIf { !rs.wasNull() },
            pekeReceiptReasonKbnCd = rs.getString("peke_receipt_reason_kbn_cd"),
            opeRecFlg = rs.getString("ope_rec_flg"),
            opeUsageKbn = rs.getString("ope_usage_kbn"),
            opeUsageName = rs.getString("ope_usage_name"),
            opeUsagePurposeKbn = rs.getString("ope_usage_purpose_kbn"),
            opeUsagePurposeName = rs.getString("ope_usage_purpose_name"),
            partUsageGaku = rs.getLong("part_usage_gaku").takeIf { !rs.wasNull() },
            opeYmd = rs.getString("ope_ymd"),
        )
    }
}

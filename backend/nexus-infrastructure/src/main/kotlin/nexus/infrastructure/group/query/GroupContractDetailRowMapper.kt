package nexus.infrastructure.group.query

import nexus.group.query.GroupContractDetailDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractDetailDto の RowMapper
 *
 * SQL alias (snake_case) を直接読む
 * - 命名ルール: SQL alias は lower_snake_case
 * - 変換は RowMapper で明示的に行う（自動マッピング禁止）
 * - 取得できない項目（CAST(NULL AS ...)）は nullable として扱う
 * - フィールド順: SQL SELECT 順に準拠
 */
class GroupContractDetailRowMapper : RowMapper<GroupContractDetailDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractDetailDto =
        GroupContractDetailDto(
            // 基本識別情報
            cmpCd = requireNotNull(rs.getString("cmp_cd")) {
                "cmp_cd is null (row=$rowNum)"
            },
            cmpShortName = rs.getString("cmp_short_name"),
            contractNo = requireNotNull(rs.getString("contract_no")) {
                "contract_no is null (row=$rowNum)"
            },
            familyNo = requireNotNull(rs.getString("family_no")) {
                "family_no is null (row=$rowNum)"
            },
            houseNo = rs.getString("house_no"),
            
            // 契約者情報
            familyNameGaiji = rs.getString("family_name_gaiji"),
            firstNameGaiji = rs.getString("first_name_gaiji"),
            familyNameKana = rs.getString("family_name_kana"),
            firstNameKana = rs.getString("first_name_kana"),
            contractReceiptYmd = rs.getString("contract_receipt_ymd"),
            birthday = rs.getString("birthday"),
            
            // 契約状態（表示用材料）
            contractStatusKbn = rs.getString("contract_status_kbn"),
            contractStatusName = rs.getString("contract_status_name"),
            dmdStopReasonKbn = rs.getString("dmd_stop_reason_kbn"),
            dmdStopReasonName = rs.getString("dmd_stop_reason_name"),
            cancelReasonKbn = rs.getString("cancel_reason_kbn"),
            cancelReasonName = rs.getString("cancel_reason_name"),
            zashuReasonKbn = rs.getString("zashu_reason_kbn"),
            zashuReasonName = rs.getString("zashu_reason_name"),
            anspApproveKbn = rs.getString("ansp_approve_kbn"),
            anspApproveName = rs.getString("ansp_approve_name"),
            torikeshiReasonKbn = rs.getString("torikeshi_reason_kbn"),
            torikeshiReasonName = rs.getString("torikeshi_reason_name"),
            ecApproveKbn = rs.getString("ec_approve_kbn"),
            ecApproveName = rs.getString("ec_approve_name"),
            cancelStatusKbn = rs.getString("cancel_status_kbn"),
            cancelStatusName = rs.getString("cancel_status_name"),
            contractStatus = rs.getString("contract_status"),
            
            // コース情報
            courseCd = rs.getString("course_cd"),
            courseName = rs.getString("course_name"),
            
            // 連絡先
            telNo = rs.getString("tel_no"),
            mobileNo = rs.getString("mobile_no"),
            
            // 住所
            prefName = rs.getString("pref_name"),
            cityTownName = rs.getString("city_town_name"),
            addr1 = rs.getString("addr1"),
            addr2 = rs.getString("addr2")
        )
}

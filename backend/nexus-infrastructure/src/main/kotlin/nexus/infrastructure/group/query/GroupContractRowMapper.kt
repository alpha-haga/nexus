package nexus.infrastructure.group.query

import nexus.group.query.GroupContractSearchDto
import org.springframework.jdbc.core.RowMapper
import java.sql.ResultSet

/**
 * GroupContractSearchDto の RowMapper
 *
 * P04-2: SQL alias (snake_case) を直接読むよう全面修正
 * - 命名ルール: SQL alias は lower_snake_case
 * - 変換は RowMapper で明示的に行う（自動マッピング禁止）
 * - 取得できない項目（CAST(NULL AS ...)）は nullable として扱う
 * - フィールド順: SQL SELECT 順に準拠
 */
class GroupContractRowMapper : RowMapper<GroupContractSearchDto> {
    override fun mapRow(rs: ResultSet, rowNum: Int): GroupContractSearchDto =
        GroupContractSearchDto(
            // 基本情報
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
            familyNameGaiji = rs.getString("family_name_gaiji"),
            firstNameGaiji = rs.getString("first_name_gaiji"),
            familyNameKana = rs.getString("family_name_kana"),
            firstNameKana = rs.getString("first_name_kana"),
            contractReceiptYmd = rs.getString("contract_receipt_ymd"),
            birthday = rs.getString("birthday"),
            
            // 契約状態（SQL SELECT 順）
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
            taskName = rs.getString("task_name"),
            statusUpdateYmd = rs.getString("status_update_ymd"),
            
            // コース・保障内容
            courseCd = rs.getString("course_cd"),
            courseName = rs.getString("course_name"),
            shareNum = rs.getInt("share_num").takeIf { !rs.wasNull() },
            monthlyPremium = rs.getLong("monthly_premium").takeIf { !rs.wasNull() },
            contractGaku = rs.getLong("contract_gaku").takeIf { !rs.wasNull() },
            totalSaveNum = rs.getLong("total_save_num").takeIf { !rs.wasNull() },
            totalGaku = rs.getLong("total_gaku").takeIf { !rs.wasNull() },
            
            // 住所
            zipCd = rs.getString("zip_cd"),
            prefName = rs.getString("pref_name"),
            cityTownName = rs.getString("city_town_name"),
            oazaTownName = rs.getString("oaza_town_name"),
            azaChomeName = rs.getString("aza_chome_name"),
            addr1 = rs.getString("addr1"),
            addr2 = rs.getString("addr2"),
            
            // 連絡先
            telNo = rs.getString("tel_no"),
            mobileNo = rs.getString("mobile_no"),
            
            // ポイント
            saPoint = rs.getInt("sa_point").takeIf { !rs.wasNull() },
            aaPoint = rs.getInt("aa_point").takeIf { !rs.wasNull() },
            aPoint = rs.getInt("a_point").takeIf { !rs.wasNull() },
            newPoint = rs.getInt("new_point").takeIf { !rs.wasNull() },
            addPoint = rs.getInt("add_point").takeIf { !rs.wasNull() },
            noallwPoint = rs.getInt("noallw_point").takeIf { !rs.wasNull() },
            ssPoint = rs.getInt("ss_point").takeIf { !rs.wasNull() },
            upPoint = rs.getInt("up_point").takeIf { !rs.wasNull() },
            
            // 募集
            entryKbnName = rs.getString("entry_kbn_name"),
            recruitRespBosyuCd = rs.getString("recruit_resp_bosyu_cd"),
            bosyuFamilyNameKanji = rs.getString("bosyu_family_name_kanji"),
            bosyuFirstNameKanji = rs.getString("bosyu_first_name_kanji"),
            entryRespBosyuCd = rs.getString("entry_resp_bosyu_cd"),
            entryFamilyNameKanji = rs.getString("entry_family_name_kanji"),
            entryFirstNameKanji = rs.getString("entry_first_name_kanji"),
            
            // 供給ランク / 部門
            motoSupplyRankOrgCd = rs.getString("moto_supply_rank_org_cd"),
            motoSupplyRankOrgName = rs.getString("moto_supply_rank_org_name"),
            supplyRankOrgCd = rs.getString("supply_rank_org_cd"),
            supplyRankOrgName = rs.getString("supply_rank_org_name"),
            sectCd = rs.getString("sect_cd"),
            sectName = rs.getString("sect_name"),
            
            // その他
            anspFlg = rs.getString("ansp_flg"),
            agreementKbn = rs.getString("agreement_kbn"),
            collectOfficeCd = rs.getString("collect_office_cd"),
            foreclosureFlg = rs.getString("foreclosure_flg"),
            registYmd = rs.getString("regist_ymd"),
            receptionNo = rs.getString("reception_no")
        )
}

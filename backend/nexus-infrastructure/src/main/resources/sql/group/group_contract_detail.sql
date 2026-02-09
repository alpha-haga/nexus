-- 法人横断契約詳細取得SQL
-- 入力: :cmpCd, :contractNo
-- 出力: 1件（0件の場合は404）

SELECT
    contract_search.cmp_cd AS cmp_cd
    , cmp.cmp_short_nm AS cmp_short_name
    , contract_search.contract_no AS contract_no
    , contract_search.family_no AS family_no
    , contract_search.house_no AS house_no
    , contract_search.family_nm_gaiji AS family_name_gaiji
    , contract_search.first_nm_gaiji AS first_name_gaiji
    , contract_search.family_nm_kana AS family_name_kana
    , contract_search.first_nm_kana AS first_name_kana
    , contract_search.contract_receipt_ymd AS contract_receipt_ymd
    , contract_search.birthday AS birthday
    , status_rec.contract_status_kbn AS contract_status_kbn
    , cd0018.cd_nm1_kanji AS contract_status_name
    , status_rec.dmd_stop_reason_kbn AS dmd_stop_reason_kbn
    , cd0019.cd_nm1_kanji AS dmd_stop_reason_name
    , status_rec.cancel_reason_kbn AS cancel_reason_kbn
    , cd0020.cd_nm1_kanji AS cancel_reason_name
    , status_rec.zashu_reason_kbn AS zashu_reason_kbn
    , cd0021.cd_nm1_kanji AS zashu_reason_name
    , status_rec.ansp_approve_kbn AS ansp_approve_kbn
    , cd0150.cd_nm1_kanji AS ansp_approve_name
    , status_rec.torikeshi_reason_kbn AS torikeshi_reason_kbn
    , cd0151.cd_nm1_kanji AS torikeshi_reason_name
    , ec_entry_info.approve_kbn AS ec_approve_kbn
    , cd0168.cd_nm1_kanji AS ec_approve_name
    , status_rec.cancel_status_kbn AS cancel_status_kbn
    , cd0152.cd_nm1_kanji AS cancel_status_name
    , CAST(NULL AS VARCHAR2(10)) AS contract_status
    , contract_search.course_cd AS course_cd
    , course.course_nm AS course_name
    , contract_search.tel_no AS tel_no
    , contract_search.mobile_no AS mobile_no
    , contract_addr.pref_name AS pref_name
    , contract_addr.city_town_name AS city_town_name
    , contract_search.addr1 AS addr1
    , contract_search.addr2 AS addr2
FROM
    zgot_contract_search_key contract_search
INNER JOIN zgot_status_rec_all status_rec
    ON contract_search.cmp_cd = status_rec.cmp_cd 
    AND contract_search.contract_no = status_rec.contract_no 
    AND status_rec.last_flg = '1' 
    AND status_rec.delete_flg = '0' 
LEFT JOIN zgot_ec_entry_info_all ec_entry_info
    ON contract_search.cmp_cd = ec_entry_info.cmp_cd 
    AND contract_search.contract_no = ec_entry_info.contract_no 
    AND ec_entry_info.last_flg = '1' 
    AND ec_entry_info.delete_flg = '0' 
LEFT JOIN zgom_cmp cmp
    ON cmp.cmp_cd = contract_search.cmp_cd
    AND cmp.delete_flg = '0'
LEFT JOIN zgom_course_cd_all course 
    ON contract_search.cmp_cd = course.cmp_cd 
    AND contract_search.course_cd = course.course_cd 
    AND course.tekiyo_start_ymd <= contract_search.effective_ymd 
    AND course.tekiyo_end_ymd > contract_search.effective_ymd 
    AND course.delete_flg = '0' 
LEFT JOIN zgom_addr contract_addr 
    ON contract_addr.addr_cd = contract_search.addr_cd 
    AND contract_addr.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0018 
    ON status_rec.cmp_cd = cd0018.cmp_cd 
    AND status_rec.contract_status_kbn = cd0018.general_cd_level1 
    AND cd0018.general_cd_id = '0018' 
    AND cd0018.delete_flg = '0' 
LEFT JOIN zgom_general_cd_all cd0019 
    ON status_rec.cmp_cd = cd0019.cmp_cd 
    AND status_rec.dmd_stop_reason_kbn = cd0019.general_cd_level1 
    AND cd0019.general_cd_id = '0019' 
    AND cd0019.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0020 
    ON status_rec.cmp_cd = cd0020.cmp_cd 
    AND status_rec.cancel_reason_kbn = cd0020.general_cd_level1 
    AND cd0020.general_cd_id = '0020' 
    AND cd0020.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0021 
    ON status_rec.cmp_cd = cd0021.cmp_cd 
    AND status_rec.zashu_reason_kbn = cd0021.general_cd_level1 
    AND cd0021.general_cd_id = '0021' 
    AND cd0021.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0150 
    ON status_rec.cmp_cd = cd0150.cmp_cd 
    AND status_rec.ansp_approve_kbn = cd0150.general_cd_level1 
    AND cd0150.general_cd_id = '0150' 
    AND cd0150.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0151 
    ON status_rec.cmp_cd = cd0151.cmp_cd 
    AND status_rec.torikeshi_reason_kbn = cd0151.general_cd_level1 
    AND cd0151.general_cd_id = '0151' 
    AND cd0151.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0152 
    ON status_rec.cmp_cd = cd0152.cmp_cd 
    AND status_rec.cancel_status_kbn = cd0152.general_cd_level1 
    AND cd0152.general_cd_id = '0152' 
    AND cd0152.delete_flg = '0'
LEFT JOIN zgom_general_cd_all cd0168 
    ON ec_entry_info.cmp_cd = cd0168.cmp_cd 
    AND ec_entry_info.approve_kbn = cd0168.general_cd_level1 
    AND cd0168.general_cd_id = '0168' 
    AND cd0168.delete_flg = '0'
WHERE
    contract_search.cmp_cd = :cmpCd
    AND contract_search.contract_no = :contractNo

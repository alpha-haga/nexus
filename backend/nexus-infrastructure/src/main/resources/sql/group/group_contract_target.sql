SELECT
    contract_search.cmp_cd AS cmp_cd
    , contract_search.contract_no AS contract_no
    , contract_search.family_no AS family_no
    , contract_search.house_no AS house_no
    , contract_search.family_nm_gaiji AS family_name_gaiji
    , contract_search.first_nm_gaiji AS first_name_gaiji
    , contract_search.family_nm_kana AS family_name_kana
    , contract_search.first_nm_kana AS first_name_kana
    , contract_search.contract_receipt_ymd AS contract_receipt_ymd
    , contract_search.birthday AS birthday
    , contract_search.course_cd AS course_cd
    , contract_search.addr1 AS addr1
    , contract_search.addr2 AS addr2
    , contract_search.tel_no AS tel_no
    , contract_search.search_tel_no AS search_tel_no
    , contract_search.mobile_no AS mobile_no
    , contract_search.search_mobile_no AS search_mobile_no
    , contract_search.recruit_resp_bosyu_cd AS recruit_resp_bosyu_cd
    , contract_search.entry_resp_bosyu_cd AS entry_resp_bosyu_cd

    , course.course_nm AS course_name
    , course.monthly_premium
    , course.contract_gaku

    , contract_addr.zip_cd AS zip_cd
    , contract_addr.pref_name AS pref_name
    , contract_addr.city_town_name AS city_town_name
    , contract_addr.oaza_town_name AS oaza_town_name
    , contract_addr.aza_chome_name AS aza_chome_name

    , bosyu_staff.family_nm_kanji AS bosyu_family_name_kanji
    , bosyu_staff.first_nm_kanji AS bosyu_first_name_kanji
    , entry_staff.family_nm_kanji AS entry_family_name_kanji
    , entry_staff.first_nm_kanji AS entry_first_name_kanji

FROM zgot_contract_search_key contract_search
LEFT JOIN zgom_course_cd_all course
    ON contract_search.cmp_cd = course.cmp_cd
    AND contract_search.course_cd = course.course_cd
    AND course.tekiyo_start_ymd <= contract_search.effective_ymd
    AND course.tekiyo_end_ymd > contract_search.effective_ymd
    AND course.delete_flg = '0'
LEFT JOIN zgom_staff_all bosyu_staff
    ON bosyu_staff.cmp_cd = contract_search.cmp_cd
    AND bosyu_staff.bosyu_cd = contract_search.recruit_resp_bosyu_cd
    AND bosyu_staff.tekiyo_start_ymd <= :businessYmd
    AND bosyu_staff.tekiyo_end_ymd > :businessYmd
    AND bosyu_staff.delete_flg = '0'
LEFT JOIN zgom_staff_all entry_staff
    ON entry_staff.cmp_cd = contract_search.cmp_cd
    AND entry_staff.bosyu_cd = contract_search.entry_resp_bosyu_cd
    AND entry_staff.tekiyo_start_ymd <= :businessYmd
    AND entry_staff.tekiyo_end_ymd > :businessYmd
    AND entry_staff.delete_flg = '0'
LEFT JOIN zgom_addr contract_addr 
    ON contract_addr.addr_cd = contract_search.addr_cd 
    AND contract_addr.delete_flg = '0' 

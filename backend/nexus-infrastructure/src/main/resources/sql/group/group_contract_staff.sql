WITH base AS ( 
SELECT
    contract_search.cmp_cd AS cmp_cd
    , contract_search.contract_no AS contract_no
    , contract_search.recruit_resp_bosyu_cd  AS recruit_resp_bosyu_cd
    , bosyu_staff.family_nm_kanji AS bosyu_family_name_kanji
    , bosyu_staff.first_nm_kanji AS bosyu_first_name_kanji
    , bosyu_staff.family_nm_kana AS bosyu_family_name_kana
    , bosyu_staff.first_nm_kana AS bosyu_first_name_kana
    , contract_search.entry_resp_bosyu_cd  AS entry_resp_bosyu_cd
    , entry_staff.family_nm_kanji AS entry_family_name_kanji
    , entry_staff.first_nm_kanji AS entry_first_name_kanji
    , entry_staff.family_nm_kana AS entry_family_name_kana
    , entry_staff.first_nm_kana AS entry_first_name_kana 
    , contract_info.restore_resp_bosyu_cd  AS restore_resp_bosyu_cd
    , restore_staff.family_nm_kanji AS restore_family_name_kanji
    , restore_staff.first_nm_kanji AS restore_first_name_kanji
    , restore_staff.family_nm_kana AS restore_family_name_kana
    , restore_staff.first_nm_kana AS restore_first_name_kana 
    , contract_info.taking_over_resp_bosyu_cd  AS taking_over_resp_bosyu_cd
    , taking_staff.family_nm_kanji AS taking_family_name_kanji
    , taking_staff.first_nm_kanji AS taking_first_name_kanji
    , taking_staff.family_nm_kana AS taking_family_name_kana
    , taking_staff.first_nm_kana AS taking_first_name_kana 
FROM
    zgot_contract_search_key contract_search
INNER JOIN zgot_contract_info_all contract_info 
    ON contract_search.cmp_cd = contract_info.cmp_cd 
    AND contract_search.contract_no = contract_info.contract_no 
    AND contract_info.last_flg = '1' 
    AND contract_info.delete_flg = '0' 
INNER JOIN zgom_ope_date_management_all ope_date_management 
    ON ope_date_management.cmp_cd = contract_search.cmp_cd
    AND ope_date_management.delete_flg = '0'
LEFT JOIN zgom_staff_all bosyu_staff 
    ON bosyu_staff.cmp_cd = contract_search.cmp_cd 
    AND bosyu_staff.bosyu_cd = contract_search.recruit_resp_bosyu_cd 
    AND bosyu_staff.tekiyo_start_ymd <= ope_date_management.online_ymd 
    AND bosyu_staff.tekiyo_end_ymd > ope_date_management.online_ymd 
    AND bosyu_staff.delete_flg = '0' 
LEFT JOIN zgom_staff_all entry_staff 
    ON entry_staff.cmp_cd = contract_search.cmp_cd 
    AND entry_staff.bosyu_cd = contract_search.entry_resp_bosyu_cd 
    AND entry_staff.tekiyo_start_ymd <= ope_date_management.online_ymd 
    AND entry_staff.tekiyo_end_ymd > ope_date_management.online_ymd 
    AND entry_staff.delete_flg = '0' 
LEFT JOIN zgom_staff_all restore_staff 
    ON restore_staff.cmp_cd = contract_info.cmp_cd 
    AND restore_staff.bosyu_cd = contract_info.restore_resp_bosyu_cd 
    AND restore_staff.tekiyo_start_ymd <= ope_date_management.online_ymd 
    AND restore_staff.tekiyo_end_ymd > ope_date_management.online_ymd 
    AND restore_staff.delete_flg = '0' 
LEFT JOIN zgom_staff_all taking_staff 
    ON taking_staff.cmp_cd = contract_info.cmp_cd 
    AND taking_staff.bosyu_cd = contract_info.taking_over_resp_bosyu_cd 
    AND taking_staff.tekiyo_start_ymd <= ope_date_management.online_ymd 
    AND taking_staff.tekiyo_end_ymd > ope_date_management.online_ymd
    AND taking_staff.delete_flg = '0' 
WHERE
    contract_search.cmp_cd = :cmpCd 
    AND contract_search.contract_no = :contractNo 
)
SELECT
    cmp_cd
    , contract_no
    , sort_key
    , role
    , role_label
    , bosyu_cd
    , staff_name
FROM
    ( 
        SELECT
            base.cmp_cd AS cmp_cd
            , base.contract_no AS contract_no
            , 1 AS sort_key
            , 'bosyu' AS role
            , '募集担当者' AS role_label
            , base.recruit_resp_bosyu_cd AS bosyu_cd
            , CASE
              WHEN base.bosyu_family_name_kanji IS NOT NULL OR base.bosyu_first_name_kanji IS NOT NULL
                THEN NVL(base.bosyu_family_name_kanji,'') || NVL(base.bosyu_first_name_kanji,'')
              WHEN base.bosyu_family_name_kana IS NOT NULL OR base.bosyu_first_name_kana IS NOT NULL
                THEN NVL(base.bosyu_family_name_kana,'') || NVL(base.bosyu_first_name_kana,'')
              ELSE NULL
            END AS staff_name
        FROM
            base base
        UNION ALL 
        SELECT
            base.cmp_cd AS cmp_cd
            , base.contract_no AS contract_no
            , 2 AS sort_key
            , 'entry' AS role
            , '加入担当者' AS role_label
            , base.entry_resp_bosyu_cd AS bosyu_cd
            , CASE
              WHEN base.entry_family_name_kanji IS NOT NULL OR base.entry_first_name_kanji IS NOT NULL
                THEN NVL(base.entry_family_name_kanji,'') || NVL(base.entry_first_name_kanji,'')
              WHEN base.entry_family_name_kana IS NOT NULL OR base.entry_first_name_kana IS NOT NULL
                THEN NVL(base.entry_family_name_kana,'') || NVL(base.entry_first_name_kana,'')
              ELSE NULL
            END AS staff_name
        FROM
            base base
        UNION ALL 
        SELECT
            base.cmp_cd AS cmp_cd
            , base.contract_no AS contract_no
            , 3 AS sort_key
            , 'restore' AS role
            , '復活担当者' AS role_label
            , base.restore_resp_bosyu_cd AS bosyu_cd
            , CASE
              WHEN base.restore_family_name_kanji IS NOT NULL OR base.restore_first_name_kanji IS NOT NULL
                THEN NVL(base.restore_family_name_kanji,'') || NVL(base.restore_first_name_kanji,'')
              WHEN base.restore_family_name_kana IS NOT NULL OR base.restore_first_name_kana IS NOT NULL
                THEN NVL(base.restore_family_name_kana,'') || NVL(base.restore_first_name_kana,'')
              ELSE NULL
            END AS staff_name
        FROM
            base base
        UNION ALL 
        SELECT
            base.cmp_cd AS cmp_cd
            , base.contract_no AS contract_no
            , 4 AS sort_key
            , 'taking' AS role
            , '引継担当者' AS role_label
            , base.taking_over_resp_bosyu_cd AS bosyu_cd
            , CASE
              WHEN base.taking_family_name_kanji IS NOT NULL OR base.taking_first_name_kanji IS NOT NULL
                THEN NVL(base.taking_family_name_kanji,'') || NVL(base.taking_first_name_kanji,'')
              WHEN base.taking_family_name_kana IS NOT NULL OR base.taking_first_name_kana IS NOT NULL
                THEN NVL(base.taking_family_name_kana,'') || NVL(base.taking_first_name_kana,'')
              ELSE NULL
            END AS staff_name
        FROM
            base base
    ) 
ORDER BY
    sort_key

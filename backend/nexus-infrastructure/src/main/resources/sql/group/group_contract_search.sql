WITH base AS ( 
SELECT
    * 
FROM
    ( 
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
            , course.monthly_premium AS monthly_premium
            , course.contract_gaku AS contract_gaku
            , contract_addr.zip_cd AS zip_cd
            , contract_addr.pref_name AS pref_name
            , contract_addr.city_town_name AS city_town_name
            , contract_addr.oaza_town_name AS oaza_town_name
            , contract_addr.aza_chome_name AS aza_chome_name
            , bosyu_staff.family_nm_kanji AS bosyu_family_name_kanji
            , bosyu_staff.first_nm_kanji AS bosyu_first_name_kanji
            , bosyu_staff.family_nm_kana AS bosyu_family_name_kana
            , bosyu_staff.first_nm_kana AS bosyu_first_name_kana
            , entry_staff.family_nm_kanji AS entry_family_name_kanji
            , entry_staff.first_nm_kanji AS entry_first_name_kanji
            , entry_staff.family_nm_kana AS entry_family_name_kana
            , entry_staff.first_nm_kana AS entry_first_name_kana 
            , moto_supply_rank_org.supply_rank_org_cd AS moto_supply_rank_org_cd
            , moto_supply_rank_org.supply_rank_org_nm AS moto_supply_rank_org_name
        FROM
            zgot_contract_search_key contract_search 
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
            LEFT JOIN ( 
                SELECT
                    org.cmp_cd
                    , org.supply_rank_org_cd
                    , CASE 
                        WHEN LEVEL = 1 
                            THEN org.supply_rank_org_cd 
                        ELSE SUBSTR( 
                            SUBSTR( 
                                SYS_CONNECT_BY_PATH(RPAD(org.supply_rank_org_cd, 6, ' '), ',')
                                , 2
                            ) 
                            , 0
                            , 6
                        ) 
                        END AS motouke_cd 
                FROM
                    ( 
                        SELECT
                            * 
                        FROM
                            zgom_supply_rank_org_all org 
                        WHERE
                            org.tekiyo_start_ymd <= :businessYmd 
                            AND org.tekiyo_end_ymd > :businessYmd 
                            AND org.delete_flg = '0'
                    ) org 
                START WITH
                    org.parent_supply_rank_org_cd IS NULL 
                CONNECT BY
                    NOCYCLE PRIOR org.cmp_cd = org.cmp_cd 
                    AND PRIOR org.supply_rank_org_cd = parent_supply_rank_org_cd
            ) supply_rank_org_list 
                ON supply_rank_org_list.cmp_cd = bosyu_staff.cmp_cd 
                AND supply_rank_org_list.supply_rank_org_cd = bosyu_staff.supply_rank_org_cd 
            LEFT JOIN zgom_supply_rank_org_all moto_supply_rank_org 
                ON moto_supply_rank_org.cmp_cd = supply_rank_org_list.cmp_cd 
                AND moto_supply_rank_org.supply_rank_org_cd = supply_rank_org_list.motouke_cd 
                AND moto_supply_rank_org.tekiyo_start_ymd <= :businessYmd 
                AND moto_supply_rank_org.tekiyo_end_ymd > :businessYmd 
                AND moto_supply_rank_org.delete_flg = '0'
    )
)
SELECT
    base.cmp_cd AS company_cd
    , cmp.cmp_short_nm AS company_short_name
    , base.contract_no AS contract_no
    , base.family_no AS family_no
    , base.house_no AS house_no
    , base.family_name_gaiji AS family_name_gaiji
    , base.first_name_gaiji AS first_name_gaiji
    , base.family_name_kana AS family_name_kana
    , base.first_name_kana AS first_name_kana
    , base.contract_receipt_ymd AS contract_receipt_ymd
    , base.birthday AS birthday
    , CAST(NULL AS CHAR(1)) AS contract_status_kbn
    , CAST(NULL AS CHAR(1)) AS dmd_stop_rason_kbn
    , CAST(NULL AS CHAR(1)) AS cancel_reason_kbn
    , CAST(NULL AS CHAR(1)) AS cancel_status_kbn
    , CAST(NULL AS CHAR(1)) AS zashu_reason_kbn
    , CAST(NULL AS VARCHAR2(1)) AS contract_status
    , CAST(NULL AS VARCHAR2(30)) AS task_name
    , CAST(NULL AS CHAR(8)) AS status_update_ymd
    , base.course_cd AS course_cd
    , base.course_name AS course_name
    , NVL(contract_info.share_num, 0) AS share_num
    , NVL(base.monthly_premium, 0) AS monthly_premium
    , base.contract_gaku * contract_info.share_num AS contract_gaku
    , trunc( 
        ( 
            NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
             - NVL(status_rec.total_ope_usage_gaku, 0)
        ) / ( 
            NVL(base.monthly_premium, 0) * (NVL(contract_info.share_num, 0))
        )
    ) AS total_save_num
    , NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
     - NVL(status_rec.total_ope_usage_gaku, 0) AS total_gaku
    , base.zip_cd AS zip_cd
    , base.pref_name AS pref_name
    , base.city_town_name AS city_town_name
    , base.oaza_town_name AS oaza_town_name
    , base.aza_chome_name AS aza_chome_name
    , base.addr1 AS addr1
    , base.addr2 AS addr2
    , base.tel_no AS tel_no
    , base.search_tel_no AS search_tel_no
    , base.mobile_no AS mobile_no
    , base.search_mobile_no AS search_mobile_no
    , CAST(NULL AS NUMBER(5)) AS sa_point
    , CAST(NULL AS NUMBER(5)) AS aa_point
    , CAST(NULL AS NUMBER(5)) AS a_point
    , CAST(NULL AS NUMBER(5)) AS new_point
    , CAST(NULL AS NUMBER(5)) AS add_point
    , CAST(NULL AS NUMBER(5)) AS noallw_point
    , CAST(NULL AS NUMBER(5)) AS ss_point
    , CAST(NULL AS NUMBER(5)) AS up_point
    , CAST(NULL AS CHAR(1)) AS entry_kbn_name
    , base.recruit_resp_bosyu_cd AS recruit_resp_bosyu_cd
    , base.bosyu_family_name_kanji AS bosyu_family_name_kanji
    , base.bosyu_first_name_kanji AS bosyu_first_name_kanji
    , base.bosyu_family_name_kana AS bosyu_family_name_kana
    , base.bosyu_first_name_kana AS bosyu_first_name_kana
    , base.entry_resp_bosyu_cd AS entry_resp_bosyu_cd
    , base.entry_family_name_kanji AS entry_family_name_kanji
    , base.entry_first_name_kanji AS entry_first_name_kanji
    , base.entry_family_name_kana AS entry_family_name_kana
    , base.entry_first_name_kana AS entry_first_name_kana
    , base.moto_supply_rank_org_cd AS moto_supply_rank_org_cd
    , base.moto_supply_rank_org_name AS moto_supply_rank_org_name
    , CAST(NULL AS CHAR(6)) AS supply_rank_org_cd
    , CAST(NULL AS VARCHAR2(15)) AS supply_rank_org_name
    , CAST(NULL AS CHAR(6)) AS sect_cd
    , CAST(NULL AS VARCHAR2(25)) AS sect_name
    , CAST(NULL AS CHAR(1))  AS ansp_flg
    , CAST(NULL AS VARCHAR2(10)) AS agreement_kbn
    , CAST(NULL AS CHAR(6)) AS collect_office_cd
    , CAST(NULL AS CHAR(1)) AS foreclosure_flg
    , CAST(NULL AS CHAR(8)) AS regist_ymd
    , CAST(NULL AS CHAR(16)) AS reception_no 
FROM base base
INNER JOIN zgot_contract_info_all contract_info 
    ON base.cmp_cd = contract_info.cmp_cd 
    AND base.contract_no = contract_info.contract_no 
    AND contract_info.last_flg = '1' 
    AND contract_info.delete_flg = '0' 
INNER JOIN zgot_status_rec_all status_rec
    ON base.cmp_cd = status_rec.cmp_cd 
    AND base.contract_no = status_rec.contract_no 
    AND status_rec.last_flg = '1' 
    AND status_rec.delete_flg = '0' 
LEFT JOIN zgom_cmp cmp
    ON cmp.cmp_cd = base.cmp_cd
    AND cmp.delete_flg = '0'

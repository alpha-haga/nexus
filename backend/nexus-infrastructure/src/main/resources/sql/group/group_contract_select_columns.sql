SELECT
    contract_search.cmp_cd AS company_cd
    , cmp.cmp_short_nm AS company_short_name
    , contract_search.contract_no AS contract_no
    , contract_search.family_no AS family_no
    , contract_search.house_no AS house_no
    , contract_search.family_nm_gaiji AS family_name_gaiji
    , contract_search.first_nm_gaiji AS first_name_gaiji
    , contract_search.family_nm_kana AS family_name_kana
    , contract_search.first_nm_kana AS first_name_kana
    , contract_search.contract_receipt_ymd AS contract_receipt_ymd
    , contract_search.birthday AS birthday
    , CAST(NULL AS CHAR(1)) AS contract_status_kbn
    , CAST(NULL AS CHAR(1)) AS dmd_stop_rason_kbn
    , CAST(NULL AS CHAR(1)) AS cancel_reason_kbn
    , CAST(NULL AS CHAR(1)) AS cancel_status_kbn
    , CAST(NULL AS CHAR(1)) AS zashu_reason_kbn
    , CAST(NULL AS VARCHAR2(1)) AS contract_status
    , CAST(NULL AS VARCHAR2(30)) AS task_name
    , CAST(NULL AS CHAR(8)) AS status_update_ymd
    , contract_search.course_cd AS course_cd
    , course.course_nm AS course_name
    , NVL(contract_info.share_num, 0) AS share_num
    , NVL(course.monthly_premium, 0) AS monthly_premium
    , course.contract_gaku * contract_info.share_num AS contract_gaku
    , trunc( 
        ( 
            NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
             - NVL(status_rec.total_ope_usage_gaku, 0)
        ) / ( 
            NVL(course.monthly_premium, 0) * (NVL(contract_info.share_num, 0))
        )
    ) AS total_save_num
    , NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
     - NVL(status_rec.total_ope_usage_gaku, 0) AS total_gaku
    , contract_addr.zip_cd AS zip_cd
    , contract_addr.pref_name AS pref_name
    , contract_addr.city_town_name AS city_town_name
    , contract_addr.oaza_town_name AS oaza_town_name
    , contract_addr.aza_chome_name AS aza_chome_name
    , contract_search.addr1 AS addr1
    , contract_search.addr2 AS addr2
    , contract_search.tel_no AS tel_no
    , contract_search.mobile_no AS mobile_no
    , CAST(NULL AS NUMBER(5)) AS sa_point
    , CAST(NULL AS NUMBER(5)) AS aa_point
    , CAST(NULL AS NUMBER(5)) AS a_point
    , CAST(NULL AS NUMBER(5)) AS new_point
    , CAST(NULL AS NUMBER(5)) AS add_point
    , CAST(NULL AS NUMBER(5)) AS noallw_point
    , CAST(NULL AS NUMBER(5)) AS ss_point
    , CAST(NULL AS NUMBER(5)) AS up_point
    , CAST(NULL AS CHAR(1)) AS entry_kbn_name
    , contract_search.recruit_resp_bosyu_cd AS recruit_resp_bosyu_cd
    , bosyu_staff.family_nm_kanji AS bosyu_family_name_kanji
    , bosyu_staff.first_nm_kanji AS bosyu_first_name_kanji
    , contract_search.entry_resp_bosyu_cd AS entry_resp_bosyu_cd
    , entry_staff.family_nm_kanji AS entry_family_name_kanji
    , entry_staff.first_nm_kanji AS entry_first_name_kanji
    , CAST(NULL AS CHAR(6)) AS moto_supply_rank_org_cd
    , CAST(NULL AS VARCHAR2(15)) AS moto_supply_rank_org_name
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

SELECT
    contract_info.cmp_cd AS cmp_cd
    , contract_info.contract_no AS contract_no
    , service.rec_no AS rec_no
    , service.service_ymd AS service_ymd
    , service.service_kbn AS service_kbn
    , cd0008.cd_nm1_kanji AS service_name
    , service.service_method AS service_method
    , cd0009.cd_nm1_kanji AS service_method_name
    , service.visit_reason_kbn AS visit_reason_kbn
    , cd0037.cd_nm1_kanji AS visit_reason_name
    , service.call_status_kbn AS call_status_kbn
    , cd0072.cd_nm1_kanji AS call_status_name
    , CRYPT_DATA.DECRYPT(service.reception_psn_nm) AS reception_psn_nm
    , CRYPT_DATA.DECRYPT(service.free_comment) AS free_comment
    , NVL2( 
        staff.bosyu_cd
        , staff.family_nm_kanji
        , service.regist_psn_cd
    ) AS responsible_family_name
    , NVL2( 
        staff.bosyu_cd
        , ( 
            staff.first_nm_kanji || '　' || NVL2(staff.retire_ymd, '(D)', '　')
        ) 
        , ''
    ) AS responsible_first_name
    , sect.sect_nm AS responsible_sect_name 
FROM
    zgot_contract_info_all contract_info 
    INNER JOIN zgot_service_rec_all service 
        ON contract_info.cmp_cd = service.cmp_cd 
        AND contract_info.contract_no = service.contract_no 
        AND service.delete_flg = '0' 
    INNER JOIN zgom_ope_date_management_all ope_date_management 
        ON ope_date_management.cmp_cd = contract_info.cmp_cd 
        AND ope_date_management.delete_flg = '0' 
    LEFT JOIN zgom_staff_all staff 
        ON service.cmp_cd = staff.cmp_cd 
        AND service.regist_psn_cd = staff.bosyu_cd 
        AND staff.tekiyo_start_ymd <= ope_date_management.online_ymd 
        AND staff.tekiyo_end_ymd > ope_date_management.online_ymd 
        AND staff.delete_flg = '0' 
    LEFT JOIN zgom_sect_all sect 
        ON staff.cmp_cd = sect.cmp_cd 
        AND staff.sect_cd = sect.sect_cd 
        AND sect.tekiyo_start_ymd <= ope_date_management.online_ymd 
        AND sect.tekiyo_end_ymd > ope_date_management.online_ymd 
        AND sect.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0008 
        ON service.cmp_cd = cd0008.cmp_cd 
        AND service.service_kbn = cd0008.general_cd_level1 
        AND cd0008.general_cd_id = '0008' 
        AND cd0008.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0009 
        ON service.cmp_cd = cd0009.cmp_cd 
        AND service.service_method = cd0009.general_cd_level1 
        AND cd0009.general_cd_id = '0009' 
        AND cd0009.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0037 
        ON service.cmp_cd = cd0037.cmp_cd 
        AND service.visit_reason_kbn = cd0037.general_cd_level1 
        AND cd0037.general_cd_id = '0037' 
        AND cd0037.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0072 
        ON service.cmp_cd = cd0072.cmp_cd 
        AND service.call_status_kbn = cd0072.general_cd_level1 
        AND cd0072.general_cd_id = '0072' 
        AND cd0072.delete_flg = '0' 
WHERE
    contract_info.cmp_cd = :cmpCd 
    AND contract_info.contract_no = :contractNo 
    AND contract_info.last_flg = '1' 
    AND contract_info.delete_flg = '0' 
ORDER BY
    service.service_ymd DESC

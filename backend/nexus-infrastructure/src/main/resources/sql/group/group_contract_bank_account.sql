SELECT
    contract_search.cmp_cd AS cmp_cd
    , contract_search.contract_no AS contract_no
    , contract_info.debit_method_kbn AS debit_method_kbn
    , cd0022.cd_nm1_kanji AS debit_method_name
    , contract_info.save_method_kbn AS save_method_kbn
    , cd0017.cd_nm1_kanji AS save_method_name
    , acc.bank_cd AS bank_cd
    , bank.bank_nm_kanji AS bank_name
    , acc.bank_branch_cd AS bank_branch_cd
    , bank.bank_branch_nm_kanji AS bank_branch_name
    , CRYPT_DATA.DECRYPT(acc.depositor_nm_kana) AS depositor_name
    , acc.acc_type_kbn AS acc_type_kbn
    , CRYPT_DATA.DECRYPT(acc.acc_no) AS acc_no
    , acc.acc_status_kbn AS acc_status_kbn
    , acc.registration_update_ymd AS registration_update_ymd
    , bank.abolish_flg AS abolish_flg
    , dmd_control.compel_month_pay_flg AS compel_month_pay_flg
    , course.monthly_premium AS monthly_premium
    , course.contract_num - trunc( 
        ( 
            NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
             - NVL(status_rec.total_ope_usage_gaku, 0)
        ) / ( 
            NULLIF( 
                course.monthly_premium * contract_search.share_num
                , 0
            )
        )
    ) AS remaining_save_num
    , ( 
        NVL(course.contract_gaku, 0) * NVL(contract_search.share_num, 0)
    ) - ( 
        NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
         - NVL(status_rec.total_ope_usage_gaku, 0)
    ) AS remaining_receipt_gaku
    , course.discount_gaku AS discount_gaku
    , ( 
        CASE 
            WHEN status_rec.contract_status_kbn IN ('4', '6') 
                THEN 1 
            ELSE 0 
            END
    ) AS view_flg
FROM
    zgot_contract_search_key contract_search
INNER JOIN zgot_contract_info_all contract_info 
    ON contract_search.cmp_cd = contract_info.cmp_cd 
    AND contract_search.contract_no = contract_info.contract_no 
    AND contract_info.last_flg = '1' 
    AND contract_info.delete_flg = '0' 
INNER JOIN zgot_status_rec_all status_rec
    ON contract_search.cmp_cd = status_rec.cmp_cd 
    AND contract_search.contract_no = status_rec.contract_no 
    AND status_rec.last_flg = '1' 
    AND status_rec.delete_flg = '0' 
LEFT JOIN zgom_course_cd_all course 
    ON contract_search.cmp_cd = course.cmp_cd 
    AND contract_search.course_cd = course.course_cd 
    AND course.tekiyo_start_ymd <= contract_search.effective_ymd 
    AND course.tekiyo_end_ymd > contract_search.effective_ymd 
    AND course.delete_flg = '0' 
LEFT JOIN zgom_acc_all acc 
    ON contract_search.cmp_cd = acc.cmp_cd 
    AND contract_search.contract_no = acc.contract_no 
    AND acc.last_flg = '1' 
    AND acc.delete_flg = '0' 
LEFT JOIN zgom_bank_branch_all bank 
    ON bank.bank_cd = acc.bank_cd 
    AND bank.bank_branch_cd = acc.bank_branch_cd 
    AND bank.last_flg = '1' 
    AND bank.delete_flg = '0' 
LEFT JOIN zgot_dmd_control_all dmd_control 
    ON contract_search.cmp_cd = dmd_control.cmp_cd 
    AND contract_search.contract_no = dmd_control.contract_no 
    AND dmd_control.last_flg = '1' 
    AND dmd_control.delete_flg = '0' 
LEFT JOIN zgom_general_cd_all cd0022 
    ON contract_info.cmp_cd = cd0022.cmp_cd 
    AND contract_info.debit_method_kbn = cd0022.general_cd_level1 
    AND cd0022.general_cd_id = '0022' 
    AND cd0022.delete_flg = '0' 
LEFT JOIN zgom_general_cd_all cd0017 
    ON contract_info.cmp_cd = cd0017.cmp_cd 
    AND contract_info.save_method_kbn = cd0017.general_cd_level1 
    AND cd0017.general_cd_id = '0017' 
    AND cd0017.delete_flg = '0' 
WHERE
    contract_search.cmp_cd = :cmpCd
    AND contract_search.contract_no = :contractNo

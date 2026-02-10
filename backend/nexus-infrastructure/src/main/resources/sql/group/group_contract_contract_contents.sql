SELECT
    contract_search.cmp_cd AS cmp_cd
    , contract_search.contract_no AS contract_no
    , contract_search.contract_receipt_ymd AS contract_receipt_ymd
    , contract_search.course_cd AS course_cd
    , course.course_nm AS course_name
    , contract_search.share_num AS share_num
    , course.monthly_premium AS monthly_premium
    , course.contract_num AS contract_num
    , NVL(course.contract_gaku, 0) * NVL(contract_search.share_num, 0) AS contract_gaku
    , trunc( 
        ( 
            NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
             - NVL(status_rec.total_ope_usage_gaku, 0)
        ) / ( 
            NULLIF(course.monthly_premium * contract_search.share_num, 0)
        )
    ) AS total_save_num
    , NVL(status_rec.total_receipt_gaku, 0) + NVL(status_rec.dmd_discount_gaku, 0) - NVL(status_rec.total_refund_gaku, 0)
     - NVL(status_rec.total_ope_usage_gaku, 0) AS total_gaku
    , ROUND( 
        NVL(status_rec.total_receipt_gaku, 0) / ( 
            NVL(course.monthly_premium, 0) * NVL(contract_search.share_num, 0)
        )
    ) AS total_receipt_num
    , NVL(status_rec.total_receipt_gaku, 0) AS total_receipt_gaku
    , NVL(status_rec.dmd_discount_gaku, 0) AS dmd_discount_gaku
    , CASE 
        WHEN course.cancel_bank_fee_cstmr_brd_flg = '1' 
            THEN ( 
            NVL(status_rec.total_refund_gaku, 0) + NVL(refund_rec.bank_fee, 0)
        ) / ( 
            NVL(course.monthly_premium, 0) * NVL(contract_search.share_num, 0)
        ) 
        ELSE NVL(status_rec.total_refund_gaku, 0) / ( 
            NVL(course.monthly_premium, 0) * NVL(contract_search.share_num, 0)
        ) 
        END AS total_refund_num
    , CASE 
        WHEN course.cancel_bank_fee_cstmr_brd_flg = '1' 
            THEN NVL(status_rec.total_refund_gaku, 0) + NVL(refund_rec.bank_fee, 0) 
        ELSE NVL(status_rec.total_refund_gaku, 0) 
        END AS total_refund_gaku
    , NVL(status_rec.total_ope_usage_gaku, 0) / ( 
        NVL(course.monthly_premium, 0) * NVL(contract_search.share_num, 0)
    ) AS total_ope_usage_num
    , NVL(status_rec.total_ope_usage_gaku, 0) AS total_ope_usage_gaku
    , NVL( 
        ope_rec.ope_save_num
        , NVL(ope_rec.ope_apply_num, 0)
    ) AS ope_save_num
    , NVL(ope_rec.ope_paid_gaku, 0) AS ope_paid_gaku
    , ope_rec.ope_usage_kbn AS ope_usage_kbn
    , contract_info.debit_method_kbn AS debit_method_kbn
    , cd0022.cd_nm1_kanji AS debit_method_name
    , contract_info.save_method_kbn AS save_method_kbn
    , cd0017.cd_nm1_kanji AS save_method_name
    , tax.tax_rate AS tax_rate
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
LEFT JOIN zgot_refund_rec_all refund_rec 
    ON contract_search.cmp_cd = refund_rec.cmp_cd 
    AND contract_search.contract_no = refund_rec.contract_no 
    AND refund_rec.refund_reason_Kbn = '1' 
    AND refund_rec.refund_status_kbn = '2' 
    AND refund_rec.delete_flg = '0' 
LEFT JOIN zgot_ope_rec_all ope_rec 
    ON contract_search.cmp_cd = ope_rec.cmp_cd 
    AND contract_search.contract_no = ope_rec.contract_no 
    AND ope_rec.ope_fix_kbn = '1' 
    AND NOT EXISTS ( 
        SELECT
            'X' 
        FROM
            zgot_ope_rec_all 
        WHERE
            cmp_cd = ope_rec.cmp_cd 
            AND ope_no = ope_rec.ope_no 
            AND rec_no > ope_rec.contract_no 
            AND delete_flg = '0'
    ) 
    AND ope_rec.delete_flg = '0' 
INNER JOIN zgom_ope_date_management_all ope_date_management 
    ON ope_date_management.cmp_cd = contract_search.cmp_cd
    AND ope_date_management.delete_flg = '0'
LEFT JOIN zgom_course_cd_all course 
    ON contract_search.cmp_cd = course.cmp_cd 
    AND contract_search.course_cd = course.course_cd 
    AND course.tekiyo_start_ymd <= contract_search.effective_ymd 
    AND course.tekiyo_end_ymd > contract_search.effective_ymd 
LEFT JOIN zgom_cons_tax_rate tax 
    ON tax.tekiyo_start_ymd <= contract_search.effective_exclude_div_ymd
    AND tax.tekiyo_end_ymd > contract_search.effective_exclude_div_ymd
    AND tax.tax_rate_kbn = '2' 
    AND tax.delete_flg = '0' 
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
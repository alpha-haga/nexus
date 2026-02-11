WITH
receipt_contract_snapshot AS ( 
    SELECT
        contract_info.cmp_cd
        , contract_info.contract_no
        , receipt.receipt_no
        , receipt.num
        , MAX(contract_info.rec_no) AS rec_no 
    FROM
        zgot_contract_info_all contract_info 
        INNER JOIN zgot_receipt_all receipt 
            ON contract_info.cmp_cd = receipt.cmp_cd 
            AND contract_info.contract_no = receipt.contract_no 
            AND GREATEST( 
                NVL(contract_info.change_course_ymd, '0')
                , NVL(contract_info.share_num_div_process_ymd, '0')
                , NVL(contract_info.contract_receipt_ymd, '0')
            ) <= receipt.receipt_check_ymd 
            AND receipt.delete_flg = '0' 
    WHERE
        contract_info.cmp_cd = :cmpCd 
        AND contract_info.contract_no = :contractNo 
        AND contract_info.delete_flg = '0' 
    GROUP BY
        contract_info.cmp_cd
        , contract_info.contract_no
        , receipt.receipt_no
        , receipt.num
),
receipt_course_info AS ( 
    SELECT
        receipt_contract.cmp_cd
        , receipt_contract.receipt_no
        , receipt_contract.num
        , contract_info.share_num
        , contract_info.course_cd
        , GREATEST( 
            NVL(contract_info.change_course_ymd, '0')
            , NVL(contract_info.share_num_div_process_ymd, '0')
            , NVL(contract_info.contract_receipt_ymd, '0')
        ) AS course_effective_ymd
        , trns.trns_cd AS in_trns_cd 
    FROM
        receipt_contract_snapshot receipt_contract 
        INNER JOIN zgot_contract_info_all contract_info 
            ON contract_info.cmp_cd = receipt_contract.cmp_cd 
            AND contract_info.contract_no = receipt_contract.contract_no 
            AND contract_info.rec_no = receipt_contract.rec_no 
        LEFT OUTER JOIN zgot_trns_receipt_all trns 
            ON trns.cmp_cd = contract_info.cmp_cd 
            AND trns.trns_cd = contract_info.trns_cd 
            AND trns.in_out_kbn = '1' 
            AND trns.delete_flg = '0'
),
demand_with_receipt AS ( 
    SELECT
        dmd.cmp_cd AS cmp_cd
        , dmd.contract_no AS contract_no
        , TO_CHAR(dmd.dmd_ym) AS ym
        , dmd.dmd_method_kbn AS dmd_method_kbn
        , dmd.dmd_rslt_kbn AS dmd_rslt_kbn
        , dmd.client_consignor_kbn AS client_consignor_kbn
        , receipt.receipt_method_kbn AS receipt_receipt_method_kbn
        , NVL2( 
            receipt.receipt_ymd
            , SUBSTR(receipt.receipt_ymd, 7, 2)
            , ''
        ) AS receipt_receipt_ymd
        , receipt.receipt_gaku AS receipt_receipt_gaku
        , receipt.num AS receipt_num
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_reason_kbn
        , CAST(NULL AS VARCHAR2(2)) AS peke_receipt_ymd
        , CAST(NULL AS NUMBER) AS peke_receipt_gaku
        , CAST(NULL AS NUMBER) AS peke_num
        , CAST(NULL AS VARCHAR2(1)) AS refund_reason_kbn
        , CAST(NULL AS NUMBER) AS refund_gaku
        , CAST(NULL AS VARCHAR2(2)) AS refund_ymd
        , NVL( 
            receipt.receipt_ymd
            , NVL(dmd.debit_ymd, dmd.update_ymdhmi)
        ) AS sort_key
        , receipt.receipt_no AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '0' AS ope_rec_flg
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_kbn
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_purpose_kbn
        , CAST(NULL AS NUMBER) AS part_usage_gaku
        , CAST(NULL AS VARCHAR2(2)) AS ope_ymd 
    FROM
        zgot_dmd_all dmd 
        LEFT JOIN zgot_receipt_all receipt 
            ON dmd.cmp_cd = receipt.cmp_cd 
            AND dmd.dmd_no = receipt.dmd_no 
            AND receipt.receipt_method_kbn IN ('1', '2', '3', '4', '9', 'C', 'K') 
            AND receipt.contract_no = :contractNo 
            AND receipt.receipt_check_ymd IS NOT NULL 
            AND receipt.delete_flg = '0' 
    WHERE
        dmd.cmp_cd = :cmpCd 
        AND dmd.contract_no = :contractNo 
        AND dmd.delete_flg = '0'
),
receipt_without_demand AS ( 
    SELECT
        CAST(NULL AS CHAR (2)) AS cmp_cd
        , CAST(NULL AS CHAR (7)) AS contract_no
        , SUBSTR(receipt.receipt_ymd, 1, 6) AS ym
        , CAST(NULL AS VARCHAR2(1)) AS dmd_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS dmd_rslt_kbn
        , CAST(NULL AS VARCHAR2(1)) AS client_consignor_kbn
        , receipt.receipt_method_kbn AS receipt_receipt_method_kbn
        , NVL2( 
            receipt.receipt_ymd
            , SUBSTR(receipt.receipt_ymd, 7, 2)
            , ''
        ) AS receipt_receipt_ymd
        , receipt.receipt_gaku AS receipt_receipt_gaku
        , receipt.num AS receipt_num
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_reason_kbn
        , CAST(NULL AS VARCHAR2(2)) AS peke_receipt_ymd
        , CAST(NULL AS NUMBER) AS peke_receipt_gaku
        , CAST(NULL AS NUMBER) AS peke_num
        , CAST(NULL AS VARCHAR2(1)) AS refund_reason_kbn
        , CAST(NULL AS NUMBER) AS refund_gaku
        , CAST(NULL AS VARCHAR2(2)) AS refund_ymd
        , receipt.receipt_ymd AS sort_key
        , receipt.receipt_no AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '0' AS ope_rec_flg
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_kbn
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_purpose_kbn
        , CAST(NULL AS NUMBER) AS part_usage_gaku
        , CAST(NULL AS VARCHAR2(2)) AS ope_ymd 
    FROM
        zgot_receipt_all receipt 
    WHERE
        receipt.cmp_cd = :cmpCd 
        AND receipt.contract_no = :contractNo 
        AND receipt.dmd_no IS NULL 
        AND receipt.receipt_method_kbn IN ('1', '2', '3', '4', '9', 'K') 
        AND receipt.receipt_check_ymd IS NOT NULL 
        AND receipt.delete_flg = '0'
),
receipt_peke AS ( 
    SELECT
        peke.cmp_cd AS cmp_cd
        , peke.contract_no AS contract_no
        , SUBSTR(peke.receipt_ymd, 1, 6) AS ym
        , CAST(NULL AS VARCHAR2(1)) AS dmd_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS dmd_rslt_kbn
        , CAST(NULL AS VARCHAR2(1)) AS client_consignor_kbn
        , CAST(NULL AS VARCHAR2(1)) AS receipt_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(2)) AS receipt_receipt_ymd
        , CAST(NULL AS NUMBER) AS receipt_receipt_gaku
        , CAST(NULL AS NUMBER) AS receipt_num
        , peke.receipt_method_kbn AS peke_receipt_method_kbn
        , peke.receipt_reason_kbn AS peke_receipt_reason_kbn
        , NVL2( 
            peke.receipt_ymd
            , SUBSTR(peke.receipt_ymd, 7, 2)
            , ''
        ) AS peke_receipt_ymd
        , peke.receipt_gaku AS peke_receipt_gaku
        , peke.num AS peke_num
        , CAST(NULL AS VARCHAR2(1)) AS refund_reason_kbn
        , CAST(NULL AS NUMBER) AS refund_gaku
        , CAST(NULL AS VARCHAR2(2)) AS refund_ymd
        , peke.receipt_ymd AS sort_key
        , peke.receipt_no AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '0' AS ope_rec_flg
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_kbn
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_purpose_kbn
        , CAST(NULL AS NUMBER) AS part_usage_gaku
        , CAST(NULL AS VARCHAR2(2)) AS ope_ymd 
    FROM
        zgot_receipt_all peke 
    WHERE
        peke.cmp_cd = :cmpCd 
        AND peke.contract_no = :contractNo 
        AND ( 
            peke.receipt_method_kbn NOT IN ('1', '2', '3', '4', '9', 'C', 'K') 
            OR peke.receipt_method_kbn IS NULL
        ) 
        AND peke.receipt_check_ymd IS NOT NULL 
        AND peke.delete_flg = '0'
),
refund_data AS ( 
    SELECT
        refund.cmp_cd AS cmp_cd
        , refund.contract_no AS contract_no
        , SUBSTR(refund.refund_ymd, 1, 6) AS ym
        , CAST(NULL AS VARCHAR2(1)) AS dmd_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS dmd_rslt_kbn
        , CAST(NULL AS VARCHAR2(1)) AS client_consignor_kbn
        , CAST(NULL AS VARCHAR2(1)) AS receipt_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(2)) AS receipt_receipt_ymd
        , CAST(NULL AS NUMBER) AS receipt_receipt_gaku
        , CAST(NULL AS NUMBER) AS receipt_num
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_reason_kbn
        , CAST(NULL AS VARCHAR2(2)) AS peke_receipt_ymd
        , CAST(NULL AS NUMBER) AS peke_receipt_gaku
        , CAST(NULL AS NUMBER) AS peke_num
        , refund.refund_reason_kbn AS refund_reason_kbn
        , refund.refund_gaku AS refund_gaku
        , NVL2( 
            refund.refund_ymd
            , SUBSTR(refund.refund_ymd, 7, 2)
            , ''
        ) AS refund_ymd
        , refund.refund_ymd AS sort_key
        , CAST(NULL AS NUMBER) AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '0' AS ope_rec_flg
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_kbn
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_purpose_kbn
        , CAST(NULL AS NUMBER) AS part_usage_gaku
        , CAST(NULL AS VARCHAR2(2)) AS ope_ymd 
    FROM
        zgot_refund_rec_all refund 
    WHERE
        refund.cmp_cd = :cmpCd 
        AND refund.contract_no = :contractNo 
        AND refund.refund_ymd IS NOT NULL 
        AND refund.delete_flg = '0'
),
demand_corporate AS ( 
    SELECT
        kigyo.cmp_cd AS cmp_cd
        , kigyo.contract_no AS contract_no
        , TO_CHAR(kigyo.dmd_ym) AS ym
        , CAST(NULL AS VARCHAR2(1)) AS dmd_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS dmd_rslt_kbn
        , CAST(NULL AS VARCHAR2(1)) AS client_consignor_kbn
        , CAST(NULL AS VARCHAR2(1)) AS receipt_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(2)) AS receipt_receipt_ymd
        , CAST(NULL AS NUMBER) AS receipt_receipt_gaku
        , CAST(NULL AS NUMBER) AS receipt_num
        , kigyo.dmd_method_kbn AS peke_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_reason_kbn
        , CAST(NULL AS VARCHAR2(2)) AS peke_receipt_ymd
        , CAST(NULL AS NUMBER) AS peke_receipt_gaku
        , CAST(NULL AS NUMBER) AS peke_num
        , CAST(NULL AS VARCHAR2(1)) AS refund_reason_kbn
        , CAST(NULL AS NUMBER) AS refund_gaku
        , CAST(NULL AS VARCHAR2(2)) AS refund_ymd
        , SUBSTR(kigyo.create_ymdhmi, 1, 8) AS sort_key
        , CAST(NULL AS NUMBER) AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '0' AS ope_rec_flg
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_kbn
        , CAST(NULL AS VARCHAR2(1)) AS ope_usage_purpose_kbn
        , CAST(NULL AS NUMBER) AS part_usage_gaku
        , CAST(NULL AS VARCHAR2(2)) AS ope_ymd 
    FROM
        zgot_dmd_all kigyo 
    WHERE
        kigyo.cmp_cd = :cmpCd 
        AND kigyo.contract_no = :contractNo 
        AND kigyo.dmd_method_kbn = 'K' 
        AND kigyo.dmd_no IS NULL 
        AND kigyo.dmd_rslt_kbn IS NULL 
        AND kigyo.delete_flg = '0'
),
operation_usage AS ( 
    SELECT
        ope_rec.cmp_cd AS cmp_cd
        , ope_rec.contract_no AS contract_no
        , SUBSTR(ope_rec.ope_ymd, 1, 6) AS ym
        , CAST(NULL AS VARCHAR2(1)) AS dmd_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS dmd_rslt_kbn
        , CAST(NULL AS VARCHAR2(1)) AS client_consignor_kbn
        , CAST(NULL AS VARCHAR2(1)) AS receipt_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(2)) AS receipt_receipt_ymd
        , CAST(NULL AS NUMBER) AS receipt_receipt_gaku
        , CAST(NULL AS NUMBER) AS receipt_num
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_method_kbn
        , CAST(NULL AS VARCHAR2(1)) AS peke_receipt_reason_kbn
        , CAST(NULL AS VARCHAR2(2)) AS peke_receipt_ymd
        , CAST(NULL AS NUMBER) AS peke_receipt_gaku
        , CAST(NULL AS NUMBER) AS peke_num
        , CAST(NULL AS VARCHAR2(1)) AS refund_reason_kbn
        , CAST(NULL AS NUMBER) AS refund_gaku
        , CAST(NULL AS VARCHAR2(2)) AS refund_ymd
        , ope_rec.ope_ymd AS sort_key
        , CAST(NULL AS NUMBER) AS receipt_no
        , CAST(NULL AS NUMBER) AS count
        , CAST(NULL AS NUMBER) AS payment_rec
        , CAST(NULL AS NUMBER) AS refund_count
        , CAST(NULL AS NUMBER) AS refund_payment
        , '1' AS ope_rec_flg
        , ope_rec.ope_usage_kbn AS ope_usage_kbn
        , ope_rec.ope_usage_purpose_kbn AS ope_usage_purpose_kbn
        , ope_rec.part_usage_gaku AS part_usage_gaku
        , SUBSTR(ope_rec.ope_ymd, 7, 2) AS ope_ymd 
    FROM
        zgot_ope_rec_all ope_rec 
    WHERE
        ope_rec.cmp_cd = :cmpCd 
        AND ope_rec.contract_no = :contractNo 
        AND ope_rec.ope_usage_kbn = '4' 
        AND ope_rec.ope_fix_kbn = '1' 
        AND ope_rec.ope_ymd IS NOT NULL 
        AND ope_rec.delete_flg = '0'
),
unified_data AS ( 
    SELECT
        * 
    FROM
        demand_with_receipt 
    UNION ALL 
    SELECT
        * 
    FROM
        receipt_without_demand 
    UNION ALL 
    SELECT
        * 
    FROM
        receipt_peke 
    UNION ALL 
    SELECT
        * 
    FROM
        refund_data 
    UNION ALL 
    SELECT
        * 
    FROM
        demand_corporate 
    UNION ALL 
    SELECT
        * 
    FROM
        operation_usage
)
SELECT
    ROW_NUMBER() OVER ( 
        PARTITION BY
            rslt.contract_no 
        ORDER BY
            rslt.ym DESC
            , rslt.sort_key DESC
            , rslt.receipt_no DESC
    ) AS list_no
    , rslt.cmp_cd AS cmp_cd
    , rslt.contract_no AS contract_no
    , rslt.ym AS ym
    , rslt.dmd_method_kbn AS dmd_method_kbn
    , cd0033.cd_nm1_kanji AS dmd_method_name
    , rslt.dmd_rslt_kbn AS dmd_rslt_kbn
    , cd0063.cd_nm1_kanji AS dmd_rslt_name
    , rslt.client_consignor_kbn AS client_consignor_kbn
    , cd0091.cd_nm1_kanji AS client_consignor_name
    , CASE
        WHEN rslt.peke_receipt_reason_kbn = '3' 
        OR ( 
            rslt.peke_receipt_reason_kbn = '7' 
            AND rci.in_trns_cd IS NOT NULL
        ) 
            THEN NULL 
        ELSE CASE
            WHEN rslt.peke_receipt_reason_kbn = '6' 
                THEN 0
            WHEN NVL( 
                rslt.receipt_receipt_gaku
                , rslt.peke_receipt_gaku
            ) < 0 
                THEN 0
            WHEN NVL(rci.num, 0) = 0 
                THEN 0
            ELSE NVL( 
                rci.num * rci.share_num * course.monthly_premium - NVL( 
                    rslt.receipt_receipt_gaku
                    , rslt.peke_receipt_gaku
                ) 
                , 0
            ) 
            END 
        END AS discount_gaku
    , rci.share_num AS share_num
    , course.monthly_premium AS course_monthly_premium
    , rslt.receipt_receipt_method_kbn AS receipt_receipt_method_kbn
    , cd0033_receipt.cd_nm1_kanji AS receipt_receipt_method_name
    , rslt.receipt_receipt_ymd AS receipt_receipt_ymd
    , rslt.receipt_receipt_gaku AS receipt_receipt_gaku
    , rslt.receipt_num AS receipt_num
    , rslt.peke_receipt_method_kbn AS peke_receipt_method_kbn
    , rslt.peke_receipt_reason_kbn AS peke_receipt_reason_kbn
    , cd0046.cd_nm1_kanji AS peke_receipt_reason_name
    , rslt.peke_receipt_ymd AS peke_receipt_ymd
    , rslt.peke_receipt_gaku AS peke_receipt_gaku
    , rslt.peke_num AS peke_num
    , rslt.refund_reason_kbn AS refund_reason_kbn
    , cd0011.cd_nm1_kanji AS refund_reason_name
    , rslt.refund_gaku AS refund_gaku
    , rslt.refund_ymd AS refund_ymd
    , rslt.count AS count
    , rslt.payment_rec AS payment_rec
    , rslt.refund_count AS refund_count
    , rslt.refund_payment AS refund_payment
    , rslt.peke_receipt_reason_kbn AS peke_receipt_reason_kbn_cd
    , rslt.ope_rec_flg AS ope_rec_flg
    , rslt.ope_usage_kbn AS ope_usage_kbn
    , cd0004.cd_nm1_kanji AS ope_usage_name
    , rslt.ope_usage_purpose_kbn AS ope_usage_purpose_kbn
    , cd0106.cd_nm1_kanji AS ope_usage_purpose_name
    , rslt.part_usage_gaku AS part_usage_gaku
    , rslt.ope_ymd AS ope_ymd 
FROM
    unified_data rslt
    LEFT JOIN receipt_course_info rci 
        ON rslt.cmp_cd = rci.cmp_cd 
        AND rslt.receipt_no = rci.receipt_no
    LEFT JOIN zgom_course_cd_all course 
        ON rci.cmp_cd = course.cmp_cd 
        AND rci.course_cd = course.course_cd 
        AND course.tekiyo_start_ymd <= rci.course_effective_ymd 
        AND course.tekiyo_end_ymd > rci.course_effective_ymd 
        AND course.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0033 
        ON rslt.cmp_cd = cd0033.cmp_cd 
        AND rslt.dmd_method_kbn = cd0033.general_cd_level1 
        AND cd0033.general_cd_id = '0033' 
        AND cd0033.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0063 
        ON rslt.cmp_cd = cd0063.cmp_cd 
        AND rslt.dmd_rslt_kbn = cd0063.general_cd_level1 
        AND cd0063.general_cd_id = '0063' 
        AND cd0063.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0091 
        ON rslt.cmp_cd = cd0091.cmp_cd 
        AND rslt.client_consignor_kbn = cd0091.general_cd_level1 
        AND cd0091.general_cd_id = '0091' 
        AND cd0091.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0033_receipt 
        ON rslt.cmp_cd = cd0033_receipt.cmp_cd 
        AND rslt.receipt_receipt_method_kbn = cd0033_receipt.general_cd_level1 
        AND cd0033_receipt.general_cd_id = '0033' 
        AND cd0033_receipt.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0046 
        ON rslt.cmp_cd = cd0046.cmp_cd 
        AND rslt.peke_receipt_reason_kbn = cd0046.general_cd_level1 
        AND cd0046.general_cd_id = '0046' 
        AND cd0046.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0011 
        ON rslt.cmp_cd = cd0011.cmp_cd 
        AND rslt.refund_reason_kbn = cd0011.general_cd_level1 
        AND cd0011.general_cd_id = '0011' 
        AND cd0011.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0004 
        ON rslt.cmp_cd = cd0004.cmp_cd 
        AND rslt.ope_usage_kbn = cd0004.general_cd_level1 
        AND cd0004.general_cd_id = '0004' 
        AND cd0004.delete_flg = '0' 
    LEFT JOIN zgom_general_cd_all cd0106 
        ON rslt.cmp_cd = cd0106.cmp_cd 
        AND rslt.ope_usage_purpose_kbn = cd0106.general_cd_level1 
        AND cd0106.general_cd_id = '0106' 
        AND cd0106.delete_flg = '0' 

ORDER BY
    rslt.ym DESC
    , rslt.sort_key DESC
    , rslt.receipt_no DESC

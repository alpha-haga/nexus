SELECT COUNT(1)
FROM zgot_contract_search_key contract_search
WHERE
    (:contractReceiptYmdFrom IS NULL OR contract_search.contract_receipt_ymd >= :contractReceiptYmdFrom)
    AND (:contractReceiptYmdTo IS NULL OR contract_search.contract_receipt_ymd <= :contractReceiptYmdTo)
    AND (:contractNo IS NULL OR contract_search.contract_no LIKE :contractNo || '%')
    AND (:familyNmKana IS NULL OR contract_search.family_nm_kana LIKE '%' || :familyNmKana || '%')
    AND (:telNo IS NULL OR contract_search.search_tel_no LIKE '%' || :telNo || '%')
    AND (:bosyuCd IS NULL OR contract_search.recruit_resp_bosyu_cd = :bosyuCd)
    AND (:courseCd IS NULL OR contract_search.course_cd = :courseCd) 
    AND (:contractStatusKbn IS NULL OR contract_search.contract_status_kbn = :contractStatusKbn)

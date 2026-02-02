FROM zgot_contract_search_key contract_search
LEFT JOIN zgom_cmp cmp
    ON cmp.cmp_cd = contract_search.cmp_cd
    AND cmp.delete_flg = '0'
LEFT JOIN zgom_course_cd_all course
    ON contract_search.cmp_cd = course.cmp_cd
    AND contract_search.course_cd = course.course_cd
    AND course.tekiyo_start_ymd <= contract_search.effective_ymd
    AND course.tekiyo_end_ymd > contract_search.effective_ymd
    AND course.delete_flg = '0'

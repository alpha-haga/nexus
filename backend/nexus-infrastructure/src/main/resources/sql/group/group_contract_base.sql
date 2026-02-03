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
LEFT JOIN zgom_staff_all bosyu_staff
    ON bosyu_staff.cmp_cd = contract_search.cmp_cd
    AND bosyu_staff.staff_cd = contract_search.recruit_resp_bosyu_cd
    AND bosyu_staff.tekiyo_start_ymd <= TO_CHAR(SYSDATE, 'YYYYMMDD')
    AND bosyu_staff.tekiyo_end_ymd > TO_CHAR(SYSDATE, 'YYYYMMDD')
    AND bosyu_staff.delete_flg = '0'

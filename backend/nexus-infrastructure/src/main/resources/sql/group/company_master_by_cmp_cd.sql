SELECT
    cmp_cd,
    company_name,
    company_name_short,
    region_cd,
    company_cd,
    available_domains,
    display_order,
    is_active
FROM NXCM_COMPANY
WHERE cmp_cd = :cmpCd

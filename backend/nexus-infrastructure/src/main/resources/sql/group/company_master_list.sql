SELECT
    cmp.cmp_cd as cmp_cd
    , cmp.cmp_nm as cmp_nm
    , cmp.cmp_short_nm as cmp_short_nm
    , cmp.region_cd as region_cd
FROM
    zgom_cmp cmp 
WHERE
    cmp.cmp_cd in ('01', '12', '06', '09', '28', '05', '15') 
ORDER BY
    cmp.region_cd
    , cmp.cmp_cd

package nexus.bff.controller.group.mapper

import nexus.bff.controller.group.dto.CompanyResponse
import nexus.group.query.CompanyDto

/**
 * CompanyDto -> CompanyResponse の変換
 */
fun CompanyDto.toResponse(): CompanyResponse =
    CompanyResponse(
        cmpCd = cmpCd,
        cmpShortNm = cmpShortNm,
        regionCd = regionCd
    )

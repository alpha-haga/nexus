package nexus.bff.controller.group.mapper

import nexus.bff.controller.group.dto.GroupContractSearchResponse
import nexus.bff.controller.group.dto.PaginatedGroupContractResponse
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractSearchDto

/**
 * GroupContractSearchDto -> GroupContractSearchResponse の変換
 */
fun GroupContractSearchDto.toResponse(): GroupContractSearchResponse =
    GroupContractSearchResponse(
        cmpCd = cmpCd,
        cmpShortName = cmpShortName,
        contractNo = contractNo,
        familyNo = familyNo,
        houseNo = houseNo,
        familyNameGaiji = familyNameGaiji,
        firstNameGaiji = firstNameGaiji,
        familyNameKana = familyNameKana,
        firstNameKana = firstNameKana,
        contractReceiptYmd = contractReceiptYmd,
        birthday = birthday,
        contractStatusKbn = contractStatusKbn,
        dmdStopRasonKbn = dmdStopRasonKbn,
        cancelReasonKbn = cancelReasonKbn,
        cancelStatusKbn = cancelStatusKbn,
        zashuReasonKbn = zashuReasonKbn,
        contractStatus = contractStatus,
        taskName = taskName,
        statusUpdateYmd = statusUpdateYmd,
        courseCd = courseCd,
        courseName = courseName,
        shareNum = shareNum,
        monthlyPremium = monthlyPremium,
        contractGaku = contractGaku,
        totalSaveNum = totalSaveNum,
        totalGaku = totalGaku,
        zipCd = zipCd,
        prefName = prefName,
        cityTownName = cityTownName,
        oazaTownName = oazaTownName,
        azaChomeName = azaChomeName,
        addr1 = addr1,
        addr2 = addr2,
        telNo = telNo,
        mobileNo = mobileNo,
        saPoint = saPoint,
        aaPoint = aaPoint,
        aPoint = aPoint,
        newPoint = newPoint,
        addPoint = addPoint,
        noallwPoint = noallwPoint,
        ssPoint = ssPoint,
        upPoint = upPoint,
        entryKbnName = entryKbnName,
        recruitRespBosyuCd = recruitRespBosyuCd,
        bosyuFamilyNameKanji = bosyuFamilyNameKanji,
        bosyuFirstNameKanji = bosyuFirstNameKanji,
        entryRespBosyuCd = entryRespBosyuCd,
        entryFamilyNameKanji = entryFamilyNameKanji,
        entryFirstNameKanji = entryFirstNameKanji,
        motoSupplyRankOrgCd = motoSupplyRankOrgCd,
        motoSupplyRankOrgName = motoSupplyRankOrgName,
        supplyRankOrgCd = supplyRankOrgCd,
        supplyRankOrgName = supplyRankOrgName,
        sectCd = sectCd,
        sectName = sectName,
        anspFlg = anspFlg,
        agreementKbn = agreementKbn,
        collectOfficeCd = collectOfficeCd,
        foreclosureFlg = foreclosureFlg,
        registYmd = registYmd,
        receptionNo = receptionNo
    )

/**
 * PaginatedResult<GroupContractSearchDto> -> PaginatedGroupContractResponse の変換
 */
fun PaginatedResult<GroupContractSearchDto>.toResponse(): PaginatedGroupContractResponse =
    PaginatedGroupContractResponse(
        content = content.map { it.toResponse() },
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        size = size
    )

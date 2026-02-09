package nexus.bff.controller.group.mapper

import nexus.bff.controller.group.mapper.buildContractStatus
import nexus.bff.controller.group.dto.GroupContractSearchResponse
import nexus.bff.controller.group.dto.PaginatedGroupContractResponse
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractSearchDto

/**
 * GroupContractSearchDto -> GroupContractSearchResponse の変換
 * フィールド順: SQL SELECT 順に準拠
 */
fun GroupContractSearchDto.toResponse(): GroupContractSearchResponse =
    GroupContractSearchResponse(
        // 基本情報
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
        
        // 契約状態（SQL SELECT 順）
        contractStatusKbn = contractStatusKbn,
        contractStatusName = contractStatusName,
        dmdStopReasonKbn = dmdStopReasonKbn,
        dmdStopReasonName = dmdStopReasonName,
        cancelReasonKbn = cancelReasonKbn,
        cancelReasonName = cancelReasonName,
        zashuReasonKbn = zashuReasonKbn,
        zashuReasonName = zashuReasonName,
        anspApproveKbn = anspApproveKbn,
        anspApproveName = anspApproveName,
        torikeshiReasonKbn = torikeshiReasonKbn,
        torikeshiReasonName = torikeshiReasonName,
        ecApproveKbn = ecApproveKbn,
        ecApproveName = ecApproveName,
        cancelStatusKbn = cancelStatusKbn,
        cancelStatusName = cancelStatusName,
        contractStatus = buildContractStatus(this),
        taskName = taskName,
        statusUpdateYmd = statusUpdateYmd,
        
        // コース・保障内容
        courseCd = courseCd,
        courseName = courseName,
        shareNum = shareNum,
        monthlyPremium = monthlyPremium,
        contractGaku = contractGaku,
        totalSaveNum = totalSaveNum,
        totalGaku = totalGaku,
        
        // 住所
        zipCd = zipCd,
        prefName = prefName,
        cityTownName = cityTownName,
        oazaTownName = oazaTownName,
        azaChomeName = azaChomeName,
        addr1 = addr1,
        addr2 = addr2,
        
        // 連絡先
        telNo = telNo,
        mobileNo = mobileNo,
        
        // ポイント
        saPoint = saPoint,
        aaPoint = aaPoint,
        aPoint = aPoint,
        newPoint = newPoint,
        addPoint = addPoint,
        noallwPoint = noallwPoint,
        ssPoint = ssPoint,
        upPoint = upPoint,
        
        // 募集
        entryKbnName = entryKbnName,
        recruitRespBosyuCd = recruitRespBosyuCd,
        bosyuFamilyNameKanji = bosyuFamilyNameKanji,
        bosyuFirstNameKanji = bosyuFirstNameKanji,
        entryRespBosyuCd = entryRespBosyuCd,
        entryFamilyNameKanji = entryFamilyNameKanji,
        entryFirstNameKanji = entryFirstNameKanji,
        
        // 供給ランク / 部門
        motoSupplyRankOrgCd = motoSupplyRankOrgCd,
        motoSupplyRankOrgName = motoSupplyRankOrgName,
        supplyRankOrgCd = supplyRankOrgCd,
        supplyRankOrgName = supplyRankOrgName,
        sectCd = sectCd,
        sectName = sectName,
        
        // その他
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

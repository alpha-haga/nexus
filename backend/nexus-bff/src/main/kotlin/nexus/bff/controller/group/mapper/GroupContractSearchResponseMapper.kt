package nexus.bff.controller.group.mapper

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
        dmdStopRasonKbn = dmdStopRasonKbn,
        dmdStopRasonName = dmdStopRasonName,
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
 * 状態文字列を結合する（片側 NULL でも表示）
 * 
 * - base と reason の両方が非null → "base（reason）"
 * - base のみ非null → base
 * - reason のみ非null → reason
 * - 両方 null → null
 */
private fun joinStatus(base: String?, reason: String?): String? {
    return when {
        base != null && reason != null -> "$base（$reason）"
        base != null -> base
        reason != null -> reason
        else -> null
    }
}

/**
 * contract_status を組み立てる（SQL CASE ロジックを 1:1 で写経）
 * 
 * SQL CASE の構造をそのまま Kotlin when/if に変換
 * 文字列結合は joinStatus を使用（片側 NULL でも表示）
 */
private fun buildContractStatus(dto: GroupContractSearchDto): String? {
    val contractStatusKbn = dto.contractStatusKbn
    val contractStatusName = dto.contractStatusName
    
    return when (contractStatusKbn) {
        "1" -> contractStatusName
        "2" -> {
            when (dto.torikeshiReasonKbn) {
                "1" -> joinStatus(contractStatusName, dto.torikeshiReasonName)
                "2" -> contractStatusName
                "3" -> dto.torikeshiReasonName
                else -> null
            }
        }
        "3" -> {
            when {
                dto.anspApproveKbn == "1" -> {
                    when {
                        dto.dmdStopRasonKbn == "B" -> joinStatus(contractStatusName, dto.dmdStopRasonName)
                        else -> joinStatus(contractStatusName, dto.anspApproveName)
                    }
                }
                dto.anspApproveKbn == "2" -> joinStatus(contractStatusName, dto.anspApproveName)
                dto.ecApproveKbn == "1" -> {
                    when {
                        dto.dmdStopRasonKbn == "B" -> joinStatus(contractStatusName, dto.dmdStopRasonName)
                        else -> joinStatus(contractStatusName, dto.ecApproveName)
                    }
                }
                else -> {
                    when {
                        dto.dmdStopRasonName == null -> contractStatusName
                        else -> joinStatus(contractStatusName, dto.dmdStopRasonName)
                    }
                }
            }
        }
        "4" -> joinStatus(contractStatusName, dto.cancelReasonName)
        "5" -> joinStatus(contractStatusName, dto.zashuReasonName)
        "6" -> contractStatusName
        else -> null
    }
}

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

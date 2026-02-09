package nexus.bff.controller.group.mapper

import nexus.bff.controller.group.mapper.buildContractStatus
import nexus.bff.controller.group.dto.GroupContractDetailResponse
import nexus.group.query.GroupContractDetailDto

/**
 * GroupContractDetailDto -> GroupContractDetailResponse の変換
 */
fun GroupContractDetailDto.toDetailResponse(): GroupContractDetailResponse =
    GroupContractDetailResponse(
        // 基本識別情報
        cmpCd = cmpCd,
        cmpShortName = cmpShortName,
        contractNo = contractNo,
        familyNo = familyNo,
        houseNo = houseNo,
        
        // 契約者情報
        familyNameGaiji = familyNameGaiji,
        firstNameGaiji = firstNameGaiji,
        familyNameKana = familyNameKana,
        firstNameKana = firstNameKana,
        contractReceiptYmd = contractReceiptYmd,
        birthday = birthday,
        
        // 契約状態（表示用材料）
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
        
        // コース情報
        courseCd = courseCd,
        courseName = courseName,
        
        // 連絡先
        telNo = telNo,
        mobileNo = mobileNo,
        
        // 住所
        prefName = prefName,
        cityTownName = cityTownName,
        addr1 = addr1,
        addr2 = addr2
    )

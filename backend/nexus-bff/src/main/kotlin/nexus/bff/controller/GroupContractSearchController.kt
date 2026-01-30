package nexus.bff.controller

import nexus.core.exception.ValidationException
import nexus.core.pagination.PaginatedResult
import nexus.group.query.GroupContractQueryService
import nexus.group.query.GroupContractSearchCondition
import nexus.group.query.GroupContractSearchDto
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 法人横断契約検索API（P04-2 JDBC Read 実装）
 *
 * @Profile("jdbc") で有効化、Bean 競合を回避
 * - 既存の GroupContractController とは異なる URL にマッピング
 * - 新しい SQL ベースの検索条件に対応
 * - 検索結果は GroupContractSearchDto（SQL alias に完全一致）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts/search")
class GroupContractSearchController(
    private val groupContractQueryService: GroupContractQueryService
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) contractReceiptYmdFrom: String?,
        @RequestParam(required = false) contractReceiptYmdTo: String?,
        @RequestParam(required = false) contractNo: String?,
        @RequestParam(required = false) familyNmKana: String?,
        @RequestParam(required = false) telNo: String?,
        @RequestParam(required = false) bosyuCd: String?,
        @RequestParam(required = false) courseCd: String?,
        @RequestParam(required = false) contractStatusKbn: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedGroupContractResponse> {
        // ページネーション検証
        if (page < 0) throw ValidationException("page", "page must be >= 0")
        if (size <= 0) throw ValidationException("size", "size must be > 0")
        if (size > 100) throw ValidationException("size", "size must be <= 100")

        // 検索条件を構築（全て nullable、指定されたもののみ WHERE に含める）
        val condition = GroupContractSearchCondition(
            contractReceiptYmdFrom = contractReceiptYmdFrom,
            contractReceiptYmdTo = contractReceiptYmdTo,
            contractNo = contractNo,
            familyNmKana = familyNmKana,
            telNo = telNo,
            bosyuCd = bosyuCd,
            courseCd = courseCd,
            contractStatusKbn = contractStatusKbn
        )

        // 検索実行
        val result = groupContractQueryService.search(
            condition = condition,
            page = page,
            size = size
        )

        return ResponseEntity.ok(result.toResponse())
    }
}

// Response DTOs

data class PaginatedGroupContractResponse(
    val content: List<GroupContractSearchResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val page: Int,
    val size: Int
)

data class GroupContractSearchResponse(
    // 基本情報
    val companyCd: String,
    val companyShortName: String?,
    val contractNo: String,
    val familyNo: String,
    val houseNo: String?,
    val familyNameGaiji: String?,
    val firstNameGaiji: String?,
    val familyNameKana: String?,
    val firstNameKana: String?,
    val contractReceiptYmd: String?,
    val birthday: String?,
    
    // 契約状態
    val contractStatusKbn: String?,
    val dmdStopRasonKbn: String?,
    val cancelReasonKbn: String?,
    val cancelStatusKbn: String?,
    val zashuReasonKbn: String?,
    val contractStatus: String?,
    val taskName: String?,
    val statusUpdateYmd: String?,
    
    // コース・保障内容
    val courseCd: String?,
    val courseName: String?,
    val shareNum: Int?,
    val monthlyPremium: Long?,
    val contractGaku: Long?,
    val totalSaveNum: Long?,
    val totalGaku: Long?,
    
    // 住所
    val zipCd: String?,
    val prefName: String?,
    val cityTownName: String?,
    val oazaTownName: String?,
    val azaChomeName: String?,
    val addr1: String?,
    val addr2: String?,
    
    // 連絡先
    val telNo: String?,
    val mobileNo: String?,
    
    // ポイント
    val saPoint: Int?,
    val aaPoint: Int?,
    val aPoint: Int?,
    val newPoint: Int?,
    val addPoint: Int?,
    val noallwPoint: Int?,
    val ssPoint: Int?,
    val upPoint: Int?,
    
    // 募集
    val entryKbnName: String?,
    val recruitRespBosyuCd: String?,
    val bosyuFamilyNameKanji: String?,
    val bosyuFirstNameKanji: String?,
    val entryRespBosyuCd: String?,
    val entryFamilyNameKanji: String?,
    val entryFirstNameKanji: String?,
    
    // 供給ランク / 部門
    val motoSupplyRankOrgCd: String?,
    val motoSupplyRankOrgName: String?,
    val supplyRankOrgCd: String?,
    val supplyRankOrgName: String?,
    val sectCd: String?,
    val sectName: String?,
    
    // その他
    val anspFlg: String?,
    val agreementKbn: String?,
    val collectOfficeCd: String?,
    val foreclosureFlg: String?,
    val registYmd: String?,
    val receptionNo: String?
)

private fun PaginatedResult<GroupContractSearchDto>.toResponse(): PaginatedGroupContractResponse =
    PaginatedGroupContractResponse(
        content = content.map { it.toResponse() },
        totalElements = totalElements,
        totalPages = totalPages,
        page = page,
        size = size
    )

private fun GroupContractSearchDto.toResponse(): GroupContractSearchResponse =
    GroupContractSearchResponse(
        companyCd = companyCd,
        companyShortName = companyShortName,
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
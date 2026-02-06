package nexus.bff.controller.group

import nexus.bff.controller.group.dto.PaginatedGroupContractResponse
import nexus.bff.controller.group.mapper.toResponse
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractQueryService
import nexus.group.query.GroupContractSearchCondition
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
        @RequestParam(required = false) contractorName: String?,
        @RequestParam(required = false) telNo: String?,
        @RequestParam(required = false) staffName: String?,
        @RequestParam(required = false) cmpCds: List<String>?,
        @RequestParam(required = false) bosyuCd: String?,
        @RequestParam(required = false) courseCd: String?,
        @RequestParam(required = false) courseName: String?,
        @RequestParam(required = false) contractStatusKbn: String?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<PaginatedGroupContractResponse> {
        // ページネーション検証
        if (page < 0) throw ValidationException("page", "page must be >= 0")
        if (size <= 0) throw ValidationException("size", "size must be > 0")
        if (size > 100) throw ValidationException("size", "size must be <= 100")

        // cmpCds の衛生処理（空配列/空文字のみの場合は null に正規化）
        val normalizedCmpCds = cmpCds?.filter { it.isNotBlank() }?.takeIf { it.isNotEmpty() }

        // 検索条件を構築（全て nullable、指定されたもののみ WHERE に含める）
        val condition = GroupContractSearchCondition(
            contractReceiptYmdFrom = contractReceiptYmdFrom,
            contractReceiptYmdTo = contractReceiptYmdTo,
            contractNo = contractNo,
            contractorName = contractorName,
            telNo = telNo,
            staffName = staffName,
            cmpCds = normalizedCmpCds,
            bosyuCd = bosyuCd,
            courseCd = courseCd,
            courseName = courseName,
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

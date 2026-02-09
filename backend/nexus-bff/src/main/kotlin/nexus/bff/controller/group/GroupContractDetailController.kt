package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractDetailResponse
import nexus.bff.controller.group.mapper.toDetailResponse
import nexus.core.exception.ResourceNotFoundException
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 法人横断契約詳細API（P2-6 実装）
 *
 * @Profile("jdbc") で有効化、Bean 競合を回避
 * - 既存の GroupContractSearchController とは異なる URL にマッピング
 * - 詳細取得専用の API
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractDetailController(
    private val groupContractQueryService: GroupContractQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}")
    fun getDetail(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractDetailResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        // 詳細取得
        val detail = groupContractQueryService.findDetail(cmpCd, contractNo)
            ?: throw ResourceNotFoundException("GroupContract", "$cmpCd/$contractNo")

        return ResponseEntity.ok(detail.toDetailResponse())
    }
}

package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractContractContentsResponse
import nexus.core.exception.ResourceNotFoundException
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractContractContentsQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約内容（追加情報） API（TODO カード: 契約内容）
 *
 * P2-7: 実装完了（501 → 200）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractContractContentsController(
    private val contractContentsQueryService: GroupContractContractContentsQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}/contractContents")
    fun getContractContents(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractContractContentsResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        // 契約内容取得
        val contents = contractContentsQueryService.getContractContents(cmpCd, contractNo)
            ?: throw ResourceNotFoundException("GroupContractContractContents", "$cmpCd/$contractNo")

        // Response DTO に変換
        return ResponseEntity.ok(
            GroupContractContractContentsResponse(
                cmpCd = contents.cmpCd,
                contractNo = contents.contractNo,
                attributes = contents.attributes
            )
        )
    }
}

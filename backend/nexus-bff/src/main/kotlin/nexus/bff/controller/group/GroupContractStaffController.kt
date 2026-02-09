package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractStaffResponse
import nexus.core.exception.NotImplementedException
import nexus.core.exception.ValidationException
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約担当者情報 API（TODO カード: 担当者情報）
 *
 * P2-6：分離設計確定のため、エンドポイントを先に確保し、未実装であることを 501 で明示する。
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractStaffController {

    @GetMapping("/{cmpCd}/{contractNo}/staff")
    fun getStaff(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractStaffResponse> {
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        throw NotImplementedException("GroupContract Staff: $cmpCd/$contractNo")
    }
}

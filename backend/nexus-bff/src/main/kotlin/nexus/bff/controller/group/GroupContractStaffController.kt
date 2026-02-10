package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractStaffResponse
import nexus.core.exception.ResourceNotFoundException
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractStaffQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約担当者情報 API（TODO カード: 担当者情報）
 *
 * P2-7: 実装完了（501 → 200）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractStaffController(
    private val staffQueryService: GroupContractStaffQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}/staff")
    fun getStaff(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractStaffResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd is blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo is blank")
        }

        // 担当者情報取得
        val staff = staffQueryService.getStaffs(cmpCd, contractNo)
            ?: throw ResourceNotFoundException("GroupContractStaff", "$cmpCd/$contractNo")

        // Response DTO に変換
        return ResponseEntity.ok(
            GroupContractStaffResponse(
                cmpCd = staff.cmpCd,
                contractNo = staff.contractNo,
                staffs = staff.staffs.map { s ->
                    GroupContractStaffResponse.Staff(
                        role = s.role,
                        roleLabel = s.roleLabel,
                        bosyuCd = s.bosyuCd,
                        staffName = s.staffName
                    )
                }
            )
        )
    }
}

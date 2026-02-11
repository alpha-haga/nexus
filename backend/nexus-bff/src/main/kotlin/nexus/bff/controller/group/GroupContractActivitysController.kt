package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractActivitysResponse
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractActivitysQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約対応履歴 API（TODO カード: 対応履歴）
 *
 * P2-10: 実装完了（501 → 200）
 * - 権限制御は既存の GlobalExceptionHandler に従う（403/404）
 * - 0件時は空配列を返す（404 にしない）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractActivitysController(
    private val activitysQueryService: GroupContractActivitysQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}/activitys")
    fun getActivitys(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractActivitysResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        // 対応履歴取得（0件時も空配列を返す）
        val activitysDto = activitysQueryService.getActivitys(cmpCd, contractNo)

        // Response DTO に変換
        return ResponseEntity.ok(
            GroupContractActivitysResponse(
                cmpCd = activitysDto.cmpCd,
                contractNo = activitysDto.contractNo,
                activities = activitysDto.activities.map { activity ->
                    GroupContractActivitysResponse.Activity(
                        recNo = activity.recNo,
                        serviceYmd = activity.serviceYmd,
                        serviceKbn = activity.serviceKbn,
                        serviceName = activity.serviceName,
                        serviceMethod = activity.serviceMethod,
                        serviceMethodName = activity.serviceMethodName,
                        visitReasonKbn = activity.visitReasonKbn,
                        visitReasonName = activity.visitReasonName,
                        callStatusKbn = activity.callStatusKbn,
                        callStatusName = activity.callStatusName,
                        receptionPsnNm = activity.receptionPsnNm,
                        freeComment = activity.freeComment,
                        responsibleFamilyName = activity.responsibleFamilyName,
                        responsibleFirstName = activity.responsibleFirstName,
                        responsibleSectName = activity.responsibleSectName,
                    )
                }
            )
        )
    }
}

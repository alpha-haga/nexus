package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractBankAccountResponse
import nexus.core.exception.ResourceNotFoundException
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractBankAccountQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約口座情報 API（TODO カード: 口座情報）
 *
 * P2-8: 実装完了（501 → 200）
 * - 権限制御は既存の GlobalExceptionHandler に従う（403/404）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractBankAccountController(
    private val bankAccountQueryService: GroupContractBankAccountQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}/bankAccount")
    fun getBankAccount(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractBankAccountResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        // 口座情報取得
        val bankAccount = bankAccountQueryService.getBankAccount(cmpCd, contractNo)
            ?: throw ResourceNotFoundException("GroupContractBankAccount", "$cmpCd/$contractNo")

        // Response DTO に変換
        return ResponseEntity.ok(
            GroupContractBankAccountResponse(
                cmpCd = bankAccount.cmpCd,
                contractNo = bankAccount.contractNo,
                debitMethodKbn = bankAccount.debitMethodKbn,
                debitMethodName = bankAccount.debitMethodName,
                saveMethodKbn = bankAccount.saveMethodKbn,
                saveMethodName = bankAccount.saveMethodName,
                bankCd = bankAccount.bankCd,
                bankName = bankAccount.bankName,
                bankBranchCd = bankAccount.bankBranchCd,
                bankBranchName = bankAccount.bankBranchName,
                depositorName = bankAccount.depositorName,
                accTypeKbn = bankAccount.accTypeKbn,
                accNo = bankAccount.accNo,
                accStatusKbn = bankAccount.accStatusKbn,
                registrationUpdateYmd = bankAccount.registrationUpdateYmd,
                abolishFlg = bankAccount.abolishFlg,
                compelMonthPayFlg = bankAccount.compelMonthPayFlg,
                monthlyPremium = bankAccount.monthlyPremium,
                remainingSaveNum = bankAccount.remainingSaveNum,
                remainingReceiptGaku = bankAccount.remainingReceiptGaku,
                discountGaku = bankAccount.discountGaku,
                viewFlg = bankAccount.viewFlg
            )
        )
    }
}

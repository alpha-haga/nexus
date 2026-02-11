package nexus.bff.controller.group

import nexus.bff.controller.group.dto.GroupContractReceiptResponse
import nexus.core.exception.ValidationException
import nexus.group.query.GroupContractReceiptQueryService
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 契約入金情報 API（TODO カード: 入金情報）
 *
 * P2-9: 実装完了（501 → 200）
 * - 権限制御は既存の GlobalExceptionHandler に従う（403/404）
 * - 0件時は空配列を返す（404 にしない）
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts")
class GroupContractReceiptController(
    private val receiptQueryService: GroupContractReceiptQueryService
) {

    @GetMapping("/{cmpCd}/{contractNo}/receipts")
    fun getReceipts(
        @PathVariable cmpCd: String,
        @PathVariable contractNo: String
    ): ResponseEntity<GroupContractReceiptResponse> {
        // バリデーション
        if (cmpCd.isBlank()) {
            throw ValidationException("cmpCd", "cmpCd must not be blank")
        }
        if (contractNo.isBlank()) {
            throw ValidationException("contractNo", "contractNo must not be blank")
        }

        // 入金情報取得（0件時も空配列を返す）
        val receiptDto = receiptQueryService.getReceipts(cmpCd, contractNo)

        // Response DTO に変換
        return ResponseEntity.ok(
            GroupContractReceiptResponse(
                cmpCd = receiptDto.cmpCd,
                contractNo = receiptDto.contractNo,
                receipts = receiptDto.receipts.map { receipt ->
                    GroupContractReceiptResponse.Receipt(
                        listNo = receipt.listNo,
                        ym = receipt.ym,
                        dmdMethodKbn = receipt.dmdMethodKbn,
                        dmdRsltKbn = receipt.dmdRsltKbn,
                        dmdMethodName = receipt.dmdMethodName,
                        dmdRsltName = receipt.dmdRsltName,
                        clientConsignorKbn = receipt.clientConsignorKbn,
                        clientConsignorName = receipt.clientConsignorName,
                        discountGaku = receipt.discountGaku,
                        shareNum = receipt.shareNum,
                        courseMonthlyPremium = receipt.courseMonthlyPremium,
                        receiptReceiptMethodKbn = receipt.receiptReceiptMethodKbn,
                        receiptReceiptMethodName = receipt.receiptReceiptMethodName,
                        receiptReceiptYmd = receipt.receiptReceiptYmd,
                        receiptReceiptGaku = receipt.receiptReceiptGaku,
                        receiptNum = receipt.receiptNum,
                        pekeReceiptMethodKbn = receipt.pekeReceiptMethodKbn,
                        pekeReceiptReasonKbn = receipt.pekeReceiptReasonKbn,
                        pekeReceiptReasonName = receipt.pekeReceiptReasonName,
                        pekeReceiptYmd = receipt.pekeReceiptYmd,
                        pekeReceiptGaku = receipt.pekeReceiptGaku,
                        pekeNum = receipt.pekeNum,
                        refundReasonKbn = receipt.refundReasonKbn,
                        refundReasonName = receipt.refundReasonName,
                        refundGaku = receipt.refundGaku,
                        refundYmd = receipt.refundYmd,
                        count = receipt.count,
                        paymentRec = receipt.paymentRec,
                        refundCount = receipt.refundCount,
                        refundPayment = receipt.refundPayment,
                        pekeReceiptReasonKbnCd = receipt.pekeReceiptReasonKbnCd,
                        opeRecFlg = receipt.opeRecFlg,
                        opeUsageKbn = receipt.opeUsageKbn,
                        opeUsageName = receipt.opeUsageName,
                        opeUsagePurposeKbn = receipt.opeUsagePurposeKbn,
                        opeUsagePurposeName = receipt.opeUsagePurposeName,
                        partUsageGaku = receipt.partUsageGaku,
                        opeYmd = receipt.opeYmd,
                    )
                }
            )
        )
    }
}

package nexus.gojo.contract.service

import nexus.core.exception.BusinessRuleViolationException
import nexus.core.exception.ResourceNotFoundException
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import nexus.gojo.contract.dto.CreateContractCommand
import nexus.gojo.contract.entity.Contract
import nexus.gojo.contract.entity.ContractStatus
import nexus.gojo.contract.entity.Payment
import nexus.gojo.contract.entity.PaymentMethod
import nexus.gojo.contract.repository.ContractRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 互助会契約サービス
 *
 * 業務モジュール - identity/group への直接更新禁止
 * PersonIdでのみ人物を参照する
 */
@Service
@Transactional
class ContractService(
    private val contractRepository: ContractRepository
) {

    /**
     * 契約を作成
     */
    fun createContract(command: CreateContractCommand): Contract {
        val contract = Contract.create(
            contractId = GojoContractId.generate(),
            corporationId = command.corporationId,
            contractorPersonId = command.contractorPersonId,
            planCode = command.planCode,
            planName = command.planName,
            monthlyFee = command.monthlyFee,
            maturityAmount = command.maturityAmount,
            contractDate = command.contractDate
        ).apply {
            beneficiaryPersonId = command.beneficiaryPersonId?.value
        }

        return contractRepository.save(contract)
    }

    /**
     * 契約を取得
     */
    @Transactional(readOnly = true)
    fun findById(contractId: GojoContractId): Contract {
        return contractRepository.findById(contractId)
            ?: throw ResourceNotFoundException("Contract", contractId)
    }
    
    /**
     * 契約者の契約一覧を取得
     */
    @Transactional(readOnly = true)
    fun findByContractor(personId: PersonId): List<Contract> {
        return contractRepository.findByContractorPersonId(personId.value)
    }

    /**
     * 入金を記録
     */
    fun recordPayment(
        contractId: GojoContractId,
        amount: Long,
        paymentDate: LocalDate,
        paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER
    ): Contract {
        val contract = findById(contractId)

        if (contract.status != ContractStatus.ACTIVE) {
            throw BusinessRuleViolationException(
                "INACTIVE_CONTRACT",
                "Cannot record payment for inactive contract"
            )
        }

        val payment = Payment(
            contract = contract,
            amount = amount,
            paymentDate = paymentDate,
            paymentMethod = paymentMethod
        )

        contract.payments.add(payment)
        contract.updatedAt = LocalDateTime.now()

        // 満期チェック
        if (contract.isMatured) {
            contract.status = ContractStatus.MATURED
            contract.maturityDate = LocalDate.now()
        }

        return contractRepository.save(contract)
    }

    /**
     * 契約を解約
     */
    fun cancel(contractId: GojoContractId, reason: String): Contract {
        val contract = findById(contractId)

        if (contract.status != ContractStatus.ACTIVE) {
            throw BusinessRuleViolationException(
                "ALREADY_INACTIVE",
                "Contract is already inactive"
            )
        }

        contract.status = ContractStatus.CANCELLED
        contract.updatedAt = LocalDateTime.now()

        return contractRepository.save(contract)
    }

    /**
     * 契約を使用済みにする（葬祭/冠婚で使用）
     */
    fun markAsUsed(contractId: GojoContractId): Contract {
        val contract = findById(contractId)

        if (contract.status != ContractStatus.ACTIVE && contract.status != ContractStatus.MATURED) {
            throw BusinessRuleViolationException(
                "CANNOT_USE",
                "Contract cannot be used in current status: ${contract.status}"
            )
        }

        contract.status = ContractStatus.USED
        contract.updatedAt = LocalDateTime.now()

        return contractRepository.save(contract)
    }
}

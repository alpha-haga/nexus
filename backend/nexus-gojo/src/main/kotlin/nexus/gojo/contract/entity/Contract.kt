package nexus.gojo.contract.entity

import jakarta.persistence.*
import nexus.core.id.CorporationId
import nexus.core.id.GojoContractId
import nexus.core.id.PersonId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 互助会契約エンティティ
 *
 * 業務モジュール - person/identity への直接依存禁止
 * PersonIdでのみ参照する
 */
@Entity
@Table(name = "gojo_contracts")
class Contract(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    /** 契約者 PersonId（identity モジュールへの直接参照禁止） */
    @Column(name = "contractor_person_id", nullable = false)
    val contractorPersonId: String,

    /** 受益者 PersonId（契約者と異なる場合がある） */
    @Column(name = "beneficiary_person_id")
    var beneficiaryPersonId: String? = null,

    @Column(name = "plan_code", nullable = false)
    val planCode: String,

    @Column(name = "plan_name", nullable = false)
    val planName: String,

    /** 掛金（月額） */
    @Column(name = "monthly_fee", nullable = false)
    val monthlyFee: Long,

    /** 満期金額 */
    @Column(name = "maturity_amount", nullable = false)
    val maturityAmount: Long,

    /** 契約日 */
    @Column(name = "contract_date", nullable = false)
    val contractDate: LocalDate,

    /** 満期日 */
    @Column(name = "maturity_date")
    var maturityDate: LocalDate? = null,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: ContractStatus = ContractStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    @OneToMany(mappedBy = "contract", cascade = [CascadeType.ALL], orphanRemoval = true)
    val payments: MutableList<Payment> = mutableListOf()

    fun toContractId(): GojoContractId = GojoContractId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)
    fun contractorPersonId(): PersonId = PersonId(contractorPersonId)
    fun beneficiaryPersonId(): PersonId? = beneficiaryPersonId?.let { PersonId(it) }

    /** 積立済み金額 */
    val totalPaidAmount: Long
        get() = payments.filter { it.status == PaymentStatus.COMPLETED }
            .sumOf { it.amount }

    /** 進捗率 */
    val progressRate: Double
        get() = if (maturityAmount > 0) totalPaidAmount.toDouble() / maturityAmount else 0.0

    /** 満期到達済み */
    val isMatured: Boolean
        get() = totalPaidAmount >= maturityAmount

    companion object {
        fun create(
            contractId: GojoContractId,
            corporationId: CorporationId,
            contractorPersonId: PersonId,
            planCode: String,
            planName: String,
            monthlyFee: Long,
            maturityAmount: Long,
            contractDate: LocalDate
        ): Contract {
            return Contract(
                id = contractId.value,
                corporationId = corporationId.value,
                contractorPersonId = contractorPersonId.value,
                planCode = planCode,
                planName = planName,
                monthlyFee = monthlyFee,
                maturityAmount = maturityAmount,
                contractDate = contractDate
            )
        }
    }
}

/**
 * 契約ステータス
 */
enum class ContractStatus {
    ACTIVE,     // 契約中
    MATURED,    // 満期
    USED,       // 使用済み
    CANCELLED,  // 解約
    SUSPENDED   // 休止
}

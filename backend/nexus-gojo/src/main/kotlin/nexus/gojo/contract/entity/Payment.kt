package nexus.gojo.contract.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 互助会入金
 */
@Entity
@Table(name = "gojo_payments")
class Payment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    val contract: Contract,

    @Column(name = "amount", nullable = false)
    val amount: Long,

    @Column(name = "payment_date", nullable = false)
    val paymentDate: LocalDate,

    @Column(name = "payment_method")
    @Enumerated(EnumType.STRING)
    val paymentMethod: PaymentMethod = PaymentMethod.BANK_TRANSFER,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.COMPLETED,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class PaymentMethod {
    BANK_TRANSFER,  // 銀行振込
    CASH,           // 現金
    CREDIT_CARD,    // クレジットカード
    DIRECT_DEBIT    // 口座振替
}

enum class PaymentStatus {
    PENDING,    // 未確定
    COMPLETED,  // 入金済み
    FAILED,     // 失敗
    REFUNDED    // 返金
}

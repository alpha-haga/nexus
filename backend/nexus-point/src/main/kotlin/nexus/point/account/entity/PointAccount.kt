package nexus.point.account.entity

import jakarta.persistence.*
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.id.PointAccountId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * ポイントアカウントエンティティ
 *
 * 業務モジュール - identity/group への直接更新禁止
 */
@Entity
@Table(name = "point_accounts")
class PointAccount(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    /** 所有者 PersonId */
    @Column(name = "owner_person_id", nullable = false)
    val ownerPersonId: String,

    /** 現在のポイント残高 */
    @Column(name = "balance", nullable = false)
    var balance: Long = 0,

    /** 累計獲得ポイント */
    @Column(name = "total_earned", nullable = false)
    var totalEarned: Long = 0,

    /** 累計使用ポイント */
    @Column(name = "total_used", nullable = false)
    var totalUsed: Long = 0,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    var status: AccountStatus = AccountStatus.ACTIVE,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    @OneToMany(mappedBy = "account", cascade = [CascadeType.ALL], orphanRemoval = true)
    val transactions: MutableList<PointTransaction> = mutableListOf()

    fun toAccountId(): PointAccountId = PointAccountId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)
    fun ownerPersonId(): PersonId = PersonId(ownerPersonId)

    companion object {
        fun create(
            accountId: PointAccountId,
            corporationId: CorporationId,
            ownerPersonId: PersonId
        ): PointAccount {
            return PointAccount(
                id = accountId.value,
                corporationId = corporationId.value,
                ownerPersonId = ownerPersonId.value
            )
        }
    }
}

enum class AccountStatus {
    ACTIVE,     // 有効
    SUSPENDED,  // 停止
    CLOSED      // 閉鎖
}

/**
 * ポイント取引履歴
 */
@Entity
@Table(name = "point_transactions")
class PointTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    val account: PointAccount,

    @Column(name = "transaction_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val transactionType: TransactionType,

    /** ポイント数（付与は正、使用・失効は負） */
    @Column(name = "points", nullable = false)
    val points: Long,

    /** 取引後残高 */
    @Column(name = "balance_after", nullable = false)
    val balanceAfter: Long,

    /** 取引理由 */
    @Column(name = "reason")
    val reason: String? = null,

    /** 関連業務ID（互助会契約ID、葬祭案件ID等） */
    @Column(name = "reference_id")
    val referenceId: String? = null,

    /** 有効期限（付与ポイントの場合） */
    @Column(name = "expires_at")
    val expiresAt: LocalDate? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class TransactionType {
    EARN,       // 獲得
    USE,        // 使用
    EXPIRE,     // 失効
    ADJUST,     // 調整
    TRANSFER    // 移行
}

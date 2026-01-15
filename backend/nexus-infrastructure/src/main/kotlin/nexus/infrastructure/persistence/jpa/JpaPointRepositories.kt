package nexus.infrastructure.persistence.jpa

import nexus.point.account.entity.PointAccount
import nexus.point.account.entity.PointTransaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface JpaPointAccountRepository : JpaRepository<PointAccount, String> {

    fun findByCorporationId(corporationId: String): List<PointAccount>
    fun findByOwnerPersonId(ownerPersonId: String): PointAccount?

    @Query("""
        SELECT pa FROM PointAccount pa
        WHERE pa.corporationId = :corporationId
          AND pa.status = 'ACTIVE'
          AND pa.balance > 0
    """)
    fun findActiveAccountsWithBalance(corporationId: String): List<PointAccount>
}

interface JpaPointTransactionRepository : JpaRepository<PointTransaction, Long> {

    fun findByAccountIdOrderByCreatedAtDesc(accountId: String): List<PointTransaction>

    @Query("""
        SELECT pt FROM PointTransaction pt
        WHERE pt.account.id = :accountId
          AND pt.transactionType = 'EARN'
          AND pt.expiresAt IS NOT NULL
          AND pt.expiresAt <= :date
    """)
    fun findExpiringTransactions(
        accountId: String,
        date: LocalDate
    ): List<PointTransaction>
}

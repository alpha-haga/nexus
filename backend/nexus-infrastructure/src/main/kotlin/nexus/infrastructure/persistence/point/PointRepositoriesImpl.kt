package nexus.infrastructure.persistence.point

import nexus.point.account.entity.PointAccount
import nexus.point.account.entity.PointTransaction
import nexus.point.account.repository.PointAccountRepository
import nexus.point.account.repository.PointTransactionRepository
import nexus.infrastructure.persistence.jpa.JpaPointAccountRepository
import nexus.infrastructure.persistence.jpa.JpaPointTransactionRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class PointAccountRepositoryImpl(
    private val jpa: JpaPointAccountRepository
) : PointAccountRepository {

    override fun save(account: PointAccount): PointAccount =
        jpa.save(account)

    override fun findById(id: String): PointAccount? =
        jpa.findById(id).orElse(null)

    override fun findByCorporationId(corporationId: String): List<PointAccount> =
        jpa.findByCorporationId(corporationId)

    override fun findByOwnerPersonId(ownerPersonId: String): PointAccount? =
        jpa.findByOwnerPersonId(ownerPersonId)

    override fun findActiveAccountsWithBalance(corporationId: String): List<PointAccount> =
        jpa.findActiveAccountsWithBalance(corporationId)
}

@Repository
class PointTransactionRepositoryImpl(
    private val jpa: JpaPointTransactionRepository
) : PointTransactionRepository {

    override fun save(tx: PointTransaction): PointTransaction =
        jpa.save(tx)

    override fun findByAccountIdOrderByCreatedAtDesc(accountId: String): List<PointTransaction> =
        jpa.findByAccountIdOrderByCreatedAtDesc(accountId)

    override fun findExpiringTransactions(accountId: String, date: LocalDate): List<PointTransaction> =
        jpa.findExpiringTransactions(accountId, date)
}

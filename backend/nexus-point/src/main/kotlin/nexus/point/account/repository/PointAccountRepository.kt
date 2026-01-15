package nexus.point.account.repository

import nexus.point.account.entity.PointAccount
import nexus.point.account.entity.PointTransaction
import java.time.LocalDate

interface PointAccountRepository {
    fun save(account: PointAccount): PointAccount
    fun findById(id: String): PointAccount?
    fun findByCorporationId(corporationId: String): List<PointAccount>
    fun findByOwnerPersonId(ownerPersonId: String): PointAccount?
    fun findActiveAccountsWithBalance(corporationId: String): List<PointAccount>
}

interface PointTransactionRepository {
    fun save(tx: PointTransaction): PointTransaction
    fun findByAccountIdOrderByCreatedAtDesc(accountId: String): List<PointTransaction>
    fun findExpiringTransactions(accountId: String, date: LocalDate): List<PointTransaction>
}

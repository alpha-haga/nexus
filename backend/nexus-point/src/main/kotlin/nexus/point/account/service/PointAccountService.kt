package nexus.point.account.service

import nexus.core.exception.BusinessRuleViolationException
import nexus.core.exception.ResourceNotFoundException
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.id.PointAccountId
import nexus.point.account.dto.EarnPointsCommand
import nexus.point.account.dto.UsePointsCommand
import nexus.point.account.entity.AccountStatus
import nexus.point.account.entity.PointAccount
import nexus.point.account.entity.PointTransaction
import nexus.point.account.entity.TransactionType
import nexus.point.account.repository.PointAccountRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * ポイントアカウントサービス
 */
@Service
@Transactional
class PointAccountService(
    private val pointAccountRepository: PointAccountRepository
) {

    /**
     * アカウントを作成
     */
    fun createAccount(corporationId: CorporationId, ownerPersonId: PersonId): PointAccount {
        // 既存アカウントの確認
        val existing = pointAccountRepository.findByOwnerPersonId(ownerPersonId.value)
        if (existing != null) {
            throw BusinessRuleViolationException(
                "ACCOUNT_ALREADY_EXISTS",
                "Point account already exists for this person"
            )
        }

        val account = PointAccount.create(
            accountId = PointAccountId.generate(),
            corporationId = corporationId,
            ownerPersonId = ownerPersonId
        )

        return pointAccountRepository.save(account)
    }

    /**
     * アカウントを取得
     */
    @Transactional(readOnly = true)
    fun findById(accountId: PointAccountId): PointAccount {
        return pointAccountRepository.findById(accountId.value)
            .orElseThrow { ResourceNotFoundException("PointAccount", accountId.value) }
    }

    /**
     * 人物のアカウントを取得
     */
    @Transactional(readOnly = true)
    fun findByOwner(ownerPersonId: PersonId): PointAccount? {
        return pointAccountRepository.findByOwnerPersonId(ownerPersonId.value)
    }

    /**
     * ポイントを付与
     */
    fun earnPoints(accountId: PointAccountId, command: EarnPointsCommand): PointAccount {
        val account = findById(accountId)

        if (account.status != AccountStatus.ACTIVE) {
            throw BusinessRuleViolationException(
                "ACCOUNT_NOT_ACTIVE",
                "Cannot earn points on inactive account"
            )
        }

        val newBalance = account.balance + command.points
        val transaction = PointTransaction(
            account = account,
            transactionType = TransactionType.EARN,
            points = command.points,
            balanceAfter = newBalance,
            reason = command.reason,
            referenceId = command.referenceId,
            expiresAt = command.expiresAt
        )

        account.transactions.add(transaction)
        account.balance = newBalance
        account.totalEarned += command.points
        account.updatedAt = LocalDateTime.now()

        return pointAccountRepository.save(account)
    }

    /**
     * ポイントを使用
     */
    fun usePoints(accountId: PointAccountId, command: UsePointsCommand): PointAccount {
        val account = findById(accountId)

        if (account.status != AccountStatus.ACTIVE) {
            throw BusinessRuleViolationException(
                "ACCOUNT_NOT_ACTIVE",
                "Cannot use points on inactive account"
            )
        }

        if (account.balance < command.points) {
            throw BusinessRuleViolationException(
                "INSUFFICIENT_BALANCE",
                "Insufficient point balance"
            )
        }

        val newBalance = account.balance - command.points
        val transaction = PointTransaction(
            account = account,
            transactionType = TransactionType.USE,
            points = -command.points,
            balanceAfter = newBalance,
            reason = command.reason,
            referenceId = command.referenceId
        )

        account.transactions.add(transaction)
        account.balance = newBalance
        account.totalUsed += command.points
        account.updatedAt = LocalDateTime.now()

        return pointAccountRepository.save(account)
    }

    /**
     * アカウントを停止
     */
    fun suspend(accountId: PointAccountId): PointAccount {
        val account = findById(accountId)
        account.status = AccountStatus.SUSPENDED
        account.updatedAt = LocalDateTime.now()
        return pointAccountRepository.save(account)
    }

    /**
     * アカウントを再開
     */
    fun activate(accountId: PointAccountId): PointAccount {
        val account = findById(accountId)
        account.status = AccountStatus.ACTIVE
        account.updatedAt = LocalDateTime.now()
        return pointAccountRepository.save(account)
    }
}

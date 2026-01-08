package nexus.point.account.dto

import java.time.LocalDate

/**
 * ポイント付与コマンド
 */
data class EarnPointsCommand(
    val points: Long,
    val reason: String? = null,
    val referenceId: String? = null,
    val expiresAt: LocalDate? = null
)

/**
 * ポイント使用コマンド
 */
data class UsePointsCommand(
    val points: Long,
    val reason: String? = null,
    val referenceId: String? = null
)

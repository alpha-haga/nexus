package nexus.core.exception

/**
 * NEXUS システム共通例外
 *
 * アプリケーション固有の例外階層
 */

/** NEXUS基底例外 */
sealed class NexusException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/** リソースが見つからない */
class ResourceNotFoundException(
    val resourceType: String,
    val resourceId: String,
    message: String = "$resourceType not found: $resourceId"
) : NexusException(message)

/** バリデーションエラー */
class ValidationException(
    val field: String,
    val reason: String,
    message: String = "Validation failed for $field: $reason"
) : NexusException(message)

/** ビジネスルール違反 */
class BusinessRuleViolationException(
    val ruleCode: String,
    message: String
) : NexusException(message)

/** 権限エラー */
class AuthorizationException(
    val action: String,
    val resource: String,
    message: String = "Not authorized to $action on $resource"
) : NexusException(message)

/** 法人横断操作禁止エラー */
class CrossCorporationMutationException(
    val sourceCorporation: String,
    val targetCorporation: String,
    message: String = "Cross-corporation mutation is not allowed: $sourceCorporation -> $targetCorporation"
) : NexusException(message)

/** 名寄せ競合エラー */
class PersonMergeConflictException(
    val personIds: List<String>,
    message: String = "Person merge conflict detected: $personIds"
) : NexusException(message)

/** 楽観的ロックエラー */
class OptimisticLockException(
    val resourceType: String,
    val resourceId: String,
    message: String = "Optimistic lock failed for $resourceType: $resourceId"
) : NexusException(message)

package nexus.core.auth

/**
 * 権限エラー（403）
 */
class AccessDeniedException(message: String) : RuntimeException(message)

/**
 * 法人利用不可エラー（503）
 */
class CompanyNotAvailableException(message: String) : RuntimeException(message)

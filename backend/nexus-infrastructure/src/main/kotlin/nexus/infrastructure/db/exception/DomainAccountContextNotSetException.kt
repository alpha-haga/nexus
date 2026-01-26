package nexus.infrastructure.db.exception

/**
 * DomainAccountContext が未設定の場合にスローされる例外
 */
class DomainAccountContextNotSetException : IllegalStateException(
    "DomainAccountContext is not set. DomainAccount must be set when using region DB."
)
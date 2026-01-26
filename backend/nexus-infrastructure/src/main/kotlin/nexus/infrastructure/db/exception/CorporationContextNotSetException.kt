package nexus.infrastructure.db.exception

/**
 * CorporationContext が未設定の場合にスローされる例外
 */
class CorporationContextNotSetException : IllegalStateException(
    "CorporationContext is not set. Corporation must be set when using region DB."
)
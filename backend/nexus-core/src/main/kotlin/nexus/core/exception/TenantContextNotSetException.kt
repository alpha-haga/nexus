package nexus.core.exception

/**
 * TenantContext が未設定の場合に投げられる例外
 */
class TenantContextNotSetException(
    message: String = "TenantContext is not set"
) : NexusException(message)

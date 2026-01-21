package nexus.core.exception

/**
 * RegionContext が未設定のまま DB アクセスに到達した場合の例外（fail fast）
 *
 * P0-3b 方針:
 * - Region 未設定は integration フォールバックせず FAIL する
 */
class RegionContextNotSetException(
    message: String = "RegionContext is not set. Region must be set before accessing database."
) : NexusException(message)
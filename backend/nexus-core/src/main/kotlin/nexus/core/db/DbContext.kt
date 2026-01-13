package nexus.core.db

/**
 * NEXUS システム DB接続基盤
 *
 * 地区DB（REGION）と統合DB（INTEGRATION）を明確に分離
 * - 地区DB: gojo, funeral, accounting など各業務ドメイン用
 * - 統合DB: nexus-group 専用（法人横断検索）
 *
 * 禁止事項:
 * - 法人単位での DataSource 作成禁止
 * - tenant_id ごとの接続切り替え禁止
 */

/**
 * データベース種別
 *
 * 接続先は以下の2種類のみ:
 * - REGION: 地区DB（regionId で識別）
 * - INTEGRATION: 統合DB（nexus-group 専用、regionId 不要）
 */
enum class DatabaseType {
    /** 地区DB - gojo, funeral, accounting 等の業務ドメイン用 */
    REGION,

    /** 統合DB - nexus-group 専用（法人横断検索用） */
    INTEGRATION
}

/**
 * DB接続コンテキスト
 *
 * ドメイン層から DB 接続を要求する際に使用
 * - REGION の場合: regionId 必須
 * - INTEGRATION の場合: regionId は null
 *
 * @property databaseType 接続先DB種別
 * @property regionId 地区ID（REGION の場合は必須）
 */
data class DbContext(
    val databaseType: DatabaseType,
    val regionId: String? = null
) {
    init {
        when (databaseType) {
            DatabaseType.REGION -> {
                requireNotNull(regionId) {
                    "regionId is required for REGION database type"
                }
                require(regionId.isNotBlank()) {
                    "regionId must not be blank for REGION database type"
                }
            }
            DatabaseType.INTEGRATION -> {
                require(regionId == null) {
                    "regionId must be null for INTEGRATION database type"
                }
            }
        }
    }

    companion object {
        /**
         * 地区DB用のコンテキストを生成
         *
         * @param regionId 地区ID（必須）
         * @return 地区DB用のDbContext
         */
        fun forRegion(regionId: String): DbContext =
            DbContext(DatabaseType.REGION, regionId)

        /**
         * 統合DB用のコンテキストを生成
         *
         * @return 統合DB用のDbContext
         */
        fun forIntegration(): DbContext =
            DbContext(DatabaseType.INTEGRATION)
    }
}

/**
 * 地区ID
 *
 * 地区DBへの接続時に使用する識別子
 * 将来的に Keycloak の region_ids から取得予定
 */
@JvmInline
value class RegionId(val value: String) {
    init {
        require(value.isNotBlank()) { "RegionId must not be blank" }
    }
}

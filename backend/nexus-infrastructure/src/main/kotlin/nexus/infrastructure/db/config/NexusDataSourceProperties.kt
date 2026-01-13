package nexus.infrastructure.db.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * NEXUS DB接続設定プロパティ
 *
 * application.yml から読み込む設定:
 * ```yaml
 * nexus:
 *   datasource:
 *     integration:
 *       url: jdbc:oracle:thin:@//host:1521/service
 *       username: nexus_integration
 *       password: password
 *       hikari:
 *         maximum-pool-size: 10
 *     regions:
 *       tokyo:
 *         url: jdbc:oracle:thin:@//host:1521/tokyo
 *         username: nexus_tokyo
 *         password: password
 *       osaka:
 *         url: jdbc:oracle:thin:@//host:1521/osaka
 *         username: nexus_osaka
 *         password: password
 * ```
 */
@ConfigurationProperties(prefix = "nexus.datasource")
data class NexusDataSourceProperties(
    /**
     * 統合DB（integration）設定
     * nexus-group 専用
     */
    val integration: DataSourceConfig = DataSourceConfig(),

    /**
     * 地区DB設定（Map<regionId, DataSourceConfig>）
     * 例: tokyo, osaka, fukuoka
     */
    val regions: Map<String, DataSourceConfig> = emptyMap()
)

/**
 * 個別 DataSource 設定
 */
data class DataSourceConfig(
    /** JDBC URL */
    val url: String = "",

    /** データベースユーザー名 */
    val username: String = "",

    /** データベースパスワード */
    val password: String = "",

    /** JDBC ドライバークラス名（省略時は URL から自動判定） */
    val driverClassName: String? = null,

    /** HikariCP 設定 */
    val hikari: HikariConfig = HikariConfig()
)

/**
 * HikariCP 接続プール設定
 */
data class HikariConfig(
    /** 最大プールサイズ */
    val maximumPoolSize: Int = 10,

    /** 最小アイドル接続数 */
    val minimumIdle: Int = 2,

    /** 接続タイムアウト（ミリ秒） */
    val connectionTimeout: Long = 30000,

    /** アイドルタイムアウト（ミリ秒） */
    val idleTimeout: Long = 600000,

    /** 最大ライフタイム（ミリ秒） */
    val maxLifetime: Long = 1800000,

    /** プール名（デバッグ用） */
    val poolName: String? = null
)

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
    val integration: DataSourceProps,

    /**
     * 地区DB設定（Map<regionId, DataSourceProps>）
     * 例: tokyo, osaka, fukuoka
     */
    val regions: Map<String, DataSourceProps> = emptyMap()
)

/**
 * 個別 DataSource 設定プロパティ
 */
data class DataSourceProps(
    /** JDBC URL */
    val url: String = "",

    /** JDBC ドライバークラス名 */
    val driverClassName: String,

    /** データベースユーザー名 */
    val username: String = "",

    /** データベースパスワード（空/未指定許容） */
    val password: String? = null,
     
    /** HikariCP 設定（任意） */
    val hikari: HikariProps? = null
)

/**
 * HikariCP 接続プール設定プロパティ
 */
data class HikariProps(
    /** 最大プールサイズ */
    val maximumPoolSize: Int? = null,

    /** 最小アイドル接続数 */
    val minimumIdle: Int? = null,

    /** 接続タイムアウト（ミリ秒） */
    val connectionTimeout: Long? = null,

    /** プール名（デバッグ用） */
    val poolName: String? = null
)

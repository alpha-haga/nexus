package nexus.infrastructure.db.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nexus.core.region.Region
import nexus.infrastructure.db.RoutingDataSource
import nexus.infrastructure.db.CorporationDomainAccountRoutingDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.beans.factory.DisposableBean
import javax.sql.DataSource

/**
 * NEXUS DataSource 構成
 *
 * application.yml の設定から DataSource を構築し、Bean として登録
 *
 * 構成:
 * - saitamaDataSource: 埼玉地区DB
 * - fukushimaDataSource: 福島地区DB
 * - tochigiDataSource: 栃木地区DB
 * - integrationDataSource: 統合DB（nexus-group 専用）
 * - routingDataSource: ルーティング DataSource（上記を束ねる、@Primary）
 *
 * 重要:
 * - P04-4: 法人単位での DataSource 作成は、安全策（列挙制限＋遅延生成＋キャッシュ）により許可
 * - 列挙された法人のみ使用可能（未列挙は FAIL FAST）
 * - DataSource は遅延生成（初回アクセス時）＋キャッシュ（ConcurrentHashMap）
 * - アプリ終了時に生成済み DataSource を close
 * - JPA/JDBC は routingDataSource を使用する
 *
 * P0-3b 方針:
 * - Region 未設定時は integration へフォールバックせず FAIL（事故防止）
 */
@Configuration
@EnableConfigurationProperties(NexusDataSourceProperties::class)
class DataSourceConfiguration(
    private val properties: NexusDataSourceProperties
) : DisposableBean {
    private val logger = LoggerFactory.getLogger(DataSourceConfiguration::class.java)
    private val regionRoutingDataSources = mutableListOf<CorporationDomainAccountRoutingDataSource>()

    /**
     * 埼玉地区DB DataSource
     * P04-4: CorporationDomainAccountRoutingDataSource に置き換え
     */
    @Bean
    fun saitamaDataSource(environment: Environment): DataSource {
        logger.info("Creating saitama DataSource")
        val regionConfig = properties.regionConfigs["saitama"]
            ?: throw IllegalStateException("saitama region configuration not found")
        val routingDataSource = CorporationDomainAccountRoutingDataSource(
            region = Region.SAITAMA,
            regionConfig = regionConfig,
            environment = environment
        )
        regionRoutingDataSources.add(routingDataSource)
        return routingDataSource
    }

    /**
     * 福島地区DB DataSource
     * P04-4: CorporationDomainAccountRoutingDataSource に置き換え
     */
    @Bean
    fun fukushimaDataSource(environment: Environment): DataSource {
        logger.info("Creating fukushima DataSource")
        val regionConfig = properties.regionConfigs["fukushima"]
            ?: throw IllegalStateException("fukushima region configuration not found")
        val routingDataSource = CorporationDomainAccountRoutingDataSource(
            region = Region.FUKUSHIMA,
            regionConfig = regionConfig,
            environment = environment
        )
        regionRoutingDataSources.add(routingDataSource)
        return routingDataSource
    }

    /**
     * 栃木地区DB DataSource
     * P04-4: CorporationDomainAccountRoutingDataSource に置き換え
     */
    @Bean
    fun tochigiDataSource(environment: Environment): DataSource {
        logger.info("Creating tochigi DataSource")
        val regionConfig = properties.regionConfigs["tochigi"]
            ?: throw IllegalStateException("tochigi region configuration not found")
        val routingDataSource = CorporationDomainAccountRoutingDataSource(
            region = Region.TOCHIGI,
            regionConfig = regionConfig,
            environment = environment
        )
        regionRoutingDataSources.add(routingDataSource)
        return routingDataSource
    }

    /**
     * 統合DB DataSource
     *
     * nexus-group 専用の法人横断検索用DB
     */
    @Bean
    fun integrationDataSource(): DataSource {
        logger.info("Creating integration DataSource")
        return createDataSource(
            config = properties.integration,
            poolName = "nexus-integration"
        )
    }

    /**
     * アプリ終了時に生成済み DataSource を close
     */
    override fun destroy() {
        logger.info("Closing ${regionRoutingDataSources.size} region routing DataSources")
        regionRoutingDataSources.forEach { routingDataSource ->
            try {
                routingDataSource.close()
            } catch (e: Exception) {
                logger.warn("Failed to close region routing DataSource", e)
            }
        }
        regionRoutingDataSources.clear()
    }
    
    /**
     * ルーティング DataSource（@Primary）
     *
     * RegionContext の値に応じて DataSource を切り替える
     * JPA/JDBC はこの DataSource を使用する
     */
    @Bean
    @Primary
    fun routingDataSource(
        environment: Environment,
        saitamaDataSource: DataSource,
        fukushimaDataSource: DataSource,
        tochigiDataSource: DataSource,
        integrationDataSource: DataSource
    ): DataSource {
        logger.info("Creating routing DataSource")
        val allowFallbackToIntegrationWhenUnset =
            environment.acceptsProfiles(Profiles.of("local", "test"))

        val routingDataSource = RoutingDataSource(
            allowFallbackToIntegrationWhenUnset = allowFallbackToIntegrationWhenUnset
        )
        val targetDataSources = mapOf<Any, Any>(
            Region.SAITAMA to saitamaDataSource,
            Region.FUKUSHIMA to fukushimaDataSource,
            Region.TOCHIGI to tochigiDataSource,
            Region.INTEGRATION to integrationDataSource
        )
        routingDataSource.setTargetDataSources(targetDataSources)
        // P0-3b: Region 未設定時の “意図しない integration 参照” を防ぐため、
        // defaultTargetDataSource は設定しない（RegionContext.get() 側で fail fast する）
        routingDataSource.afterPropertiesSet()
        logger.info("Routing DataSource configured with {} target DataSources", targetDataSources.size)
        return routingDataSource
    }

    /**
     * 地区DB DataSource Map（後方互換性のため残す）
     *
     * @deprecated 個別の DataSource Bean を使用すること
     * P04-4: region は遅延生成のため、起動時に HikariDataSource を初期化しない
     */
    @Bean
    @Deprecated("Use individual DataSource beans instead")
    fun regionDataSources(): Map<String, DataSource> {
        // P04-4: region は遅延生成（CorporationDomainAccountRoutingDataSource）のため、
        // 起動時に HikariDataSource を初期化しない（接続試行を避ける）
        // 個別の DataSource Bean（saitamaDataSource / fukushimaDataSource / tochigiDataSource）を使用すること
        logger.warn("regionDataSources() is deprecated and returns empty map. Use individual DataSource beans instead.")
        return emptyMap()
    }

    /**
     * HikariDataSource を構築
     */
    private fun createDataSource(config: DataSourceProps, poolName: String): DataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.username
            password = config.password ?: ""

            // ドライバークラス名
            driverClassName = config.driverClassName

            this.poolName = poolName

            // HikariCP 設定（nullable プロパティの扱い）
            config.hikari?.let { hikari ->
                hikari.maximumPoolSize?.let { maximumPoolSize = it }
                hikari.minimumIdle?.let { minimumIdle = it }
                hikari.connectionTimeout?.let { connectionTimeout = it }
                hikari.poolName?.let { this.poolName = it }
            }

            // 接続検証（Oracle Base Database 前提）
            connectionTestQuery =
                if (config.driverClassName.contains("h2", ignoreCase = true)) "SELECT 1"
                else "SELECT 1 FROM DUAL"
        }

        return HikariDataSource(hikariConfig)
    }
}

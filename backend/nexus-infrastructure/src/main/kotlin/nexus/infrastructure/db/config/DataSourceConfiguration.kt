package nexus.infrastructure.db.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import nexus.core.region.Region
import nexus.infrastructure.db.RoutingDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
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
 * - 法人単位での DataSource 作成は禁止
 * - 同一地区内の全法人は同じ DataSource を共有
 * - JPA/JDBC は routingDataSource を使用する
 *
 * P0-3b 方針:
 * - Region 未設定時は integration へフォールバックせず FAIL（事故防止）
 */
@Configuration
@EnableConfigurationProperties(NexusDataSourceProperties::class)
class DataSourceConfiguration(
    private val properties: NexusDataSourceProperties
) {
    private val logger = LoggerFactory.getLogger(DataSourceConfiguration::class.java)

    /**
     * 埼玉地区DB DataSource
     */
    @Bean
    fun saitamaDataSource(): DataSource {
        logger.info("Creating saitama DataSource")
        val config = properties.regions["saitama"]
            ?: throw IllegalStateException("saitama region configuration not found")
        return createDataSource(
            config = config,
            poolName = "nexus-region-saitama"
        )
    }

    /**
     * 福島地区DB DataSource
     */
    @Bean
    fun fukushimaDataSource(): DataSource {
        logger.info("Creating fukushima DataSource")
        val config = properties.regions["fukushima"]
            ?: throw IllegalStateException("fukushima region configuration not found")
        return createDataSource(
            config = config,
            poolName = "nexus-region-fukushima"
        )
    }

    /**
     * 栃木地区DB DataSource
     */
    @Bean
    fun tochigiDataSource(): DataSource {
        logger.info("Creating tochigi DataSource")
        val config = properties.regions["tochigi"]
            ?: throw IllegalStateException("tochigi region configuration not found")
        return createDataSource(
            config = config,
            poolName = "nexus-region-tochigi"
        )
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
     */
    @Bean
    @Deprecated("Use individual DataSource beans instead")
    fun regionDataSources(): Map<String, DataSource> {
        logger.info("Creating region DataSources: {}", properties.regions.keys)

        return properties.regions.mapValues { (regionId, config) ->
            createDataSource(
                config = config,
                poolName = "nexus-region-$regionId"
            )
        }.also { dataSources ->
            logger.info("Created {} region DataSources: {}", dataSources.size, dataSources.keys)
        }
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

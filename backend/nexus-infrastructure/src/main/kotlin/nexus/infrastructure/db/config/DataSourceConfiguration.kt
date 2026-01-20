package nexus.infrastructure.db.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

/**
 * NEXUS DataSource 構成
 *
 * application.yml の設定から DataSource を構築し、Bean として登録
 *
 * 構成:
 * - integrationDataSource: 統合DB（nexus-group 専用）
 * - regionDataSources: 地区DB Map<regionId, DataSource>
 *
 * 重要:
 * - 法人単位での DataSource 作成は禁止
 * - 同一地区内の全法人は同じ DataSource を共有
 */
@Configuration
@EnableConfigurationProperties(NexusDataSourceProperties::class)
class DataSourceConfiguration(
    private val properties: NexusDataSourceProperties
) {
    private val logger = LoggerFactory.getLogger(DataSourceConfiguration::class.java)

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
     * 地区DB DataSource Map
     *
     * regionId -> DataSource のマッピング
     */
    @Bean
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

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
    private fun createDataSource(config: DataSourceConfig, poolName: String): DataSource {
        val hikariConfig = HikariConfig().apply {
            jdbcUrl = config.url
            username = config.username
            password = config.password

            // ドライバークラス名（省略時は URL から自動判定）
            config.driverClassName?.let { driverClassName = it }

            // HikariCP 設定
            maximumPoolSize = config.hikari.maximumPoolSize
            minimumIdle = config.hikari.minimumIdle
            connectionTimeout = config.hikari.connectionTimeout
            idleTimeout = config.hikari.idleTimeout
            maxLifetime = config.hikari.maxLifetime
            this.poolName = config.hikari.poolName ?: poolName

            // 接続検証
            // Oracle: "SELECT 1 FROM DUAL", H2/PostgreSQL: "SELECT 1"
            // H2 は DUAL テーブルもサポートしているが、よりポータブルな形式を使用
            connectionTestQuery = "SELECT 1"
        }

        return HikariDataSource(hikariConfig)
    }
}

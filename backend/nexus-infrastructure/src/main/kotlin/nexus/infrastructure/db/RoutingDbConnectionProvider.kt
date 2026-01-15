package nexus.infrastructure.db

import nexus.core.db.DatabaseType
import nexus.core.db.DbContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.sql.Connection
import javax.sql.DataSource

/**
 * ルーティング DB 接続プロバイダー
 *
 * DbContext に基づいて適切な DataSource を選択し、Connection を提供
 *
 * DataSource 構成:
 * - 地区DB: Map<regionId, DataSource> で管理（例: saitama, fukushima, tochigi）
 * - 統合DB: 単一の DataSource（nexus-group 専用）
 *
 * 重要な設計制約:
 * - 法人単位での DataSource 作成は禁止
 * - tenant_id ごとの接続切り替えは禁止
 * - 同一 regionId 内の全法人は同じ DataSource を共有
 */
@Component
class RoutingDbConnectionProvider(
    private val regionDataSources: Map<String, DataSource>,
    private val integrationDataSource: DataSource
) : DbConnectionProvider {

    private val logger = LoggerFactory.getLogger(RoutingDbConnectionProvider::class.java)

    override fun getConnection(context: DbContext): Connection {
        return when (context.databaseType) {
            DatabaseType.REGION -> getRegionConnectionInternal(context.regionId!!)
            DatabaseType.INTEGRATION -> getIntegrationConnectionInternal()
        }
    }

    private fun getRegionConnectionInternal(regionId: String): Connection {
        val dataSource = regionDataSources[regionId]
            ?: throw UnknownRegionException(regionId)

        return try {
            logger.debug("Getting connection for region: {}", regionId)
            dataSource.connection
        } catch (e: Exception) {
            logger.error("Failed to get connection for region: {}", regionId, e)
            throw DbConnectionException("Failed to connect to region database: $regionId", e)
        }
    }

    private fun getIntegrationConnectionInternal(): Connection {
        return try {
            logger.debug("Getting connection for integration database")
            integrationDataSource.connection
        } catch (e: Exception) {
            logger.error("Failed to get connection for integration database", e)
            throw DbConnectionException("Failed to connect to integration database", e)
        }
    }

    /**
     * 登録されている地区ID一覧を取得
     *
     * デバッグ・監視用途
     */
    fun getAvailableRegionIds(): Set<String> = regionDataSources.keys

    /**
     * 指定された地区IDが登録されているか確認
     *
     * @param regionId 地区ID
     * @return 登録されていれば true
     */
    fun isRegionAvailable(regionId: String): Boolean = regionDataSources.containsKey(regionId)
}

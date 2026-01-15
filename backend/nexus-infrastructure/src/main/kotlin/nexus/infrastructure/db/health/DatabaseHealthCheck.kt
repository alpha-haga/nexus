package nexus.infrastructure.db.health

import nexus.core.db.DbContext
import nexus.infrastructure.db.DbConnectionProvider
import org.springframework.stereotype.Component

/**
 * データベース接続ヘルスチェック
 *
 * リポジトリとは分離された、インフラストラクチャ層の責務
 */
@Component
class DatabaseHealthCheck(
    private val dbConnectionProvider: DbConnectionProvider
) {

    /**
     * 地区DB への接続テスト
     *
     * @param regionId 地区ID
     * @return 接続成功なら true
     */
    fun testRegion(regionId: String): Boolean {
        val context = DbContext.forRegion(regionId)

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                conn.prepareStatement("SELECT 1 FROM DUAL").use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("DB health check failed: regionId={}", regionId, e)
            false
        }
    }

    /**
     * 統合DB への接続テスト
     */
    fun testIntegration(): Boolean {
        val context = DbContext.forIntegration()

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                conn.prepareStatement("SELECT 1 FROM DUAL").use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn("DB health check failed: regionId={}", regionId, e)
            false
        }
    }
}
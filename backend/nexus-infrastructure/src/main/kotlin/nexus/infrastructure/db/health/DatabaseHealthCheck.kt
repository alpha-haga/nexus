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
    fun testRegionConnection(regionId: String): Boolean {
        val context = DbContext.forRegion(regionId)

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                conn.prepareStatement("SELECT 1").use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 統合DB への接続テスト
     */
    fun testIntegrationConnection(): Boolean {
        val context = DbContext.forIntegration()

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                conn.prepareStatement("SELECT 1").use { ps ->
                    ps.executeQuery().use { rs ->
                        rs.next()
                    }
                }
            }
        } catch (e: Exception) {
            false
        }
    }
}
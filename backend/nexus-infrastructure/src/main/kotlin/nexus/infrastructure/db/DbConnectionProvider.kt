package nexus.infrastructure.db

import nexus.core.db.DbContext
import java.sql.Connection

/**
 * DB接続プロバイダー
 *
 * ドメイン層から DB 接続を取得するための抽象インターフェース
 * 実装クラスは DataSource の選択・ルーティングを担当
 *
 * 使用方法:
 * ```kotlin
 * @Service
 * class SomeRepository(
 *     private val dbConnectionProvider: DbConnectionProvider
 * ) {
 *     fun findSomething(context: DbContext): Something {
 *         dbConnectionProvider.getConnection(context).use { conn ->
 *             // SQL実行
 *         }
 *     }
 * }
 * ```
 *
 * 重要:
 * - 取得した Connection は必ず close すること（use {} 推奨）
 * - ドメイン層は DataSource / JDBC に直接依存してはならない
 */
interface DbConnectionProvider {

    /**
     * 指定されたコンテキストに対応する DB 接続を取得
     *
     * @param context DB接続コンテキスト（DatabaseType + regionId）
     * @return JDBC Connection
     * @throws DbConnectionException 接続に失敗した場合
     */
    fun getConnection(context: DbContext): Connection

    /**
     * 統合DB（integration）への接続を取得
     *
     * nexus-group 専用のショートカットメソッド
     *
     * @return 統合DB への JDBC Connection
     * @throws DbConnectionException 接続に失敗した場合
     */
    fun getIntegrationConnection(): Connection =
        getConnection(DbContext.forIntegration())

    /**
     * 地区DB への接続を取得
     *
     * @param regionId 地区ID
     * @return 地区DB への JDBC Connection
     * @throws DbConnectionException 接続に失敗した場合
     */
    fun getRegionConnection(regionId: String): Connection =
        getConnection(DbContext.forRegion(regionId))
}

/**
 * DB接続例外
 *
 * DB 接続に失敗した場合にスローされる
 */
class DbConnectionException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 未登録の地区ID例外
 *
 * 指定された regionId に対応する DataSource が存在しない場合にスローされる
 */
class UnknownRegionException(
    regionId: String
) : DbConnectionException("Unknown regionId: $regionId. No DataSource configured for this region.")

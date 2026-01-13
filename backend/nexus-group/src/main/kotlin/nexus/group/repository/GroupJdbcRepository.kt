package nexus.group.repository

import nexus.core.db.DbContext
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.infrastructure.db.DbConnectionProvider
import org.springframework.stereotype.Repository

/**
 * 法人横断検索リポジトリ（JDBC実装）
 *
 * 統合DB（integration）のみを使用
 * - DbContext.forIntegration() で接続を取得
 * - 地区DBへの接続は禁止
 *
 * 重要:
 * - Read Only - 更新操作は提供しない
 * - 法人横断の参照専用DB
 */
@Repository
class GroupJdbcRepository(
    private val dbConnectionProvider: DbConnectionProvider
) {

    /**
     * 法人横断での人物検索
     *
     * 統合DB（integration）から検索
     *
     * @param criteria 検索条件
     * @return マッチした人物リスト
     */
    fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult> {
        // 統合DB への接続を取得
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            // 注意: このリポジトリは Read Only
            //
            // 実装予定:
            // val sql = """
            //     SELECT
            //         p.person_id,
            //         p.corporation_id,
            //         p.last_name,
            //         p.first_name,
            //         p.last_name_kana,
            //         p.first_name_kana
            //     FROM integration_persons p
            //     WHERE 1=1
            //         AND (:name IS NULL OR p.last_name LIKE :name OR p.first_name LIKE :name)
            //         AND (:corporationId IS NULL OR p.corporation_id = :corporationId)
            //     ORDER BY p.last_name_kana, p.first_name_kana
            //     FETCH FIRST :limit ROWS ONLY
            // """
            //
            // val ps = conn.prepareStatement(sql)
            // ...
            emptyList()
        }
    }

    /**
     * 名寄せ候補の検索
     *
     * 同一人物の可能性がある候補を検出
     *
     * @param personId 対象人物ID
     * @return 名寄せ候補リスト（スコア付き）
     */
    fun findMergeCandidates(personId: PersonId): List<GroupSearchResult> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 名寄せアルゴリズムを実装
            // 候補検出ロジック:
            // 1. 同一電話番号
            // 2. 同一住所 + 類似氏名
            // 3. カナ読み一致
            //
            // 注意: Read Only - 名寄せの実行は nexus-identity の責務
            emptyList()
        }
    }

    /**
     * 統合DB への接続テスト
     *
     * ヘルスチェック用
     */
    fun testConnection(): Boolean {
        val context = DbContext.forIntegration()

        return try {
            dbConnectionProvider.getConnection(context).use { conn ->
                // H2/PostgreSQL: "SELECT 1", Oracle: "SELECT 1 FROM DUAL"
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

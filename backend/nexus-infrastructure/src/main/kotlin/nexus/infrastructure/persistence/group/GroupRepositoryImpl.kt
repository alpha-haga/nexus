package nexus.infrastructure.persistence.group

import nexus.core.db.DbContext
import nexus.core.id.PersonId
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.group.repository.GroupRepository
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
class GroupRepositoryImpl(
    private val dbConnectionProvider: DbConnectionProvider
) : GroupRepository {

    override fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 実際のSQL検索を実装
            emptyList()
        }
    }

    override fun findMergeCandidates(personId: PersonId): List<GroupSearchResult> {
        val context = DbContext.forIntegration()

        return dbConnectionProvider.getConnection(context).use { conn ->
            // TODO: 名寄せアルゴリズムを実装
            emptyList()
        }
    }
}
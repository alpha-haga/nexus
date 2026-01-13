package nexus.group.repository

import nexus.core.id.PersonId
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult

/**
 * 法人横断検索リポジトリ（インターフェース）
 *
 * 実装は infrastructure 層に配置（GroupRepositoryImpl）
 * domain 層は JDBC / SQL を知らない
 *
 * 重要:
 * - Read Only - 更新操作は提供しない
 * - 法人横断の参照専用DB
 */
interface GroupRepository {

    /**
     * 法人横断での人物検索
     */
    fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult>

    /**
     * 名寄せ候補の検索
     */
    fun findMergeCandidates(personId: PersonId): List<GroupSearchResult>
}
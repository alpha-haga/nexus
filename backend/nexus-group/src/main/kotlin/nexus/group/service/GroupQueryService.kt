package nexus.group.service

import nexus.core.id.PersonId
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.group.query.PersonGroupSummary
import org.springframework.stereotype.Service

/**
 * 法人横断検索サービス
 *
 * Read Only - 更新操作は提供しない
 */
@Service
class GroupQueryService {

    /**
     * 法人横断での人物検索
     *
     * @param criteria 検索条件
     * @return マッチした人物リスト（名寄せスコア付き）
     */
    fun searchPersons(criteria: GroupSearchCriteria): List<GroupSearchResult> {
        // TODO: 実際のDB検索ロジックを実装
        // 全法人のデータソースから検索を行う
        return emptyList()
    }

    /**
     * 特定人物の法人横断サマリー取得
     *
     * @param personId 人物ID
     * @return 全法人にまたがるサマリー情報
     */
    fun getPersonSummary(personId: PersonId): PersonGroupSummary? {
        // TODO: 各法人のデータを集約してサマリーを返す
        return null
    }

    /**
     * 名寄せ候補の検出
     *
     * @param personId 対象人物ID
     * @return 同一人物の可能性がある候補リスト
     */
    fun findMergeCandidates(personId: PersonId): List<GroupSearchResult> {
        // TODO: 名寄せアルゴリズムによる候補検出
        return emptyList()
    }
}

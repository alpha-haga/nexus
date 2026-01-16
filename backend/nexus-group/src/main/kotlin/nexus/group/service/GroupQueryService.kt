package nexus.group.service

import nexus.core.exception.ValidationException
import nexus.core.pagination.PaginatedResult
import nexus.core.id.PersonId
import nexus.group.query.GroupContractDto
import nexus.group.query.GroupSearchCriteria
import nexus.group.query.GroupSearchResult
import nexus.group.query.PersonGroupSummary
import nexus.group.repository.GroupRepository
import org.springframework.stereotype.Service

/**
 * 法人横断検索サービス
 *
 * Read Only - 更新操作は提供しない
 */
@Service
class GroupQueryService(
    private val groupRepository: GroupRepository
) {

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
     * 法人横断契約一覧を取得（統合DB）
     *
     * @param corporationId 法人ID（null の場合は全法人）
     * @param page ページ番号（0始まり、必須）
     * @param size ページサイズ（20, 50, 100のみ、必須）
     * @return ページネーション結果
     */
    fun listContracts(corporationId: String?, page: Int, size: Int): PaginatedResult<GroupContractDto> {
        // size バリデーション（20, 50, 100のみ）
        if (size !in listOf(20, 50, 100)) {
            throw ValidationException("size", "size must be one of: 20, 50, 100")
        }

        // page バリデーション
        if (page < 0) {
            throw ValidationException("page", "page must be >= 0")
        }

        // corporationId は optional（null許可、追加バリデーションなし）
        // 要件: "No additional guards/required filters (ALL is allowed)"

        return groupRepository.findContracts(corporationId, page, size)
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

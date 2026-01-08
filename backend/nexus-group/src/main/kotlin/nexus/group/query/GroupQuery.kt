package nexus.group.query

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.core.vo.PersonName

/**
 * 法人横断検索クエリ
 *
 * 全法人のデータを横断的に検索するためのRead Onlyインターフェース
 * このモジュールからデータの更新は一切行わない
 */

/** 法人横断検索条件 */
data class GroupSearchCriteria(
    val name: String? = null,
    val nameKana: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val corporationIds: List<CorporationId>? = null, // null = 全法人
    val limit: Int = 100,
    val offset: Int = 0
)

/** 法人横断検索結果 */
data class GroupSearchResult(
    val personId: PersonId,
    val corporationId: CorporationId,
    val name: PersonName,
    val matchScore: Double // 名寄せスコア
)

/** 法人横断サマリー */
data class PersonGroupSummary(
    val personId: PersonId,
    val registeredCorporations: List<CorporationId>,
    val hasGojoContract: Boolean,
    val hasFuneralHistory: Boolean,
    val hasBridalHistory: Boolean,
    val totalPoints: Long
)

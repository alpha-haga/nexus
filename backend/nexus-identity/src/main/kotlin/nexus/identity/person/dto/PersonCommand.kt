package nexus.identity.person.dto

import nexus.core.id.CorporationId
import nexus.identity.person.entity.Gender
import nexus.identity.person.entity.Person
import java.time.LocalDate

/**
 * 人物登録コマンド
 */
data class RegisterPersonCommand(
    val corporationId: CorporationId,
    val lastName: String,
    val firstName: String,
    val lastNameKana: String? = null,
    val firstNameKana: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

/**
 * 人物更新コマンド
 */
data class UpdatePersonCommand(
    val lastName: String? = null,
    val firstName: String? = null,
    val lastNameKana: String? = null,
    val firstNameKana: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

/**
 * 名寄せ候補
 */
data class MergeCandidate(
    val person: Person,
    val matchType: MatchType,
    val score: Double
)

/**
 * マッチ種別
 */
enum class MatchType {
    PHONE_NUMBER,
    EMAIL,
    NAME_KANA,
    ADDRESS,
    MULTIPLE
}

package nexus.identity.person.entity

import jakarta.persistence.*
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 人物エンティティ
 *
 * NEXUS システムの中核となる人物マスタ
 * 法人横断で一意のIDを持つ
 */
@Entity
@Table(name = "persons")
class Person(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    @Column(name = "last_name", nullable = false)
    var lastName: String,

    @Column(name = "first_name", nullable = false)
    var firstName: String,

    @Column(name = "last_name_kana")
    var lastNameKana: String? = null,

    @Column(name = "first_name_kana")
    var firstNameKana: String? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @Column(name = "gender")
    @Enumerated(EnumType.STRING)
    var gender: Gender? = null,

    @Column(name = "phone_number")
    var phoneNumber: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "postal_code")
    var postalCode: String? = null,

    @Column(name = "prefecture")
    var prefecture: String? = null,

    @Column(name = "city")
    var city: String? = null,

    @Column(name = "street")
    var street: String? = null,

    @Column(name = "building")
    var building: String? = null,

    @Column(name = "merged_into_id")
    var mergedIntoId: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    fun toPersonId(): PersonId = PersonId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)

    val fullName: String get() = "$lastName $firstName"
    val fullNameKana: String? get() =
        if (lastNameKana != null && firstNameKana != null) "$lastNameKana $firstNameKana" else null

    val isMerged: Boolean get() = mergedIntoId != null

    companion object {
        fun create(
            personId: PersonId,
            corporationId: CorporationId,
            lastName: String,
            firstName: String
        ): Person {
            return Person(
                id = personId.value,
                corporationId = corporationId.value,
                lastName = lastName,
                firstName = firstName
            )
        }
    }
}

enum class Gender {
    MALE, FEMALE, OTHER
}

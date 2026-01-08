package nexus.household.member.entity

import jakarta.persistence.*
import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import java.time.LocalDateTime

/**
 * 世帯エンティティ
 */
@Entity
@Table(name = "households")
class Household(
    @Id
    @Column(name = "id")
    val id: String,

    @Column(name = "corporation_id", nullable = false)
    val corporationId: String,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "head_person_id")
    var headPersonId: String? = null,

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

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    @Column(name = "version")
    var version: Long = 0
) {
    @OneToMany(mappedBy = "household", cascade = [CascadeType.ALL], orphanRemoval = true)
    val members: MutableList<HouseholdMember> = mutableListOf()

    fun toHouseholdId(): HouseholdId = HouseholdId(id)
    fun toCorporationId(): CorporationId = CorporationId(corporationId)
    fun headPersonId(): PersonId? = headPersonId?.let { PersonId(it) }

    companion object {
        fun create(
            householdId: HouseholdId,
            corporationId: CorporationId,
            name: String? = null
        ): Household {
            return Household(
                id = householdId.value,
                corporationId = corporationId.value,
                name = name
            )
        }
    }
}

/**
 * 世帯構成員
 */
@Entity
@Table(name = "household_members")
class HouseholdMember(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    val household: Household,

    @Column(name = "person_id", nullable = false)
    val personId: String,

    @Column(name = "relationship", nullable = false)
    @Enumerated(EnumType.STRING)
    var relationship: Relationship,

    @Column(name = "joined_at", nullable = false)
    val joinedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "left_at")
    var leftAt: LocalDateTime? = null
) {
    fun toPersonId(): PersonId = PersonId(personId)

    val isActive: Boolean get() = leftAt == null
}

/**
 * 続柄
 */
enum class Relationship {
    HEAD,           // 世帯主
    SPOUSE,         // 配偶者
    CHILD,          // 子
    PARENT,         // 親
    GRANDPARENT,    // 祖父母
    GRANDCHILD,     // 孫
    SIBLING,        // 兄弟姉妹
    OTHER_RELATIVE, // その他親族
    OTHER           // その他
}

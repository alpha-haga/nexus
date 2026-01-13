package nexus.household.member.service

import nexus.core.exception.BusinessRuleViolationException
import nexus.core.exception.ResourceNotFoundException
import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import nexus.household.member.dto.CreateHouseholdCommand
import nexus.household.member.entity.Household
import nexus.household.member.entity.HouseholdMember
import nexus.household.member.entity.Relationship
import nexus.household.member.repository.HouseholdMemberRepository
import nexus.household.member.repository.HouseholdRepository
import nexus.identity.person.service.PersonService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 世帯管理サービス
 */
@Service
@Transactional
class HouseholdService(
    private val householdRepository: HouseholdRepository,
    private val householdMemberRepository: HouseholdMemberRepository,
    private val personService: PersonService
) {

    /**
     * 世帯を作成
     */
    fun create(command: CreateHouseholdCommand): Household {
        val household = Household.create(
            householdId = HouseholdId.generate(),
            corporationId = command.corporationId,
            name = command.name
        ).apply {
            postalCode = command.postalCode
            prefecture = command.prefecture
            city = command.city
            street = command.street
            building = command.building
        }

        return householdRepository.save(household)
    }

    /**
     * 世帯を取得
     */
    @Transactional(readOnly = true)
    fun findById(householdId: HouseholdId): Household {
        return householdRepository.findById(householdId)
            ?: throw ResourceNotFoundException("Household", householdId.value)
    }

    /**
     * 人物が属する世帯を取得
     */
    @Transactional(readOnly = true)
    fun findByPerson(personId: PersonId): List<Household> {
        return householdRepository.findByMemberPersonId(personId)
    }

    /**
     * 世帯にメンバーを追加
     */
    fun addMember(
        householdId: HouseholdId,
        personId: PersonId,
        relationship: Relationship
    ): HouseholdMember {
        val household = findById(householdId)
        val person = personService.findById(personId) // 人物の存在確認

        // 既に同じ世帯のメンバーでないか確認
        val existingMember = household.members.find {
            it.personId == personId.value && it.isActive
        }
        if (existingMember != null) {
            throw BusinessRuleViolationException(
                "ALREADY_MEMBER",
                "Person is already a member of this household"
            )
        }

        val member = HouseholdMember(
            household = household,
            personId = personId.value,
            relationship = relationship
        )

        household.members.add(member)

        // 世帯主の場合は headPersonId を更新
        if (relationship == Relationship.HEAD) {
            household.headPersonId = personId.value
        }

        household.updatedAt = LocalDateTime.now()
        householdRepository.save(household)

        return member
    }

    /**
     * 世帯からメンバーを外す
     */
    fun removeMember(householdId: HouseholdId, personId: PersonId) {
        val household = findById(householdId)

        val member = household.members.find {
            it.personId == personId.value && it.isActive
        } ?: throw ResourceNotFoundException("HouseholdMember", personId.value)

        member.leftAt = LocalDateTime.now()

        // 世帯主の場合は headPersonId をクリア
        if (member.relationship == Relationship.HEAD) {
            household.headPersonId = null
        }

        household.updatedAt = LocalDateTime.now()
        householdRepository.save(household)
    }

    /**
     * 世帯主を変更
     */
    fun changeHead(householdId: HouseholdId, newHeadPersonId: PersonId) {
        val household = findById(householdId)

        // 新しい世帯主がメンバーか確認
        val newHeadMember = household.members.find {
            it.personId == newHeadPersonId.value && it.isActive
        } ?: throw BusinessRuleViolationException(
            "NOT_MEMBER",
            "New head must be a member of the household"
        )

        // 現在の世帯主の続柄を変更
        household.members.filter {
            it.relationship == Relationship.HEAD && it.isActive
        }.forEach {
            it.relationship = Relationship.OTHER
        }

        // 新しい世帯主を設定
        newHeadMember.relationship = Relationship.HEAD
        household.headPersonId = newHeadPersonId.value
        household.updatedAt = LocalDateTime.now()

        householdRepository.save(household)
    }
}

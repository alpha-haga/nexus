package nexus.household.member.repository

import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import nexus.household.member.entity.Household
import nexus.household.member.entity.HouseholdMember

/**
 * 世帯リポジトリ（interface のみ）
 *
 * 実装は infrastructure 層に配置（HouseholdRepositoryImpl）
 * domain 層は JPA / JDBC を知らない
 */
interface HouseholdRepository {

    fun save(household: Household): Household

    fun findById(householdId: HouseholdId): Household?

    fun findByCorporationId(corporationId: CorporationId): List<Household>

    fun findByHeadPersonId(personId: PersonId): Household?

    fun findByMemberPersonId(personId: PersonId): List<Household>
}

/**
 * 世帯構成員リポジトリ（interface のみ）
 *
 * 実装は infrastructure 層に配置（HouseholdMemberRepositoryImpl）
 */
interface HouseholdMemberRepository {

    fun save(member: HouseholdMember): HouseholdMember

    fun findActiveByPersonId(personId: PersonId): List<HouseholdMember>
}

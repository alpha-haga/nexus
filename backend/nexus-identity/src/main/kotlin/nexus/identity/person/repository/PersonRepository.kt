package nexus.identity.person.repository

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.identity.person.entity.Person

/**
 * 人物リポジトリ（interface のみ）
 *
 * 実装は infrastructure 層に配置（PersonRepositoryImpl）
 * domain 層は JPA / JDBC を知らない
 */
interface PersonRepository {

    fun save(person: Person): Person

    fun findById(personId: PersonId): Person?

    fun findByCorporationId(corporationId: CorporationId): List<Person>

    fun findAllActive(): List<Person>

    fun searchByName(keyword: String): List<Person>

    fun findByPhoneNumber(phoneNumber: String): List<Person>

    fun findByEmail(email: String): List<Person>
}

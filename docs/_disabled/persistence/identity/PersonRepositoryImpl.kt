/*
 * TEMP DISABLED:
 * nexus-infrastructure は DB接続基盤に限定する方針のため、業務/横断の永続化実装を退避。
 * 後で各ドメイン側（例: nexus-identity-infrastructure 等）へ移設する。
 */
package nexus.infrastructure._disabled.persistence.identity

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.identity.person.entity.Person
import nexus.identity.person.repository.PersonRepository
import nexus.infrastructure.persistence.jpa.JpaPersonRepository
import org.springframework.stereotype.Repository

/**
 * PersonRepository の実装
 *
 * domain 層の interface を実装し、JPA への橋渡しを行う
 * domain 層はこの実装クラスを知らない（DI で注入）
 */
@Repository
class PersonRepositoryImpl(
    private val jpaRepository: JpaPersonRepository
) : PersonRepository {

    override fun save(person: Person): Person {
        return jpaRepository.save(person)
    }

    override fun findById(personId: PersonId): Person? {
        return jpaRepository.findById(personId.value).orElse(null)
    }

    override fun findByCorporationId(corporationId: CorporationId): List<Person> {
        return jpaRepository.findByCorporationId(corporationId.value)
    }

    override fun findAllActive(): List<Person> {
        return jpaRepository.findByMergedIntoIdIsNull()
    }

    override fun searchByName(keyword: String): List<Person> {
        return jpaRepository.searchByName(keyword)
    }

    override fun findByPhoneNumber(phoneNumber: String): List<Person> {
        return jpaRepository.findByPhoneNumber(phoneNumber)
    }

    override fun findByEmail(email: String): List<Person> {
        return jpaRepository.findByEmail(email)
    }
}

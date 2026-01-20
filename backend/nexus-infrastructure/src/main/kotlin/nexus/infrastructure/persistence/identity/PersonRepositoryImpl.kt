package nexus.infrastructure.persistence.identity

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.identity.person.entity.Person
import nexus.identity.person.repository.PersonRepository
import nexus.infrastructure.persistence.jpa.JpaPersonRepository
import org.springframework.stereotype.Repository

/**
 * PersonRepository の JPA 実装
 *
 * 目的（P0-2）:
 * - PersonRepository の Bean を提供し、bootRun を起動できる状態にする
 *
 * TODO（P0-3 / P1-1）:
 * - 検索系メソッドは Entity の項目/検索要件確定後に Spring Data / JDBC 実装へ置き換える
 */
@Repository
class PersonRepositoryImpl(
    private val jpa: JpaPersonRepository
) : PersonRepository {

    override fun save(person: Person): Person =
        jpa.save(person)

    override fun findById(personId: PersonId): Person? =
        jpa.findById(personId.value).orElse(null)

    override fun findByCorporationId(corporationId: CorporationId): List<Person> =
        TODO("P0-3: Person Entity の corporationId 項目と検索要件確定後に実装")

    override fun findAllActive(): List<Person> =
        TODO("P0-3: active 判定項目確定後に実装")

    override fun searchByName(keyword: String): List<Person> =
        TODO("P0-3: Person の氏名項目（例: name/fullName/kana 等）確定後に実装")

    override fun findByPhoneNumber(phoneNumber: String): List<Person> =
        TODO("P0-3: phone 項目確定後に実装")

    override fun findByEmail(email: String): List<Person> =
        TODO("P0-3: email 項目確定後に実装")
}

package nexus.identity.person.repository

import nexus.identity.person.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * 人物リポジトリ
 */
@Repository
interface PersonRepository : JpaRepository<Person, String> {

    fun findByCorporationId(corporationId: String): List<Person>

    fun findByMergedIntoIdIsNull(): List<Person>

    @Query("""
        SELECT p FROM Person p
        WHERE p.mergedIntoId IS NULL
        AND (
            LOWER(p.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.firstName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.lastNameKana) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.firstNameKana) LIKE LOWER(CONCAT('%', :keyword, '%'))
        )
    """)
    fun searchByName(keyword: String): List<Person>

    @Query("""
        SELECT p FROM Person p
        WHERE p.mergedIntoId IS NULL
        AND p.phoneNumber = :phoneNumber
    """)
    fun findByPhoneNumber(phoneNumber: String): List<Person>

    @Query("""
        SELECT p FROM Person p
        WHERE p.mergedIntoId IS NULL
        AND p.email = :email
    """)
    fun findByEmail(email: String): List<Person>
}

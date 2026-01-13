package nexus.infrastructure.persistence.jpa

import nexus.identity.person.entity.Person
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

/**
 * JPA用 Person Repository
 *
 * infrastructure 層に閉じ込め、domain からは見えない
 * domain 層の PersonRepository interface の実装で使用
 */
@Repository
interface JpaPersonRepository : JpaRepository<Person, String> {

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

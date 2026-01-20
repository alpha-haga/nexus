package nexus.infrastructure.persistence.jpa

import nexus.identity.person.entity.Person
import org.springframework.data.jpa.repository.JpaRepository

/**
 * infrastructure 層に閉じ込める Spring Data JPA Repository
 *
 * NOTE:
 * - まずは「Bean を作って起動を通す」ことを優先し、メソッドは最小にする
 * - 検索系は Person Entity のフィールド確定後に追加する（P0-3〜）
 */
interface JpaPersonRepository : JpaRepository<Person, String>

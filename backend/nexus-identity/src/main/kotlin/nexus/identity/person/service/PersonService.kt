package nexus.identity.person.service

import nexus.core.exception.ResourceNotFoundException
import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.identity.person.dto.RegisterPersonCommand
import nexus.identity.person.dto.UpdatePersonCommand
import nexus.identity.person.entity.Person
import nexus.identity.person.repository.PersonRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 人物管理サービス
 *
 * 人物の登録・更新・検索を提供
 * 業務モジュールからは直接呼び出さず、API層経由でのみアクセス
 */
@Service
@Transactional
class PersonService(
    private val personRepository: PersonRepository
) {

    /**
     * 人物を登録
     */
    fun register(command: RegisterPersonCommand): Person {
        val person = Person.create(
            personId = PersonId.generate(),
            corporationId = command.corporationId,
            lastName = command.lastName,
            firstName = command.firstName
        ).apply {
            lastNameKana = command.lastNameKana
            firstNameKana = command.firstNameKana
            birthDate = command.birthDate
            gender = command.gender
            phoneNumber = command.phoneNumber
            email = command.email
            postalCode = command.postalCode
            prefecture = command.prefecture
            city = command.city
            street = command.street
            building = command.building
        }

        return personRepository.save(person)
    }

    /**
     * 人物情報を更新
     */
    fun update(personId: PersonId, command: UpdatePersonCommand): Person {
        val person = findById(personId)

        person.apply {
            lastName = command.lastName ?: lastName
            firstName = command.firstName ?: firstName
            lastNameKana = command.lastNameKana ?: lastNameKana
            firstNameKana = command.firstNameKana ?: firstNameKana
            birthDate = command.birthDate ?: birthDate
            gender = command.gender ?: gender
            phoneNumber = command.phoneNumber ?: phoneNumber
            email = command.email ?: email
            postalCode = command.postalCode ?: postalCode
            prefecture = command.prefecture ?: prefecture
            city = command.city ?: city
            street = command.street ?: street
            building = command.building ?: building
            updatedAt = LocalDateTime.now()
        }

        return personRepository.save(person)
    }

    /**
     * IDで検索
     */
    @Transactional(readOnly = true)
    fun findById(personId: PersonId): Person {
        return personRepository.findById(personId.value)
            .orElseThrow { ResourceNotFoundException("Person", personId.value) }
    }

    /**
     * 法人IDで検索
     */
    @Transactional(readOnly = true)
    fun findByCorporation(corporationId: CorporationId): List<Person> {
        return personRepository.findByCorporationId(corporationId.value)
    }

    /**
     * 名前で検索
     */
    @Transactional(readOnly = true)
    fun searchByName(keyword: String): List<Person> {
        return personRepository.searchByName(keyword)
    }
}

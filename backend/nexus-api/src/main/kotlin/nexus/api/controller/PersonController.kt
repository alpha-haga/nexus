package nexus.api.controller

import nexus.core.id.CorporationId
import nexus.core.id.PersonId
import nexus.identity.domain.Gender
import nexus.identity.domain.Person
import nexus.identity.service.PersonService
import nexus.identity.service.RegisterPersonCommand
import nexus.identity.service.UpdatePersonCommand
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

/**
 * 人物管理API
 */
@RestController
@RequestMapping("/api/v1/persons")
class PersonController(
    private val personService: PersonService
) {

    @PostMapping
    fun register(@RequestBody request: RegisterPersonRequest): ResponseEntity<PersonResponse> {
        val command = RegisterPersonCommand(
            corporationId = CorporationId(request.corporationId),
            lastName = request.lastName,
            firstName = request.firstName,
            lastNameKana = request.lastNameKana,
            firstNameKana = request.firstNameKana,
            birthDate = request.birthDate,
            gender = request.gender,
            phoneNumber = request.phoneNumber,
            email = request.email,
            postalCode = request.postalCode,
            prefecture = request.prefecture,
            city = request.city,
            street = request.street,
            building = request.building
        )

        val person = personService.register(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(person.toResponse())
    }

    @GetMapping("/{personId}")
    fun findById(@PathVariable personId: String): ResponseEntity<PersonResponse> {
        val person = personService.findById(PersonId(personId))
        return ResponseEntity.ok(person.toResponse())
    }

    @GetMapping
    fun search(@RequestParam keyword: String): ResponseEntity<List<PersonResponse>> {
        val persons = personService.searchByName(keyword)
        return ResponseEntity.ok(persons.map { it.toResponse() })
    }

    @PatchMapping("/{personId}")
    fun update(
        @PathVariable personId: String,
        @RequestBody request: UpdatePersonRequest
    ): ResponseEntity<PersonResponse> {
        val command = UpdatePersonCommand(
            lastName = request.lastName,
            firstName = request.firstName,
            lastNameKana = request.lastNameKana,
            firstNameKana = request.firstNameKana,
            birthDate = request.birthDate,
            gender = request.gender,
            phoneNumber = request.phoneNumber,
            email = request.email,
            postalCode = request.postalCode,
            prefecture = request.prefecture,
            city = request.city,
            street = request.street,
            building = request.building
        )

        val person = personService.update(PersonId(personId), command)
        return ResponseEntity.ok(person.toResponse())
    }
}

// Request/Response DTOs

data class RegisterPersonRequest(
    val corporationId: String,
    val lastName: String,
    val firstName: String,
    val lastNameKana: String? = null,
    val firstNameKana: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

data class UpdatePersonRequest(
    val lastName: String? = null,
    val firstName: String? = null,
    val lastNameKana: String? = null,
    val firstNameKana: String? = null,
    val birthDate: LocalDate? = null,
    val gender: Gender? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

data class PersonResponse(
    val id: String,
    val corporationId: String,
    val lastName: String,
    val firstName: String,
    val fullName: String,
    val lastNameKana: String?,
    val firstNameKana: String?,
    val fullNameKana: String?,
    val birthDate: LocalDate?,
    val gender: Gender?,
    val phoneNumber: String?,
    val email: String?,
    val postalCode: String?,
    val prefecture: String?,
    val city: String?,
    val street: String?,
    val building: String?
)

private fun Person.toResponse() = PersonResponse(
    id = id,
    corporationId = corporationId,
    lastName = lastName,
    firstName = firstName,
    fullName = fullName,
    lastNameKana = lastNameKana,
    firstNameKana = firstNameKana,
    fullNameKana = fullNameKana,
    birthDate = birthDate,
    gender = gender,
    phoneNumber = phoneNumber,
    email = email,
    postalCode = postalCode,
    prefecture = prefecture,
    city = city,
    street = street,
    building = building
)

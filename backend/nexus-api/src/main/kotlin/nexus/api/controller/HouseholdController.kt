package nexus.api.controller

import nexus.core.id.CorporationId
import nexus.core.id.HouseholdId
import nexus.core.id.PersonId
import nexus.household.domain.Household
import nexus.household.domain.HouseholdMember
import nexus.household.domain.Relationship
import nexus.household.service.CreateHouseholdCommand
import nexus.household.service.HouseholdService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 世帯管理API
 */
@RestController
@RequestMapping("/api/v1/households")
class HouseholdController(
    private val householdService: HouseholdService
) {

    @PostMapping
    fun create(@RequestBody request: CreateHouseholdRequest): ResponseEntity<HouseholdResponse> {
        val command = CreateHouseholdCommand(
            corporationId = CorporationId(request.corporationId),
            name = request.name,
            postalCode = request.postalCode,
            prefecture = request.prefecture,
            city = request.city,
            street = request.street,
            building = request.building
        )

        val household = householdService.create(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(household.toResponse())
    }

    @GetMapping("/{householdId}")
    fun findById(@PathVariable householdId: String): ResponseEntity<HouseholdResponse> {
        val household = householdService.findById(HouseholdId(householdId))
        return ResponseEntity.ok(household.toResponse())
    }

    @GetMapping("/by-person/{personId}")
    fun findByPerson(@PathVariable personId: String): ResponseEntity<List<HouseholdResponse>> {
        val households = householdService.findByPerson(PersonId(personId))
        return ResponseEntity.ok(households.map { it.toResponse() })
    }

    @PostMapping("/{householdId}/members")
    fun addMember(
        @PathVariable householdId: String,
        @RequestBody request: AddMemberRequest
    ): ResponseEntity<HouseholdMemberResponse> {
        val member = householdService.addMember(
            householdId = HouseholdId(householdId),
            personId = PersonId(request.personId),
            relationship = request.relationship
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(member.toResponse())
    }

    @DeleteMapping("/{householdId}/members/{personId}")
    fun removeMember(
        @PathVariable householdId: String,
        @PathVariable personId: String
    ): ResponseEntity<Void> {
        householdService.removeMember(
            householdId = HouseholdId(householdId),
            personId = PersonId(personId)
        )
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/{householdId}/head")
    fun changeHead(
        @PathVariable householdId: String,
        @RequestBody request: ChangeHeadRequest
    ): ResponseEntity<Void> {
        householdService.changeHead(
            householdId = HouseholdId(householdId),
            newHeadPersonId = PersonId(request.personId)
        )
        return ResponseEntity.ok().build()
    }
}

// Request/Response DTOs

data class CreateHouseholdRequest(
    val corporationId: String,
    val name: String? = null,
    val postalCode: String? = null,
    val prefecture: String? = null,
    val city: String? = null,
    val street: String? = null,
    val building: String? = null
)

data class AddMemberRequest(
    val personId: String,
    val relationship: Relationship
)

data class ChangeHeadRequest(
    val personId: String
)

data class HouseholdResponse(
    val id: String,
    val corporationId: String,
    val name: String?,
    val headPersonId: String?,
    val postalCode: String?,
    val prefecture: String?,
    val city: String?,
    val street: String?,
    val building: String?,
    val members: List<HouseholdMemberResponse>
)

data class HouseholdMemberResponse(
    val personId: String,
    val relationship: Relationship,
    val isActive: Boolean
)

private fun Household.toResponse() = HouseholdResponse(
    id = id,
    corporationId = corporationId,
    name = name,
    headPersonId = headPersonId,
    postalCode = postalCode,
    prefecture = prefecture,
    city = city,
    street = street,
    building = building,
    members = members.map { it.toResponse() }
)

private fun HouseholdMember.toResponse() = HouseholdMemberResponse(
    personId = personId,
    relationship = relationship,
    isActive = isActive
)

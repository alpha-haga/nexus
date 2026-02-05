package nexus.bff.controller.group

import nexus.bff.controller.group.dto.CompanyResponse
import nexus.group.query.CompanyQueryService
import nexus.bff.controller.group.mapper.toResponse
import org.springframework.context.annotation.Profile
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * 法人マスタ一覧API（P2-5-2 JDBC Read 実装）
 *
 * @Profile("jdbc") で有効化、Bean 競合を回避
 */
@Profile("jdbc")
@RestController
@RequestMapping("/api/group/companies")
class GroupCompaniesController(
    private val companyQueryService: CompanyQueryService
) {

    @GetMapping
    fun list(): ResponseEntity<List<CompanyResponse>> {
        val companies = companyQueryService.findAll()
        return ResponseEntity.ok(companies.map { it.toResponse() })
    }
}

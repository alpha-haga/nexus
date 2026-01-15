package nexus.api.controller

import nexus.infrastructure.db.health.DatabaseHealthCheck
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * DB接続ヘルスチェックAPI
 *
 * nexus-api は DataSource/JPA を所有しない
 * DatabaseHealthCheck を呼び出すだけ
 */
@RestController
@RequestMapping("/api/v1/health/db")
class DbHealthController(
    private val databaseHealthCheck: DatabaseHealthCheck
) {

    @GetMapping("/integration")
    fun checkIntegration(): ResponseEntity<DbHealthResponse> {
        val ok = databaseHealthCheck.testIntegration()
        return ResponseEntity.ok(DbHealthResponse(ok = ok))
    }

    @GetMapping("/regions/{regionId}")
    fun checkRegion(@PathVariable regionId: String): ResponseEntity<DbHealthResponse> {
        val ok = databaseHealthCheck.testRegion(regionId)
        return ResponseEntity.ok(DbHealthResponse(ok = ok))
    }
}

data class DbHealthResponse(
    val ok: Boolean
)
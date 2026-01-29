package nexus.bff.security

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.oauth2.jwt.Jwt

class DbAccessRoleExtractorTest {

    private fun jwtWithRoles(roles: List<String>): Jwt {
        return Jwt.withTokenValue("t")
            .header("alg", "none")
            .claim("nexus_db_access", roles)
            .build()
    }

    @Test
    fun `normalizes roles`() {
        val jwt = jwtWithRoles(listOf("Saitama__Musashino__gojo"))
        val roles = extractDbAccessRolesOrFail(jwt)
        // raw role 名そのまま（trim のみ）を返す
        assertEquals(listOf("Saitama__Musashino__gojo"), roles)
    }

    @Test
    fun `rejects empty claim`() {
        val jwt = jwtWithRoles(emptyList())
        assertThrows(IllegalArgumentException::class.java) {
            extractDbAccessRolesOrFail(jwt)
        }
    }

    @Test
    fun `rejects illegal wildcard in region`() {
        val jwt = jwtWithRoles(listOf("ALL__musashino__GOJO"))
        assertThrows(IllegalArgumentException::class.java) {
            extractDbAccessRolesOrFail(jwt)
        }
    }

    @Test
    fun `rejects illegal wildcard in corp`() {
        val jwt = jwtWithRoles(listOf("saitama__ALL__GOJO"))
        assertThrows(IllegalArgumentException::class.java) {
            extractDbAccessRolesOrFail(jwt)
        }
    }

    @Test
    fun `allows integration ALL GROUP`() {
        val jwt = jwtWithRoles(listOf("integration__ALL__GROUP"))
        val roles = extractDbAccessRolesOrFail(jwt)
        // raw role 名そのままを返す
        assertEquals(listOf("integration__ALL__GROUP"), roles)
    }

    @Test
    fun `allows integration ALL GROUP case insensitive`() {
        val jwt = jwtWithRoles(listOf("Integration__ALL__group"))
        val roles = extractDbAccessRolesOrFail(jwt)
        // raw role 名そのままを返す（case 変換しない）
        assertEquals(listOf("Integration__ALL__group"), roles)
    }

    @Test
    fun `rejects multiple illegal wildcards`() {
        val jwt = jwtWithRoles(listOf("saitama__ALL__GOJO", "fukushima__ALL__FUNERAL"))
        assertThrows(IllegalArgumentException::class.java) {
            extractDbAccessRolesOrFail(jwt)
        }
    }

    @Test
    fun `allows integration ALL GROUP with other valid roles`() {
        val jwt = jwtWithRoles(listOf("integration__ALL__GROUP", "saitama__musashino__GOJO"))
        val roles = extractDbAccessRolesOrFail(jwt)
        // raw role 名そのままを返す
        assertEquals(listOf("integration__ALL__GROUP", "saitama__musashino__GOJO"), roles)
    }
}

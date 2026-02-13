package nexus.core.auth

/**
 * ロール解析（Spring 非依存）
 *
 * Keycloak の nexus_db_access claim のロール文字列:
 *   region__company__domain
 * 例:
 *   saitama__musashino__GOJO
 *   integration__ALL__GROUP
 */
object RoleParser {

    data class ParsedRole(
        val region: String,   // saitama, fukushima, tochigi, integration
        val company: String,  // musashino, saikan, ALL, ...
        val domain: String,   // GOJO, FUNERAL, GROUP, ...
    )

    fun parseRoles(roles: List<String>): List<ParsedRole> =
        roles.mapNotNull { role ->
            val parts = role.split("__")
            if (parts.size == 3) {
                ParsedRole(
                    region = parts[0].trim(),
                    company = parts[1].trim(),
                    domain = parts[2].trim(),
                )
            } else {
                null
            }
        }

    fun hasIntegrationAccess(roles: List<String>): Boolean =
        roles.any { it.equals("integration__ALL__GROUP", ignoreCase = true) }

    /**
     * 利用可能な法人とドメインを取得
     * @return Map<"region__company", Set<domain>>
     */
    fun getAvailableCompanies(roles: List<String>): Map<String, Set<String>> =
        parseRoles(roles)
            .filter { it.region.lowercase() != "integration" }
            .groupBy { "${it.region.lowercase()}__${it.company.lowercase()}" }
            .mapValues { (_, parsedRoles) ->
                parsedRoles.map { it.domain.uppercase() }.toSet()
            }

    fun hasAccess(roles: List<String>, regionCd: String, companyCd: String, domain: String): Boolean {
        val targetRole = "${regionCd.lowercase()}__${companyCd.lowercase()}__${domain.uppercase()}"
        return roles.any { it.equals(targetRole, ignoreCase = true) }
    }

    fun hasCompanyAccess(roles: List<String>, regionCd: String, companyCd: String): Boolean {
        val prefix = "${regionCd.lowercase()}__${companyCd.lowercase()}__"
        return roles.any { it.startsWith(prefix, ignoreCase = true) }
    }
}

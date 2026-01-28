package nexus.bff.security

import org.springframework.security.oauth2.jwt.Jwt

private const val ALL_LOWER = "all"
private const val INTEGRATION = "integration"
private const val GROUP = "GROUP"
private const val SEP = "__"

fun extractDbAccessRolesOrFail(jwt: Jwt): List<String> {
    val raw = jwt.getClaimAsStringList(CLAIM_NAME)
        ?: throw IllegalArgumentException("Missing claim: $CLAIM_NAME")
    if (raw.isEmpty()) throw IllegalArgumentException("Empty claim: $CLAIM_NAME")

    // role 値は raw（trim のみ）を維持。wildcard 判定用にのみ正規化を使う
    val trimmed = raw.map { it.trim() }

    // Wildcard rule: only integration__ALL__GROUP is permitted
    val hasIllegalWildcard = trimmed.any { role ->
        val normalized = normalizeForCheck(role)
        val parts = normalized.split(SEP)
        if (parts.size != 3) return@any false // 形式エラーは別で処理
        
        val region = parts[0].lowercase()
        val corp = parts[1].lowercase()
        val domain = parts[2].uppercase()
        
        // integration__ALL__GROUP は許可（case-insensitive 比較）
        if (region == INTEGRATION && corp == ALL_LOWER && domain == GROUP) {
            return@any false
        }
        
        // region wildcard is always forbidden
        (region == ALL_LOWER) ||
            // corp wildcard is forbidden except integration__ALL__GROUP (already handled above)
            (corp == ALL_LOWER)
    }

    if (hasIllegalWildcard) {
        throw IllegalArgumentException("Illegal wildcard in $CLAIM_NAME")
    }

    return trimmed.distinct()
}

// wildcard 判定用の正規化（返却値には使わない）
private fun normalizeForCheck(raw: String): String {
    val parts = raw.split(SEP)
    if (parts.size != 3) throw IllegalArgumentException("Invalid role format: $raw")
    val region = parts[0].trim().lowercase()
    val corp = parts[1].trim().lowercase()
    val domain = parts[2].trim().uppercase()
    if (region.isBlank() || corp.isBlank() || domain.isBlank()) {
        throw IllegalArgumentException("Invalid role format: $raw")
    }
    return "$region$SEP$corp$SEP$domain"
}

private const val CLAIM_NAME = "nexus_db_access"
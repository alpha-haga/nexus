package nexus.bff.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nexus.infrastructure.db.CorporationContext
import nexus.infrastructure.db.DomainAccount
import nexus.infrastructure.db.DomainAccountContext
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * CorporationDomainAccountContext フィルター
 *
 * HTTP Header "X-NEXUS-CORP" と "X-NEXUS-DOMAIN-ACCOUNT" を読み取り、
 * CorporationContext / DomainAccountContext をセット/クリアする
 *
 * 動作:
 * - Request ごとに Context をセット（ヘッダがある場合のみ）
 * - レスポンス後に clear する（finally で確実にクリア）
 *
 * Header 形式:
 * - X-NEXUS-CORP: 法人名（例: musashino）
 * - X-NEXUS-DOMAIN-ACCOUNT: GOJO / FUNERAL（大小文字は許容）
 * - ヘッダが無い場合は何もしない（region DB を使う導線で必要なら FAIL FAST）
 *
 * プロファイル:
 * - local プロファイルでのみ有効（検証用）
 */
@Component
@Order(2) // RegionContextFilter の後に実行
@Profile("local")
class CorporationDomainAccountContextFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(CorporationDomainAccountContextFilter::class.java)

    companion object {
        private const val CORP_HEADER = "X-NEXUS-CORP"
        private const val DOMAIN_ACCOUNT_HEADER = "X-NEXUS-DOMAIN-ACCOUNT"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val corpHeader = request.getHeader(CORP_HEADER)
            val domainAccountHeader = request.getHeader(DOMAIN_ACCOUNT_HEADER)

            if (corpHeader != null) {
                CorporationContext.set(corpHeader)
                if (logger.isDebugEnabled) {
                    logger.debug("CorporationContext set to: $corpHeader")
                }
            }

            if (domainAccountHeader != null) {
                val domainAccount = DomainAccount.fromStringOrThrow(domainAccountHeader)
                DomainAccountContext.set(domainAccount)
                if (logger.isDebugEnabled) {
                    logger.debug("DomainAccountContext set to: $domainAccount")
                }
            }
            filterChain.doFilter(request, response)
        } finally {
            CorporationContext.clear()
            DomainAccountContext.clear()
            if (logger.isDebugEnabled) {
                logger.debug("CorporationContext and DomainAccountContext cleared")
            }
        }
    }
}
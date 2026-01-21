package nexus.bff.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import nexus.core.region.Region
import nexus.core.region.RegionContext
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * RegionContext フィルター
 *
 * HTTP Header "X-NEXUS-REGION" を読み取り、RegionContext をセット/クリアする
 *
 * 動作:
 * - Request ごとに RegionContext をセット
 * - レスポンス後に clear する（finally で確実にクリア）
 *
 * Header 形式:
 * - X-NEXUS-REGION: saitama/fukushima/tochigi/integration（大小文字は許容）
 * - Header が無い場合は FAIL（例外）
 *
 * プロファイル:
 * - local プロファイルでのみ有効（暫定）
 */
@Component
@Order(1) // 他のフィルターより先に実行
@Profile("local")
class RegionContextFilter : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(RegionContextFilter::class.java)

    companion object {
        private const val REGION_HEADER = "X-NEXUS-REGION"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val regionHeader = request.getHeader(REGION_HEADER)
                ?: throw IllegalArgumentException("Missing required header: $REGION_HEADER")

            val region = Region.fromStringOrThrow(regionHeader)
            RegionContext.set(region)
            if (logger.isDebugEnabled) {
                logger.debug("RegionContext set to: $region from header: $regionHeader")
            }
            filterChain.doFilter(request, response)
        } finally {
            RegionContext.clear()
            if (logger.isDebugEnabled) {
                logger.debug("RegionContext cleared")
            }
        }
    }
}

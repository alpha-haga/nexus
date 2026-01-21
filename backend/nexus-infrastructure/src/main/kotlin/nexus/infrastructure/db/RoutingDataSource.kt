package nexus.infrastructure.db

import nexus.core.region.Region
import nexus.core.region.RegionContext
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

/**
 * ルーティング DataSource
 *
 * RegionContext の値に応じて DataSource を切り替える
@@
 * 未設定時:
 * - 原則は RegionContext 側で例外になる（fail fast）
 * - ただし local/test では起動時の EntityManagerFactory 初期化で Region 未設定が起きるため、
 *   allowFallbackToIntegrationWhenUnset=true の場合のみ INTEGRATION にフォールバックする
*/
class RoutingDataSource(
    private val allowFallbackToIntegrationWhenUnset: Boolean = false
) : AbstractRoutingDataSource() {

    private val logger = LoggerFactory.getLogger(RoutingDataSource::class.java)

     /**
      * ルーティングキーを決定
      *
      * RegionContext から Region を取得し、それをキーとして使用
@@
      * @return ルーティングキー（Region enum）
      * @throws RegionContextNotSetException Region が未設定の場合
      */
      override fun determineCurrentLookupKey(): Any {
        val region = RegionContext.getOrNull()
            ?: if (allowFallbackToIntegrationWhenUnset) Region.INTEGRATION else RegionContext.get()

        // Kotlin が debug(String, Throwable) を選んでしまう事故を避けるため
        // 第2引数を String に寄せて確実に (String, Object) 側に解決させる
        // ✅ Kotlin + SLF4J 安全形
        if (logger.isDebugEnabled) {
            logger.debug("Routing to region: $region")
        }
        return region
    }
}
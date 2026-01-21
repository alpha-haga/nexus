package nexus.infrastructure.db
 
import nexus.core.region.RegionContext
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource

/**
 * ルーティング DataSource
 *
 * RegionContext の値に応じて DataSource を切り替える
@@
 */
class RoutingDataSource : AbstractRoutingDataSource() {

    private val logger: org.slf4j.Logger =
        org.slf4j.LoggerFactory.getLogger(RoutingDataSource::class.java)

    override fun determineCurrentLookupKey(): Any {
        val region = RegionContext.get()
        logger.debug("Routing to region: {}", region)
        return region
    }
}
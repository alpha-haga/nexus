package nexus.infrastructure.db

import nexus.core.region.Region
import nexus.core.region.RegionContext
import nexus.infrastructure.db.exception.CorporationContextNotSetException
import nexus.infrastructure.db.exception.DomainAccountContextNotSetException
import nexus.infrastructure.db.config.RegionDataSourceConfig
import org.slf4j.LoggerFactory
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource
import java.util.concurrent.ConcurrentHashMap
import javax.sql.DataSource

/**
 * Corporation と DomainAccount によるルーティング DataSource
 *
 * Region が region DB（SAITAMA / FUKUSHIMA / TOCHIGI）の場合、
 * CorporationContext と DomainAccountContext に基づいて DataSource を切り替える
 *
 * 実装方針:
 * - (region, corporation, domainAccount) 単位で HikariDataSource を遅延生成＋キャッシュ
 * - 生成キー: "${region.name}|${corporation}|${domainAccount.name}"
 * - corp が region の corporations に列挙されていなければ FAIL FAST
 * - env が不足していれば FAIL FAST（どの env が無いかメッセージに含める）
 *
 * P04-4 方針:
 * - 法人単位での DataSource 作成は禁止という制約を、安全策（列挙制限＋遅延生成＋キャッシュ）で回避
 */
class CorporationDomainAccountRoutingDataSource(
    private val region: Region,
    private val regionConfig: nexus.infrastructure.db.config.RegionDataSourceConfig,
    private val environment: org.springframework.core.env.Environment
) : AbstractRoutingDataSource() {

    private val logger = LoggerFactory.getLogger(CorporationDomainAccountRoutingDataSource::class.java)

    /**
     * DataSource キャッシュ（遅延生成）
     * キー: "${region.name}|${corporation}|${domainAccount.name}"
     */
    private val dataSourceCache = ConcurrentHashMap<String, DataSource>()

    /**
     * 生成済み HikariDataSource を保持（close 用）
     */
    private val hikariDataSources = mutableListOf<com.zaxxer.hikari.HikariDataSource>()

    init {
        // AbstractRoutingDataSource は Spring 初期化時に afterPropertiesSet() が必ず呼ばれるため、
        // targetDataSources を必ず設定する（空Mapでよい）。
        // defaultTargetDataSource は設定しない（FAIL FAST維持）。
        setTargetDataSources(emptyMap<Any, Any>())
        setLenientFallback(false)
    }

/**
     * ルーティングキーを決定
     *
     * @return ルーティングキー（"${region.name}|${corporation}|${domainAccount.name}"）
     * @throws CorporationContextNotSetException Corporation が未設定の場合
     * @throws DomainAccountContextNotSetException DomainAccount が未設定の場合
     */
    override fun determineCurrentLookupKey(): Any {
        // RegionContext が region DB であることを確認
        val currentRegion = RegionContext.get()
        require(currentRegion == region) {
            "RegionContext mismatch: expected $region but got $currentRegion"
        }

        val corporation = CorporationContext.get()
        val domainAccount = DomainAccountContext.get()

        val lookupKey = "${region.name}|$corporation|${domainAccount.name}"

        if (logger.isDebugEnabled) {
            logger.debug("Routing to: region=$region, corporation=$corporation, domainAccount=$domainAccount (key=$lookupKey)")
        }

        return lookupKey
    }

    /**
     * ルーティングキーに基づいて DataSource を解決
     *
     * AbstractRoutingDataSource の getConnection() が呼ばれたときに、
     * determineCurrentLookupKey() で取得したキーを使ってこのメソッドが呼ばれる
     */
    override fun determineTargetDataSource(): DataSource {
        val lookupKey = determineCurrentLookupKey() as String
        return dataSourceCache.computeIfAbsent(lookupKey) { key ->
            createDataSourceForKey(key)
        }
    }

    /**
     * ルーティングキーから DataSource を生成
     *
     * @param key "${region.name}|${corporation}|${domainAccount.name}"
     * @return 生成された HikariDataSource
     */
    private fun createDataSourceForKey(key: String): DataSource {
        val parts = key.split("|")
        require(parts.size == 3) { "Invalid lookup key format: $key" }

        val regionName = parts[0]
        val corporation = parts[1]
        val domainAccountName = parts[2]

        // corp が列挙されているか確認
        require(regionConfig.corporations.contains(corporation)) {
            "Corporation '$corporation' is not listed in region '$regionName' corporations. " +
                "Available corporations: ${regionConfig.corporations.joinToString()}"
        }

        val domainAccount = DomainAccount.valueOf(domainAccountName)

        // env 変数から接続情報を取得
        val envPrefix = "NEXUS_DB_${regionName}_${corporation.uppercase()}_${domainAccountName}"
        val usernameEnv = "${envPrefix}_USER"
        val passwordEnv = "${envPrefix}_PASSWORD"

        val username = environment.getProperty(usernameEnv)
            ?: throw IllegalStateException("Environment variable not found: $usernameEnv")

        val password = environment.getProperty(passwordEnv)
            ?: throw IllegalStateException("Environment variable not found: $passwordEnv")

        // インスタンス接続情報（host/port/service）は regionConfig から取得
        val host = environment.getProperty("NEXUS_DB_${regionName}_HOST")
            ?: throw IllegalStateException("Environment variable not found: NEXUS_DB_${regionName}_HOST")
        val port = environment.getProperty("NEXUS_DB_${regionName}_PORT")
            ?: throw IllegalStateException("Environment variable not found: NEXUS_DB_${regionName}_PORT")
        val service = environment.getProperty("NEXUS_DB_${regionName}_SERVICE")
            ?: throw IllegalStateException("Environment variable not found: NEXUS_DB_${regionName}_SERVICE")

        val driver = environment.getProperty("NEXUS_DB_DRIVER", "oracle.jdbc.OracleDriver")
        val maxPool = environment.getProperty("NEXUS_DB_POOL_MAX", "5").toInt()
        val minPool = environment.getProperty("NEXUS_DB_POOL_MIN", "1").toInt()

        val jdbcUrl = "jdbc:oracle:thin:@//$host:$port/$service"

        logger.info("Creating DataSource for: region=$regionName, corporation=$corporation, domainAccount=$domainAccountName")

        val hikariConfig = com.zaxxer.hikari.HikariConfig()
        hikariConfig.jdbcUrl = jdbcUrl
        hikariConfig.username = username
        hikariConfig.password = password
        hikariConfig.driverClassName = driver
        hikariConfig.poolName = "nexus-${regionName.lowercase()}-$corporation-${domainAccountName.lowercase()}"
        hikariConfig.maximumPoolSize = maxPool
        hikariConfig.minimumIdle = minPool
        hikariConfig.connectionTestQuery = "SELECT 1 FROM DUAL"

        val dataSource = com.zaxxer.hikari.HikariDataSource(hikariConfig)
        hikariDataSources.add(dataSource)

        logger.info("DataSource created: poolName=${hikariConfig.poolName}")

        return dataSource
    }

    /**
     * 生成済み DataSource を close（DisposableBean で呼ばれる）
     */
    fun close() {
        logger.info("Closing ${hikariDataSources.size} HikariDataSources for region: $region")
        hikariDataSources.forEach { dataSource ->
            try {
                dataSource.close()
            } catch (e: Exception) {
                logger.warn("Failed to close DataSource: ${dataSource.poolName}", e)
            }
        }
        hikariDataSources.clear()
        dataSourceCache.clear()
    }
}

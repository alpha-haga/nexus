package nexus.infrastructure.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

/**
 * JDBC設定
 *
 * JDBC の NamedParameterJdbcTemplate を Bean として登録
 * - RoutingDataSource を使用（routingDataSource が @Primary のため自動的に注入される）
 *
 * 注意:
 * - P0-3b では Read=JDBC を強制しない（導線のみ）
 * - 未使用でも配線だけ入れておく
 */
@Configuration
class JdbcConfiguration {

    /**
     * Routing用 NamedParameterJdbcTemplate Bean
     *
     * routingDataSource（@Primary）が自動的に使用される
     */
    @Bean("routingNamedParameterJdbcTemplate")
    @Primary
    fun routingNamedParameterJdbcTemplate(
        routingDataSource: DataSource
    ): NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(routingDataSource)

    /**
     * Integration DB 専用の NamedParameterJdbcTemplate
     *
     * NXCM_COMPANY 等の Integration DB 専用テーブルアクセス用
     */
    @Bean("integrationJdbcTemplate")
    fun integrationJdbcTemplate(
        @Qualifier("integrationDataSource") integrationDataSource: DataSource
    ): NamedParameterJdbcTemplate =
        NamedParameterJdbcTemplate(integrationDataSource)
}

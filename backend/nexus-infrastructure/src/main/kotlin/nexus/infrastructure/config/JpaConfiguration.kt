package nexus.infrastructure.config

import jakarta.persistence.EntityManagerFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * JPA設定
 *
 * infrastructure 層で JPA の設定を集約
 * - Entity スキャン: domain 層の entity パッケージ
 * - Repository スキャン: infrastructure 層の JPA Repository パッケージ
 * - EntityManagerFactory / TransactionManager は RoutingDataSource を使用
 *
 * この設定により、domain 層は JPA の存在を知らずに済む
 *
 * 注意:
 * - Spring Boot の自動設定により、@Primary の DataSource（routingDataSource）が自動的に使用される
 * - 明示的な設定は不要だが、ドキュメント化のためにコメントを追加
 */
@Configuration
@EntityScan(
    basePackages = [
        "nexus.identity.person.entity",
        "nexus.household.member.entity",
        "nexus.gojo.contract.entity"
        ]
)
@EnableJpaRepositories(
    basePackages = [
        "nexus.infrastructure.persistence.jpa"
    ]
)
class JpaConfiguration {

    /**
     * JPA TransactionManager
     *
     * Spring Boot の自動設定により、@Primary の DataSource（routingDataSource）が使用される
     * 明示的な設定は不要だが、ドキュメント化のために Bean を定義
     */
    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory)
    }
}

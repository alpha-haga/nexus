package nexus.infrastructure.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * JPA設定
 *
 * infrastructure 層で JPA の設定を集約
 * - Entity スキャン: domain 層の entity パッケージ
 * - Repository スキャン: infrastructure 層の JPA Repository パッケージ
 *
 * この設定により、domain 層は JPA の存在を知らずに済む
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
class JpaConfiguration

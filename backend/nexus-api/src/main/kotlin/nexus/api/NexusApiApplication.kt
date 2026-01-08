package nexus.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * NEXUS API アプリケーション
 *
 * 外部公開APIのエントリーポイント
 * 人物管理・世帯管理・法人横断検索機能を提供
 */
@SpringBootApplication(
    scanBasePackages = [
        "nexus.api",
        "nexus.group",
        "nexus.identity",
        "nexus.household"
    ]
)
@EntityScan(
    basePackages = [
        "nexus.identity.domain",
        "nexus.household.domain"
    ]
)
@EnableJpaRepositories(
    basePackages = [
        "nexus.identity.repository",
        "nexus.household.repository"
    ]
)
class NexusApiApplication

fun main(args: Array<String>) {
    runApplication<NexusApiApplication>(*args)
}

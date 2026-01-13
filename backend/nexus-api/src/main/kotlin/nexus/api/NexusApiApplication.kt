package nexus.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * NEXUS API アプリケーション
 *
 * 外部公開APIのエントリーポイント
 * 人物管理・世帯管理・法人横断検索機能を提供
 *
 * JPA の設定（@EntityScan, @EnableJpaRepositories）は
 * infrastructure 層の JpaConfiguration に集約
 */
@SpringBootApplication(
    scanBasePackages = [
        "nexus.api",
        "nexus.group",
        "nexus.identity",
        "nexus.household",
        "nexus.infrastructure"
    ]
)
class NexusApiApplication

fun main(args: Array<String>) {
    runApplication<NexusApiApplication>(*args)
}

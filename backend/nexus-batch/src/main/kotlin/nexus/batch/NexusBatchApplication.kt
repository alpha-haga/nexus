package nexus.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * NEXUS バッチアプリケーション
 *
 * データ取り込み・名寄せ補助処理のエントリーポイント
 */
@SpringBootApplication(
    scanBasePackages = [
        "nexus.batch",
        "nexus.identity"
    ]
)
@EntityScan(
    basePackages = [
        "nexus.identity.domain"
    ]
)
@EnableJpaRepositories(
    basePackages = [
        "nexus.identity.repository"
    ]
)
class NexusBatchApplication

fun main(args: Array<String>) {
    runApplication<NexusBatchApplication>(*args)
}

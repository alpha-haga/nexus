package nexus.bff

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * NEXUS BFF アプリケーション
 *
 * 社内UI（Web / Tablet）向け Backend For Frontend。
 * 業務ドメイン（gojo / funeral / point / payment 等）や、UI向け集約APIはここに配置する。
 *
 * 注意:
 * - nexus-api は外部公開・基盤用途（group/identity/household）に限定する。
 * - UI向け Controller を nexus-api に追加しない。
 */
@SpringBootApplication(
    scanBasePackages = [
        "nexus.bff",
        "nexus.infrastructure",
        "nexus.gojo",
        "nexus.group",
        "nexus.identity",
        "nexus.household"
    ]
)
class NexusBffApplication

fun main(args: Array<String>) {
    runApplication<NexusBffApplication>(*args)
}
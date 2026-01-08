/*
 * nexus-batch
 *
 * バッチ処理モジュール
 *
 * 役割:
 * - 外部データの取り込み
 * - 名寄せ補助処理
 * - 定期的なデータ整合性チェック
 *
 * 依存:
 * - nexus-core
 * - nexus-identity（名寄せ処理）
 *
 * 注意:
 * - 大量データ処理のため、トランザクション管理に注意
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-identity"))

    // Batch
    implementation("org.springframework.boot:spring-boot-starter-batch")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

tasks.bootJar {
    archiveBaseName.set("nexus-batch")
}

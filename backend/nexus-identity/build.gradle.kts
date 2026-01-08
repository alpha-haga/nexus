/*
 * nexus-identity
 *
 * 人物（Person）管理モジュール
 *
 * 役割:
 * - 人物マスタの管理
 * - 名寄せロジック
 * - 人物の統合・分離
 *
 * 依存:
 * - nexus-core
 *
 * 重要:
 * - 業務モジュールからの直接更新は禁止
 * - 更新は本モジュール経由でのみ行う
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

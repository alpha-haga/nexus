/*
 * nexus-household
 *
 * 世帯管理モジュール
 *
 * 役割:
 * - 世帯の管理
 * - 人物と世帯の関係管理
 * - 世帯主・続柄の管理
 *
 * 依存:
 * - nexus-core
 * - nexus-identity
 *
 * 重要:
 * - identity モジュールを参照のみで使用
 * - 世帯変更は本モジュール経由でのみ行う
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-identity"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

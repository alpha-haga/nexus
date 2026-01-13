/*
 * nexus-gojo
 *
 * 互助会業務モジュール
 *
 * 役割:
 * - 互助会契約の管理
 * - 積立金の管理
 * - 掛金の計算
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - identity / group への直接アクセスは禁止
 * - person情報はIDでのみ参照
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-infrastructure"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

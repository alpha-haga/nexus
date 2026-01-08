/*
 * nexus-point
 *
 * ポイント管理モジュール
 *
 * 役割:
 * - ポイントアカウントの管理
 * - ポイントの付与・利用・失効
 * - ポイント履歴の管理
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - identity / group への直接アクセスは禁止
 * - 業務モジュール間の直接依存は禁止
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

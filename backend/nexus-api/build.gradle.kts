/*
 * nexus-api
 *
 * 外部公開・基盤用途の API モジュール
 *
 * 役割:
 * - 外部公開・基盤用途の REST API エンドポイントの提供
 * - 認証・認可（将来的にKeycloak連携）
 * - リクエスト/レスポンスの変換
 *
 * 依存:
 * - nexus-core
 * - nexus-infrastructure（DB接続基盤のみ、DataSource設定は所有しない）
 * - nexus-group（法人横断検索）
 * - nexus-identity（人物管理）
 * - nexus-household（世帯管理）
 *
 * 注意:
 * - 業務モジュール（gojo/funeral/bridal/point/agent/payment/accounting/reporting）への直接依存は禁止
 * - 業務ドメインの Controller は nexus-bff に実装する（nexus-api には追加禁止）
 * - 社内UI向けAPIは nexus-bff が提供する
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-infrastructure"))
    implementation(project(":nexus-group"))
    implementation(project(":nexus-identity"))
    implementation(project(":nexus-household"))

    // Web
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // JPA (for @EnableJpaRepositories, @EntityScan)
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Security (スタブ状態)
    // implementation("org.springframework.boot:spring-boot-starter-security")
    // implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Database
    // H2 はローカル検証用（暫定）。業務DBは OCI Oracle
    runtimeOnly("com.h2database:h2")
}

tasks.bootJar {
    archiveBaseName.set("nexus-api")
}

/*
 * nexus-api
 *
 * 外部公開APIモジュール
 *
 * 役割:
 * - REST API エンドポイントの提供
 * - 認証・認可（将来的にKeycloak連携）
 * - リクエスト/レスポンスの変換
 *
 * 依存:
 * - nexus-group（法人横断検索）
 * - nexus-identity（人物管理）
 * - nexus-household（世帯管理）
 *
 * 注意:
 * - 業務モジュール（gojo/funeral/bridal/point）への直接依存は禁止
 * - 業務機能は将来的にBFFまたは別APIとして提供
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-group"))
    implementation(project(":nexus-identity"))
    implementation(project(":nexus-household"))

    // Web
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Security (スタブ状態)
    // implementation("org.springframework.boot:spring-boot-starter-security")
    // implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    // Documentation
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")

    // Database
    runtimeOnly("com.h2database:h2")
}

tasks.bootJar {
    archiveBaseName.set("nexus-api")
}

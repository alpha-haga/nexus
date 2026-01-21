/*
 * nexus-architecture-tests
 *
 * ArchUnit による境界ルールの機械強制（P0-3a）
 * - CI では :nexus-architecture-tests:test を必ず実行する想定
 * - テスト専用モジュール（プロダクションコードは持たない）
 */

dependencies {
    // ★ 追加：Spring Boot の依存バージョンを解決できるようにする
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.2.2"))
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")

    // 全体スキャンのため、対象モジュールを test classpath に乗せる
    testImplementation(project(":nexus-core"))
    testImplementation(project(":nexus-infrastructure"))

    // Domain
    testImplementation(project(":nexus-group"))
    testImplementation(project(":nexus-identity"))
    testImplementation(project(":nexus-household"))
    testImplementation(project(":nexus-gojo"))
    testImplementation(project(":nexus-funeral"))
    testImplementation(project(":nexus-bridal"))
    testImplementation(project(":nexus-point"))
    testImplementation(project(":nexus-agent"))
    testImplementation(project(":nexus-payment"))
    testImplementation(project(":nexus-accounting"))
    testImplementation(project(":nexus-reporting"))

    // App
    testImplementation(project(":nexus-api"))
    testImplementation(project(":nexus-bff"))
    testImplementation(project(":nexus-batch"))
}

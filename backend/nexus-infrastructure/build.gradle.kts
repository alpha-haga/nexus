/*
 * nexus-infrastructure
 *
 * DB接続基盤モジュール
 * - DataSource管理（地区DB × 3 + 統合DB × 1）
 * - DbConnectionProvider による接続提供
 * - ドメイン層からの直接JDBC依存を遮断
 *
 * 依存: nexus-core
 * 依存先: nexus-api, nexus-batch, 各業務モジュール
 */

dependencies {
    implementation(project(":nexus-core"))

    // Spring JDBC (JPA ではなく素の JDBC)
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Oracle JDBC Driver
    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    // HikariCP (Spring Boot JDBC starter に含まれるが明示)
    implementation("com.zaxxer:HikariCP")

    // 開発用: H2 Database (テスト・ローカル開発用)
    runtimeOnly("com.h2database:h2")
}

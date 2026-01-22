/*
 * nexus-infrastructure
 *
 * インフラストラクチャ層
 * - DataSource管理（地区DB × 3 + 統合DB × 1）
 * - DbConnectionProvider による接続提供
 * - Repository 実装（JPA）
 * - ドメイン層からの直接JDBC/JPA依存を遮断
 *
 * 依存: nexus-core, nexus-identity, nexus-household（interface のみ）
 * 依存先: nexus-api, nexus-batch
 */

plugins {
    // 既に plugins ブロックがある場合はそこに追記してください
    kotlin("kapt")
}

dependencies {
    // VSCode / IDE が nexus.datasource.* を既知プロパティとして解釈できるように
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation(project(":nexus-core"))
    implementation(project(":nexus-identity"))
    implementation(project(":nexus-household"))
    implementation(project(":nexus-gojo"))
    implementation(project(":nexus-group"))

    // Spring Data JPA（Repository 実装用）
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // Spring JDBC（素の JDBC 接続用）
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    // Oracle JDBC Driver
    implementation("com.oracle.database.jdbc:ojdbc11:23.3.0.23.09")

    // HikariCP (Spring Boot starter に含まれるが明示)
    implementation("com.zaxxer:HikariCP")

    // 開発用: H2 Database (テスト・ローカル開発用)
    runtimeOnly("com.h2database:h2")
}

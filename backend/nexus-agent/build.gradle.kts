/*
 * nexus-agent
 *
 * 代理店・業務委託ドメイン（トップドメイン）
 *
 * 役割:
 * - 業務委託契約の管理
 * - 案件割当の管理
 * - 報酬・支給の管理
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - 特定業務（互助会・葬祭）に依存しない
 * - gojo のサブドメインではなくトップドメインとして扱う
 * - 会計仕訳は作成しない（accounting の責務）
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}

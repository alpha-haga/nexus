/*
 * nexus-payment
 *
 * 入金・支払管理ドメイン
 *
 * 役割:
 * - 入金の記録・管理
 * - 支払の記録・管理
 * - 金銭の流れの追跡
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - 会計仕訳は作成しない（accounting の責務）
 * - 金銭イベント（Fact）のみを扱う
 * - 勘定科目・借方貸方を持たない
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}

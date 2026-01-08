/*
 * nexus-accounting
 *
 * 会計・仕訳管理ドメイン
 *
 * 役割:
 * - 会計仕訳の作成・管理
 * - 勘定科目の管理
 * - 月次処理・締め処理
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - 唯一、会計仕訳を作成してよいドメイン
 * - 業務ドメインからの会計イベント（Fact）を受け取り仕訳化
 * - 仕訳確定は月次処理で行う
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}

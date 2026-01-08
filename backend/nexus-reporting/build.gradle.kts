/*
 * nexus-reporting
 *
 * 集計・レポーティングドメイン（Read Only）
 *
 * 役割:
 * - 各種集計・統計の提供
 * - レポート生成
 * - ダッシュボード用データ提供
 *
 * 依存:
 * - nexus-core のみ
 *
 * 重要:
 * - Read Only - 登録・更新・業務ロジックを持たない
 * - 将来、DWH / BI に切り出せる前提
 * - Query / View のみを提供
 */

dependencies {
    implementation(project(":nexus-core"))

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2")
}

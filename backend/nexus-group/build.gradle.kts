/*
 * nexus-group
 *
 * 全法人横断検索モジュール（Read Only）
 *
 * 役割:
 * - 複数法人にまたがるデータの統合検索
 * - 参照専用（更新操作は提供しない）
 *
 * 依存:
 * - nexus-core のみ
 *
 * 禁止事項:
 * - データの更新操作
 * - 業務モジュールへの依存
 */

dependencies {
    implementation(project(":nexus-core"))

    // Read用のDB接続
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

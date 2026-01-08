/*
 * nexus-funeral
 *
 * 葬祭業務モジュール
 *
 * 役割:
 * - 葬祭案件の管理
 * - 施行プランの管理
 * - 互助会契約との連携（参照のみ）
 *
 * 依存:
 * - nexus-core
 * - nexus-gojo（参照のみ - 契約情報の取得）
 *
 * 重要:
 * - identity / group への直接アクセスは禁止
 * - gojo への更新操作は禁止（参照のみ）
 */

dependencies {
    implementation(project(":nexus-core"))
    implementation(project(":nexus-gojo")) // 参照のみ

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    runtimeOnly("com.h2database:h2") // 開発用
}

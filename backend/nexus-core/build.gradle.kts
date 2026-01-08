/*
 * nexus-core
 *
 * Pure Kotlin モジュール
 * - Spring依存禁止
 * - Entity/Repository禁止
 * - ID定義、ValueObject、共通例外のみ
 *
 * このモジュールは全てのモジュールから参照される基盤
 */

plugins {
    // Pure Kotlin のみ（Spring プラグインなし）
}

dependencies {
    // 外部依存は最小限に
    // Spring関連の依存は禁止
}

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
    
    // CompanyMasterQueryServiceインターフェース参照のため
    // 注意: interface参照のみ（実装は参照しない）
    
    // @Serviceアノテーション用（設計書の指示に従いcore層に配置）
    // 注意: 設計原則に反するが、設計書の指示に従う
}

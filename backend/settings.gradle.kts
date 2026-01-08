/*
 * NEXUS - 互助会・葬祭・冠婚・ポイント統合システム
 *
 * マルチモジュール構成:
 * - 依存関係は一方向のみ許可
 * - 業務モジュールから identity/group への直接更新は禁止
 * - core は Pure Kotlin（Spring依存禁止）
 */

rootProject.name = "nexus"

// ====================
// Core Layer
// ====================
// Pure Kotlin module - Spring依存禁止
// ID定義、ValueObject、共通例外のみ
include("nexus-core")

// ====================
// Domain Layer
// ====================
// 全法人横断検索（Read Only）
// 依存: nexus-core
include("nexus-group")

// person管理・名寄せロジック
// 依存: nexus-core
include("nexus-identity")

// 世帯管理・personとの関係管理
// 依存: nexus-core, nexus-identity
include("nexus-household")

// ====================
// Business Layer
// ====================
// 互助会（契約・積立）
// 依存: nexus-core
include("nexus-gojo")

// 葬祭業務
// 依存: nexus-core, nexus-gojo（参照のみ）
include("nexus-funeral")

// 冠婚業務
// 依存: nexus-core, nexus-gojo（参照のみ）
include("nexus-bridal")

// ポイント管理
// 依存: nexus-core
include("nexus-point")

// 代理店・業務委託（トップドメイン）
// 依存: nexus-core
// 注意: 特定業務（互助会・葬祭）に依存しない制度ドメイン
include("nexus-agent")

// ====================
// Financial Layer
// ====================
// 入金・支払管理
// 依存: nexus-core
// 注意: 会計仕訳は作成しない（accounting の責務）
include("nexus-payment")

// 会計・仕訳管理
// 依存: nexus-core
// 注意: 唯一、会計仕訳を作成してよいドメイン
include("nexus-accounting")

// ====================
// Reporting Layer
// ====================
// 集計・レポーティング（Read Only）
// 依存: nexus-core
// 注意: 登録・更新・業務ロジックを持たない
include("nexus-reporting")

// ====================
// Application Layer
// ====================
// 外部公開API
// 依存: nexus-group, nexus-identity, nexus-household
include("nexus-api")

// データ取込・名寄せ補助バッチ
// 依存: nexus-core, nexus-identity
include("nexus-batch")

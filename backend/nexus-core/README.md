# nexus-core

## 役割

NEXUS システム全体の基盤となる Pure Kotlin モジュール。

- **ID 定義**: PersonId, HouseholdId, GojoContractId 等の型安全な識別子
- **Value Object**: Money, Address, PersonName 等の不変オブジェクト
- **共通例外**: NexusException 階層による統一的なエラーハンドリング
- **ユーティリティ**: 純粋関数による共通処理

## やってよいこと

| 許可 | 例 |
|------|-----|
| ID クラスの定義 | `@JvmInline value class PersonId(val value: String)` |
| Value Object の定義 | `data class Money(val yen: Long)` |
| 共通例外の定義 | `sealed class NexusException` |
| 純粋関数のユーティリティ | 日付計算、文字列正規化など |
| バリデーションロジック | ID/VO の `init` ブロックでの検証 |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| Spring アノテーション | `@Component`, `@Service`, `@Repository` は禁止。フレームワーク依存を避ける |
| JPA Entity | `@Entity`, `@Table` は禁止。永続化はドメインモジュールの責務 |
| Repository インターフェース | データアクセスは各ドメインモジュールで定義 |
| DI コンテナ依存 | コンストラクタインジェクション前提のクラス設計は禁止 |
| 外部ライブラリ依存 | Kotlin 標準ライブラリ以外の依存は原則禁止 |
| 他モジュールへの依存 | core は誰にも依存しない。依存グラフの根となる |

## ディレクトリ構成

```
src/main/kotlin/nexus/core/
├── id/           # 識別子定義
│   └── Identifiers.kt
├── vo/           # Value Object
│   └── ValueObjects.kt
├── exception/    # 共通例外
│   └── Exceptions.kt
└── util/         # ユーティリティ（純粋関数のみ）
```

## 設計指針

### なぜ Pure Kotlin か

1. **依存の最小化**: core が Spring に依存すると、全モジュールが Spring 必須になる
2. **テスト容易性**: DI 不要で単体テストが書ける
3. **移植性**: 将来的に別フレームワーク（Ktor等）への移行が容易
4. **コンパイル速度**: Spring の annotation processing が不要

### Value Object の原則

```kotlin
// Good: 不変、検証付き
@JvmInline
value class PersonId(val value: String) {
    init {
        require(value.isNotBlank()) { "PersonId must not be blank" }
    }
}

// Bad: 可変、検証なし
data class PersonId(var value: String?)
```

## 違反チェック

build.gradle.kts で Spring 依存がないことを確認：

```kotlin
// nexus-core/build.gradle.kts
dependencies {
    // Spring 関連の依存は一切なし
    // implementation("org.springframework:...") ← 禁止
}
```

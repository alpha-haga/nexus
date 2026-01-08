# nexus-identity

## 役割

**人物（Person）の Single Source of Truth**。

- **人物マスタ管理**: 全法人共通の人物情報を一元管理
- **名寄せロジック**: 重複人物の検出・統合
- **人物ライフサイクル**: 登録・更新・統合・論理削除

## やってよいこと

| 許可 | 例 |
|------|-----|
| Person Entity の定義 | `@Entity class Person` |
| Person Repository | `interface PersonRepository : JpaRepository` |
| 人物の CRUD | 登録、更新、検索、論理削除 |
| 名寄せの実行 | `PersonMergeService.merge(sourceId, targetId)` |
| 名寄せ候補の検出 | 電話番号・メール・名前での候補検索 |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| 業務データの管理 | 契約・案件は各業務モジュールの責務 |
| 他モジュール Entity の定義 | GojoContract 等は identity に置かない |
| 業務ロジックの実装 | 掛金計算、葬儀プラン選定等は業務モジュールで |
| group への依存 | identity は core のみに依存 |

## ディレクトリ構成

```
src/main/kotlin/nexus/identity/
├── controller/   # REST エンドポイント
├── dto/          # リクエスト/レスポンス DTO
├── domain/       # Entity
│   └── Person.kt
├── repository/   # データアクセス
│   └── PersonRepository.kt
├── service/      # ビジネスロジック
│   ├── PersonService.kt
│   └── PersonMergeService.kt
└── config/       # モジュール設定
```

## 設計指針

### Single Source of Truth とは

人物に関する「正しい情報」は identity のみが持つ。

```
┌─────────────┐
│  identity   │  ← 人物の「正」
│   Person    │
└──────┬──────┘
       │ PersonId で参照
       ▼
┌──────────────────────────────────────┐
│  gojo / funeral / bridal / point     │
│  PersonId を外部キーとして保持        │
│  人物情報のコピーは持たない           │
└──────────────────────────────────────┘
```

### 名寄せの仕組み

```kotlin
// 名寄せ実行
fun merge(sourceId: PersonId, targetId: PersonId): Person {
    // source の情報を target に統合
    // source は mergedIntoId を設定して論理削除
    // 関連する業務データは PersonId の参照先が変わる
}
```

**名寄せ時の注意**:
- 統合元（source）は `mergedIntoId` で統合先を記録
- 統合先（target）に欠損情報を補完
- 業務モジュールの外部キーは自動的に統合先を参照

### 業務モジュールとの関係

業務モジュールは identity を **参照のみ** で利用：

```kotlin
// Good: PersonId で参照
class GojoContract(
    val contractorPersonId: String  // identity.Person.id への参照
)

// Bad: Person のコピーを保持
class GojoContract(
    val contractorName: String,     // 名前が変わったら不整合
    val contractorPhone: String     // identity と二重管理になる
)
```

## 依存関係

```
nexus-identity
    └── nexus-core （ID, VO, 例外）
```

**このモジュールに依存してよいモジュール**:
- nexus-household（世帯は人物を含む）
- nexus-api（人物管理APIを公開）
- nexus-batch（外部データ取り込み）

**参照のみ許可**:
- nexus-gojo / nexus-funeral / nexus-bridal / nexus-point
  - PersonId での参照は可
  - PersonService の更新メソッド呼び出しは禁止

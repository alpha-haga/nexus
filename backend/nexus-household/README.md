# nexus-household

## 役割

**世帯（Household）の Single Source of Truth**。

- **世帯マスタ管理**: 世帯単位での顧客管理
- **構成員管理**: 世帯主・配偶者・子等の関係管理
- **世帯住所管理**: 世帯単位の住所情報

## やってよいこと

| 許可 | 例 |
|------|-----|
| Household Entity の定義 | `@Entity class Household` |
| HouseholdMember Entity | 世帯構成員の関係定義 |
| Household Repository | `interface HouseholdRepository : JpaRepository` |
| 世帯の CRUD | 作成、構成員追加/削除、世帯主変更 |
| identity.Person の参照 | PersonId で構成員を特定 |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| Person の更新 | 人物情報の変更は identity の責務 |
| Person Entity の定義 | Person は identity で定義済み |
| 業務データの管理 | 契約・案件は各業務モジュールの責務 |
| group への依存 | household は core と identity のみに依存 |

## ディレクトリ構成

```
src/main/kotlin/nexus/household/
├── controller/   # REST エンドポイント
├── dto/          # リクエスト/レスポンス DTO
├── domain/       # Entity
│   └── Household.kt  # Household, HouseholdMember, Relationship
├── repository/   # データアクセス
│   └── HouseholdRepository.kt
├── service/      # ビジネスロジック
│   └── HouseholdService.kt
└── config/       # モジュール設定
```

## 設計指針

### 世帯と人物の関係

```
┌─────────────────────────────────────────┐
│              Household                   │
│  ┌─────────────────────────────────┐    │
│  │ HouseholdMember (世帯主)         │    │
│  │   personId ──────────────────────┼────┼──▶ identity.Person
│  │   relationship: HEAD             │    │
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │ HouseholdMember (配偶者)         │    │
│  │   personId ──────────────────────┼────┼──▶ identity.Person
│  │   relationship: SPOUSE           │    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

### 続柄（Relationship）

```kotlin
enum class Relationship {
    HEAD,           // 世帯主
    SPOUSE,         // 配偶者
    CHILD,          // 子
    PARENT,         // 親
    GRANDPARENT,    // 祖父母
    GRANDCHILD,     // 孫
    SIBLING,        // 兄弟姉妹
    OTHER_RELATIVE, // その他親族
    OTHER           // その他
}
```

### identity への依存方法

```kotlin
// Good: PersonId で参照し、必要時に identity から取得
class HouseholdMember(
    val personId: String,  // identity.Person.id
    val relationship: Relationship
)

// 人物情報が必要な場合は Service 層で取得
fun getMemberDetails(member: HouseholdMember): PersonDto {
    return personService.findById(PersonId(member.personId))
}
```

### 業務モジュールとの関係

業務モジュールは household を **参照のみ** で利用：

```kotlin
// Good: HouseholdId で参照
class GojoContract(
    val householdId: String?  // 世帯単位契約の場合
)

// Bad: 世帯構成員をコピー
class GojoContract(
    val familyMembers: List<String>  // household と二重管理
)
```

## 依存関係

```
nexus-household
    ├── nexus-core （ID, VO, 例外）
    └── nexus-identity （PersonService を参照）
```

**このモジュールに依存してよいモジュール**:
- nexus-api（世帯管理APIを公開）

**参照のみ許可**:
- nexus-gojo / nexus-funeral / nexus-bridal / nexus-point
  - HouseholdId での参照は可
  - HouseholdService の更新メソッド呼び出しは禁止

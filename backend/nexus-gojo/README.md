# nexus-gojo

## 役割

**互助会業務モジュール**。

- **互助会契約管理**: 契約の作成・変更・解約
- **積立金管理**: 入金記録・残高管理
- **掛金計算**: 月額掛金・満期金額の計算

## やってよいこと

| 許可 | 例 |
|------|-----|
| GojoContract Entity | 互助会契約の定義 |
| GojoPayment Entity | 入金履歴の定義 |
| 契約の CRUD | 作成、入金記録、ステータス変更 |
| PersonId での参照 | 契約者・受益者の特定 |
| 業務ロジック | 満期判定、進捗率計算 |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| identity への更新 | 顧客情報変更は identity の責務 |
| household への更新 | 世帯情報変更は household の責務 |
| group への依存 | 業務モジュールは横断検索不要 |
| Person Entity の定義 | Person は identity で定義済み |
| 人物情報のコピー保持 | PersonId で参照し、必要時に取得 |
| **会計仕訳の作成** | 会計は accounting の責務 |
| **勘定科目・借方貸方の保持** | 金銭 Fact のみ扱う |
| **payment / accounting 内部ロジック参照** | 境界を越えない |

## 内部サブドメイン

gojo は将来最も肥大化するドメインのため、以下のサブドメインに分割：

```
nexus-gojo/
├── contract/     # 契約・権利管理
├── enrollment/   # 加入処理
├── change/       # 契約変更
├── task/         # ワークフロー・承認
└── master/       # 互助会マスタ
```

各サブドメインの詳細は個別の README.md を参照。

## ディレクトリ構成

```
src/main/kotlin/nexus/gojo/
├── controller/   # REST エンドポイント
├── dto/          # リクエスト/レスポンス DTO
├── domain/       # Entity
│   └── GojoContract.kt  # GojoContract, GojoPayment
├── repository/   # データアクセス
│   └── GojoContractRepository.kt
├── service/      # ビジネスロジック
│   └── GojoContractService.kt
└── config/       # モジュール設定
```

## 設計指針

### 顧客参照の方法

```kotlin
// Good: PersonId で参照（コピーを持たない）
@Entity
class GojoContract(
    val contractorPersonId: String,   // identity.Person.id
    val beneficiaryPersonId: String?  // 受益者（任意）
) {
    // 人物情報が必要な場合は API 層で結合
}

// Bad: 人物情報をコピー
@Entity
class GojoContract(
    val contractorName: String,   // identity と二重管理
    val contractorPhone: String   // 更新漏れのリスク
)
```

### 契約ステータス

```kotlin
enum class ContractStatus {
    ACTIVE,     // 契約中（積立継続）
    MATURED,    // 満期到達
    USED,       // 使用済み（葬祭/冠婚で利用）
    CANCELLED,  // 解約
    SUSPENDED   // 休止
}
```

### funeral / bridal からの参照

葬祭・冠婚モジュールは gojo を **参照のみ** で利用：

```kotlin
// funeral/bridal からの利用
class FuneralCase(
    val gojoContractId: String?  // 互助会契約を利用する場合
)

// 参照のみ許可
val contract = gojoContractService.findById(contractId)  // OK
gojoContractService.markAsUsed(contractId)               // OK（ステータス変更）

// 禁止
gojoContractService.createContract(...)  // NG（funeral から契約作成しない）
```

### 積立進捗の計算

```kotlin
// 業務ロジックは gojo 内で完結
val progressRate: Double
    get() = if (maturityAmount > 0)
        totalPaidAmount.toDouble() / maturityAmount
    else 0.0

val isMatured: Boolean
    get() = totalPaidAmount >= maturityAmount
```

## 依存関係

```
nexus-gojo
    └── nexus-core （ID, VO, 例外）
```

**このモジュールに依存してよいモジュール**:
- nexus-funeral（互助会契約を参照）
- nexus-bridal（互助会契約を参照）

**禁止される依存**:
- nexus-identity（PersonId 参照は可、更新呼び出しは禁止）
- nexus-household（HouseholdId 参照は可、更新呼び出しは禁止）
- nexus-group（業務モジュールは横断検索不要）

## 他業務モジュールとの関係

```
          ┌─────────────────┐
          │    nexus-gojo    │
          │   互助会契約     │
          └────────┬────────┘
                   │ 参照のみ
        ┌──────────┴──────────┐
        ▼                     ▼
┌───────────────┐     ┌───────────────┐
│ nexus-funeral  │     │ nexus-bridal   │
│ 契約を利用して │     │ 契約を利用して │
│ 葬祭を実施    │     │ 冠婚を実施     │
└───────────────┘     └───────────────┘
```

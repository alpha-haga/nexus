# nexus-payment

## 役割

**入金・支払を管理するドメイン**。

金銭の流れを記録・追跡するが、会計仕訳は作成しない。

- **入金管理**: 顧客からの入金記録
- **支払管理**: 業者・代理店への支払記録
- **消込処理**: 入金と債権の紐付け

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| gojo | イベント受信 | 積立入金の Fact を受け取る |
| funeral | イベント受信 | 葬祭代金入金の Fact を受け取る |
| agent | イベント受信 | 報酬支払の Fact を受け取る |
| accounting | イベント送信 | 入金・支払 Fact を accounting へ |

## やってはいけないこと

| 禁止事項 | 理由 |
|---------|------|
| 会計仕訳の作成 | 会計は accounting の責務 |
| 勘定科目の管理 | accounting の責務 |
| 借方・貸方の記録 | accounting の責務 |
| 業務ロジックの実装 | 契約・案件管理は各業務ドメインで |
| identity / household の更新 | 顧客マスタ更新は identity の責務 |

## 設計方針

### 会計との境界

```
┌─────────────────┐         ┌─────────────────┐
│    payment      │         │   accounting    │
│  ───────────    │         │  ───────────    │
│  入金 Fact      │────────▶│  入金仕訳       │
│  支払 Fact      │────────▶│  支払仕訳       │
│  消込 Fact      │────────▶│  消込仕訳       │
│                 │         │                 │
│  ※仕訳は作らない │         │  ※仕訳を作る    │
└─────────────────┘         └─────────────────┘
```

### Fact（事実）の例

```kotlin
// payment で扱う Fact
data class PaymentReceivedFact(
    val paymentId: PaymentId,
    val amount: Money,
    val receivedAt: LocalDateTime,
    val sourceType: SourceType  // GOJO_INSTALLMENT, FUNERAL_FEE, etc.
)

// これを accounting に渡して仕訳化
// payment 自体は「いくら受け取った」だけを記録
```

## 依存関係

```
nexus-payment
    └── nexus-core （ID, VO, 例外）
```

## 将来の拡張

- 決済代行連携（クレジットカード、口座振替）
- 入金予測・督促管理
- キャッシュフロー分析（Read Only で reporting へ）

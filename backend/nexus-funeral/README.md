# nexus-funeral

## 役割

**葬祭業務ドメイン**。

将来の分割を見据え、内部サブドメインを明確に分離。

- **受付・CRM**: 顧客対応・問い合わせ
- **施行管理**: 葬儀の実施管理
- **運行・車両**: 搬送・車両手配
- **債権・入金**: 請求・入金管理（※会計ではない）

## 内部サブドメイン

```
nexus-funeral/
├── reception/    # 受付・CRM
├── operation/    # 施行管理
├── logistics/    # 運行・車両
├── finance/      # 債権・入金・預り金（※会計ではない）
├── reporting/    # 集計・リアルタイム（Read Only）
└── master/       # 葬祭マスタ
```

各サブドメインの詳細は個別の README.md を参照。

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| gojo | 参照 | 互助会契約の利用 |
| identity | 参照 | 顧客情報の参照 |
| household | 参照 | 世帯情報の参照 |
| agent | 参照 | 請負代理店 |
| payment | イベント送信 | 入金 Fact |
| accounting | イベント送信 | 会計イベント |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| identity への更新 | 顧客情報変更は identity の責務 |
| household への更新 | 世帯情報変更は household の責務 |
| **会計仕訳の作成** | 会計は accounting の責務 |
| **会計ルールの保持** | 勘定科目は accounting で |
| gojo 契約の作成 | gojo の責務 |

## 依存関係

```
nexus-funeral
    ├── nexus-core （ID, VO, 例外）
    └── nexus-gojo （互助会契約を参照のみ）
```

## 会計連携

```kotlin
// funeral で発生する会計イベント（Fact）
data class FuneralSalesFact(
    val caseId: FuneralCaseId,
    val amount: Money,
    val salesDate: LocalDate
)

// → accounting で仕訳化（funeral では仕訳を作らない）
```

## 将来の分割

funeral は将来、以下のように分割される可能性がある：

- nexus-funeral-reception（受付・CRM）
- nexus-funeral-operation（施行管理）
- nexus-funeral-logistics（運行・車両）

現時点ではサブドメインとして責務を明確化。

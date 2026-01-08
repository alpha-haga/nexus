# nexus-reporting

## 役割

**集計・レポーティング専用ドメイン（Read Only）**。

各ドメインのデータを集計・分析し、レポートとして提供する。
将来、DWH / BI に切り出せる前提で設計。

- **集計処理**: 各種 KPI の算出
- **レポート生成**: 帳票・ダッシュボード用データ
- **リアルタイム集計**: 現時点での状況把握

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| gojo | 参照 | 契約・積立状況の集計 |
| funeral | 参照 | 施行実績の集計 |
| bridal | 参照 | 冠婚実績の集計 |
| accounting | 参照 | 会計データの分析 |
| 全ドメイン | 参照 | 横断的なレポート生成 |

## やってはいけないこと

| 禁止事項 | 理由 |
|---------|------|
| データの登録・更新 | reporting は Read Only |
| 業務ロジックの実装 | 計算は各業務ドメインで |
| トランザクション処理 | 参照のみ。更新しない |
| マスタの管理 | マスタは各ドメインで |
| identity / household の参照以外 | 参照のみ許可 |

## 設計方針

### Read Only の徹底

```kotlin
// Good: 参照専用の Query
@Service
class SalesReportQueryService(
    // Read Only なデータソースへの接続
) {
    fun getMonthlySales(yearMonth: YearMonth): SalesReport
    fun getDailySummary(date: LocalDate): DailySummary
}

// Bad: 更新を含むサービス
@Service
class ReportService(
    private val repository: ReportRepository
) {
    fun save(report: Report)  // NG: 更新は禁止
}
```

### DWH/BI 分離の前提

```
現在:
┌─────────────────┐
│   reporting     │ ← 業務DBを直接参照
│  (Read Only)    │
└─────────────────┘

将来:
┌─────────────────┐     ┌─────────────────┐
│   reporting     │     │    DWH / BI     │
│  (Read Only)    │────▶│  (分析基盤)     │
└─────────────────┘     └─────────────────┘
        │
        ▼
  reporting は
  DWH への橋渡し役に
```

### 集計の種類

| 種類 | 説明 | 例 |
|------|------|-----|
| リアルタイム | 現時点の状況 | 本日の入金件数 |
| 日次 | 日単位の集計 | 日別売上推移 |
| 月次 | 月単位の集計 | 月次決算レポート |
| 年次 | 年単位の集計 | 年間業績サマリー |

## 依存関係

```
nexus-reporting
    └── nexus-core （ID, VO, 例外）
```

## ディレクトリ構成

```
nexus-reporting/
├── query/      # 集計クエリ
├── service/    # レポートサービス（Read Only）
└── config/     # 設定
```

## 注意事項

- 現時点ではパッケージ構成と README による設計固定のみ
- 実際の集計ロジック実装は将来
- パフォーマンスを考慮し、専用の Read Replica 接続を検討

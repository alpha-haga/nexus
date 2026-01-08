# funeral/reporting

## 責務

**葬祭の集計・リアルタイムレポート（Read Only）**。

- 施行実績の集計
- リアルタイムダッシュボード
- KPI 算出

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| funeral/* | 参照 | 各サブドメインのデータを集計 |
| reporting | 連携 | 全社レポートへデータ提供 |

## 禁止事項

| 禁止 | 理由 |
|------|------|
| **データの登録・更新** | Read Only |
| **業務ロジックの実装** | 集計に徹する |
| **トランザクション処理** | 参照のみ |
| 施行管理 | operation の責務 |
| 請求処理 | finance の責務 |

## 設計方針

```kotlin
// Good: Read Only の Query
fun getDailyStats(date: LocalDate): FuneralDailyStats
fun getMonthlyReport(yearMonth: YearMonth): FuneralMonthlyReport

// Bad: 更新を含む
fun save(report: Report)  // NG
```

# TaskStatus - 共通ステータス一覧

## 設計方針

- **業務非依存**：状態名は汎用的な用語を使用
- **最小構成**：必要十分な状態数に抑える
- **拡張抑制**：安易に状態を増やさない

## ステータス一覧

| Status | 日本語名 | 説明 |
|--------|---------|------|
| `DRAFT` | 下書き | 作成中。まだ依頼されていない |
| `REQUESTED` | 依頼済 | 依頼が発行された。作業待ち |
| `IN_PROGRESS` | 作業中 | 担当者が作業を開始した |
| `APPROVED` | 承認済 | 承認された（承認系 Task の場合） |
| `REJECTED` | 却下 | 却下された（承認系 Task の場合） |
| `COMPLETED` | 完了 | 作業が正常に完了した |
| `CANCELLED` | 取消 | 依頼自体が取り消された |

## 状態の意味

### DRAFT（下書き）

- Task が作成されたが、まだ正式な依頼ではない
- 作成者のみが編集可能
- いつでも削除可能

### REQUESTED（依頼済）

- 正式な依頼として発行された
- 担当者に通知される
- 作業開始を待っている状態

### IN_PROGRESS（作業中）

- 担当者が作業を開始した
- 他の担当者による同時作業を防ぐロック的な役割

### APPROVED（承認済）

- 承認系 Task で、承認者が承認した
- 後続処理のトリガーとなる

### REJECTED（却下）

- 承認系 Task で、承認者が却下した
- 理由の記録が必須

### COMPLETED（完了）

- Task の目的が達成された
- 確認系 Task の正常終了
- 最終状態（変更不可）

### CANCELLED（取消）

- 依頼自体が不要になった
- 作業開始前にのみ可能
- 最終状態（変更不可）

## 承認系 vs 確認系

Task には「承認系」と「確認系」の 2 つの用途がある。
**状態は共通**、用途で区別する。

| 用途 | 使用する状態 | 例 |
|------|-------------|-----|
| 承認系 | DRAFT → REQUESTED → IN_PROGRESS → APPROVED/REJECTED | 契約承認、支払承認 |
| 確認系 | DRAFT → REQUESTED → IN_PROGRESS → COMPLETED | 書類確認、データチェック |

## 禁止事項

### やってはいけない状態追加

```
❌ PENDING_MANAGER_APPROVAL  → 承認段階ごとに状態を増やさない
❌ GOJO_REVIEW               → 業務名を状態に入れない
❌ WAITING_FOR_DOCUMENT      → 待機理由ごとに状態を増やさない
❌ PARTIALLY_APPROVED        → 中間状態を増やさない
```

### 正しい対応方法

| 要件 | 状態を増やす ❌ | 正しい対応 ✅ |
|------|---------------|--------------|
| 多段階承認 | LEVEL1_APPROVED, LEVEL2_APPROVED | Task を複数作成 |
| 業務別の状態 | GOJO_PENDING | TaskType で区別 |
| 待機理由の記録 | WAITING_FOR_XXX | metadata に記録 |

## Kotlin 実装例

```kotlin
package nexus.common.task

enum class TaskStatus {
    DRAFT,
    REQUESTED,
    IN_PROGRESS,
    APPROVED,
    REJECTED,
    COMPLETED,
    CANCELLED;

    fun isTerminal(): Boolean = this in setOf(APPROVED, REJECTED, COMPLETED, CANCELLED)
    fun isActive(): Boolean = !isTerminal()
}
```

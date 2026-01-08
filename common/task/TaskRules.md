# TaskRules - 責務と制約

## Task の責務

### やること

| 責務 | 説明 |
|------|------|
| 状態の管理 | DRAFT → REQUESTED → ... の遷移を管理 |
| 担当者の記録 | 誰が作業するかを記録 |
| 期限の管理 | いつまでに完了すべきかを管理 |
| 判断の記録 | 承認/却下の結果と理由を記録 |
| 履歴の保持 | 状態変更の履歴を保持 |

### やらないこと

| 禁止事項 | 理由 |
|---------|------|
| **業務データの更新** | 業務ドメインの責務 |
| **会計仕訳の作成** | accounting の責務 |
| **支払処理の実行** | payment の責務 |
| **マスタデータの更新** | 各ドメインの責務 |
| **外部システム連携** | 統合層の責務 |

## Task と業務データの関係

```
┌─────────────────────────────────────────────────────┐
│                    業務ドメイン                      │
│  ┌─────────────┐    ┌─────────────┐                │
│  │  業務データ  │    │    Task     │                │
│  │  (Contract)  │◄───│  (参照のみ)  │                │
│  └─────────────┘    └─────────────┘                │
│         │                  │                        │
│         │ 更新             │ 状態変更               │
│         ▼                  ▼                        │
│  ┌─────────────┐    ┌─────────────┐                │
│  │   Service    │    │ TaskService │                │
│  │  (業務処理)  │    │ (状態管理)  │                │
│  └─────────────┘    └─────────────┘                │
└─────────────────────────────────────────────────────┘
```

### 原則

1. **Task は業務データを参照するが、更新しない**
2. **業務データの更新は、Task の状態変更を契機に業務サービスが行う**
3. **Task は「何を承認したか」を記録し、「どう処理したか」は記録しない**

### 例：契約承認

```
1. 契約データ作成（業務）  → Contract: status=PENDING
2. 承認 Task 作成         → Task: status=DRAFT, targetId=Contract.id
3. Task 依頼              → Task: status=REQUESTED
4. Task 承認              → Task: status=APPROVED
5. 契約ステータス更新（業務）→ Contract: status=ACTIVE  ← Task ではなく業務が更新
```

## Task と会計（accounting）の関係

### 絶対ルール

**Task で仕訳を作らない。**

```
❌ 禁止
Task.onApproved() {
    accountingService.createJournalEntry(...)  // Task から仕訳を作成
}

✅ 正しい設計
Task.onApproved() {
    // Task は状態変更のみ
}

// 別のサービスが Task の状態を監視し、必要に応じて会計イベントを発行
CommissionService.onTaskApproved(task) {
    commissionEvent.emit(...)  // Fact を発行
}

// accounting が Fact を受け取り、仕訳を作成
AccountingService.onCommissionEvent(event) {
    journalEntryRepository.save(...)  // ここで初めて仕訳
}
```

### 会計との境界

```
┌──────────────────┐     ┌──────────────────┐     ┌──────────────────┐
│      Task        │     │   業務ドメイン    │     │   accounting     │
│   ───────────    │     │   ───────────    │     │   ───────────    │
│   状態管理       │────▶│   Fact 発行      │────▶│   仕訳作成       │
│   承認/却下      │     │   業務処理       │     │   勘定科目管理   │
│                  │     │                  │     │                  │
│   ※判断のみ      │     │   ※事実を記録    │     │   ※仕訳を作る    │
└──────────────────┘     └──────────────────┘     └──────────────────┘
```

## Task 完了の定義

### 承認系 Task

| 状態 | 完了か | 後続処理 |
|------|--------|---------|
| APPROVED | 完了 | 業務サービスが後続処理を実行 |
| REJECTED | 完了 | 却下理由を記録して終了 |

### 確認系 Task

| 状態 | 完了か | 後続処理 |
|------|--------|---------|
| COMPLETED | 完了 | 必要に応じて次の Task を作成 |

### 共通

| 状態 | 完了か | 後続処理 |
|------|--------|---------|
| CANCELLED | 完了 | なし（依頼自体が取消） |

## 禁止パターン

### 1. Task 内で業務更新

```kotlin
// ❌ 禁止
class ApprovalTask {
    fun approve() {
        this.status = APPROVED
        contract.status = ACTIVE  // Task が業務データを更新している
    }
}
```

### 2. Task 内で会計処理

```kotlin
// ❌ 禁止
class PaymentApprovalTask {
    fun approve() {
        this.status = APPROVED
        accountingService.createJournalEntry(  // Task が仕訳を作成している
            debit = "費用",
            credit = "未払金"
        )
    }
}
```

### 3. ドメイン固有の Task ルール

```kotlin
// ❌ 禁止
// gojo/task/GojoTaskStateMachine.kt
object GojoTaskStateMachine {
    // gojo 独自の遷移ルール
}
```

## 正しいパターン

### Task は状態管理のみ

```kotlin
// ✅ 正しい
class Task {
    fun approve(approver: UserId, reason: String) {
        require(TaskStateMachine.canTransition(status, APPROVED))
        this.status = APPROVED
        this.approvedBy = approver
        this.approvedAt = Instant.now()
        this.approvalReason = reason
        // 業務更新・会計処理はここでは行わない
    }
}
```

### 業務サービスが Task の結果を利用

```kotlin
// ✅ 正しい
class ContractApprovalService {
    fun handleTaskApproved(task: Task) {
        val contract = contractRepository.findById(task.targetId)
        contract.activate()  // 業務サービスが業務データを更新
        contractRepository.save(contract)

        // 必要に応じて Fact を発行
        eventPublisher.publish(ContractActivatedEvent(contract.id))
    }
}
```

## チェックリスト

Task 実装時に確認すること：

- [ ] Task は状態遷移のみを管理しているか
- [ ] Task から業務データを直接更新していないか
- [ ] Task から会計仕訳を作成していないか
- [ ] 状態遷移ルールは common/task を参照しているか
- [ ] ドメイン固有の状態を追加していないか

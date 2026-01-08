# gojo/task

## 責務

**互助会業務のワークフロー・承認管理**。

- 加入承認フロー
- 変更承認フロー
- タスクの割当・進捗管理

## Task 共通モジュール参照

**状態定義・遷移ルールは共通モジュールを参照する。**

```
nexus/common/task/
├── TaskStatus.md        # 状態一覧
├── TaskStateMachine.md  # 遷移ルール
└── TaskRules.md         # 責務と制約
```

参照: [common/task/README.md](../../../../../../common/task/README.md)

### このモジュールでやること

- Task の **作成・利用** のみ
- 互助会固有の Task 種別（加入承認、変更承認など）の定義

### このモジュールでやらないこと

- Task の状態定義（共通モジュールを参照）
- Task の遷移ルール定義（共通モジュールを参照）
- 独自の TaskStatus 作成

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| **common/task** | 参照 | 状態・遷移ルール |
| gojo/enrollment | 連携 | 加入承認タスク |
| gojo/change | 連携 | 変更承認タスク |
| identity | 参照 | 承認者情報 |

## 禁止事項

| 禁止 | 理由 |
|------|------|
| **独自の TaskStatus 定義** | common/task を参照する |
| **独自の遷移ルール定義** | common/task を参照する |
| 契約の直接更新 | contract 経由で行う |
| 会計仕訳の作成 | accounting の責務 |
| 顧客情報の更新 | identity の責務 |
| 業務ロジックの実装 | ワークフロー管理に徹する |

## 実装例

```kotlin
// ✅ 正しい使い方
import nexus.common.task.TaskStatus
import nexus.common.task.TaskStateMachine

class GojoEnrollmentTask(
    val id: TaskId,
    val enrollmentId: EnrollmentId,
    var status: TaskStatus = TaskStatus.DRAFT  // 共通の状態を使用
) {
    fun request() {
        status = TaskStateMachine.transition(status, TaskStatus.REQUESTED)
    }
}

// ❌ やってはいけない
enum class GojoTaskStatus { ... }  // gojo 独自の状態を作らない
```

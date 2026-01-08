# common/task

## 概要

業務タスク（承認・確認・作業管理）の **状態遷移とルール** を一元管理するモジュール。

## なぜ Task を共通化するのか

1. **ルールの一貫性**
   - 全ドメインで同じ状態遷移ルールを適用
   - 「承認済みから差し戻し可能か」などの判断が統一される

2. **保守性**
   - 状態遷移の変更が 1 か所で完結
   - ドメインごとに異なるルールが生まれない

3. **テスト容易性**
   - Task のロジックを独立してテスト可能
   - ドメインロジックと分離

## なぜ各ドメインで状態を持たせないのか

```
❌ 悪い例：ドメインごとに状態を定義
gojo/task/GojoTaskStatus.kt      → GOJO_PENDING, GOJO_APPROVED...
funeral/task/FuneralTaskStatus.kt → FUNERAL_WAITING, FUNERAL_DONE...
agent/task/AgentTaskStatus.kt    → AGENT_REVIEW, AGENT_CONFIRMED...
```

問題点：
- 状態名がバラバラになる
- 遷移ルールが分散する
- 共通処理が書けない

```
✅ 正しい設計：共通状態を参照
common/task/TaskStatus          → DRAFT, REQUESTED, IN_PROGRESS...
gojo   → 参照のみ
funeral → 参照のみ
agent  → 参照のみ
```

## 将来の拡張

このモジュールは以下を想定して設計されている：

- **別サービスへの切り出し**：Task を独立したマイクロサービスにできる
- **ワークフローエンジン連携**：Camunda、Temporal 等への移行が容易
- **監査ログ統合**：Task の状態変更を一元的に記録可能

## ファイル構成

| ファイル | 内容 |
|---------|------|
| [TaskStatus.md](./TaskStatus.md) | 共通ステータス一覧 |
| [TaskStateMachine.md](./TaskStateMachine.md) | 状態遷移ルール |
| [TaskRules.md](./TaskRules.md) | 責務と制約 |

## 利用方法

各ドメインは Task の状態定義を **参照のみ** する。

```kotlin
// ✅ 正しい使い方
import nexus.common.task.TaskStatus

class GojoEnrollmentTask(
    val status: TaskStatus = TaskStatus.DRAFT
)

// ❌ やってはいけない
enum class GojoTaskStatus { ... }  // ドメイン固有の状態を作らない
```

## 関連ドキュメント

- [gojo/task/README.md](../../backend/nexus-gojo/src/main/kotlin/nexus/gojo/task/README.md)
- [funeral/README.md](../../backend/nexus-funeral/README.md)
- [agent/README.md](../../backend/nexus-agent/README.md)

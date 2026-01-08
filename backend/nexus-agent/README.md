# nexus-agent

## 役割

**代理店・業務委託を管理するトップドメイン**。

agent は「販売・請負を行う主体」を表す **制度ドメイン** であり、
特定業務（互助会・葬祭）に依存しない。

- **業務委託契約**: 代理店との契約管理
- **案件割当**: 代理店への案件アサイン
- **報酬・支給**: 成果報酬・手数料の管理

## 内部サブドメイン

```
nexus-agent/
├── contract/      # 業務委託契約
├── assignment/    # 案件割当
└── commission/    # 支給・報酬
```

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| gojo | 参照 | 互助会契約の紹介元としてagent参照 |
| funeral | 参照 | 葬祭案件の請負元としてagent参照 |
| payment | イベント送信 | 報酬支払のFactをpaymentへ |
| accounting | イベント送信 | 会計イベントをaccountingへ |

## やってはいけないこと

| 禁止事項 | 理由 |
|---------|------|
| 互助会業務ロジックの実装 | agent は制度ドメイン。業務は gojo で |
| 葬祭業務ロジックの実装 | agent は制度ドメイン。業務は funeral で |
| 会計仕訳の作成 | 会計は accounting の責務 |
| 勘定科目・借方貸方の保持 | 金銭 Fact のみ。仕訳化は accounting で |
| gojo / funeral への依存 | agent は特定業務に依存しない |

## 設計方針

### なぜトップドメインか

agent は互助会営業に強く結びついているが、以下を見据えトップドメインとして扱う：

1. **葬祭請負**: 将来的に葬祭の外注先管理に拡張
2. **他業務委託**: 冠婚・ポイント等への展開可能性
3. **制度の独立性**: 代理店制度は業務横断の概念

### gojo のサブドメインにしない理由

```
❌ gojo/agent/     → 互助会専用に見える
✓ agent/          → 業務横断の制度ドメイン
```

## 依存関係

```
nexus-agent
    └── nexus-core （ID, VO, 例外）
```

## 会計連携

```kotlin
// agent で発生する会計イベント（Fact）
data class CommissionPaidFact(
    val agentId: AgentId,
    val amount: Money,
    val paidAt: LocalDateTime
)

// → accounting で仕訳化（agent では仕訳を作らない）
```

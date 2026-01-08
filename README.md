# NEXUS

冠婚葬祭互助会事業の基幹業務を統合する社内システム

---

## 1. NEXUS とは

### 名称の意味

NEXUS（ネクサス）= **結節点・中核**

互助会・葬祭・冠婚・ポイントなど、複数の業務ドメインを"つなぐ"中核システムとして位置付ける。

### 目的

- 顧客情報の一元管理（名寄せ）
- 業務ドメイン間のデータ連携
- 会計処理の自動化
- 既存システムからの段階的移行

### 対象業務

| 業務 | 概要 |
|------|------|
| 互助会（gojo） | 契約・積立・権利管理 |
| 葬祭（funeral） | 受付・施行・請求 |
| 冠婚（bridal） | 予約・施行・請求 |
| ポイント（point） | 付与・利用・残高管理 |

---

## 2. システム全体構成

### 構成図

```
nexus/
├── backend/           # Kotlin + Spring Boot（業務ロジック・API）
├── frontend/          # Next.js + TypeScript（画面）
└── common/            # 業務横断の定義（Task 状態など）
```

### backend と frontend の役割

| 層 | 役割 |
|-----|------|
| backend | 業務ロジック・データ永続化・API 提供 |
| frontend | 画面表示・ユーザー操作・BFF 経由のデータ取得 |

### モノレポ構成の理由

1. **一貫性**: 全ドメインで同じ規約・バージョンを維持
2. **依存制御**: Gradle モジュール依存で参照方向を強制
3. **複雑性回避**: 初期段階でマイクロサービス化しない

### 技術選定

| 層 | 技術 | 選定理由 |
|-----|------|---------|
| Backend | Kotlin + Spring Boot 3.x | 型安全性・エコシステムの成熟・社内実績 |
| Frontend | Next.js 14 + TypeScript | App Router による構造化・型安全性 |
| DB | PostgreSQL（本番）/ H2（開発） | 信頼性・運用実績 |

---

## 3. ドメイン構成と責務

### モジュール一覧

```
backend/
├── nexus-core         # 共通基盤
├── nexus-identity     # 人物管理
├── nexus-household    # 世帯管理
├── nexus-group        # 法人横断検索
├── nexus-gojo         # 互助会
├── nexus-funeral      # 葬祭
├── nexus-bridal       # 冠婚
├── nexus-point        # ポイント
├── nexus-agent        # 代理店
├── nexus-payment      # 請求・入金
├── nexus-accounting   # 会計・仕訳
├── nexus-reporting    # 帳票・集計
├── nexus-api          # 外部公開 API
└── nexus-batch        # バッチ処理
```

### 共通・横断ドメイン

| モジュール | 責務 | 持たないもの |
|-----------|------|-------------|
| core | ID 定義・ValueObject・共通例外 | Spring 依存・業務ロジック |
| identity | Person 管理・名寄せロジック | 契約情報・ポイント情報 |
| household | 世帯構成・家族関係管理 | 個人詳細・業務データ |
| group | 全法人横断の検索（Read Only） | 更新処理・業務ロジック |
| api | BFF・外部向け API 提供 | 業務ロジック（委譲のみ） |
| batch | データ取込・名寄せ補助 | リアルタイム処理 |

### 業務ドメイン

| モジュール | 責務 | 持たないもの |
|-----------|------|-------------|
| gojo | 互助会契約・積立・権利管理 | 施行詳細・会計仕訳 |
| funeral | 葬祭の受付・施行・請求 | 互助会契約管理・会計仕訳 |
| bridal | 冠婚の予約・施行・請求 | 互助会契約管理・会計仕訳 |
| point | ポイント付与・利用・残高 | 契約情報・施行情報 |

### 横断業務

| モジュール | 責務 | 持たないもの |
|-----------|------|-------------|
| agent | 代理店契約・案件割当・報酬 | 業務固有ロジック |
| payment | 請求・入金・消込 | 会計仕訳（accounting の責務） |
| accounting | 会計仕訳の生成・管理 | 業務ロジック・業務データ参照 |
| reporting | 帳票・集計（Read Only） | 登録・更新処理 |

### 依存方向

```
nexus-core（誰にも依存しない）
    ↑
identity / household / gojo / funeral / ...（core に依存）
    ↑
nexus-api / nexus-batch（業務モジュールを束ねる）
```

---

## 4. gojo と funeral の構成差について

### 現状の構成

| ドメイン | 構成 | サブドメイン例 |
|---------|------|---------------|
| gojo | トップドメイン + サブドメイン | contract, enrollment, change, task |
| funeral | サブドメイン構成 | reception, operation, logistics, finance |

### gojo がトップドメインである理由

1. **業務の核**: 互助会契約は葬祭・冠婚の前提となる
2. **参照方向**: funeral/bridal が gojo を参照する（逆はない）
3. **複雑性**: 契約・積立・権利という独立した概念を持つ

### funeral がサブドメイン構成である理由

1. **工程の多さ**: 受付→施行→物流→精算と工程が複雑
2. **将来の分割**: サブドメイン単位で切り出す可能性がある
3. **チーム分業**: 工程ごとに担当を分けやすい

### 変更可能性

この構成は現時点の判断であり、将来変更可能。
ただし変更時は本 README を更新し、理由を明記すること。

---

## 5. Task / State / Phase 設計

### 基本思想

業務は「タスク（Task）」として扱い、状態遷移で管理する。

### State と Phase の分離

| 概念 | 役割 | 例 |
|------|------|-----|
| State | タスクの承認状態 | DRAFT → REQUESTED → APPROVED |
| Phase | 業務の進行段階 | 受付 → 見積 → 契約 → 施行 |

**分離の理由**:
- State は「承認されたか」を表す（システム共通）
- Phase は「業務がどこまで進んだか」を表す（業務固有）
- 書類のやり取り・進捗は Phase で管理する

### 状態遷移ルール

```
DRAFT → REQUESTED → IN_PROGRESS → APPROVED / REJECTED / COMPLETED
                                ↘ CANCELLED
```

### Task の責務

- Task は「承認した」という事実のみを記録
- 業務データの直接更新は行わない
- 業務データの更新は業務サービスが行う

### 重要ルール

1. 各ドメインで独自の State を定義しない（`common/task/TaskStatus` を参照）
2. State の巻き戻しは禁止（やり直しは新しい Task を作成）
3. Phase はドメインごとに定義してよい

---

## 6. 会計・請求の考え方

### 基本思想

**業務イベントと会計仕訳を分離する**

```
業務ドメイン → 会計イベント（Fact）発行 → accounting → 仕訳作成
```

### 仕訳を即時生成しない理由

1. **業務と会計の独立性**: 業務ルール変更が会計に波及しない
2. **柔軟な仕訳ルール**: 仕訳ルールは accounting で一元管理
3. **バッチ処理との親和性**: 月次締め・一括処理に対応しやすい

### 責務分担

| モジュール | 責務 |
|-----------|------|
| 業務ドメイン（gojo, funeral 等） | 「何が起きたか」を Fact として発行 |
| payment | 請求・入金の記録（仕訳は作らない） |
| accounting | Fact を受け取り、仕訳ルールに従って仕訳を生成 |

### 絶対ルール

- **会計仕訳を作成できるのは `nexus-accounting` のみ**
- accounting は業務ドメインの Entity/Repository を直接参照しない
- 業務ドメインは JournalEntry を作成しない

---

## 7. 設計原則

### ドメイン境界を越えた直接参照は禁止

```kotlin
// ❌ 禁止
class FuneralService(
    private val gojoContractRepository: GojoContractRepository
)

// ✅ 正しい
class FuneralService(
    private val gojoQueryService: GojoQueryService  // 公開された参照用サービス
)
```

### 状態を if 文で持たない

```kotlin
// ❌ 禁止
if (contract.status == "ACTIVE" && contract.paidAmount >= contract.targetAmount) {
    // 満期判定
}

// ✅ 正しい
if (contract.isMatured()) {
    // Entity にロジックを持たせる
}
```

### 将来のために「今不要な実装」は入れない

- 使わない引数・メソッドは作らない
- 「いつか使うかも」は実装しない
- 必要になったら追加する

### 既存業務を壊さず段階移行する

- 既存システムとの並行稼働を前提とする
- 一括移行ではなく、機能単位で段階移行
- データ移行は batch モジュールで対応

---

## 8. この README の位置付け

### 目的

この README は **実装詳細ではなく、判断基準を書く文書** である。

- 「どこに何を置くか」の指針
- 「なぜこの構成か」の理由
- 「何をしてはいけないか」の制約

### 使い方

1. 実装判断に迷ったら、まずこの README を読む
2. README に記載がなければ、チームで議論して追記する
3. README に反する実装が必要な場合は、README を更新してから実装する

### 更新ルール

- ルールを破る場合は、必ずこの README を更新し、理由を記載する
- 「例外」を増やすのではなく、ルール自体を見直す
- 更新時は PR でレビューを受ける

---

## 付録: 開発環境

### 必要条件

- JDK 21
- Node.js 20+
- Gradle 8.x

### 起動方法

```bash
# Backend
cd backend
./gradlew :nexus-api:bootRun

# Frontend
cd frontend
npm install
npm run dev
```

### 開発用 URL

| サービス | URL |
|---------|-----|
| API | http://localhost:8080 |
| Frontend | http://localhost:3000 |

---

## 付録: 関連ドキュメント

| ドキュメント | 内容 |
|-------------|------|
| [common/task/README.md](common/task/README.md) | Task 共通設計 |
| [backend/nexus-gojo/README.md](backend/nexus-gojo/README.md) | 互助会ドメイン詳細 |
| [backend/nexus-funeral/README.md](backend/nexus-funeral/README.md) | 葬祭ドメイン詳細 |
| [backend/nexus-accounting/README.md](backend/nexus-accounting/README.md) | 会計ドメイン詳細 |

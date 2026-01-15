# NEXUS 設計原則・ドメイン境界

本文書は NEXUS のドメイン分割思想・責務境界・禁止事項を定義する。
実装判断に迷った場合は本文書を参照すること。

---

## 1. ドメイン分割の判断軸

### 基本思想

ドメインは **機能単位ではなく「業務の性質」** で分割する。

| 業務の性質 | 分割単位 | 例 |
|-----------|---------|-----|
| 長期・制度的・反復的 | トップドメイン | gojo（互助会契約） |
| 案件単位・単発・工程管理 | サブドメイン | funeral/reception, funeral/operation |

### 分割判断の基準

1. **ライフサイクルの長さ**: 数年にわたる管理 → トップドメイン
2. **独立したルール体系**: 固有の制度・計算ロジックを持つ → トップドメイン
3. **他ドメインからの参照**: 複数ドメインから参照される → トップドメイン
4. **工程・フェーズの複雑さ**: 複数工程を持つ案件型 → サブドメイン構成

### 同一機能名でも境界が異なる例

| 機能名 | gojo での扱い | funeral での扱い |
|--------|--------------|-----------------|
| 契約 | トップドメインの中核概念 | 案件内の一工程（reception） |
| 入金 | 積立として長期管理 | 案件精算として短期処理 |
| マスタ | 互助会制度マスタ | 葬祭サービスマスタ |

**重要**: 機能名が同じでも、業務の性質により配置先が異なる。

---

## 2. トップドメイン一覧

### 共通基盤

#### nexus-core

| 項目 | 内容 |
|------|------|
| 主責務 | ID 定義・Value Object・共通例外・純粋関数ユーティリティ |
| 保持してよい情報 | 型定義、バリデーションルール、共通エラー定義 |
| 禁止事項 | Spring 依存、JPA Entity、Repository、DI コンテナ依存、外部ライブラリ依存 |

#### nexus-identity

| 項目 | 内容 |
|------|------|
| 主責務 | Person（個人）の実体管理、名寄せキーの管理、重複検知 |
| 保持してよい情報 | 氏名・住所・連絡先・生年月日など個人の基本属性 |
| 禁止事項 | 契約情報、業務判断ロジック、ポイント残高、案件情報 |

#### nexus-household

| 項目 | 内容 |
|------|------|
| 主責務 | 世帯構成・家族関係の管理 |
| 保持してよい情報 | 世帯 ID、構成員の関係性、世帯属性 |
| 禁止事項 | 個人詳細（identity の責務）、業務データ、契約情報 |

#### nexus-group

| 項目 | 内容 |
|------|------|
| 主責務 | 全法人横断の検索・統合ビュー提供（Read Only） |
| 保持してよい情報 | 検索インデックス、集約ビュー（キャッシュ的位置付け） |
| 禁止事項 | **マスタの正を持つこと**、登録・更新処理、業務ロジック |

**identity と group の責務分離**:
- identity: Person / Household / 名寄せキーの **実体（正）** を管理
- group: 法人横断の **検索・参照ビュー** を提供（正は持たない）

---

### 業務ドメイン

#### nexus-gojo（互助会）

| 項目 | 内容 |
|------|------|
| 主責務 | 互助会契約の作成・積立管理・権利管理 |
| 保持してよい情報 | 契約情報、積立履歴、権利状態、満期判定ロジック |
| 禁止事項 | 施行詳細、会計仕訳の直接作成、identity/household の更新 |

#### nexus-funeral（葬祭）

| 項目 | 内容 |
|------|------|
| 主責務 | 葬祭案件の受付・施行管理・工程管理 |
| 保持してよい情報 | 案件情報、施行記録、工程状態、見積・請求データ |
| 禁止事項 | **独自の会計処理・請求完結**、互助会契約の作成、identity/household の更新 |

#### nexus-bridal（冠婚）

| 項目 | 内容 |
|------|------|
| 主責務 | 冠婚案件の予約・施行管理 |
| 保持してよい情報 | 案件情報、予約情報、施行記録 |
| 禁止事項 | 独自の会計処理、互助会契約の作成、identity/household の更新 |

#### nexus-point（ポイント）

| 項目 | 内容 |
|------|------|
| 主責務 | ポイント付与・利用・残高管理 |
| 保持してよい情報 | ポイント口座、付与履歴、利用履歴、残高 |
| 禁止事項 | 契約情報、施行情報、業務固有の付与ルール（リクエストを受けるのみ） |

---

### 横断業務

#### nexus-agent（代理店）

| 項目 | 内容 |
|------|------|
| 主責務 | 代理店契約・案件割当・報酬計算 |
| 保持してよい情報 | 代理店情報、割当履歴、報酬計算結果 |
| 禁止事項 | 業務固有ロジック（gojo/funeral の内部処理）、会計仕訳の作成 |

#### nexus-payment（請求・入金）

| 項目 | 内容 |
|------|------|
| 主責務 | 請求書発行・入金記録・消込管理 |
| 保持してよい情報 | 請求データ、入金履歴、消込状態 |
| 禁止事項 | **契約条件・割引ロジック**、会計仕訳の作成、業務判断 |

**重要**: payment は「いくら請求するか」を決めない。業務ドメインが決定した金額を記録・管理するのみ。

#### nexus-accounting（会計）

| 項目 | 内容 |
|------|------|
| 主責務 | 会計仕訳の生成・管理（唯一の仕訳作成権限） |
| 保持してよい情報 | 仕訳データ、勘定科目マスタ、仕訳ルール |
| 禁止事項 | **業務イベント起点の即時仕訳判断**、業務ロジック、業務データの直接参照 |

**重要**: accounting は Fact を受け取り、仕訳ルールに従って仕訳を生成する。「いつ・どのタイミングで仕訳するか」の判断は accounting が持つ（月次締め等）。

#### nexus-reporting（帳票・集計）

| 項目 | 内容 |
|------|------|
| 主責務 | 帳票生成・集計・分析（Read Only） |
| 保持してよい情報 | 集計結果（キャッシュ）、帳票テンプレート |
| 禁止事項 | 登録・更新処理、業務判断、マスタの正の保持 |

---

## 3. 命名ポリシー

### finance を採用しない理由

「finance」は以下の責務を曖昧に含み得るため、採用しない：

- 請求・入金管理 → **payment** で明確化
- 会計仕訳 → **accounting** で明確化
- 財務分析 → **reporting** で明確化

「finance」を使うと、どこまでが責務か曖昧になり、境界侵犯の温床となる。

### accounting / payment 分離の思想

| モジュール | 責務の本質 | 扱うデータ |
|-----------|-----------|-----------|
| payment | 「お金のやり取り」の記録 | 請求・入金・消込 |
| accounting | 「帳簿上の記録」の作成 | 仕訳・勘定科目 |

分離理由：
1. **責務の明確化**: 入金管理と仕訳作成は異なる関心事
2. **タイミングの違い**: 入金は即時記録、仕訳は締め単位で生成し得る
3. **変更影響の局所化**: 会計基準変更が payment に波及しない

---

## 4. 設計原則

### ドメイン境界を越えた直接参照は禁止

```kotlin
// 禁止: 他ドメインの Repository を直接利用
class FuneralService(
    private val gojoContractRepository: GojoContractRepository  // NG
)

// 正しい: 公開された参照用サービスを利用
class FuneralService(
    private val gojoQueryService: GojoQueryService  // OK
)
```

### 状態を if 文で持たない

```kotlin
// 禁止: 判定ロジックが散在
if (contract.status == "ACTIVE" && contract.paidAmount >= contract.targetAmount) { ... }

// 正しい: Entity にロジックを集約
if (contract.isMatured()) { ... }
```

### 今不要な実装は入れない（YAGNI）

- 使わない引数・メソッドは作らない
- 「いつか使うかも」は実装しない
- 必要になったら追加する


### ドメインと永続化の分離（移行中）

**現状**: 一部のドメインエンティティにJPAアノテーションが含まれている（移行負債として認識）。

**目標**: ドメインモデルと永続化モデルを完全に分離する。

**方針**:
 新しいドメインエンティティは永続化に依存しない（JPAアノテーションなし）
- 永続化エンティティは infrastructure 層に配置
- マッピングは infrastructure 層で実装
- 既存エンティティは機会を見て段階的に移行

詳細は [ai-rules.md](./ai-rules.md) の「Transitional Policy: JPA Annotations in Domain Entities」を参照。

### 永続化戦略：JPA と JDBC の併用

**目的**  
性能と保守性を両立し、一覧・検索・帳票などにおける SQL の表現力と実行性能を確保する。

過去の PoC において、SQL*Plus では数秒で完了する SQL が、
JPA を介した実装では数十秒を要する事例が確認された。
特に、集計・横断検索・画面表示用 DTO に直結する SQL においては、
JPA のマッピングコストやライフサイクル管理がボトルネックとなる場合がある。

本システムではこれを踏まえ、**JPA と JDBC を用途に応じて意図的に使い分ける**。

**基本方針**
- Domain / Application / API 層は JPA / JDBC を知らない（技術非依存）
- Repository インターフェースは Domain 層に配置する
- Repository 実装（JPA 版 / JDBC 版）は Infrastructure 層に配置する
- 永続化方式の選択は Infrastructure 層の責務とする

**選択基準**

| 用途 | 推奨技術 | 理由 |
|------|----------|------|
| CRUD・状態遷移・集約の永続化 | **JPA** | エンティティのライフサイクル管理が容易、トランザクション境界が明確 |
| 一覧・検索・帳票・横断参照 | **JDBC（推奨）** | 大きな JOIN / 集計 / DTO 直結 SQL の表現力と性能を確保 |
| 性能要件が厳しい／SQL 主導 | **JDBC 優先** | SQL 最適化の自由度が高い |
| N+1 やマッピングコストが疑われる場合 | **JDBC へ切替可能** | 性能事故を回避 |

**実装パターン（例）**

```kotlin
// Domain 層: 技術非依存な契約（interface のみ）
interface GojoContractRepository {
    fun save(contract: Contract): Contract
    fun findByRegion(
        regionId: String,
        page: Int,
        size: Int
    ): PaginatedResult<Contract>
}

// Infrastructure 層: 実装例（方式は用途に応じて選択）
@Repository
class GojoContractRepositoryImpl(
    private val jpaContractRepository: JpaContractRepository, // JPA 実装
    private val dbConnectionProvider: DbConnectionProvider     // JDBC 実装
) : GojoContractRepository {

    override fun save(contract: Contract): Contract =
        jpaContractRepository.save(contract)

    override fun findByRegion(
        regionId: String,
        page: Int,
        size: Int
    ): PaginatedResult<Contract> {
        // JDBC による一覧取得（詳細は Infrastructure に閉じる）
        TODO("Implement JDBC query")
    }
}
```

※ 上記は一例であり、JPA 実装 / JDBC 実装をクラス分離する構成も許容する。

**やってはいけないこと**
- Domain / Application / API 層に `JdbcTemplate` や `EntityManager` を持ち込まない
- API（nexus-api）が DataSource / JPA 設定を所有しない
- JPA を理由に巨大 SQL を Entity に無理にマッピングしない（一覧系は DTO を使用）
- 一覧・帳票処理で JPA の N+1 問題を放置しない（JDBC への切替を検討）

**モジュール別推奨**

| モジュール | 推奨 | 理由 |
|------------|------|------|
| group（integration） | **JDBC ファースト** | 法人横断検索・集計が主用途で、大きな JOIN が多い |
| gojo | **用途により併用** | CRUD は JPA、一覧（local / all）は JDBC も可 |
| funeral / bridal | **用途により併用** | 案件管理は JPA、帳票・集計は JDBC |

**判断に迷った場合**
1. 性能要件が不明確 → JPA から開始（後で JDBC に切替可能）
2. JOIN が多い／集計関数が多い → JDBC を検討
3. 画面 DTO と 1:1 で返却したい → JDBC
4. 状態遷移・整合性管理が主目的 → JPA

 ---

## 5. 本文書の位置付け

### 使い方

1. 新機能の配置先に迷ったら、まず本文書を確認する
2. 禁止事項に該当しないか確認する
3. 記載がなければチームで議論し、本文書を更新する

### 更新ルール

- ルールを破る場合は、必ず本文書を更新し理由を記載する
- 「例外」を増やすのではなく、原則自体を見直す
- 更新時は PR でレビューを受ける

---

## 関連ドキュメント

| ドキュメント | 内容 |
|-------------|------|
| [README.md](../../README.md) | システム概要・判断基準 |
| [dependency-rules.md](./dependency-rules.md) | 依存ルール・会計連携・Task 設計 |
| [overview.md](./overview.md) | 全体像・データの流れ |

# NEXUS Project Roadmap

## 目的

本ドキュメントは、NEXUS プロジェクト全体のフェーズ構成（P0〜P2）と現在地を明確にし、
設計憲法（nexus-design-constitution.md）とは役割を分離した「進行管理・合意用ロードマップ」を提供する。

* **設計憲法**：不変の原則・思想（Why / What）
* **ロードマップ**：進行計画・現在地（When / Where）

---

## 全体フェーズ概要（P0〜P2）

### P0（完了）— 壊れない土台

* PoC 完了
* モジュール分離（domain / infrastructure / bff）
* Read / Write 導線分離
* RegionContext / RoutingDataSource 確立
* アーキテクチャテスト導入

**成果**：

* 設計的に「壊れにくい」構造が確立

---

### P04（完了）— JDBC Read 実戦投入

* JDBC Read 導線の本格導入
* SQL を正とした Read 設計
* DTO / RowMapper / Controller 再設計
* JOIN なしでの検索成立
* Oracle 実 DB を前提とした確認準備
* Keycloak Claim による権限制御設計確定

**位置づけ**：

* P1 に進むための「検索基盤の確定」フェーズ

---

### P1（現在進行中）— 業務要件との接続

* JOIN の段階的復活
* 表示要件の確定（業務と合意）
* パフォーマンスチューニング
* Count / Search SQL 最適化
* 不要カラム・条件の整理
* Keycloak Claim による権限制御実装

---

### P2（予定）— 本番運用準備

* Frontend 本格接続
* 権限制御（Scope / Region / Tenant）反映
* 検索条件拡張
* 本番運用前の最終調整

---

## P04 詳細ロードマップ（完了）

### P04-1（完了）— JDBC 導線確立

* JdbcGroupContractQueryService 導入
* Profile（jdbc / !jdbc）切替
* Bean 競合回避

---

### P04-2（完了）— SQL 正式化（JOIN なし）

* SQL を正とした設計に切替
* GroupContractSearchDto / Condition 導入
* BFF 新検索 API 実装
* SQL ⇄ DTO ⇄ RowMapper の完全整合

---

### P04-3（完了）— Oracle 接続・実行確認

* Oracle 接続用 env 定義整理
* local / dev / stg / prod 方針確定
* Datasource 定義の最終固定（application-jdbc.yml に集約）
* local の .env 自動読み込み方針確定（Gradle bootRun 限定）
* 実 Oracle での検索確認
* エラーハンドリング方針整理

**完了条件**：

* Oracle 実 DB で検索 API が成立
* SQL / DTO / RowMapper が実テーブル前提で破綻しない

**成果物**：

* [p04-3-oracle-env.md](./p04-3-oracle-env.md)

---

### P04-4（完了）— Region 側の法人別スキーマ切替設計

**目的**: region×corp×DomainAccount の切替設計/成立

* Region DB（saitama / fukushima / tochigi）側の業務ドメイン別 DB 接続アカウント（DomainAccount）切替設計に着手
* 切替キー `(region, corporation, domainAccount)` による接続切替方式を設計
* DomainAccount の定義（GOJO / FUNERAL、master は synonym 経由）
* 既存の DataSourceConfiguration 制約との整合性確認
* 切替方式の決定と実装

 **設計範囲**：

* 切替方式の設計（実装は設計確定後）
* RegionContext の拡張方針（region, corporation, domainAccount の追加）
* 環境変数の命名規則確定
* 接続プール管理方針
* master の扱い（synonym 経由、直接接続しない）

 **成果物**：

 * [p04-4-region-corp-schema-switching.md](./p04-4-region-corp-schema-switching.md)

**Done 条件**：

* `(region, corporation, domainAccount)` による切替方式が決定し、設計ドキュメントに明文化されている
* RegionContext に `(region, corporation, domainAccount)` が追加されている（実装着手の場合）
* DataSource 切替ロジックが実装されている（実装着手の場合）
* 環境変数の命名規則が確定している
* 同一 region 内の複数法人・複数 DomainAccount への切替が動作する（実装着手の場合）
* `(region, corporation, domainAccount)` 未設定時は FAIL FAST する
* 接続ユーザー未定義時は FAIL FAST する

**完了の根拠**：

* local,jdbc で bootRun 起動成功
* integration の既存導線が 200（group 契約検索）
* region（saitama/fukushima/tochigi）は起動時に接続せず遅延生成（DomainAccount/Corporation 前提）
* Context 未設定時は fail fast

**未実施/次フェーズでやること**：

* gojo/sousai は PoC で実テーブル未整合・region 導線未整備のため、実 SQL 疎通（DomainAccount 切替の実接続確認）は P1/導線実装時に行う

 ---

### P04-5（完了）— Keycloak Claim による権限制御設計（DB Routing 連携）

**目的**: Keycloak token claim（`nexus_db_access`）を用いて、BFF が Region × Corporation × DomainAccount の許可判定を行い、Context を fail fast で設定するための設計を確定する。

**スコープ（設計のみ）**:
- `nexus_db_access` claim の形式・命名規約の確定
- ワイルドカード（ALL）の使用条件の固定
- DomainAccount / Region / Corporation の決定規則の明文化
- BFF における認可判定と Context set の責務確定
- 403 / 404 の使い分けルール確定
- local 検証ヘッダーと本番相当の切り分け明示
- 同一 DomainAccount で複数 Region/Corporation が解釈される token の 403 判定規則確定

**非スコープ**:
- Keycloak の実設定手順（別ドキュメントに委譲）
- BFF の実装（P1-1 で実施）
- Region を token からどう取得するかの詳細（P1-1 で確定）

**成果物**:
- 設計正本: [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)
- 実設定手順書: [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md)

**状態**: 設計確定（Implementation Free）

**注意**: P04-5 は設計確定フェーズであり、実装は含めない。実装は P1-1 で行う。

 ---

## P1 詳細ロードマップ（現在地）

### P1-1（実装開始）— Keycloak Claim による権限制御実装

**目的**: P04-5 で確定した設計に基づいて、BFF で Keycloak token claim による権限制御を実装する。

**実装範囲**:

* Keycloak token claim（`nexus_db_access`）を BFF で解析する実装
* Region / Corporation / DomainAccount の token 由来決定ロジック実装
* 認可判定（403 fail fast）と Context set フィルター実装
* local 検証ヘッダーと token 由来の切替制御（local プロファイル限定）
* 同一 DomainAccount で複数 Region/Corporation が解釈される token の 403 判定実装
* Infrastructure 層は変更最小（Context を読むだけ）

**設計前提**:

* 設計正本: [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) に従う
* 実装ロードマップ: [p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md) に従う
* P04-5 の再設計・再解釈は禁止

**成果物**:

* BFF 認可フィルター実装（`NexusAuthorizationContextFilter`）
* Keycloak token 解析ロジック実装（`extractDbAccessRolesOrFail`）
* Context set ロジック実装
* 同一 DomainAccount で複数 Region/Corporation の検証ロジック実装

**Done 条件**（[p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md) の 13 節と整合）:

1. **実装完了**
   - `NexusAuthorizationContextFilter` が実装されている
   - `extractDbAccessRolesOrFail` 関数が実装されている
   - `nexus_db_access` を用いた認可判定が BFF で動作する
   - 未許可リクエストが Controller に到達しない
   - Context が正しく set / clear される
   - Infrastructure 層に認可ロジックが存在しない
   - local / 本番相当の切り分けが守られている（local プロファイル限定で検証ヘッダーを読み取る）
   - 同一 DomainAccount で複数 Region/Corporation が解釈される token が 403 を返す

2. **検証完了**
   - 検証手順（[p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md) の 14 節）をすべて実行し、期待通り動作することを確認

**現在地**: P1-1 実装開始

 ---

## 補足

* 本ドキュメントは**更新される前提**の資料
* 設計原則の変更は nexus-design-constitution.md のみで行う
* Cursor / Agent 実行時は、常に「現在地（P1-1 実装開始）」を明示して開始する

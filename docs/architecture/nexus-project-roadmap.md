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

### P04（現在進行中）— JDBC Read 実戦投入

* JDBC Read 導線の本格導入
* SQL を正とした Read 設計
* DTO / RowMapper / Controller 再設計
* JOIN なしでの検索成立
* Oracle 実 DB を前提とした確認準備

**位置づけ**：

* P1 に進むための「検索基盤の確定」フェーズ

---

### P1（予定）— 業務要件との接続

* JOIN の段階的復活
* 表示要件の確定（業務と合意）
* パフォーマンスチューニング
* Count / Search SQL 最適化
* 不要カラム・条件の整理

---

### P2（予定）— 本番運用準備

* Frontend 本格接続
* 権限制御（Scope / Region / Tenant）反映
* 検索条件拡張
* 本番運用前の最終調整

---

## P04 詳細ロードマップ（現在地）

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

## 補足

* 本ドキュメントは**更新される前提**の資料
* 設計原則の変更は nexus-design-constitution.md のみで行う
* Cursor / Agent 実行時は、常に「現在地（P04-4 完了）」を明示して開始する

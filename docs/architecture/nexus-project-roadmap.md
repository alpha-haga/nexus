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

### P04-3（次に進む作業）— Oracle 接続・実行確認

* Oracle 接続用 env 定義整理

  * local / dev / stg / prod 方針確定
* Datasource 定義の最終固定
* 実 Oracle での検索確認
* エラーハンドリング方針整理

**完了条件**：

* Oracle 実 DB で検索 API が成立
* SQL / DTO / RowMapper が実テーブル前提で破綻しない

---

## 補足

* 本ドキュメントは**更新される前提**の資料
* 設計原則の変更は nexus-design-constitution.md のみで行う
* Cursor / Agent 実行時は、常に「現在地（P04-3）」を明示して開始する

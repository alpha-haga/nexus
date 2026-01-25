# P04-2 決め事サマリー（法人横断契約一覧 / JDBC Read）

本ドキュメントは、P04-2 までの実装・議論を通じて **確定した設計・運用ルール** を整理したものです。以降の実装は本書を前提とします。

---

## 1. 大前提（P04 方針）

- **Read 導線は JDBC を正** とする
- SQL は *最終的に表示されるデータ構造* を正とし、DTO / RowMapper / Service はそれに追従する
- POC 時点の DTO はダミーであり、**実 SQL に合わせて破棄・再設計してよい**

---

## 2. SQL を中心とした責務分離

### 2.1 SQL の位置

- SQL は **infrastructure 層の一部**
- 配置場所：
  ```
  backend/nexus-infrastructure/src/main/resources/sql/group/
  ```
- domain（nexus-group）に SQL を置かない

### 2.2 SQL を正とする範囲

- SELECT 句に定義された **列と alias が唯一の真実**
- DTO / RowMapper は SQL alias に **完全一致** させる
- JOIN を外して取得できない項目は
  - `CAST(NULL AS ...) AS column_alias` で残す
  - 取れない列は **消すのではなく NULL で表現**（後で JOIN 復活可能にする）

---

## 3. 命名ルール（確定）

### 3.1 SQL

- **lower_snake_case**
- 省略しない（多少長くても意味優先）
- 例：
  ```sql
  contract_search.contract_receipt_ymd AS contract_receipt_ymd
  ```

### 3.2 Kotlin

- DTO / Condition / Response は **lowerCamelCase**
- snake_case → camelCase の変換は **RowMapper で明示的に行う**
- 自動マッピングは禁止

---

## 4. DTO 設計方針

### 4.1 Domain DTO（Read 用）

- `GroupContractSearchDto`
- SQL alias と **1:1 対応**
- ValueObject は使わない（Read 専用）
- 日付・数値は **SQL の型を尊重**（String / Int / Long / nullable）

### 4.2 検索条件 DTO

- `GroupContractSearchCondition`
- SQL の bind parameter と対応
- **全フィールド nullable**
- 未指定項目は WHERE 句に影響しない

---

## 5. WHERE 句・検索条件の書き方

- 基本形：
  ```sql
  (:param IS NULL OR column = :param)
  ```
- 前方一致・中間一致も SQL 側で定義
- Kotlin 側で条件分岐はしない

---

## 6. COUNT SQL のルール

- `group_contract_count.sql` は
  - FROM / WHERE を **search SQL と完全一致** させる
  - SELECT 句のみ `COUNT(1)` に差し替える
- JOIN / 条件の差分は禁止

---

## 7. RowMapper の責務

- snake_case alias を **明示的に取得**
- `requireNotNull` は **業務的に必須な列のみ**
- NULL 許容列は `getXxx().takeIf { !wasNull() }` を使用
- 変換ロジックは RowMapper に閉じる

---

## 8. QueryService の責務

- Interface：`GroupContractQueryService`（domain）
- 実装：infrastructure

### JDBC 実装

- `@Profile("jdbc")`
- SQL Loader + NamedParameterJdbcTemplate
- Pagination は `PaginationOffsetLimit`

### JPA 実装（MIN）

- `@Profile("!jdbc")`
- **compile を通すためだけに存在**
- ロジックは持たない（将来削除予定）

---

## 9. BFF / Controller 方針

### 9.1 JDBC 用新導線

- Controller：`GroupContractSearchController`
- URL：
  ```
  /api/v1/group/contracts/search
  ```
- `@Profile("jdbc")` で有効

### 9.2 旧導線

- `GroupContractController` は **削除 or !jdbc のみ**
- P04-2 以降は使用しない

---

## 10. DB / 環境に関する決定

- **業務データは Oracle 接続のみ**
- H2 は Keycloak 等の補助用途のみ
- JDBC Read で H2 を前提にしない
- Oracle 接続情報は **env で注入**（本フェーズでは未整備）

---

## 11. 今後の進め方（確定）

1. SQL を実テーブル・実列ベースで固める
2. DTO / RowMapper を SQL に追従させる
3. JOIN を段階的に復活（NULL → 実データ）
4. Oracle 接続 env を整備
5. 実データでの検索確認

---

## 12. NG 集

- SQL と DTO がズレたまま進める
- Kotlin 側で表示都合の列を作る
- alias を省略して意味が分からなくなる
- Read に ValueObject を持ち込む

---

以上。本書が P04-2 以降の唯一の前提です。


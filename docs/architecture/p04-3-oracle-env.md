# P04-3 決め事サマリー（Oracle 接続・環境変数注入）

本ドキュメントは、P04-3 までの実装・議論を通じて **確定した設計・運用ルール** を整理したものです。以降の実装は本書を前提とします。

---

## 1. 目的

P04-3 の目的は以下です：

- Oracle 実 DB への接続を前提とした環境変数注入方式を確定する
- local / dev / stg / prod 環境での接続定義方針を統一する
- 実 Oracle DB での検索 API 動作確認を可能にする

---

## 2. 確定した環境変数項目

### 2.1 Integration DB（統合DB）

- `NEXUS_DB_INTEGRATION_HOST`
- `NEXUS_DB_INTEGRATION_PORT`
- `NEXUS_DB_INTEGRATION_SERVICE`
- `NEXUS_DB_INTEGRATION_USER`
- `NEXUS_DB_INTEGRATION_PASSWORD`

### 2.2 Region DB（地区DB）

各地区（saitama / fukushima / tochigi）ごとに以下を定義：

- `NEXUS_DB_{REGION}_HOST`
- `NEXUS_DB_{REGION}_PORT`
- `NEXUS_DB_{REGION}_SERVICE`
- `NEXUS_DB_{REGION}_USER`
- `NEXUS_DB_{REGION}_PASSWORD`

例：
- `NEXUS_DB_SAITAMA_HOST`
- `NEXUS_DB_SAITAMA_PORT`
- `NEXUS_DB_SAITAMA_SERVICE`
- `NEXUS_DB_SAITAMA_USER`
- `NEXUS_DB_SAITAMA_PASSWORD`

### 2.3 共通設定

- `NEXUS_DB_DRIVER`（デフォルト: `oracle.jdbc.OracleDriver`）
- `NEXUS_DB_POOL_MAX`（デフォルト: `5`）
- `NEXUS_DB_POOL_MIN`（デフォルト: `1`）

### 2.4 OIDC 設定（local 環境）

- `NEXUS_OIDC_ISSUER_URI`（例: `http://localhost:18080/realms/nexus`）

---

## 3. Spring Boot 設定ルール（確定）

### 3.1 DB 定義の集約方針

- **DB 接続定義は `application-jdbc.yml` に集約**
- `application-local.yml` / `application-dev.yml` / `application-stg.yml` / `application-prod.yml` は DB 定義を持たない
- 環境変数は `application-jdbc.yml` 内で `${ENV_VAR}` 形式で参照

### 3.2 Profile 切替

- `application-jdbc.yml` は `spring.config.activate.on-profile: jdbc` で有効化
- local / dev / stg / prod は環境変数のみで切替（設定ファイルの分岐なし）

### 3.3 設定ファイル構造
application.yml # 共通設定（DB定義なし）
application-jdbc.yml # DB 接続定義（env 変数参照）
application-local.yml # local 固有設定（DB定義なし）
application-dev.yml # dev 固有設定（DB定義なし）
application-stg.yml # stg 固有設定（DB定義なし）
application-prod.yml # prod 固有設定（DB定義なし）


---

## 4. local 環境の .env 自動読み込み方針

### 4.1 読み込み対象

- **Gradle `bootRun` タスク実行時のみ有効**
- `./gradlew build` には影響しない
- CI / 本番環境には影響しない

### 4.2 実装場所

- `backend/nexus-bff/build.gradle.kts` の `bootRun` タスク内で実装
- `backend/.env` ファイルを読み込む
- OS 側で既に設定済みの環境変数は上書きしない

### 4.3 .env ファイル形式

- `KEY=VALUE` 形式
- `#` で始まる行はコメントとして無視
- `export KEY=VALUE` 形式も対応（`export` を自動除去）
- 引用符（`"value"` / `'value'`）は自動除去

### 4.4 .env.example

- `backend/.env.example` を配置
- 実際の値は含めず、キー名のみ列挙
- 開発者は `.env.example` をコピーして `.env` を作成し、実際の値を設定

---

## 5. 起動手順（local, jdbc profile）

### 5.1 前提条件

- Oracle DB が起動していること
- `backend/.env` ファイルが存在し、正しい接続情報が設定されていること

### 5.2 起動コマンド

cd backend
SPRING_PROFILES_ACTIVE=jdbc ./gradlew :nexus-bff:bootRun

### 5.3 期待される動作
bootRun ログに [bootRun] injected .env vars: ... が表示される
Oracle 接続先がログ上で H2 ではなく Oracle URL になる
検索 API が実 Oracle DB に対して実行される

## 6. API 実行例
### 6.1 統合DB 検索（法人横断契約一覧）
curl -X GET "http://localhost:8080/api/v1/group/contracts/search?page=0&size=20" \  -H "X-NEXUS-REGION: integration"

### 6.2 地区DB 検索（将来実装予定）
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \  -H "X-NEXUS-REGION: saitama"

## 7. やっていないこと（P04-3 の範囲外）
### 7.1 Region 側の法人別スキーマ切替
P04-3 では region DB への接続ユーザーは固定（例: XXXX_gojo）
同一 region 内の複数法人（例: XXXX_gojo / XXXX_master / XXXX_sousai）への切替は未実装
これは P04-4 で設計・実装する

### 7.2 JOIN の復活
P04-3 では JOIN なしの SQL を維持
CAST(NULL AS ...) で表現できない列は取得しない

### 7.3 性能最適化
COUNT / SEARCH SQL の最適化は行わない
インデックス設計は行わない

### 7.4 Frontend 接続
UI からの API 呼び出しは未対応
手動 curl での動作確認のみ

## 8. 次フェーズへの前提
### 8.1 P04-4 への前提
P04-3 で確定した環境変数注入方式は維持
Region DB への接続は確立済み
P04-4 では region 側の法人別スキーマ切替設計に着手

### 8.2 P1 への前提
Oracle 実 DB での検索が成立していること
SQL / DTO / RowMapper が実テーブル前提で破綻しないこと
JOIN 復活時に SQL のみを修正すれば動作すること

## 9. エラーハンドリング方針（確定）
### 9.1 接続エラー
環境変数未設定時は FAIL FAST（フォールバック禁止）
DataSourceConfiguration で IllegalStateException を throw

### 9.2 SQL 実行エラー
JDBC 例外はそのまま上位に伝播
業務エラーと技術エラーの分離は P1 以降で検討

## 10. NG 集
.env を build タスクで読み込む
OS 環境変数を上書きする
application-local.yml に DB 定義を書く
環境変数未設定時にデフォルト値で接続を試みる
Region 未設定時に integration へフォールバックする
以上。本書が P04-3 以降の唯一の前提です。
---

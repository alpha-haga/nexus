# NEXUS プロジェクト 設計憲法（Design Constitution）

本ドキュメントは、NEXUS プロジェクトにおける
**設計思想・進行ルール・ロードマップ・現在地** を固定するための
最上位ドキュメントである。

本書に記載された内容は「設計憲法」として扱い、
後続フェーズ・別チャット・別 AI・別開発者においても
原則として再議論しない。

---

## 1. プロジェクト概要

- プロジェクト名: **NEXUS**
- 種別: 社内基幹システム
- 対象業務:
  - 互助会（gojo）
  - 葬祭（funeral）
  - 冠婚（bridal）
  - household / point 等の周辺ドメイン
- 技術スタック（確定）:
  - Backend: Spring Boot / Kotlin
  - Frontend: Next.js（App Router）
  - DB: Oracle（本番・検証）、H2 は Keycloak 等の限定用途のみ
  - 認証: Keycloak

---

## 2. 設計憲法（最重要原則）

### 2.1 レイヤ責務の固定

#### Domain（nexus-group / 各ドメイン）
- 業務概念・DTO・QueryService インターフェースのみを持つ
- **SQL / JDBC / JPA / Spring 依存を持たない**
- 技術詳細を一切知らない

#### Infrastructure（nexus-infrastructure）
- JDBC / JPA / SQL 実装の置き場
- SQL はすべて以下に配置する  
nexus-infrastructure/src/main/resources/sql/

- SQL は **JDBC 実装の一部** として扱う

#### Presentation（nexus-bff / frontend）
- 業務判断をしない
- DTO 変換・入力バリデーション・ルーティングのみ
- 「動いている風」の実装は禁止

---

## 3. SQL / DTO / Mapper の設計ルール

### 3.1 SQL を正とする方針（P04 以降）

- **SQL が返す列定義を正とする**
- DTO / RowMapper / Controller は SQL に合わせて変更する
- POC 時代の DTO は「暫定」であり、将来の表示要件に従って破棄・再設計する

### 3.2 命名ルール

| 対象 | ルール |
|----|----|
| SQL alias | lower_snake_case（省略しない） |
| Kotlin DTO | lowerCamelCase |
| 変換 | RowMapper で明示的に行う（自動マッピング禁止） |

### 3.3 JOIN の扱い

- 初期段階では JOIN を行わない
- 取得できない列は `CAST(NULL AS ...) AS column_name` で定義
- JOIN 復活時に SQL のみを修正し、DTO/Mapper は維持する

---

## 4. 環境・接続方針

### 4.1 DB 接続

- Oracle（integration / region DB）
  - Integration DB: 法人横断検索専用（nexus-group）
  - Region DB: 地区別業務データ（saitama / fukushima / tochigi）
  - 各地区は別 Oracle インスタンス
  - 同一 region 内に複数の法人スキーマが存在（例: XXXX_gojo / XXXX_master / XXXX_sousai）
  - 接続ユーザー/パスワードは法人により切替が必要（P04-4 で設計・実装）

### 4.2 環境変数

- `application.yml` に直書きしない
- DB 接続定義は `application-jdbc.yml` に集約
- `application-local.yml` / `application-dev.yml` / `application-stg.yml` / `application-prod.yml` は DB 定義を持たない
- local 環境の `.env` 自動読み込みは **Gradle bootRun タスク限定**
  - `./gradlew build` には影響しない
  - CI / 本番環境には影響しない
  - OS 側で既に設定済みの環境変数は上書きしない
  
---

## 5. 全体ロードマップ（P0〜P2）

### P0（完了）
- PoC
- アーキテクチャ分離
- Read / Write 導線分離
- RegionContext / RoutingDataSource 確立

### P04（現在進行中）
- JDBC Read 導線の実戦投入
- SQL 正式化
- DTO / RowMapper / Controller の再設計
- Oracle 接続を前提とした検索確認

### P1（予定）
- JOIN 段階的復活
- 表示要件確定
- パフォーマンスチューニング
- Count / Search の最適化

### P2（予定）
- Frontend 本格接続
- 権限制御・検索条件拡張
- 本番運用前最終調整

---

## 6. P04 詳細ロードマップ（現在地）

### P04-1（完了）
- JDBC QueryService 導入
- Profile 切替（jdbc / !jdbc）
- Bean 競合回避

### P04-2（完了）
- SQL 正式化（JOIN なし）
- GroupContractSearchDto / Condition 導入
- BFF 新検索 API 実装
- SQL ⇄ DTO ⇄ RowMapper 整合

### **P04-3（次に進む作業）**
- Oracle 接続用 env 定義整理
- local/dev/stg/prod の接続定義方針固定
- 実 Oracle での検索確認
- エラーハンドリング方針整理

---

## 7. 引き継ぎ・AI 利用ルール

- 新チャット開始時は必ず本設計憲法を貼る
- Cursor / Agent 実行時は以下を明示する:
- 対象フェーズ（例: P04-3）
- 変更対象ファイル一覧
- diff か全文かの指定
- Done 条件
- 実行結果は build / bootRun / curl 結果を貼る

---

本ドキュメントは **NEXUS プロジェクトの最上位設計憲法** として扱う。

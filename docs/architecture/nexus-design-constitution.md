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
- **Frontend の責務（P2-2 適用）**:
  - Region は業務判断ではなく「必須入力状態」として扱う
  - Region 未設定時は API を実行せず、UI で未設定状態を明示する
  - 暗黙補完（デフォルト値の自動付与等）を禁止する
  - 全 API リクエストに `X-NEXUS-REGION` を必須付与する
- **Region セレクターの扱い（P2-2 / P2-3 適用）**:
  - **現状**: 画面に Region セレクターを表示しているのは **暫定UI** である
  - **Region 未選択時の動作**: Region 未選択時は API を叩かない（Fail Fast / 明示状態）
  - **X-NEXUS-REGION の付与**: X-NEXUS-REGION は明示状態の結果として付与される（隠蔽しない）
  - **将来的な変更**: Region の指定方式を変更する可能性はあるが、それは「別フェーズ（P3以降で検討）」であり、P2-3 では触らない
  - **規約**: Region セレクターは P2-2 で実装した暫定UIであり、P2-3 では変更しない

---

## 3. SQL / DTO / Mapper の設計ルール

### 3.1 SQL を正とする方針（P04 以降）

- **SQL が返す列定義を正とする**
- DTO / RowMapper / Controller は SQL に合わせて変更する
- POC 時代の DTO は「暫定」であり、将来の表示要件に従って破棄・再設計する

**P1 適用メモ**: P1 では SQL 起点を徹底する。表示要件（P1-B0）で確定した項目のみを SQL に反映し、DTO/Mapper/Controller は SQL に従属させる。

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

**P1 適用メモ**: P1-B0 で JOIN 復活順序を固定し、P1-B1 で段階的に復活させる。順序は P1-B0 で合意した順でのみ進める。

---

 ## 4. 環境・接続方針

 ### 4.1 DB 接続

 - Oracle（integration / region DB）
   - Integration DB: 法人横断検索専用（nexus-group）
   - Region DB: 地区別業務データ（saitama / fukushima / tochigi）
  - **Region（地区）**: 各地区は別 Oracle インスタンス
  - **Corporation（法人）**: 地区インスタンス内に複数の法人が存在する
    - saitama: musashino / saikan / fukushisousai
    - fukushima: fukushima / touhoku / gifu
    - tochigi: tochigi / shizuoka / tochigi_takusel / 新法人予定
  - **DomainAccount（業務ドメイン別 DB 接続アカウント種別）**:
    - GOJO: `XXX_gojo`（nexus-gojo ドメイン用）
    - FUNERAL: `XXX_sousai`（nexus-funeral ドメイン用）
    - master は DomainAccount に内包（synonym 経由で参照可能）だが、直接接続対象にしない
  - **業務ドメイン接続の切替単位**: `(region, corporation, domainAccount)` の 3 要素
    - 業務ドメインの DB 接続は「地区×法人×業務システム」で接続ユーザーが変わる
    - nexus-gojo: `XXX_gojo`（`XXX_master` は synonym で gojo から参照可能）
    - nexus-funeral: `XXX_sousai`（`XXX_master` は synonym で sousai から参照可能）
  - **integration インスタンス（read only）を使うドメイン**: nexus-group / nexus-identity / nexus-household
  - **横断ドメイン**: payment / accounting / reporting 等は業務ドメイン DB にアクセス可能（= region / corp の切替が必要）
  - P04-4 の段階では解決策を断定せず、責務境界（domain へ持ち込まない、infrastructure で扱う）を固定する

### 4.2 環境変数

- `application.yml` に直書きしない
- DB 接続定義は `application-jdbc.yml` に集約
- `application-local.yml` / `application-dev.yml` / `application-stg.yml` / `application-prod.yml` は DB 定義を持たない
- local 環境の `.env` 自動読み込みは **Gradle bootRun タスク限定**
  - `./gradlew build` には影響しない
  - CI / 本番環境には影響しない
  - OS 側で既に設定済みの環境変数は上書きしない

### 4.3 権限制御（Keycloak Claim）と DB Routing の関係

- **設計確定（P04-5 完了）**: Keycloak token claim（`nexus_db_access`）による権限制御と DB Routing の設計が確定している
- **設計正本**: [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) を参照
- **実装開始（P1-1）**: BFF で Keycloak token claim による権限制御の実装に入る

- **Source of Truth**: `nexus_db_access` claim（`List<String>`）が唯一の権限情報源である。他の claim やヘッダーから権限を推測してはならない。

- **Presentation 層（BFF）の責務**:
  - Keycloak token の claim（`nexus_db_access`）から許可された `(region, corporation, domainAccount)` を取得する
  - リクエストの `(region, corporation, domainAccount)` が許可されているかを **BFF で fail fast 判定**し、未許可の場合は 403 Forbidden を返す
  - 同一 DomainAccount で複数の Region または複数の Corporation が解釈される token は 403 Forbidden を返す（認可判定以前に検出）
  - 許可されている場合のみ、RegionContext / CorporationContext / DomainAccountContext を設定する
  - 本番環境では検証ヘッダー（`X-NEXUS-REGION` / `X-NEXUS-CORP`）は完全に無効。local プロファイル限定で検証ヘッダーを読み取る。

- **Infrastructure 層の責務**:
  - Context を読むだけ（権限判定は行わない）
  - DataSource 切替は Context の値に基づいて行う
  - Context 未設定時は既存の fail fast 機構（DomainAccountContextNotSetException 等）で例外を投げる

**責務境界**: 設計責務（P04-5 で確定）と実装責務（P1-1 で実施）は明確に分かれている。設計の再解釈・再設計は禁止。

### 4.4 Region と Tenant の責務分離（確定）

- Region は DB ルーティング内部責務
- Tenant は UI 責務
- フロントは Region を扱わない
- RegionSelector は存在しない
- TenantContext が唯一の選択コンテキスト
- group ドメインは常に INTEGRATION 固定
- DB routing は Backend 内部責務
- フロントは X-NEXUS-REGION を送らない
  
---

## 5. Read / Write 分離方針

### 5.1 Read 導線（JDBC / SQL）

- Read は **JDBC + SQL 正** とする
- SQL が返す列定義を正とし、DTO / RowMapper は SQL に従属する
- JOIN は段階的に復活させる（P1-B0 で順序を固定）

**P1 適用メモ**: P1-B1 では P1-B0 で固定した JOIN 復活順序に従い、SQL → DTO → Mapper → API の順で段階的に実装する。

### 5.2 Write 導線（JPA / Domain）

- Write は **JPA + Domain 正** とする
- Domain は技術詳細を持たない
- Infrastructure で JPA 実装を行う

---

## 6. Fail Fast とエラーの意味

### 6.1 「動いている風」禁止

- すべての API で **200 / 400 / 403 / 404 の意味が明確** であること
- 曖昧な成功・失敗は禁止
- エラーレスポンスは業務的に意味を持つ
- **状態を隠さない（P2-2 適用）**:
  - 未設定状態（Region 未選択等）を UI で明示する
  - 暗黙補完（デフォルト値の自動付与等）を禁止する
  - 必須入力項目が未設定の場合は、API を実行せず UI で状態を明示する
  - **null 表示**: null（値なし）は UI 上で `-` と表示する。空文字や未表示で隠さない。`(null)` という技術表現は業務UIに馴染まないため採用しない。
  - **0件表示**: 0件（データなし）は null 表示とは区別し、「◯◯なし」等の既存ルールに従う（既存表示を変えない）。

**P1 適用メモ**: P1-B0 で各エラーの意味を固定し、P1-B1 で実装時に 200/400/403/404 の境界を明確にする。

### 6.2 Fail Fast 方針

- 未許可のリクエストは早期に拒否（403 Forbidden）
- 不正な入力は早期に拒否（400 Bad Request）
- 存在しないリソースは早期に拒否（404 Not Found）
- Infrastructure 層での権限判定は行わない（BFF で判定）

---

## 7. 全体ロードマップ（P0〜P2）

### P0（完了）
- PoC
- アーキテクチャ分離
- Read / Write 導線分離
- RegionContext / RoutingDataSource 確立

### P04（完了）
- JDBC Read 導線の実戦投入
- SQL 正式化
- DTO / RowMapper / Controller の再設計
- Oracle 接続を前提とした検索確認
- Keycloak Claim による権限制御設計確定

### P1（現在進行中）
- JOIN 段階的復活（P1-B1 完了）
- 表示要件確定（P1-B0）
- Count / Search の最適化（P1-B2 完了）
- パフォーマンスチューニング（P1-B3、原則P2送り）
- Keycloak Claim による権限制御実装（P1-A1 完了）

### P2（予定）
- Frontend 本格接続
- 権限制御・検索条件拡張
- 本番運用前最終調整

---

## 8. P04 詳細ロードマップ（完了）

### P04-1（完了）
- JDBC QueryService 導入
- Profile 切替（jdbc / !jdbc）
- Bean 競合回避

### P04-2（完了）
- SQL 正式化（JOIN なし）
- GroupContractSearchDto / Condition 導入
- BFF 新検索 API 実装
- SQL ⇄ DTO ⇄ RowMapper 整合

### P04-3（完了）
- Oracle 接続用 env 定義整理
- local/dev/stg/prod の接続定義方針固定
- 実 Oracle での検索確認
- エラーハンドリング方針整理

### P04-4（完了）
- Region DB 側の業務ドメイン別 DB 接続アカウント（DomainAccount）切替設計
- 切替キー `(region, corporation, domainAccount)` による接続切替方式の設計
- DomainAccount の定義（GOJO / FUNERAL、master は synonym 経由）
- 既存の DataSourceConfiguration 制約との整合性確認
- 切替方式の決定と実装

### P04-5（完了）
- Keycloak token claim（`nexus_db_access`）による権限制御設計確定
- BFF における認可判定と Context set の責務確定
- 詳細は [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) を参照

---

## 9. 引き継ぎ・AI 利用ルール

- 新チャット開始時は必ず本設計憲法を貼る
- Cursor / Agent 実行時は以下を明示する:
- 対象フェーズ（例: P1-1）
- 変更対象ファイル一覧
- diff か全文かの指定
- Done 条件
- 実行結果は build / bootRun / curl 結果を貼る

---

本ドキュメントは **NEXUS プロジェクトの最上位設計憲法** として扱う。

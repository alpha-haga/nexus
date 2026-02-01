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

### P1（完了）— 業務要件との接続

P1 は **2 レーン構成**で進行した。

**完了状況**:
- P1-A（セキュリティ・ルーティング成立レーン）: **完了**
  - P1-A0: Keycloak 設定完了
  - P1-A1: BFF 認可・Context set 実装完了
  - P1-A2: E2E 検証完了
- P1-B（業務成立・検索成立レーン）: **完了**
  - P1-B0: 表示要件確定（ドキュメント化完了）
  - P1-B1: JOIN 段階的復活完了
  - P1-B2: Count/Search 最適化完了
  - P1-B3: パフォーマンス（P2-5 で実施）

---

### P2（現在進行中）— 本番運用準備

* Frontend 本格接続
* 権限制御（Scope / Region / Tenant）反映
* 検索条件拡張
* 本番運用前の最終調整

**詳細**: [p2-detailed-roadmap.md](./p2-detailed-roadmap.md) を参照

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

**Done 条件（要点）**:
- claim: nexus_db_access（List<String>）の形式と照合規約が確定
- wildcard: integration__ALL__GROUP のみ許可、region 側 ALL 禁止
- BFF が fail fast（403/404）と Context set を担い、Infrastructure は Context を読むだけ

 ---

## P1 詳細ロードマップ（現在地）

### P1-A：セキュリティ・ルーティング成立レーン（最優先）

P1-A は「本番相当の入口条件」を確定させるフェーズとする。

#### P1-A0（完了）— Keycloak 設定

**目的**: `nexus_db_access` claim を実トークンに載せる。

**スコープ**:
- Keycloak 側の設定（client role / mapper / scope）
- 実トークンに `nexus_db_access` claim が含まれることを確認

**参照**:
- [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md)
- [P1-A0-VERIFICATION.A.md](./P1-A0-VERIFICATION.A.md)（検証手順書）
- [P1-A0-COMPLETION.md](./P1-A0-COMPLETION.md)（完了宣言）

**Done 条件**:
- 任意ユーザーで token に `nexus_db_access` が配列で出力される
- 例の role（例: `saitama__musashino__GOJO`、`integration__ALL__GROUP`）が claim に含まれることを確認済み

**状態**: 完了

**完了宣言**: [P1-A0-COMPLETION.md](./P1-A0-COMPLETION.md) を参照

---

#### P1-A1（完了）— BFF 認可・Context set 実装

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

**実装成果物**:
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/NexusAuthorizationContextFilter.kt
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/DbAccessRoleExtractor.kt
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/NexusSecurityConfig.kt
- backend/nexus-bff/src/test/kotlin/nexus/bff/security/DbAccessRoleExtractorTest.kt

**Done 条件（要点）**:
- request path で scope 決定（DomainAccount を token から推測しない）
- nexus_db_access 不在/空/不正 -> 403
- integration__ALL__GROUP のみ許可、region 側 ALL 禁止
- 同一 DomainAccount で複数 Region/Corp 解釈 -> 403
- integration は RegionContext のみ set、region は Region/Corp/DomainAccount を set
- finally で必ず Context clear
- テストが実行可能（:nexus-bff:test が通る）

**状態**: 実装完了（マージ済み）

---

#### P1-A2（完了）— E2E 検証

**目的**: 実トークンを用いた E2E 検証を実施する。

**スコープ**:
- Keycloak から取得した実トークンで curl 検証
- 403 / 404 / 200 の確認
- local 検証ヘッダーと token 由来の動作確認
- 同一 DomainAccount で複数 Region/Corporation の 403 確認

**前提**:
- P1-A0（Keycloak 設定）が完了していること

**Done 条件**:
- 検証手順（[p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md) の 15 節）をすべて実行し、期待通り動作することを確認

---

#### P1-A3（「P2-6に統合」）— 本番境界整理

**実施タイミング**: P1 本実装の終盤（締め）／P2 開始直前

**目的**: 本番運用に向けた境界を整理する。

**スコープ**:
- local 検証ヘッダー無効化方針の確定
- ログ・運用前提の整理
- 監査ログの要件整理

**Done 条件**:
- local 検証ヘッダーの無効化方針が確定
- ログ出力要件が整理されている
- 運用前提が明文化されている

---

### P1-B：業務成立・検索成立レーン（後続）

P1-B は「業務要件と検索要件を成立させる」ためのフェーズとする。

**前提**:
- P1-A の完了を前提とする（特に P1-A2 の E2E 検証完了後）

**順序**: 以下の順で固定（前フェーズ完了後に次フェーズに着手）

---

#### P1-B0（完了）— 表示要件確定

**目的**: 業務要件として必要な表示項目・条件を確定する。

**スコープ**:
- 一覧項目の確定（MUST/SHOULD/WON'T）
- 検索条件の確定（query param、バリデーション、400条件）
- ソート要件の確定（許可キー、デフォルト、方向、不正時の扱い）
- ページング要件の確定（page/size のデフォルトと上限、上限超過時の扱い）
- エラーの意味の確定（200/400/403/404）
- JOIN 段階的復活の順序の確定（B1でやる順番を箇条書きで固定）

**成果物**:
- docs/architecture/p1-b0-group-contract-display-requirements.md

**Done 条件**:
- ✅ MUST/SHOULD/WON'T が固定されている
- ✅ 検索条件、ソート、ページングが固定されている
- ✅ JOIN 復活順序が固定されている
- ✅ エラーの意味（200/400/403/404）が固定されている
- ✅ 上記がドキュメント化されている

**状態**: 完了（ドキュメント化済み）

---

#### P1-B1（完了）— JOIN 段階的復活（SQL→DTO→Mapper→API）

**目的**: P1-B0 で固定した JOIN 復活順序に従い、段階的に JOIN を復活させる。

**スコープ**:
- P1-B0 で固定した順序に従い、JOIN を段階的に復活
- SQL → DTO → Mapper → API の順で実装
- 各段階で回帰テストを実施

**実施内容**:
- Step 1: `company_short_name`（法人名）のJOIN復活（`zgom_cmp` テーブル）
- Step 2: `course_name`, `monthly_premium`（コース名・月額保険料）のJOIN復活（`zgom_course_cd_all` テーブル）
- `GlobalExceptionHandler` 追加（`nexus-bff` モジュール）

**Done 条件**:
- ✅ P1-B0 の MUST が 200 で返る
- ✅ 400/403/404 の境界が明確
- ✅ 回帰が取れている（既存機能が壊れていない）

**状態**: 完了（Step 1 & Step 2）

**完了宣言**: [p1-b1-completion.md](./p1-b1-completion.md) を参照

---

#### P1-B2（完了）— Count/Search 最適化

**目的**: Count クエリと Search クエリを最適化する。

**スコープ**:
- count クエリと search クエリの条件整合
- paging ルールの固定
- 検索条件の最適化

**実施内容**:
- Step 1: `group_contract_count.sql` に P1-B1 で追加した JOIN を反映
- Step 2: `size` の上限値を 100 に設定、上限超過時は 400 Bad Request
- Step 3: 実データ量（約470万件）での実行時間測定を実施
- Step 4: パフォーマンス測定結果を記録（平均1.6秒、最長2.6秒）

**Done 条件**:
- ✅ search と count 条件の整合が取れている（JOIN含む）
- ✅ paging ルールが固定されている（size上限100）
- ✅ 許容時間内で実行される（実データ量前提、平均1.6秒、最長2.6秒）
- ✅ パフォーマンス測定結果が記録されている

**状態**: 完了

**完了宣言**: [p1-b2-completion.md](./p1-b2-completion.md) を参照

**注意事項**:
- COUNT SQL は FROM/WHERE を search SQL と完全一致させる（P04-2 のルール）
- JOIN は LEFT JOIN のまま維持（NULL 許容）
- パフォーマンスチューニング（インデックス追加等）は P1-B3 以降の責務
- 実データ量での測定を前提とする（推測禁止）

---

#### P1-B3（未着手）— パフォーマンス（原則P2送り、業務が死ぬ場合のみP1繰上げ）

**目的**: 実データ量・権限スコープ前提でパフォーマンスを調整する。

**スコープ**:
- 実データ量を前提としたチューニング
- 権限スコープを考慮した最適化
- 原則 P2 送りだが、業務に耐えない場合のみ P1 に繰り上げ可能

**Done 条件**:
- 計測根拠あり（推測禁止）
- 実データ量・権限スコープ前提で許容時間内で動作する
- パフォーマンス要件が満たされている

---

## P2 詳細ロードマップ（現在地）

**詳細ドキュメント**: [p2-detailed-roadmap.md](./p2-detailed-roadmap.md)

### P2 の基本スタンス

**P2 は「つなぐ・固める・事故らせない」フェーズ**

- 新思想・新基盤・作り直しは禁止
- P1 で成立した Read / 認可 / Routing を Frontend・運用に接続する

### P2 の目的

- Frontend 本格接続（Next.js と Backend BFF の完全統合）
- 権限制御（Scope / Region / Tenant）の Frontend 反映
- 検索条件拡張（P1-B0 で確定した追加検索条件の実装）
- 本番運用前の最終調整（ログ・監査ログ・エラーハンドリング・パフォーマンス）

### P2 サブフェーズ概要

| フェーズ | 名称 | 状態 |
|---------|------|------|
| P2-1 | Frontend 認証・認可統合 | **完了** |
| P2-2 | Frontend 本格接続 | **次着手** |
| P2-3 | 権限制御反映 | 未着手 |
| P2-4 | 検索条件拡張 | 未着手 |
| P2-5 | パフォーマンス最適化 | 未着手 |
| P2-6 | 本番運用前最終調整 | 未着手 |

---

### P2-1（完了）— Frontend 認証・認可統合（Keycloak 統合）

**目的**: Frontend（Next.js）と Keycloak の認証・認可を統合し、Backend BFF への認証付きリクエストを実現する。

**スコープ**:
- NextAuth.js と Keycloak の統合
- Keycloak token の取得・管理
- Backend BFF への認証付きリクエスト
- Token の自動リフレッシュ
- 認証失敗時のリダイレクト

**Done 条件**:
- ✅ NextAuth.js と Keycloak が統合されている
- ✅ Keycloak から取得した token で Backend BFF にリクエストできる
- ✅ Token の自動リフレッシュが動作する
- ✅ 認証失敗時にログイン画面にリダイレクトされる
- ✅ E2E 検証（Frontend → Keycloak → Backend BFF → API）が成立する

**状態**: 完了

**完了宣言**: E2E 検証完了により、P2-1 は完了と判断する。詳細は [p2-1-e2e-verification.md](./p2-1-e2e-verification.md) を参照。

---

### P2-2（次着手）— Frontend 本格接続（Group Contract List の完成）

**目的**: Frontend の Group Contract List 画面を完成させ、Backend BFF API と完全に統合する。

**スコープ**:
- Group Contract List 画面の完成
- エラーハンドリングの実装（400/403/404/500）
- ローディング状態の表示
- ソート機能の実装

**Done 条件**:
- Group Contract List 画面が正常に表示される
- 検索・ページネーション・ソートが動作する
- エラーハンドリングが適切に実装されている

---

### P2-3（未着手）— 権限制御反映（Frontend 側での権限制御）

**目的**: Backend BFF の権限制御を Frontend 側に反映し、権限に応じた画面表示制御を実現する。

**スコープ**:
- `nexus_db_access` claim の Frontend 側取得・解析
- 権限に応じた画面表示制御
- 権限不足時のエラー表示

**Done 条件**:
- 権限に応じた画面表示制御が実装されている
- 権限不足時に 403 エラーが表示される

---

### P2-4（未着手）— 検索条件拡張

**目的**: P1-B0 で確定した追加検索条件を実装する。

**スコープ**:
- 法人コード検索、氏名検索、住所検索等の追加
- SQL → DTO → Controller → Frontend の順で実装

**Done 条件**:
- 追加検索条件が実装されている
- 回帰テストが完了している

---

### P2-5（未着手）— パフォーマンス最適化（P1-B3 の実施）

**目的**: 実データ量・権限スコープ前提でパフォーマンスを調整する。

**スコープ**:
- インデックス追加の検討
- 検索方式の見直し
- パフォーマンス測定

**Done 条件**:
- 計測根拠あり（推測禁止）
- 許容時間内で動作する

---

### P2-6（未着手）— 本番運用前最終調整

**目的**: 本番運用に向けた最終調整を実施する。

**スコープ**:
- ログ・監査ログ要件整理
- エラーハンドリング最終確認
- セキュリティ要件最終確認
- 運用前提の整理

**Done 条件**:
- ログ出力要件が明文化されている
- セキュリティ設定が確認されている
- 運用前提が整理されている

---

## 補足

* 本ドキュメントは**更新される前提**の資料
* 設計原則の変更は nexus-design-constitution.md のみで行う
* Cursor / Agent 実行時は、常に「現在地（P1 完了、P2-1 完了、P2-2 次着手）」を明示して開始する

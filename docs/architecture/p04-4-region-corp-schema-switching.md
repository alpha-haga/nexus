
---

## 2. 新規ファイル：`docs/architecture/p04-4-region-corp-schema-switching.md`

# P04-4 設計（Region 側の法人別スキーマ切替）

本ドキュメントは、P04-4 における **region DB の法人別スキーマ/認証切替** を安全に実現するための設計を固めるためのものです。

---

## 1. 目的

P04-4 の目的は以下です：

- Region DB（saitama / fukushima / tochigi）において、**法人別スキーマへの切替** を安全に実現する
- 同一 region 内の複数法人（例: `XXXX_gojo` / `XXXX_master` / `XXXX_sousai`）への接続切替を可能にする
- 切替方式の設計方針を確定し、実装に着手する

---

## 2. 前提（事実）

### 2.1 地区DB の構造

- **各地区（saitama / fukushima / tochigi）は別 Oracle インスタンス**
- 同一 region 内に複数の法人スキーマが存在する
- 例（埼玉地区）：
  - `XXXX_gojo`（互助会）
  - `XXXX_master`（マスター）
  - `XXXX_sousai`（総務）

### 2.2 接続認証の現状

- P04-3 では **接続ユーザーは固定**（例: `NEXUS_DB_SAITAMA_USER=XXXX_gojo`）
- 同一 region 内の他法人スキーマへの切替は未実装
- **接続ユーザー/パスワードは法人により切替が必要**

### 2.3 既存の制約

- `DataSourceConfiguration` では **法人単位での DataSource 作成は禁止**（設計憲法で確定）
- 同一地区内の全法人は同じ DataSource を共有する方針

---

## 3. 非目的（P04-4 では行わないこと）

- JOIN の復活
- 機能追加（新 API / 新エンドポイント）
- 性能最適化（インデックス設計 / SQL チューニング）
- Frontend 接続
- 権限制御の実装

---

## 4. 切替設計方針の候補比較

### 4.1 方針A：DataSource を法人単位で持つ

| 項目 | 内容 |
|------|------|
| **方式** | 法人ごとに DataSource Bean を作成（例: `saitamaGojoDataSource`, `saitamaMasterDataSource`） |
| **メリット** | 接続プールを法人単位で分離できる、接続情報を Bean 定義で明確化 |
| **デメリット** | 設計憲法の「法人単位 DataSource 禁止」と矛盾、Bean 数が増える、動的法人追加に対応困難 |
| **実装コスト** | 中（Bean 定義の追加が必要） |
| **運用コスト** | 高（法人追加時に再デプロイが必要） |

### 4.2 方針B：接続ユーザー固定 + CURRENT_SCHEMA 切替

| 項目 | 内容 |
|------|------|
| **方式** | 接続ユーザーは固定（例: `XXXX_gojo`）、実行時に `ALTER SESSION SET CURRENT_SCHEMA = 'XXXX_master'` で切替 |
| **メリット** | DataSource は1つで済む、設計憲法との矛盾なし、動的切替が可能 |
| **デメリット** | 接続ユーザーに他スキーマへの権限が必要、セキュリティリスク（権限過多） |
| **実装コスト** | 低（JDBC 実行前の SQL 実行のみ） |
| **運用コスト** | 低（設定変更不要） |

### 4.3 方針C：動的 DataSource 切替（接続情報を動的に変更）

| 項目 | 内容 |
|------|------|
| **方式** | RegionContext に法人ID を追加し、RoutingDataSource で接続情報（username/password）を動的に切替 |
| **メリット** | 接続ユーザーを法人ごとに分離できる、セキュリティリスク低減 |
| **デメリット** | HikariCP の接続プール管理が複雑、接続情報の動的変更は HikariCP の設計思想と矛盾 |
| **実装コスト** | 高（HikariCP の接続プール再構築が必要） |
| **運用コスト** | 中（接続情報の管理が必要） |

### 4.4 方針D：接続ユーザーを法人ごとに持つが DataSource は region 単位

| 項目 | 内容 |
|------|------|
| **方式** | 接続ユーザーは法人ごとに定義（env 変数）、DataSource は region 単位で1つ、接続時に username/password を動的に選択 |
| **メリット** | セキュリティリスク低減、設計憲法との矛盾を回避（DataSource は region 単位） |
| **デメリット** | HikariCP の接続プール管理が複雑、接続情報の動的選択ロジックが必要 |
| **実装コスト** | 高（HikariCP の接続プール再構築が必要） |
| **運用コスト** | 中（接続情報の管理が必要） |

---

## 5. 採用判断の観点

P04-4 で採用する方針を決定する際は、以下を考慮する：

1. **設計憲法との整合性**
   - 「法人単位 DataSource 禁止」との矛盾がないか
   - 「同一地区内の全法人は同じ DataSource を共有」の方針と整合するか

2. **セキュリティ**
   - 接続ユーザーの権限範囲が適切か
   - 権限過多によるリスクがないか

3. **実装・運用コスト**
   - 実装の複雑さは許容範囲か
   - 法人追加時の運用負荷は許容範囲か

4. **将来拡張性**
   - 動的法人追加に対応可能か
   - P1 / P2 での要件拡張に対応可能か

---

## 6. 既存実装との矛盾点

### 6.1 DataSourceConfiguration の制約

- **現状**: 法人単位での DataSource 作成は禁止
- **P04-4 要件**: 法人別スキーマへの切替が必要
- **矛盾点**: 方針A（法人単位 DataSource）を採用する場合、この制約と矛盾する

### 6.2 RoutingDataSource の設計

- **現状**: Region 単位で DataSource を切替
- **P04-4 要件**: Region 内で法人単位の切替が必要
- **矛盾点**: RegionContext に法人ID を追加する必要がある

### 6.3 環境変数の構造

- **現状**: `NEXUS_DB_SAITAMA_USER` は1つの値のみ
- **P04-4 要件**: 法人ごとに異なる接続ユーザーが必要
- **矛盾点**: 環境変数の命名規則を変更する必要がある（例: `NEXUS_DB_SAITAMA_GOJO_USER`）

---

## 7. P04-4 で決めるべき決定事項

1. **切替方式の採用**
   - 方針A / B / C / D のいずれを採用するか
   - 採用理由の明文化

2. **RegionContext の拡張**
   - 法人ID を RegionContext に追加するか
   - 追加する場合の型定義（CorporationId / String）

3. **環境変数の命名規則**
   - 法人ごとの接続情報をどう表現するか
   - 例: `NEXUS_DB_SAITAMA_GOJO_USER` / `NEXUS_DB_SAITAMA_MASTER_USER`

4. **接続プール管理**
   - HikariCP の接続プールをどう管理するか
   - 動的切替時の接続プール再構築が必要か

5. **エラーハンドリング**
   - 法人ID 未設定時の動作（FAIL FAST / フォールバック）
   - 接続ユーザー未定義時の動作

---

## 8. 最小実装スコープ（P04-4 で実装する場合）

### 8.1 対象モジュール

- `nexus-core`: RegionContext の拡張（法人ID 追加）
- `nexus-infrastructure`: DataSourceConfiguration / RoutingDataSource の修正
- `nexus-bff`: Controller での法人ID 受け取り（Header / Query Parameter）

### 8.2 対象クラス

- `nexus.core.region.RegionContext`: 法人ID 保持機能の追加
- `nexus.infrastructure.db.config.DataSourceConfiguration`: 切替ロジックの実装
- `nexus.infrastructure.db.RoutingDataSource`: 法人単位切替の実装

### 8.3 対象設定ファイル

- `application-jdbc.yml`: 法人ごとの接続情報定義
- `.env.example`: 法人ごとの環境変数定義

---

## 9. 受け入れ条件（Done）

P04-4 を完了とする条件は以下です：

1. **設計方針の確定**
   - 切替方式が決定し、設計ドキュメントに明文化されている

2. **実装の完了（設計着手のみの場合は設計完了）**
   - RegionContext に法人ID が追加されている
   - DataSource 切替ロジックが実装されている
   - 環境変数の命名規則が確定している

3. **動作確認**
   - 同一 region 内の複数法人スキーマへの切替が動作する
   - 法人ID 未設定時は FAIL FAST する
   - 接続ユーザー未定義時は FAIL FAST する

4. **ドキュメント更新**
   - 設計憲法に P04-4 の決定事項が追記されている
   - ロードマップに P04-4 の完了が記録されている

---

## 10. 次のフェーズ（P1）への前提

- P04-4 で確定した切替方式は P1 以降も維持
- JOIN 復活時も法人別スキーマ切替は影響を受けない
- 性能最適化時も切替方式は変更しない

---

以上。本書が P04-4 の設計の正本です。

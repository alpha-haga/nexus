# P04-4 設計（Region 側の法人別スキーマ切替）

本ドキュメントは、P04-4 における **region DB の法人別スキーマ/認証切替** を「DomainAccount」前提で設計・確定するためのものです。

 ---

 ## 1. 目的

 P04-4 の目的は以下です：

- Region DB（saitama / fukushima / tochigi）において、**業務ドメイン別の DB 接続アカウント（DomainAccount）切替** を安全に実現する
- 切替キー `(region, corporation, domainAccount)` による接続切替を可能にする
- 切替方式の設計方針を確定し、実装に着手する

 ---

 ## 2. 前提（事実）

 ### 2.1 Oracle インスタンス構成

- Oracle インスタンスは以下の構成：
  - **integration**（全法人横断検索用、read only）
  - **地区別 3 インスタンス**（saitama / fukushima / tochigi）
- 地区インスタンス内に複数法人が存在する：
  - **saitama**: musashino / saikan / fukushisousai
  - **fukushima**: fukushima / touhoku / gifu
  - **tochigi**: tochigi / shizuoka / tochigi_takusel / 新法人予定

### 2.2 業務ドメインの DB 接続

- 業務ドメインの DB 接続は「地区×法人×業務システム」で接続ユーザーが変わる：
  - **nexus-gojo**: `XXX_gojo`（`XXX_master` は synonym で gojo から参照可能）
  - **nexus-funeral**: `XXX_sousai`（`XXX_master` は synonym で sousai から参照可能）
- 接続ユーザー/パスワードは法人により切替が必要

### 2.3 integration インスタンスを使用するドメイン

- **integration インスタンス（read only）を使うドメイン**：
  - nexus-group / nexus-identity / nexus-household

### 2.4 横断ドメインの接続

- 横断ドメイン（payment / accounting / reporting 等）は業務ドメイン DB にアクセス可能
- そのため、region / corp の切替が必要

### 2.5 P04-3 の確定事項

- P04-3 で integration の実 Oracle JDBC Read は成立済み
- env 注入・application-jdbc.yml 集約・local の .env 自動読み込み（bootRun 限定）も確定済み

 ---

 ## 3. 用語定義

### 3.1 Region（地区）

- 地区 = 別 Oracle インスタンス
- 値: `saitama` / `fukushima` / `tochigi`
- `integration` は地区ではない（統合DB インスタンス）

### 3.2 Corporation（法人）

- 地区インスタンス内に存在する法人
- 例: musashino / saikan / fukushisousai（saitama 地区内）

### 3.3 DomainAccount（業務ドメイン別 DB 接続アカウント種別）

- **DomainAccount** = 業務ドメイン別の DB 接続アカウント種別
  - **GOJO**: `XXX_gojo`（nexus-gojo ドメイン用）
  - **FUNERAL**: `XXX_sousai`（nexus-funeral ドメイン用）
- **master の扱い**：
  - `XXX_master` は DomainAccount に内包（synonym 経由で参照可能）
  - master は直接接続対象にしない

### 3.4 切替キー

- 切替キーは `(region, corporation, domainAccount)` の 3 要素
- この 3 要素により、接続先の DB 接続ユーザー/パスワードが決定される

 ---

## 4. 非目的（P04-4 では行わないこと）

 - JOIN の復活
 - 機能追加（新 API / 新エンドポイント）
 - 性能最適化（インデックス設計 / SQL チューニング）
 - Frontend 接続
 - 権限制御の実装
- DTO 変更
- 既存 API の JOIN 追加

 ---

## 5. 既存 DataSourceConfiguration の制約と、今回要件の矛盾点の明示

### 5.1 DataSourceConfiguration の制約

- 現状: 法人単位での DataSource 作成は禁止（設計憲法で確定）
- 同一地区内の全法人は同じ DataSource を共有する方針

### 5.2 P04-4 要件との矛盾点

- **P04-4 要件**: 業務ドメイン接続の切替単位は `(region, corporation, domainAccount)`
- **既存制約**: DataSource は region 単位で1つ
- **矛盾点**: 切替方式によっては、既存制約との整合性を確認する必要がある
- **注意**: 本ドキュメントでは解決策を断定しない。P04-4 で決めるべき決定事項として列挙する

## 6. P04-4 で決めるべき決定事項

 1. **切替方式の採用**
   - `(region, corporation, domainAccount)` による切替をどう実現するか
    - 採用理由の明文化

2. **RegionContext の拡張**
   - `(region, corporation, domainAccount)` を RegionContext に追加するか
   - 追加する場合の型定義（Corporation / DomainAccount の型定義）

 3. **環境変数の命名規則**
   - `(region, corporation, domainAccount)` ごとの接続情報をどう表現するか
   - **補足**: DomainAccount は業務名（GOJO / FUNERAL）であり、実体スキーマ名とは異なる。GOJO は `XXX_gojo`、FUNERAL は `XXX_sousai`（実体スキーマ名は sousai）に接続する。
   - 例: `NEXUS_DB_SAITAMA_MUSASHINO_GOJO_USER` / `NEXUS_DB_SAITAMA_MUSASHINO_FUNERAL_USER`
 
 4. **接続プール管理**
    - HikariCP の接続プールをどう管理するか
    - 動的切替時の接続プール再構築が必要か

 5. **エラーハンドリング**
   - `(region, corporation, domainAccount)` 未設定時の動作（FAIL FAST / フォールバック）
    - 接続ユーザー未定義時の動作

6. **master の扱い**
   - master は synonym 経由で参照可能だが、直接接続対象にしないことをどう実現するか

 ---

## 7. P04-4 Done 条件（再現可能な受入条件）

1. **切替設計の確定**
   - `(region, corporation, domainAccount)` による切替方式が決定し、設計ドキュメントに明文化されている

2. **実装の完了（設計着手のみの場合は設計完了）**
   - RegionContext に `(region, corporation, domainAccount)` が追加されている
   - DataSource 切替ロジックが実装されている
   - 環境変数の命名規則が確定している

3. **動作確認**
   - 同一 region 内の複数法人・複数 DomainAccount への切替が動作する
   - `(region, corporation, domainAccount)` 未設定時は FAIL FAST する
   - 接続ユーザー未定義時は FAIL FAST する

4. **ドキュメント更新**
   - 設計憲法に P04-4 の決定事項が追記されている
   - ロードマップに P04-4 の完了が記録されている

---

## 8. local での検証方針

- `X-NEXUS-REGION` / `X-NEXUS-CORP` / `X-NEXUS-DOMAIN-ACCOUNT` ヘッダーを検証用に任意で使える
- 必須化はしない（本番では権限制御等で決定される想定）
- local 環境での動作確認を可能にする

---

## 9. 次のフェーズ（P1）への前提

 - P04-4 で確定した切替方式は P1 以降も維持
 - JOIN 復活時も法人別スキーマ切替は影響を受けない
 - 性能最適化時も切替方式は変更しない

 ---

 以上。本書が P04-4 の設計の正本です。
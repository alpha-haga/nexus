# P1-1 実装ロードマップ  
（BFF 認可・Context 設定 実装フェーズ）

本ドキュメントは、P1-1 における **BFF の認可処理（Keycloak Claim → 403 判定 → Context set）の実装ロードマップ** を確定するものである。

**本書の位置づけ**  
- P04-5 は「設計の正」  
- 本書（P1-1）は「実装の進め方・境界・順序の正」  
- 本書は実装を含むが、設計の再定義は行わない  

設計の正は以下を必ず参照すること：  
- `docs/architecture/p04-5-keycloak-claims-db-routing.md`

---

## 1. P1-1 の目的

P1-1 の目的は以下である。

- Keycloak token の claim（`nexus_db_access`）を BFF で取得できる状態を作る
- リクエストの `(region, corporation, domainAccount)` が許可されているかを **BFF で fail fast 判定**できるようにする
- 許可された場合のみ Context（Region / Corporation / DomainAccount）を set する
- Infrastructure 層は Context を読むだけの状態を維持する

---

## 2. 前提（必読・再議論禁止）

### 2.1 設計の正

以下は **すでに確定しており、P1-1 で変更しない**。

- Claim 名・形式  
  - `nexus_db_access: List<String>`
  - 形式: `{region}__{corporation}__{domainAccount}`
- ワイルドカード規則  
  - `integration__ALL__GROUP` のみ許可
  - region 側での `ALL` 使用は禁止
- DomainAccount の決定方法  
  - token から推測しない
  - request path から決定する
- 認可責務  
  - BFF が判定
  - Infrastructure 層は判定しない

詳細は `p04-5-keycloak-claims-db-routing.md` を正とする。

---

## 3. 実装スコープ / 非スコープ

### 3.1 実装スコープ（P1-1 でやる）

- Keycloak access token から `nexus_db_access` claim を取得
- BFF に認可フィルターを追加
- 認可失敗時の 403 / 404 制御
- Context の set / clear
- local 検証ヘッダーとの併用（local プロファイル限定）

### 3.2 非スコープ（P1-1 ではやらない）

- Keycloak 側の設定変更
- Role / Claim 設計の変更
- Infrastructure 層での権限判定
- DomainAccount / Region / Corporation の再設計
- パフォーマンス最適化

---

## 4. 実装全体フロー（順序固定）

以下の順序が **実装順・理解順の正** である。

1. Authorization Filter を BFF に追加
2. Request path から DomainAccount を決定
3. token から `nexus_db_access` を取得
4. Region / Corporation の決定
5. 必要 role の組み立て
6. Claim 照合
7. 403 / 404 判定
8. Context set
9. finally で Context clear

---

## 5. Step 1: Authorization Filter 実装

### 5.1 役割

- すべての業務 API の入口で動作
- Controller に入る前に fail fast 判定を行う
- 判定成功時のみ Context を set する

### 5.2 実装方針

- Spring Filter（OncePerRequestFilter）
- BFF 層に配置
- try / finally で Context clear を保証

---

## 6. Step 2: DomainAccount 決定

### 6.1 決定方法

- request path の prefix から決定
- token claim から推測しない

| URL prefix | DomainAccount |
|-----------|---------------|
| `/api/v1/gojo/**` | GOJO |
| `/api/v1/funeral/**` | FUNERAL |
| `/api/v1/group/**` | 不要（integration） |
| `/api/v1/identity/**` | 不要（integration） |
| `/api/v1/household/**` | 不要（integration） |

### 6.2 該当しない場合

- **404 Not Found**
- 「存在しない API」として扱う

---

## 7. Step 3: Claim 取得

- access token から `nexus_db_access` を取得
- claim が存在しない / 空配列の場合 → **403 Forbidden**

---

## 8. Step 4: Region / Corporation 決定

### 8.1 Region

- 本番相当: token 由来（claim 名は後続）
- local 検証: `X-NEXUS-REGION` ヘッダーを許容

### 8.2 Corporation

- 本番相当: `nexus_db_access` の role 文字列から抽出
- local 検証: `X-NEXUS-CORP` ヘッダーを許容

---

## 9. Step 5: 認可判定

### 9.1 Integration API

- 必要 role: `integration__ALL__GROUP`
- 存在しない場合 → **403 Forbidden**
- 存在する場合:
  - RegionContext のみ set
  - Corporation / DomainAccount は set しない

### 9.2 Region API

- 必要 role: `{region}__{corporation}__{domainAccount}`
- 存在しない場合 → **403 Forbidden**
- 存在する場合:
  - Region / Corporation / DomainAccount Context を set

---

## 10. Context 管理

- Context は ThreadLocal
- **必ず finally で clear**
- clear 忘れは致命的バグ扱い

---

## 11. 403 / 404 の使い分け

### 403 Forbidden

- claim 不在
- role 不一致
- integration / region の誤用

### 404 Not Found

- DomainAccount に対応しない API パス

---

## 12. Done 条件（P1-1 完了条件）

- `nexus_db_access` を用いた認可判定が BFF で動作する
- 未許可リクエストが Controller に到達しない
- Context が正しく set / clear される
- Infrastructure 層に認可ロジックが存在しない
- local / 本番相当の切り分けが守られている

---

## 13. 次フェーズ（P1-2 以降）

- Region を token から完全決定する
- local 検証ヘッダーの段階的縮小
- 監査・ログ整備

---

以上。本書が P1-1 実装ロードマップの正本である。

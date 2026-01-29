# P1-A2 E2E 検証結果

## 1. 目的

P1-A2 の目的は、Keycloak から取得した実トークンを用いて、BFF の認可判定と API 動作を E2E で検証することである。

---

## 2. 検証前提

### 2.1 認可の source-of-truth

- BFF は Keycloak access token の `nexus_db_access: List<String>` を唯一の source-of-truth とする
- role 形式：`{region}__{corporation}__{domainAccount}`
- realm role は使用しない

### 2.2 BFF 入力規約（実測事実）

本 BFF は fail fast 設計のため、以下のリクエストヘッダーが必須である：

- すべての API リクエストにおいて `X-NEXUS-REGION` を必須とする
  - integration API も例外ではない（`X-NEXUS-REGION: integration` が必要）
- Region DB を参照する API においては、`X-NEXUS-CORP` も必須とする
- 上記ヘッダーが不足している場合、BFF は fail fast によりエラーを返却する
  - 現状、500 Internal Server Error を返すケースがある
- 現状の BFF 実装では actuator エンドポイント（`/actuator/**`）にも同一のフィルタが適用されるため、`/actuator/health` 等の確認にも `X-NEXUS-REGION` ヘッダーが必要

---

## 3. 対象 API と検証結果

### 3.1 Integration API（GROUP）

#### エンドポイント

- `GET /api/v1/group/contracts/search`

#### 検証結果

- リクエストヘッダー：
  - `Authorization: Bearer {token}`
  - `X-NEXUS-REGION: integration`
- 結果：`200 OK` を返却
- 認可判定：成功（`integration__ALL__GROUP` role が token に含まれている場合）

---

### 3.2 Region API（GOJO）

#### 存在するエンドポイント

- `GET /api/v1/gojo/contracts/local`
  - 必須パラメータ：`regionId`, `page`, `size`
- `GET /api/v1/gojo/contracts/all`
  - 必須パラメータ：`regionId`, `page`, `size`
  - オプションパラメータ：`corporationId`

#### 存在しないエンドポイント

- `GET /api/v1/gojo/contracts/search`
  - このエンドポイントは存在しない
  - リクエスト時は `404 Not Found` を返却（正しい挙動）

#### 検証結果

- 現時点では未実装または入力要件未確定として扱う
- `400 Bad Request` または `404 Not Found` となる挙動は P1-A2 の失敗を意味しない
- `500 Internal Server Error` となる場合は異常として扱う

---

## 4. ステータスコードの意味づけ

### 4.1 200 OK

- 認可判定が成功し、API が正常に処理を完了した場合に返却される
- 例：`GET /api/v1/group/contracts/search` で、token に `integration__ALL__GROUP` が含まれ、必須ヘッダーが揃っている場合

### 4.2 403 Forbidden

- 認可判定が失敗した場合に返却される（fail fast）
- 例：
  - token に `nexus_db_access` claim が存在しない
  - token に必要な role が含まれていない
  - 同一 DomainAccount で複数 Region または複数 Corporation が解釈される token（ヘッダーで一意化できない場合）

### 4.3 404 Not Found

- エンドポイントが存在しない場合に返却される
- 例：`GET /api/v1/gojo/contracts/search`（存在しないエンドポイント）

### 4.4 400 Bad Request

- リクエストパラメータが不正な場合に返却される
- 例：必須パラメータが不足している、パラメータの形式が不正

### 4.5 500 Internal Server Error

- サーバー内部エラーが発生した場合に返却される
- 例：必須ヘッダーが不足している場合に、現状 500 を返すケースがある（fail fast の実装が不完全な可能性）

---

## 5. 実施済み

### 5.1 Integration API の検証

- `GET /api/v1/group/contracts/search` が `200 OK` を返すことを確認
- Bearer token + `X-NEXUS-REGION: integration` で正常動作することを確認

### 5.2 Region API のエンドポイント確認

- `GET /api/v1/gojo/contracts/local` が存在することを確認
- `GET /api/v1/gojo/contracts/all` が存在することを確認
- `GET /api/v1/gojo/contracts/search` が存在しないことを確認（404 は正しい挙動）

### 5.3 認可判定の動作確認

- token に必要な role が含まれていない場合に `403 Forbidden` を返すことを確認
- fail fast が動作することを確認

---

## 6. スコープ外（未実施）

### 6.1 local 検証ヘッダーと token 由来の動作確認

- local プロファイルで `X-NEXUS-REGION` と `X-NEXUS-CORP` が両方ある場合のヘッダー優先動作の確認は未実施
- token 由来のみでの動作確認は未実施

### 6.2 同一 DomainAccount で複数 Region/Corporation の 403 確認

- token に同一 DomainAccount で複数 Region または複数 Corporation が含まれる場合の 403 判定確認は未実施

### 6.3 API 入力要件の確定

- API ごとの入力要件（必須パラメータ、バリデーション規則等）の確定は未実施
- gojo API の正しいクエリ形式確定は未実施

### 6.4 全エンドポイントの E2E 検証

- 全 API エンドポイントの E2E 検証は未実施
- 403 / 404 / 400 の適切な使い分け確認は未実施

---

## 7. Done 条件と到達状況

### 7.1 Done 条件（nexus-project-roadmap.md より）

- Keycloak から取得した実トークンで curl 検証
- 403 / 404 / 200 の確認
- local 検証ヘッダーと token 由来の動作確認
- 同一 DomainAccount で複数 Region/Corporation の 403 確認

### 7.2 到達状況

- **実施済み**：
  - Keycloak から取得した実トークンで curl 検証（Integration API）
  - 200 の確認（Integration API）
  - 403 の確認（role 不在時）
  - 404 の確認（存在しないエンドポイント）
  - Region API のエンドポイント存在確認

- **未実施**：
  - local 検証ヘッダーと token 由来の動作確認
  - 同一 DomainAccount で複数 Region/Corporation の 403 確認
  - 全エンドポイントの E2E 検証
  - 403 / 404 / 400 の適切な使い分け確認

---

## 8. 結論

P1-A2 の E2E 検証において、以下を確認した：

- Integration API（`GET /api/v1/group/contracts/search`）は、実トークンを用いて正常に動作する（200 OK）
- Region API（GOJO）のエンドポイント存在確認を実施し、`/local` と `/all` が存在し、`/search` が存在しないことを確認
- 認可判定の fail fast が動作することを確認（403 Forbidden）

一方で、以下の検証は未実施である：

- local 検証ヘッダーと token 由来の動作確認
- 同一 DomainAccount で複数 Region/Corporation の 403 確認
- 全エンドポイントの E2E 検証

現時点では、P1-A2 の Done 条件を完全には満たしていない。ただし、Integration API の正常動作と認可判定の基本動作は確認済みである。

---

## 9. 次フェーズへの引き継ぎ

### 9.1 未実施項目の引き継ぎ

- local 検証ヘッダーと token 由来の動作確認
- 同一 DomainAccount で複数 Region/Corporation の 403 確認
- 全エンドポイントの E2E 検証
- 403 / 404 / 400 の適切な使い分け確認

### 9.2 課題

- 必須ヘッダー不足時に 500 Internal Server Error を返すケースがある（fail fast の実装が不完全な可能性）
- gojo API の入力要件が未確定（400 / 404 の使い分けが不明確）

### 9.3 次フェーズ

- P1-A3（本番境界整理）に進む前に、上記未実施項目の検証を完了することが推奨される
- ただし、Integration API の正常動作が確認できているため、P1-B（業務成立・検索成立レーン）への並行着手は可能

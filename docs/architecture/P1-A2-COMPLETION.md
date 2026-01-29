# P1-A2 完了宣言

## 1. フェーズ前提（確定事項・再議論禁止）

- フェーズ：P1-A2（E2E 検証）
- P1-A0（Keycloak 設定）：完了済み
- P1-A1（BFF 認可・Context set 実装）：完了・マージ済み
- 本フェーズでは backend / BFF / Keycloak の変更は行っていない
- 設計の正は以下ドキュメントである：
  - docs/architecture/p04-5-keycloak-claims-db-routing.md
  - docs/architecture/p04-5b-keycloak-setup-guide.md
  - docs/architecture/p1-1-bff-authorization-implementation.md
  - docs/architecture/nexus-design-constitution.md
  - docs/architecture/nexus-project-roadmap.md

---

## 2. P1-A2 の目的（再掲）

P1-A2 は以下を目的として実施した：

- Keycloak → BFF → API の E2E 経路を実トークンで検証する
- 403 / 400 / 404 / 500 / 200 の挙動を意味づけする
- API ごとの必須ヘッダー・入力前提を明文化する
- 異常と未実装を切り分ける

---

## 3. 実施内容（事実のみ）

### 3.1 E2E 検証の実施

- `nexus_db_access` claim を用いた BFF 認可の E2E 確認を実施
- Keycloak から取得した実トークンで curl 検証を実施
- integration（GROUP）API にて正常系（200 OK）を確認

### 3.2 BFF 入力規約の実測確認

BFF 入力規約として以下が実測事実として確認された：

- すべての API リクエストにおいて `X-NEXUS-REGION` を必須とする
- integration API も例外ではない（`X-NEXUS-REGION: integration` が必要）
- Region DB を参照する API においては `X-NEXUS-CORP` も必須とする
- 上記ヘッダーが不足している場合、BFF は fail fast によりエラーを返却する
- 現状の BFF 実装では actuator エンドポイント（`/actuator/**`）にも同一のフィルタが適用されるため、`/actuator/health` 等の確認にも `X-NEXUS-REGION` ヘッダーが必要

### 3.3 API 実装状況の切り分け

- gojo API が現時点では未実装 / 入力未確定であることを確認
- 存在するエンドポイント：`/api/v1/gojo/contracts/local`、`/api/v1/gojo/contracts/all`
- 存在しないエンドポイント：`/api/v1/gojo/contracts/search`（404 は正しい挙動）

---

## 4. 検証結果の要約

### 4.1 Integration API（GROUP）

- エンドポイント：`GET /api/v1/group/contracts/search`
- 正常条件下で 200 OK を返却
- 権限不足時は 403 Forbidden を返却
- 入力不足時は 400 Bad Request を返却

### 4.2 Region API（GOJO）

- API は存在するが、現時点では未実装 / 入力要件未確定として扱う
- `400 Bad Request` または `404 Not Found` となる挙動は P1-A2 の失敗を意味しない
- `500 Internal Server Error` となる場合は異常として整理

### 4.3 ステータスコードの意味づけ

- 200 OK：認可判定が成功し、API が正常に処理を完了した場合
- 403 Forbidden：認可判定が失敗した場合（fail fast）
- 404 Not Found：エンドポイントが存在しない場合
- 400 Bad Request：リクエストパラメータが不正な場合
- 500 Internal Server Error：サーバー内部エラーが発生した場合（必須ヘッダー不足時に現状 500 を返すケースがある）

---

## 5. スコープ外事項の明確化

以下は P1-A2 のスコープ外である：

- gojo API を 200 にすることは P1-A2 の目的ではない
- actuator のステータス改善は本フェーズでは扱わない
- 仕様・実装変更は次フェーズの責務である
- local 検証ヘッダーと token 由来の動作確認は未実施（次フェーズの責務）
- 同一 DomainAccount で複数 Region/Corporation の 403 確認は未実施（次フェーズの責務）

---

## 6. Done 判定

以下の条件を満たすため、P1-A2 は完了と判断できる：

- E2E 正常系（integration）が確認できている
- 異常系と未実装の切り分けが文書化されている
- 設計ドキュメントとの矛盾がない
- 次フェーズに必要な前提条件が固定されている

---

## 7. 次フェーズへの引き継ぎ

### 7.1 次フェーズ

- 次フェーズは P1（本実装・表示・JOIN・性能）
- 本フェーズで確定した前提をもとに実装を進める

### 7.2 引き継ぎ事項

- BFF 入力規約（必須ヘッダー）が実測事実として確定している
- integration API の正常動作が確認できている
- gojo API は未実装 / 入力要件未確定として扱う
- 検証結果の詳細は [p1-a2-e2e-verification.md](./p1-a2-e2e-verification.md) を参照

---

## 8. 成果物

- [p1-a2-e2e-verification.md](./p1-a2-e2e-verification.md)（検証結果の詳細）

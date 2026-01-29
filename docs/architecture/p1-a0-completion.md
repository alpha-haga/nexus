# P1-A0 完了宣言

## 1. 実施内容

### Keycloak 設定

- `nexus-bff` client に以下の client role を作成：
  - `integration__ALL__GROUP`
  - `saitama__musashino__GOJO`
  - `saitama__musashino__FUNERAL`
- role 名は `{region}__{corporation}__{domainAccount}` 形式を遵守
- `corp-musashino` group に上記 client role を割り当て
- `dev-user` を `corp-musashino` group に所属させた
- `nexus-db-access` client scope を作成し、Protocol Mapper（`nexus-db-access-mapper`）を設定
- Protocol Mapper により、access token に `nexus_db_access: List<String>` がそのまま出力されることを確認
- `nexus-bff` client の Default client scopes に `nexus-db-access` を追加
- `nexus-frontend` client には `nexus-db-access` を割り当てていない（BFF の source-of-truth を保護）

### 未実施事項

- realm role は使用していない
- 独自 claim の追加は行っていない
- role 名の変換・正規化は行っていない（client role 名をそのまま claim に出力）

---

## 2. 検証結果

### Access Token 検証

- `nexus-bff` client で Password Grant により access token を取得
- access token をデコードし、`nexus_db_access` claim が配列形式で含まれることを確認
- claim 値は以下の通り：
  ```json
  {
    "nexus_db_access": [
      "integration__ALL__GROUP",
      "saitama__musashino__GOJO",
      "saitama__musashino__FUNERAL"
    ]
  }
  ```

### BFF 認可検証

- BFF が `nexus_db_access` claim を source-of-truth として認可判定を行っていることを確認
- BFF コード変更は行っていない（P1-A1 で実装済みの機能を使用）

### API 動作検証

#### Integration API（group）

- エンドポイント: `GET /api/v1/group/contracts/search?page=0&size=20`
- リクエストヘッダー:
  - `Authorization: Bearer {token}`
  - `X-NEXUS-REGION: integration`
- 結果: `200 OK` を返却

#### Region API（gojo）

- エンドポイント: `GET /api/v1/gojo/contracts/search?page=0&size=20`
- リクエストヘッダー:
  - `Authorization: Bearer {token}`
  - `X-NEXUS-REGION: saitama`
  - `X-NEXUS-CORP: musashino`
- 結果: `200 OK` を返却（認可判定は成功）

#### 403 検証

- エンドポイント: `GET /api/v1/gojo/contracts/search?page=0&size=20`
- リクエストヘッダー:
  - `Authorization: Bearer {token}`
  - `X-NEXUS-REGION: saitama`
  - `X-NEXUS-CORP: fukushisousai`（token に存在しない role）
- 結果: `403 Forbidden` を返却（fail fast が動作）

### 検証前提条件（BFF のヘッダー要件）

本 BFF は fail fast 設計のため、以下のリクエストヘッダーが必須：

- すべての API リクエストにおいて `X-NEXUS-REGION` を必須とする
  - integration API も例外ではない（`X-NEXUS-REGION: integration` が必要）
- Region DB を参照する API においては、`X-NEXUS-CORP` も必須とする
- 上記ヘッダーが不足している場合、BFF は fail fast によりエラーを返却する
- 現状の BFF 実装では actuator エンドポイント（`/actuator/**`）にも同一のフィルタが適用されるため、`/actuator/health` 等の確認にも `X-NEXUS-REGION` ヘッダーが必要

---

## 3. スコープ外事項

### gojo API の実装状況

- gojo API は現時点では未実装または入力要件未確定であり、400 / 404 となる挙動は P1-A0 の失敗を意味しない
- gojo API の正しいクエリ形式確定や E2E 網羅は P1-A2 の責務である

### API 入力要件の確定

- API ごとの入力要件（必須パラメータ、バリデーション規則等）の確定は P1-A2 の責務である
- P1-A0 では、認可判定が正常に動作し、少なくとも1つの実 API（group）が 200 を返すことを確認できれば十分

---

## 4. Done 判定

以下の条件を満たすため、P1-A0 は完了と判断できる：

- Keycloak access token に設計どおりの `nexus_db_access` が出力されている
- BFF が当該 claim を用いて認可・Context set を行っている
- 少なくとも1つの実 API（group）が 403 ではなく 200 を返している
- 設計ドキュメント（p04-5-keycloak-claims-db-routing.md, p04-5b-keycloak-setup-guide.md）との矛盾がない

---

## 5. 次フェーズへの引き継ぎ

### 次フェーズ: P1-A2（E2E 検証）

- 主目的: 403 / 400 / 200 の整理と API ごとの入力要件確定
- 検証範囲:
  - 全 API エンドポイントの E2E 検証
  - 403 / 404 / 400 の適切な使い分け確認
  - API 入力要件の確定とドキュメント化
  - local 検証ヘッダーと token 由来の動作確認
  - 同一 DomainAccount で複数 Region/Corporation の 403 確認

### 引き継ぎ事項

- Keycloak 設定は完了済み（本宣言文の実施内容を参照）
- BFF 認可実装は P1-A1 で完了済み（コード変更不要）
- 検証手順書: [P1-A0-VERIFICATION.A.md](./P1-A0-VERIFICATION.A.md) を参照

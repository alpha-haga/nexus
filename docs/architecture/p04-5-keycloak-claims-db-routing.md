# P04-5 設計（Keycloak Claim による権限制御と DB Routing）

本ドキュメントは、P04-5 における **Keycloak token claim から権限（Region×Corporation×DomainAccount）を取得し、BFF で fail fast する設計** を確定するためのものです。

---

## 1. 目的

P04-5 の目的は以下です：

- BFF が Keycloak token の claim から許可された `(region, corporation, domainAccount)` を取得する
- リクエストの `(region, corporation, domainAccount)` が許可されているかを判定し、未許可の場合は 403 を返す（fail fast）
- 許可されている場合のみ、RegionContext / CorporationContext / DomainAccountContext を設定する
- Infrastructure 層は Context を読むだけであり、権限判定は行わない

---

## 2. 前提（既存方針との整合）

### 2.1 既存の責務分離方針

- **Presentation 層（BFF）**: 権限判定を行い、Context を設定する
- **Infrastructure 層**: Context を読むだけ（権限判定しない）
- **Fail Fast 方針**: 未許可のリクエストは早期に拒否する

### 2.2 DomainAccount の定義（P04-4 確定事項）

- **GOJO**: `XXX_gojo`（nexus-gojo ドメイン用）
- **FUNERAL**: `XXX_sousai`（nexus-funeral ドメイン用）
- master は DomainAccount に内包（synonym 経由で参照可能）だが、直接接続対象にしない

### 2.3 Context の設定（P04-4 確定事項）

- RegionContext / CorporationContext / DomainAccountContext は ThreadLocal で管理
- Presentation 層（BFF）が set/clear する
- Infrastructure 層（DataSource 層）が読む

---

## 3. Keycloak Claim 設計

### 3.1 Claim 名

- **Claim 名**: `nexus_db_access`
- **Claim タイプ**: `List<String>`

### 3.2 Claim 形式

各要素は以下の形式の文字列：

```
"{region}:{corporation}:{domainAccount}"
```

**例**:
- `"saitama:musashino:GOJO"`
- `"saitama:musashino:FUNERAL"`
- `"fukushima:fukushima:GOJO"`

### 3.3 ワイルドカード方針

- **`"*"` は integration の corp 不要表現のみ許可**（region では禁止）
- **integration の場合**: `"integration:*:GOJO"` または `"integration:*:FUNERAL"` を許可
  - integration DB は法人横断検索専用のため、corporation は不要
- **region の場合**: `"*"` は禁止（region では必ず具体的な corporation を指定）

**許可される例**:
- `"integration:*:GOJO"`（integration DB への GOJO アクセス）
- `"integration:*:FUNERAL"`（integration DB への FUNERAL アクセス）

**禁止される例**:
- `"saitama:*:GOJO"`（region では `*` は禁止）
- `"*:musashino:GOJO"`（region では `*` は禁止）

### 3.4 DomainAccount の決め方

- **API の所属ドメインで固定**
  - `nexus-gojo` ドメインの API → `GOJO`
  - `nexus-funeral` ドメインの API → `FUNERAL`
- リクエストパスから自動判定する（例: `/api/v1/gojo/...` → `GOJO`、`/api/v1/funeral/...` → `FUNERAL`）

---

## 4. BFF の責務

### 4.1 処理フロー

1. **Token から claim を取得**: Keycloak token から `nexus_db_access` claim を取得
2. **リクエストから `(region, corporation, domainAccount)` を抽出**:
   - region: リクエストヘッダーまたは API パスから判定
   - corporation: リクエストヘッダーまたは API パスから判定
   - domainAccount: API の所属ドメインから自動判定（gojo → GOJO、funeral → FUNERAL）
3. **許可判定**: claim に含まれる `(region, corporation, domainAccount)` の組み合わせが許可されているか判定
4. **未許可の場合**: 403 Forbidden を返す（fail fast）
5. **許可されている場合**: RegionContext / CorporationContext / DomainAccountContext を設定
6. **レスポンス後**: Context を clear（finally で確実にクリア）

### 4.2 Infrastructure 層の責務

- **Context を読むだけ**（権限判定は行わない）
- DataSource 切替は Context の値に基づいて行う
- Context 未設定時は既存の fail fast 機構（DomainAccountContextNotSetException 等）で例外を投げる

---

## 5. ローカル検証ヘッダー（検証専用）

### 5.1 現状（P04-4 確定事項）

- `X-NEXUS-REGION` / `X-NEXUS-CORP` / `X-NEXUS-DOMAIN-ACCOUNT` ヘッダーを検証用に使用可能
- local プロファイルでのみ有効

### 5.2 将来の移行方針

- **local 検証ヘッダーは「検証専用」であり、本番では token 由来にする**
- P1-1 以降で Keycloak token からの claim 取得に移行する
- 移行後も local 環境では検証ヘッダーを併用可能（開発・テスト用途）

---

## 6. 例（サンプル）

### 6.1 例 1: Saitama Region - Musashino Corporation - GOJO DomainAccount

**Token claim (`nexus_db_access`)**:
```json
[
  "saitama:musashino:GOJO",
  "saitama:musashino:FUNERAL"
]
```

**リクエスト**:
- API パス: `/api/v1/gojo/contracts/search`
- リクエストヘッダー: `X-NEXUS-REGION: saitama`, `X-NEXUS-CORP: musashino`

**判定結果**:
- region: `saitama`
- corporation: `musashino`
- domainAccount: `GOJO`（API パスから自動判定）
- claim に `"saitama:musashino:GOJO"` が含まれている → **許可**
- Context を設定して処理継続

### 6.2 例 2: Integration DB - FUNERAL DomainAccount（ワイルドカード使用）

**Token claim (`nexus_db_access`)**:
```json
[
  "integration:*:FUNERAL"
]
```

**リクエスト**:
- API パス: `/api/v1/funeral/contracts/search`
- リクエストヘッダー: `X-NEXUS-REGION: integration`

**判定結果**:
- region: `integration`
- corporation: `*`（integration では corp 不要）
- domainAccount: `FUNERAL`（API パスから自動判定）
- claim に `"integration:*:FUNERAL"` が含まれている → **許可**
- Context を設定して処理継続

### 6.3 例 3: 未許可のリクエスト（403 を返す）

**Token claim (`nexus_db_access`)**:
```json
[
  "saitama:musashino:GOJO"
]
```

**リクエスト**:
- API パス: `/api/v1/gojo/contracts/search`
- リクエストヘッダー: `X-NEXUS-REGION: saitama`, `X-NEXUS-CORP: fukushisousai`

**判定結果**:
- region: `saitama`
- corporation: `fukushisousai`
- domainAccount: `GOJO`（API パスから自動判定）
- claim に `"saitama:fukushisousai:GOJO"` が含まれていない → **未許可**
- **403 Forbidden を返す**（fail fast）

---

## 7. P04-5 Done 条件（設計確定条件）

1. **Claim 形式の確定**
   - Claim 名: `nexus_db_access`
   - Claim 形式: `List<String>`（各要素は `"{region}:{corporation}:{domainAccount}"`）
   - ワイルドカード方針: `"*"` は integration の corp 不要表現のみ許可

2. **判定責務の確定**
   - BFF が token claim から許可判定を行い、Context を設定する
   - Infrastructure 層は Context を読むだけ（権限判定しない）

3. **Fail Fast 方針の確定**
   - 未許可のリクエストは 403 Forbidden を返す
   - Infrastructure 層での権限判定は行わない

4. **ドキュメント化**
   - 本設計ドキュメントに上記が明文化されている

**注意**: P04-5 は設計確定フェーズであり、実装は含めない。実装は P1-1 で行う。

---

## 8. P1-1 での利用ポイント

P1-1 で本設計に基づいて実装する際のポイント：

1. **Keycloak token の claim 取得**
   - BFF で Keycloak token を解析し、`nexus_db_access` claim を取得する実装

2. **許可判定ロジックの実装**
   - リクエストの `(region, corporation, domainAccount)` と claim を照合するロジック
   - ワイルドカード `"*"` の処理（integration のみ許可）

3. **Context 設定フィルターの実装**
   - 許可判定後に RegionContext / CorporationContext / DomainAccountContext を設定するフィルター
   - 未許可の場合は 403 を返す

4. **ローカル検証ヘッダーとの併用**
   - local 環境では検証ヘッダーを併用可能（開発・テスト用途）
   - 本番環境では token 由来のみを使用

---

以上。本書が P04-5 の設計の正本です。

# P04-5 設計（Keycloak Claim による権限制御と DB Routing）

本ドキュメントは、P04-5 における **Keycloak token claim から権限（Region×Corporation×DomainAccount）を取得し、BFF で fail fast する設計** を確定するためのものです。

**本書の位置づけ**: 設計の正本。実装は含めない（P1-1 で実装）。Keycloak の設定手順は [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md) に委譲。

---

## 1. 目的

P04-5 の目的は以下です：

- Keycloak token claim（`nexus_db_access`）から `(region, corporation, domainAccount)` の許可判定を行う設計を確定する
- BFF が 403 判定と Context set を行うことを明示する
- `nexus_db_access` claim の照合規約を固定する

**注意**: P04-5 は `nexus_db_access` の照合規約を固定する。Region を token からどう決めるか（claim 名等）は P1-1 で確定する。

---

## 2. 用語と前提整理

### 2.1 既存の責務分離方針（P04-4 確定事項）

- **Presentation 層（BFF）**: 権限判定を行い、Context を設定する
- **Infrastructure 層**: Context を読むだけ（権限判定しない）
- **Fail Fast 方針**: 未許可のリクエストは早期に拒否する（403 Forbidden）

### 2.2 Context の設定（P04-4 確定事項）

- RegionContext / CorporationContext / DomainAccountContext は ThreadLocal で管理
- Presentation 層（BFF）が set/clear する
- Infrastructure 層（DataSource 層）が読む

### 2.3 DomainAccount の定義（P04-4 確定事項）

- **GOJO**: `XXX_gojo`（nexus-gojo ドメイン用）
- **FUNERAL**: `XXX_sousai`（nexus-funeral ドメイン用）
- master は DomainAccount に内包（synonym 経由で参照可能）だが、直接接続対象にしない

### 2.4 GROUP の扱い（重要）

- **GROUP は DomainAccount enum ではない**
- **GROUP は role 文字列上の予約語**（`integration__ALL__GROUP` の domainAccount 部分）
- integration DB は corporation / domainAccount を Context に持たない
- integration API では DomainAccountContext を set しない

### 2.5 Region / Corporation / DomainAccount の決定方針

- **DomainAccount**: request path から決定（token から推測しない）
- **Region**: 本番相当は token 由来（claim 名は P1-1 で確定）、local 検証は `X-NEXUS-REGION` ヘッダーを許容
- **Corporation**: 本番相当は token（`nexus_db_access` の role から抽出）、local 検証は `X-NEXUS-CORP` ヘッダーを許容

---

## 3. Keycloak Claim 設計（nexus_db_access）

### 3.1 Claim 名と型

- **Claim 名**: `nexus_db_access`
- **Claim 型**: `List<String>`

### 3.2 Claim 要素形式

各要素は以下の形式の文字列：

```
"{region}__{corporation}__{domainAccount}"
```

**区切り文字**: `__`（アンダースコア2つ）

**例**:
- `saitama__musashino__GOJO`
- `saitama__musashino__FUNERAL`
- `fukushima__fukushima__GOJO`
- `integration__ALL__GROUP`

### 3.3 DomainAccount の表記規則

- DomainAccount は **大文字固定**（GOJO / FUNERAL / GROUP）
- `nexus_db_access` の値は client role 名そのまま（変換/接頭辞付与なし）

### 3.4 ワイルドカード規則（重要）

- **`ALL` は `integration__ALL__GROUP` のみ許可**
- **region 側での `ALL` は禁止**

**理由**: 事故防止のため。region 側で wildcard を許可すると、意図しない法人へのアクセスが発生する可能性がある。

**許可される例**:
- `integration__ALL__GROUP`（integration DB への GROUP アクセス）

**禁止される例**:
- `saitama__ALL__GOJO`（region では `ALL` は禁止）
- `ALL__musashino__GOJO`（region では `ALL` は禁止）

---

## 4. BFF における処理順（最重要）

以下の処理フローを厳守する。この順序が実装順・理解順の正である。

### 4.1 Request 受付

- HTTP リクエストを受付

### 4.2 DomainAccount 決定（request path から）

- Request path から DomainAccount を決定（5 の表を参照）
- **MUST NOT**: token claim から DomainAccount を推測しない
- Path が DomainAccount に対応しない場合 → **404 Not Found**（存在しない API として扱う）

### 4.3 Region 決定

#### 4.3.1 本番相当（原則）

- **Region は認証済み token の claim を正とする**
- 具体的な claim 名・形式は設計側で後続定義（P1-1 実装時に確定）

#### 4.3.2 Local 検証（現段階）

- `X-NEXUS-REGION` ヘッダーを許容する
- P04-5 / local 検証段階では、ヘッダー由来の Region を許容する

**注意**: Local 検証と本番相当は区別して扱う。混ぜない。

### 4.4 Integration の場合の判定フロー

1. **必要 role の確認**: `integration__ALL__GROUP`
2. **Claim 照合**: `nexus_db_access` 配列に `integration__ALL__GROUP` が存在するか確認
3. **未許可の場合**: → **403 Forbidden**
4. **許可されている場合**:
   - RegionContext のみ set（integration の場合）
   - CorporationContext / DomainAccountContext は set しない

### 4.5 Region の場合の判定フロー

1. **Corporation の決定**:
   - 本番相当: token の `nexus_db_access` 配列から該当 role を抽出し、corporation 部分を取得
   - Local 検証: `X-NEXUS-CORP` ヘッダーから取得（検証専用）
2. **必要 role の構築**: `{region}__{corporation}__{domainAccount}`
3. **Claim 照合**: 該当 role が `nexus_db_access` 配列に存在するか確認
4. **未許可の場合**: → **403 Forbidden**
5. **許可されている場合**:
   - RegionContext / CorporationContext / DomainAccountContext を set

### 4.6 Context set

- 許可判定が成功した場合のみ、該当する Context を set する
- Integration の場合: RegionContext のみ
- Region の場合: RegionContext / CorporationContext / DomainAccountContext

### 4.7 Finally で clear

- ThreadLocal リーク防止のため、必ず finally で Context を clear

---

## 5. DomainAccount 決定規則

DomainAccount は token から推測しない。BFF が request path から決定する。

| URL prefix | DomainAccount | 備考 |
|------------|---------------|------|
| `/api/v1/gojo/**` | `GOJO` | nexus-gojo ドメイン |
| `/api/v1/funeral/**` | `FUNERAL` | nexus-funeral ドメイン |
| `/api/v1/group/**` | DomainAccount 不要 | integration 専用（GROUP 扱いは role のみ） |
| `/api/v1/identity/**` | DomainAccount 不要 | integration 専用（GROUP 扱いは role のみ） |
| `/api/v1/household/**` | DomainAccount 不要 | integration 専用（GROUP 扱いは role のみ） |

**重要**: DomainAccount は token の claim から推測しない。request path のみを正とする。

---

## 6. 403 / 404 の使い分け

### 6.1 403 Forbidden（未許可）

以下の場合に 403 を返す：

- `nexus_db_access` claim が存在しない、または空配列の場合（認可情報なし）
- 必要な role が `nexus_db_access` 配列に存在しない場合（role 不一致）
  - 例: Integration API に region role だけ付いている等

### 6.2 404 Not Found（存在しない API）

以下の場合に 404 を返す：

- Request path が DomainAccount 決定規則（5 の表）に該当しない場合（存在しない API として扱う）

---

## 7. ローカル検証ヘッダーの位置づけ

### 7.1 現状（P04-4 確定事項）

- `X-NEXUS-REGION` / `X-NEXUS-CORP` / `X-NEXUS-DOMAIN-ACCOUNT` ヘッダーを検証用に使用可能
- local プロファイルでのみ有効

### 7.2 将来の移行方針

- **Local 検証ヘッダーは「検証専用」であり、本番では token 由来にする**
- P1-1 以降で Keycloak token からの claim 取得に移行する
- 移行後も local 環境では検証ヘッダーを併用可能（開発・テスト用途）
- **将来の削除前提**であることを明記

---

## 8. Infrastructure 層の責務

- **Context を読むだけ**（権限判定は行わない）
- DataSource 切替は Context の値に基づいて行う
- Context 未設定時は既存の fail fast 機構（DomainAccountContextNotSetException 等）で例外を投げる

---

## 9. 具体例（サンプル）

### 9.1 例 1: Saitama Region - Musashino Corporation - GOJO DomainAccount

**Token claim (`nexus_db_access`)**:
```json
[
  "saitama__musashino__GOJO",
  "saitama__musashino__FUNERAL"
]
```

**リクエスト**:
- API パス: `/api/v1/gojo/contracts/search`
- リクエストヘッダー（local 検証）: `X-NEXUS-REGION: saitama`, `X-NEXUS-CORP: musashino`

**処理フロー**:
1. DomainAccount 決定: `GOJO`（API パスから自動判定）
2. Region 決定: `saitama`（local 検証ではヘッダーから、本番相当では token から）
3. Corporation 決定: `musashino`（local 検証ではヘッダーから、本番相当では token から）
4. 必要 role: `saitama__musashino__GOJO`
5. Claim 照合: `saitama__musashino__GOJO` が claim に含まれている → **許可**
6. Context set: RegionContext / CorporationContext / DomainAccountContext を set
7. 処理継続

### 9.2 例 2: Integration DB - GROUP（ALL 使用）

**Token claim (`nexus_db_access`)**:
```json
[
  "integration__ALL__GROUP"
]
```

**リクエスト**:
- API パス: `/api/v1/group/contracts/search`
- リクエストヘッダー（local 検証）: `X-NEXUS-REGION: integration`

**処理フロー**:
1. DomainAccount 決定: 不要（integration 専用）
2. Region 決定: `integration`（local 検証ではヘッダーから、本番相当では token から）
3. 必要 role: `integration__ALL__GROUP`
4. Claim 照合: `integration__ALL__GROUP` が claim に含まれている → **許可**
5. Context set: RegionContext のみ set（CorporationContext / DomainAccountContext は set しない）
6. 処理継続

### 9.3 例 3: 未許可のリクエスト（403 を返す）

**Token claim (`nexus_db_access`)**:
```json
[
  "saitama__musashino__GOJO"
]
```

**リクエスト**:
- API パス: `/api/v1/gojo/contracts/search`
- リクエストヘッダー（local 検証）: `X-NEXUS-REGION: saitama`, `X-NEXUS-CORP: fukushisousai`

**処理フロー**:
1. DomainAccount 決定: `GOJO`（API パスから自動判定）
2. Region 決定: `saitama`
3. Corporation 決定: `fukushisousai`
4. 必要 role: `saitama__fukushisousai__GOJO`
5. Claim 照合: `saitama__fukushisousai__GOJO` が claim に含まれていない → **未許可**
6. **403 Forbidden を返す**（fail fast）

---

## 10. P04-5 Done 条件（設計確定条件）

以下の条件を満たした場合、P04-5 は完了とする：

1. **Claim 形式の確定**
   - Claim 名: `nexus_db_access`
   - Claim 形式: `List<String>`（各要素は `"{region}__{corporation}__{domainAccount}"`）
   - DomainAccount は大文字固定（GOJO / FUNERAL / GROUP）
   - ワイルドカード方針: `ALL` は integration の corp 不要表現のみ許可

2. **判定責務の確定**
   - BFF が token claim から許可判定を行い、Context を設定する
   - Infrastructure 層は Context を読むだけ（権限判定しない）

3. **Fail Fast 方針の確定**
   - 未許可のリクエストは 403 Forbidden を返す
   - Infrastructure 層での権限判定は行わない

4. **決定規則の固定**
   - DomainAccount の決定規則（request path から決定、表で明記）
   - Region の決定規則（本番相当と local 検証を区別）
   - Corporation の決定規則（本番相当と local 検証を区別）

5. **処理フローの固定**
   - 認可判定と Context set の順序（処理フローとして箇条書きで固定）
   - 失敗時の扱い（404/403 の使い分けを固定）

6. **ドキュメント化**
   - 本設計ドキュメントに上記が明文化されている
   - 実設定手順書へのリンク: [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md)

**注意**: P04-5 は設計確定フェーズであり、実装は含めない。実装は P1-1 で行う。

---

## 11. P1-1 での実装境界

P1-1 で本設計に基づいて実装する際の境界：

1. **Keycloak token の claim 取得**
   - BFF で Keycloak token を解析し、`nexus_db_access` claim を取得する実装

2. **Region の token 由来決定**
   - Region を token からどう決めるか（claim 名・形式等）を確定・実装する

3. **許可判定ロジックの実装**
   - リクエストの `(region, corporation, domainAccount)` と claim を照合するロジック
   - `ALL` の処理（integration のみ許可）

4. **Context 設定フィルターの実装**
   - 許可判定後に RegionContext / CorporationContext / DomainAccountContext を設定するフィルター
   - 未許可の場合は 403 を返す

5. **ローカル検証ヘッダーとの併用**
   - local 環境では検証ヘッダーを併用可能（開発・テスト用途）
   - 本番環境では token 由来のみを使用

---

## 補足

- 実設定手順書は [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md) を参照してください。

以上。本書が P04-5 の設計の正本です。

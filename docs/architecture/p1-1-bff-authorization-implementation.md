# P1-1 実装ロードマップ（確定版）
（BFF 認可・Context 設定 実装フェーズ）

本ドキュメントは、P1-1 における **BFF の認可処理（Keycloak Claim → 403 判定 → Context set）の実装ロードマップ** を確定するものである。

**本書の位置づけ**  
- P04-5 は「設計の正」  
- 本書（P1-1）は「実装の進め方・境界・順序の正」  
- 本書は実装を含むが、設計の再定義は行わない  

設計の正は以下を必ず参照すること：  
- `docs/architecture/p04-5-keycloak-claims-db-routing.md`
- `docs/architecture/p04-5b-keycloak-setup-guide.md`

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
  - role 名そのまま（変換/接頭辞付与なし）
- 要素形式
  - `{region}__{corporation}__{domainAccount}`（domainAccount は大文字固定）
- ワイルドカード規則  
  - `integration__ALL__GROUP` のみ許可
  - region 側での `ALL` 使用は禁止
- DomainAccount の決定方法  
  - token から推測しない
  - request path から決定する
- 認可責務  
  - BFF が判定
  - Infrastructure 層は判定しない
- Integration API の扱い
  - Integration API は RegionContext のみ set（Corp/DomainAccount は set しない）
- Fail fast 規則
  - 同一 DomainAccount で複数 Region / 複数 Corporation が解釈される token は 403（fail fast）
- 検証ヘッダー
  - local では検証ヘッダー（X-NEXUS-REGION / X-NEXUS-CORP）併用可
  - 本番では無効（token 由来のみ）

詳細は `p04-5-keycloak-claims-db-routing.md` を正とする。

---

## 3. 実装スコープ / 非スコープ

### 3.1 実装スコープ（P1-1 でやる）

- Keycloak access token から `nexus_db_access` claim を取得
- BFF に認可フィルター（NexusAuthorizationContextFilter）を追加
- `extractDbAccessRolesOrFail` 関数の実装（claim 取得・検証）
- 認可失敗時の 403 / 404 制御
- Context の set / clear
- local 検証ヘッダーとの併用（local プロファイル限定）
- 同一 DomainAccount で複数 Region/Corporation が解釈される token の 403 判定

### 3.2 非スコープ（P1-1 ではやらない）

- Keycloak 側の設定変更
- Role / Claim 設計の変更
- Infrastructure 層での権限判定
- DomainAccount / Region / Corporation の再設計
- パフォーマンス最適化

---

## 4. 実装全体フロー（順序固定）

以下の順序が **実装順・理解順の正** である。

1. Authorization Filter（NexusAuthorizationContextFilter）を BFF に追加
2. Request path から DomainAccount を決定
3. token から `nexus_db_access` を取得（`extractDbAccessRolesOrFail`）
4. Region / Corporation の決定
5. 同一 DomainAccount で複数 Region/Corporation が解釈される token の検証（403）
6. 必要 role の組み立て
7. Claim 照合
8. 403 / 404 判定
9. Context set
10. finally で Context clear

---

## 5. Step 1: Authorization Filter 実装

### 5.1 役割

- すべての業務 API の入口で動作
- Controller に入る前に fail fast 判定を行う
- 判定成功時のみ Context を set する

### 5.2 実装方針

- Spring Filter（OncePerRequestFilter）
- BFF 層に配置
- クラス名: `NexusAuthorizationContextFilter`
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

## 7. Step 3: Claim 取得（`extractDbAccessRolesOrFail`）

### 7.1 役割

- access token から `nexus_db_access` claim を取得
- claim が存在しない / 空配列の場合 → **403 Forbidden**

### 7.2 実装方針

- 関数名: `extractDbAccessRolesOrFail`
- Keycloak token から `nexus_db_access` claim を取得
- `List<String>` として返却
- claim 不在 / 空配列の場合は例外を投げ（403 に変換）

---

## 8. Step 4: Region / Corporation 決定

### 8.1 Region

- 本番相当: `nexus_db_access` の role 文字列から決定（別claimは作らない）
- local 検証: `X-NEXUS-REGION` ヘッダーを許容（local プロファイル限定）
- 本番環境では検証ヘッダーは無効

### 8.2 Corporation

- 本番相当: `nexus_db_access` の role 文字列から抽出
- local 検証: `X-NEXUS-CORP` ヘッダーを許容（local プロファイル限定）
- 本番環境では検証ヘッダーは無効

---

## 9. Step 5: 同一 DomainAccount で複数 Region/Corporation の検証（fail fast）

### 9.1 検証規則

- 同一 DomainAccount で複数の Region または複数の Corporation が解釈される token は **403 Forbidden** を返す（fail fast）
- これは、token の `nexus_db_access` 配列に、同一 DomainAccount に対して異なる Region または異なる Corporation の role が含まれている場合を指す

### 9.2 検証例

**403 を返すべきケース**:
- DomainAccount が `GOJO` で、`nexus_db_access` に `saitama__musashino__GOJO` と `fukushima__fukushima__GOJO` が両方含まれている（複数 Region）
- DomainAccount が `GOJO` で、`nexus_db_access` に `saitama__musashino__GOJO` と `saitama__saikan__GOJO` が両方含まれている（複数 Corporation）

**許可されるケース**:
- DomainAccount が `GOJO` で、`nexus_db_access` に `saitama__musashino__GOJO` のみ含まれている
- DomainAccount が `GOJO` で、`nexus_db_access` に `saitama__musashino__GOJO` と `saitama__musashino__FUNERAL` が含まれている（異なる DomainAccount は許可）

---

## 10. Step 6: 認可判定

### 10.1 Integration API

- 必要 role: `integration__ALL__GROUP`
- 存在しない場合 → **403 Forbidden**
- 存在する場合:
  - RegionContext のみ set
  - Corporation / DomainAccount は set しない

### 10.2 Region API

- 必要 role: `{region}__{corporation}__{domainAccount}`
- 存在しない場合 → **403 Forbidden**
- 存在する場合:
  - Region / Corporation / DomainAccount Context を set

---

## 11. Context 管理

- Context は ThreadLocal
- **必ず finally で clear**
- clear 忘れは致命的バグ扱い

---

## 12. 403 / 404 の使い分け

### 12.1 403 Forbidden

- claim 不在
- role 不一致
- integration / region の誤用
- 同一 DomainAccount で複数 Region/Corporation が解釈される token

### 12.2 404 Not Found

- DomainAccount に対応しない API パス

---

## 13. Done 条件（P1-1 完了条件）

以下の条件を満たした場合、P1-1 は完了とする：

1. **実装完了**
   - `NexusAuthorizationContextFilter` が実装されている
   - `extractDbAccessRolesOrFail` 関数が実装されている
   - `nexus_db_access` を用いた認可判定が BFF で動作する
   - 未許可リクエストが Controller に到達しない
   - Context が正しく set / clear される
   - Infrastructure 層に認可ロジックが存在しない
   - local / 本番相当の切り分けが守られている
   - 同一 DomainAccount で複数 Region/Corporation が解釈される token が 403 を返す

2. **検証完了**
   - 検証手順（後述）をすべて実行し、期待通り動作することを確認

---

## 14. 検証観点

### 14.1 基本動作

#### Integration API
- `integration__ALL__GROUP` が token に含まれていれば 200 OK
- `integration__ALL__GROUP` が無ければ 403 Forbidden
- RegionContext のみ set（Corp/DomainAccount は set されない）

#### Region API（token ベース）
- token に同一 DomainAccount の role が1つだけある場合: 200 OK
- token に同一 DomainAccount の role が複数ある場合（複数 region または複数 corp）: 403 Forbidden
- requiredRole が token に無い場合: 403 Forbidden

#### Region API（local ヘッダー優先）
- local プロファイルかつ `X-NEXUS-REGION` と `X-NEXUS-CORP` が両方存在する場合:
  - ヘッダーで region/corp を確定
  - requiredRole が token に存在すれば 200 OK（token の複数候補チェックは行わない）
  - requiredRole が token に無ければ 403 Forbidden
- local プロファイルだがヘッダーが無い or 片方欠けの場合:
  - token ベースの判定にフォールバック（複数候補チェックあり）

### 14.2 重要な検証ポイント

1. **同一 DomainAccount で複数 Region/Corporation の扱い**
   - token に同一 DomainAccount の複数 region/corp が混在していても、local で `X-NEXUS-REGION` と `X-NEXUS-CORP` を両方付ければ requiredRole がある限り通る
   - ヘッダー無し（または片方欠け）で複数候補なら 403

2. **Integration API の権限チェック**
   - integration は `integration__ALL__GROUP` が無ければ 403
   - Integration API では RegionContext のみ set（Corp/DomainAccount は set しない）

3. **検証ヘッダーの扱い**
   - local プロファイルでのみ有効
   - 本番環境では無効（token 由来のみ）

---

## 15. 検証手順（curl 想定）

### 15.1 前提

- Keycloak が設定済みで、`nexus_db_access` claim が token に含まれること
- local 環境で検証ヘッダーを使用可能なこと

### 15.2 検証ケース 1: 正常系（Region API - GOJO）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: musashino"
```

**期待結果**: 200 OK、Context が正しく set される

### 15.3 検証ケース 2: 正常系（Integration API）

**Token claim (`nexus_db_access`)**: `["integration__ALL__GROUP"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/group/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: integration"
```

**期待結果**: 200 OK、RegionContext のみ set（Corp/DomainAccount は set されない）

### 15.4 検証ケース 3: 403（role 不一致）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: fukushisousai"
```

**期待結果**: 403 Forbidden（role 不一致）

### 15.5 検証ケース 4: 403（claim 不在）

**Token claim (`nexus_db_access`)**: `[]` または claim 不在

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: musashino"
```

**期待結果**: 403 Forbidden（認可情報なし）

### 15.6 検証ケース 5: 正常系（local ヘッダー優先 - 複数候補あり）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO", "fukushima__fukushima__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: musashino"
```

**期待結果**: 200 OK（ヘッダーで一意化されるため、token の複数候補チェックは行わない）

### 15.7 検証ケース 6: 403（同一 DomainAccount で複数 Region - ヘッダー無し）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO", "fukushima__fukushima__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

**期待結果**: 403 Forbidden（ヘッダー無しのため token ベース判定、複数候補で 403）

### 15.8 検証ケース 7: 403（同一 DomainAccount で複数 Corporation - ヘッダー無し）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO", "saitama__saikan__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>"
```

**期待結果**: 403 Forbidden（ヘッダー無しのため token ベース判定、複数候補で 403）

### 15.9 検証ケース 8: 403（local ヘッダー優先 - requiredRole 不在）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: fukushisousai"
```

**期待結果**: 403 Forbidden（requiredRole が token に無い）

### 15.10 検証ケース 9: 403（integration に region role のみ）

**Token claim (`nexus_db_access`)**: `["saitama__musashino__GOJO"]`

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/group/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: integration"
```

**期待結果**: 403 Forbidden（integration__ALL__GROUP が無い）

### 15.11 検証ケース 10: 404（存在しない API パス）

**リクエスト**:
```bash
curl -X GET "http://localhost:8080/api/v1/unknown/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer <token>" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: musashino"
```

**期待結果**: 404 Not Found（DomainAccount に対応しない API パス）

---

## 16. 失敗時の切り分け

### 16.1 403 が返らない（認可判定が動作していない）

**確認事項**:
- `NexusAuthorizationContextFilter` が正しく登録されているか
- Filter の順序が適切か（Controller より前か）
- `extractDbAccessRolesOrFail` が正しく呼ばれているか

**切り分け手順**:
1. Filter のログを確認
2. `extractDbAccessRolesOrFail` の呼び出しを確認
3. Spring の Filter 登録順序を確認

### 16.2 claim が取得できない

**確認事項**:
- Keycloak token に `nexus_db_access` claim が含まれているか
- token のデコードが正しく行われているか
- claim 名が `nexus_db_access` であるか（スペルミス等）

**切り分け手順**:
1. token をデコードして claim を確認
2. `extractDbAccessRolesOrFail`の実装を確認
3. Keycloak 側の mapper 設定を確認

### 16.3 Context が set されない

**確認事項**:
- 認可判定が成功しているか
- Context set のコードが実行されているか
- ThreadLocal の実装が正しいか

**切り分け手順**:
1. 認可判定のログを確認
2. Context set のコードにブレークポイントを設定
3. ThreadLocal の実装を確認

### 16.4 同一 DomainAccount で複数 Region/Corporation の検証が動作しない

**確認事項**:
- Step 5 の検証ロジックが実装されているか
- token に複数の role が含まれているか
- DomainAccount の抽出が正しいか

**切り分け手順**:
1. token の `nexus_db_access` を確認
2. Step 5 の検証ロジックを確認
3. DomainAccount の抽出ロジックを確認

### 16.5 検証ヘッダーが本番で有効になっている

**確認事項**:
- local プロファイル限定の実装になっているか
- 本番環境で検証ヘッダーが無効化されているか

**切り分け手順**:
1. プロファイル判定の実装を確認
2. 本番環境での動作を確認

---

## 17. 次フェーズ（P1-2 以降）

- Region/Corporation の決定を `nexus_db_access` のみへ完全移行（local検証ヘッダー依存の段階的縮小）
- local 検証ヘッダーの段階的縮小
- 監査・ログ整備

---

以上。本書が P1-1 実装ロードマップの正本である。

---

## P1-1 実装結果（マージ済み）

本章は P1-1 の「ロードマップ」ではなく、**実装がマージ済みである事実の記録**である。設計の正（P04-5）を変更するものではない。

### 変更ファイル一覧（実装）

追加:
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/NexusAuthorizationContextFilter.kt
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/DbAccessRoleExtractor.kt
- backend/nexus-bff/src/main/kotlin/nexus/bff/security/NexusSecurityConfig.kt
- backend/nexus-bff/src/test/kotlin/nexus/bff/security/DbAccessRoleExtractorTest.kt

変更:
- backend/nexus-bff/build.gradle.kts  
  - spring-security / oauth2-resource-server 依存追加  
  - spring-boot-starter-test 追加  
  - useJUnitPlatform を有効化

削除（P1-1 により置き換え）:
- backend/nexus-bff/src/main/kotlin/nexus/bff/config/RegionContextFilter.kt
- backend/nexus-bff/src/main/kotlin/nexus/bff/config/CorporationDomainAccountContextFilter.kt

### 実装仕様（設計の正との対応）

- request path から scope を決定（token から DomainAccount を推測しない）
  - /api/v1/gojo/** -> Region（GOJO）
  - /api/v1/funeral/** -> Region（FUNERAL）
  - /api/v1/group/**, /api/v1/identity/**, /api/v1/household/** -> Integration
  - 上記以外 -> 404

- claim 取得・検証
  - nexus_db_access（List<String>）不在/空/不正 -> 403（fail fast）
  - role は正規化（region/corp は lowercase、domainAccount は uppercase）

- wildcard 規則（P04-5 3.4）
  - integration__ALL__GROUP のみ許可
  - region 側 ALL 禁止
  - ※実装では「正規化後の値」で判定し、case ブレでも同一扱いにしている

- fail fast 規則（P04-5）
  - 同一 DomainAccount で複数 Region または複数 Corporation が解釈される token は 403

- Integration API
  - 必要 role: integration__ALL__GROUP
  - 無ければ 403
  - 許可時: RegionContext のみ set（Corp/DomainAccount は set しない）

- Region API
  - 必要 role: {region}__{corp}__{domainAccount}
  - 無ければ 403
  - 許可時: RegionContext / CorporationContext / DomainAccountContext を set

- local 検証ヘッダー（local プロファイル限定）
  - local かつ X-NEXUS-REGION と X-NEXUS-CORP が「両方ある場合のみ」ヘッダーを優先
  - それ以外は token から region/corp を決定（local でも本番相当で動作させる）

- Context clear
  - finally で必ず clear（ThreadLocal leak 防止）

### テスト配置について
- DbAccessRoleExtractorTest は src/test/kotlin/nexus/bff/security 配下に配置する
- nexus/bff/architecture は「アーキテクチャテスト用途」であり、security 単体テストは同フォルダに置く必要はない

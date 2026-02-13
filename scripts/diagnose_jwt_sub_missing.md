# JWT sub claim 欠落問題の診断と対応

## 1. 現象（確定）

BFF ログより、JWT claim keys に `preferred_username`, `email` は出ているが `sub` が無い。
（例：`sub=null`, `subject=null`, `preferred_username=super-admin`, `email=...`）

## 2. 暫定回避（実装済み）

### 2-1. UserInfoDto の sub を nullable に変更

- `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/auth/dto/BootstrapResponse.kt`
  - `UserInfoDto.sub: String` → `String?`
- `frontend/src/types/auth.ts`
  - `UserInfo.sub: string` → `string | null`
- `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/auth/AuthController.kt`
  - `sub` が null でも 500 エラーを発生させない（WARN ログのみ）
  - `preferred_username` を `sub` の代替にしない（意味が違うため）

### 2-2. デバッグログの追加

`AuthController.bootstrap()` に以下を追加：
- `jwt.getClaimAsString("sub")`
- `jwt.subject`
- `jwt.issuer`
- `jwt.getClaimAsString("typ")`
- `jwt.getClaimAsString("azp")`
- `jwt.audience`

## 3. 原因特定のための確認手順

### 3-1. フロントから送っている Authorization の中身を確認

1. Chrome DevTools → Network → `/api/v1/auth/bootstrap` → Request Headers
2. `Authorization: Bearer ...` の JWT を取得
3. JWT を decode（jwt.io でも可、署名検証不要）して payload を確認

**確認ポイント**：
- payload に `sub` が存在するか
- 存在する場合：BFF 側の `JwtDecoder` / `Converter` 側の問題
- 存在しない場合：Keycloak が出している token が「そもそも sub を含まない」ので Keycloak 側要因

### 3-2. Keycloak の該当クライアントを特定

ログの `azp=nexus-frontend` より、問題トークンの発行元クライアントは `nexus-frontend`。

### 3-3. realm-nexus.json での根拠確認

**現状確認**：
- `nexus-frontend` クライアントの `defaultClientScopes` に `profile`, `email` が含まれている
- `profile` scope に `preferred-username` mapper が設定されている
- `email` scope に `email` mapper が設定されている
- **しかし、`sub` を明示的に出力する mapper は存在しない**

**OIDC 仕様上の問題**：
- `sub` は OIDC の標準 claim であり、通常は `openid` scope が含まれていれば自動的に access token に含まれる
- しかし、Keycloak の設定によっては access token から `sub` が除外される場合がある

## 4. 恒久対応方針（Keycloak 側で sub を復帰させる）

### 4-1. 確認すべき Keycloak 設定

1. **Client Scopes の確認**
   - `nexus-frontend` の `defaultClientScopes` に `openid` が含まれているか
   - `profile` scope の `protocolMappers` に `sub` を除外する設定がないか

2. **Client Policies / Profiles の確認**
   - Keycloak 管理画面で `Client Policies` / `Client Profiles` を確認
   - `lightweight token` / `claim 削減` 系が有効になっていないか
   - `realm-nexus.json` には `clientPolicies` / `clientProfiles` の設定は見当たらない（正常）

3. **Protocol Mappers の確認**
   - `nexus-frontend` クライアントに `sub` に干渉するカスタム mapper が存在しないか
   - `realm-nexus.json` には `nexus-frontend` の `protocolMappers` は定義されていない（正常）

4. **Access Token の種類確認**
   - 発行しているのが access token か（NextAuth 側が誤って別トークンを送っていないか）
   - `frontend/src/app/api/auth/[...nextauth]/auth-options.ts` では `scope: 'openid profile email nexus-db-access'` を指定している（正常）

### 4-2. 恒久対応の候補（根拠付き）

**候補 1: `openid` scope が access token に含まれていない可能性**
- **根拠**: OIDC 仕様では `openid` scope が含まれていれば `sub` が自動的に含まれる
- **確認方法**: フロントエンドの `auth-options.ts` で `scope` に `openid` が含まれているか確認（既に含まれている）
- **対応**: フロントエンド側で `openid` scope を明示的に要求しているが、Keycloak 側で access token に含まれていない可能性

**候補 2: Keycloak の access token 設定で `sub` が除外されている可能性**
- **根拠**: Keycloak の設定によっては、access token から標準 claim が除外される場合がある
- **確認方法**: Keycloak 管理画面で `Clients` → `nexus-frontend` → `Advanced Settings` → `Access Token Lifespan` や `Access Token Settings` を確認
- **対応**: `realm-nexus.json` に `sub` を明示的に出力する mapper を追加する

**候補 3: NextAuth が access token ではなく ID token を送っている可能性**
- **根拠**: NextAuth の設定によっては、access token ではなく ID token を使用する場合がある
- **確認方法**: フロントエンドの `auth-options.ts` で `callbacks.jwt` や `callbacks.session` を確認
- **対応**: NextAuth の設定を確認し、access token を確実に送るようにする

### 4-3. 推奨される恒久対応（最小変更）

**方針**: `realm-nexus.json` に `sub` を明示的に出力する mapper を追加する

1. `profile` scope の `protocolMappers` に `sub` mapper を追加
   - ただし、`sub` は通常 OIDC の標準 claim であり、明示的な mapper は不要
   - むしろ、Keycloak の設定で `sub` が除外されている原因を特定する必要がある

2. **より推奨される対応**: Keycloak 管理画面で以下を確認
   - `Clients` → `nexus-frontend` → `Client Scopes` → `Default Client Scopes` に `openid` が含まれているか
   - `Clients` → `nexus-frontend` → `Advanced Settings` → `Access Token Settings` で `sub` が除外されていないか
   - `Realm Settings` → `Tokens` → `Access Token Lifespan` や `Access Token Settings` を確認

## 5. 次のアクション

1. **フロントエンドから送っている JWT を decode して payload を確認**
   - `sub` が存在するか
   - 存在する場合：BFF 側の問題
   - 存在しない場合：Keycloak 側の問題

2. **Keycloak 管理画面で以下を確認**
   - `Clients` → `nexus-frontend` → `Client Scopes` → `Default Client Scopes` に `openid` が含まれているか
   - `Clients` → `nexus-frontend` → `Advanced Settings` → `Access Token Settings` を確認
   - `Realm Settings` → `Tokens` → `Access Token Settings` を確認

3. **Keycloak のログを確認**
   - Keycloak のログで access token 発行時の claim を確認
   - `sub` が含まれているか、含まれていないかを確認

4. **恒久対応の実施**
   - 原因が特定できたら、適切な対応を実施
   - 可能であれば、`realm-nexus.json` に設定を追加して再現性を確保

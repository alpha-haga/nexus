# P2-1 E2E 検証手順書

本ドキュメントは、P2-1（Frontend 認証・認可統合）の E2E 検証を実施するための手順書である。

**本書の位置づけ**: P2-1 の Done 条件確認用。検証結果は本ドキュメントに記録する。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p2-detailed-roadmap.md](./p2-detailed-roadmap.md)
- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)
- [p1-a2-e2e-verification.md](./p1-a2-e2e-verification.md)

---

## 1. 目的

P2-1 の E2E 検証の目的は以下である：

- Frontend（Next.js）と Keycloak の認証統合が正常に動作することを確認
- Keycloak から取得した access token が Backend BFF に正しく付与されることを確認
- Token refresh が正常に動作することを確認
- 認証失敗時の fail fast 動作を確認
- 権限不足時の適切なエラー表示を確認

---

## 2. 検証前提

### 2.1 環境起動

以下の環境が起動していることを確認：

- **Frontend**: Next.js (localhost:3000)
  ```bash
  cd frontend
  npm run dev
  ```

- **Backend BFF**: Spring Boot (localhost:8080)
  ```bash
  cd backend
  ./gradlew :nexus-bff:bootRun --args='--spring.profiles.active=local,jdbc'
  ```

- **Keycloak**: (localhost:8180)
  ```bash
  cd infrastructure
  docker compose -f docker-compose.dev.yml up -d
  ```

### 2.2 環境変数確認

**Frontend** (`frontend/.env.local`):
```env
KEYCLOAK_CLIENT_ID=nexus-bff
KEYCLOAK_CLIENT_SECRET=nexus-bff-secret
KEYCLOAK_ISSUER=http://localhost:8180/realms/nexus
NEXT_PUBLIC_API_URL=http://localhost:8080/api/v1
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=<your-secret>
```

**Backend BFF** (`backend/.env`):
```env
# Oracle DB接続設定（必要に応じて）
ORACLE_INTEGRATION_URL=jdbc:oracle:thin:@localhost:1521:XE
ORACLE_INTEGRATION_USERNAME=integration_user
ORACLE_INTEGRATION_PASSWORD=password
```

### 2.3 Keycloak設定確認

- Realm: `nexus`
- Client: `nexus-bff`
  - Valid Redirect URIs: `http://localhost:3000/*`, `http://localhost:8080/*`
  - Web Origins: `http://localhost:3000`, `http://localhost:8080`
  - Default Client Scopes: `nexus-db-access`
- テストユーザー: `dev-user` / `password`
  - Group: `corp-musashino`
  - Client Roles: `integration__ALL__GROUP`, `saitama__musashino__GOJO`, `saitama__musashino__FUNERAL`

---

## 3. 検証項目と手順

### 検証項目1: ログイン後 session.accessToken が存在する

**目的**: NextAuth.js が Keycloak から access token を正常に取得できていることを確認する。

**手順**:

1. ブラウザで `http://localhost:3000` にアクセス
2. ログイン画面で「Keycloakでログイン」ボタンをクリック
3. Keycloak のログイン画面で以下を入力：
   - ユーザー名: `dev-user`
   - パスワード: `password`
4. ログイン後、`/dashboard` にリダイレクトされることを確認
5. ブラウザの開発者ツール（F12）を開く
6. Console タブで以下を実行：
   ```javascript
   fetch('/api/auth/session')
     .then(res => res.json())
     .then(session => {
       console.log('Session:', session);
       console.log('AccessToken exists:', !!session.accessToken);
       console.log('AccessToken:', session.accessToken);
       console.log('Error:', session.error);
     });
   ```

**期待結果**:
- `session.accessToken` に有効な JWT トークンが設定されている
- トークンは Keycloak から取得した access_token と一致
- `session.error` が `undefined` である

**確認方法**:
- Console に `AccessToken exists: true` と表示される
- `AccessToken` に JWT トークン（`eyJ...` で始まる）が表示される

**記録**:
- [ ] 合格 / [ ] 不合格
- 確認内容: 

---

### 検証項目2: BFF呼び出しに Authorization: Bearer が付与される

**目的**: Frontend から Backend BFF へのリクエストに Authorization ヘッダーが正しく付与されることを確認する。

**手順**:

1. ログイン後、Group Contract List 画面（`/group/contracts`）にアクセス
2. ブラウザの開発者ツールの Network タブを開く
3. 検索 API を呼び出す（検索ボタンをクリック、またはページ読み込み時に自動実行）
4. Network タブで `/api/v1/group/contracts/search` リクエストを選択
5. Headers タブで Request Headers を確認

**期待結果**:
- リクエストヘッダーに `Authorization: Bearer <accessToken>` が付与されている
- トークンは `session.accessToken` の値と一致

**確認方法**:
- Network タブの Request Headers に以下が表示される：
  ```
  Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
  ```

**記録**:
- [ ] 合格 / [ ] 不合格
- 確認内容: 

---

### 検証項目3: accessTokenExpires が閾値に近い状態で refresh が発火し、accessToken が更新される

**目的**: Token refresh が正常に動作し、access token が自動更新されることを確認する。

**手順**:

1. ログイン後、ブラウザの開発者ツールの Console タブを開く
2. 現在のセッションを確認：
   ```javascript
   fetch('/api/auth/session')
     .then(res => res.json())
     .then(session => {
       console.log('Current session:', session);
     });
   ```
3. NextAuth.js の JWT コールバックを一時的に修正（検証用）：
   - `frontend/src/app/api/auth/[...nextauth]/route.ts` の `accessTokenExpires` を短く設定
   - 例: `token.accessTokenExpires = Date.now() + 30 * 1000;` (30秒後)
4. 30秒以内に API 呼び出しを実行（検索ボタンをクリック）
5. Network タブで Keycloak の `/protocol/openid-connect/token` へのリクエストを確認
6. 次の API 呼び出しで `session.accessToken` が更新されていることを確認

**期待結果**:
- 期限切れ1分前（bufferTime = 60秒）で refresh が発火
- Keycloak の `/protocol/openid-connect/token` エンドポイントに `refresh_token` リクエストが送信される
- 新しい `accessToken` が取得され、`session.accessToken` が更新される
- `token.accessTokenExpires` も新しい期限に更新される

**確認方法**:
- Network タブで `grant_type=refresh_token` のリクエストが送信される
- Console で `session.accessToken` の値が変更されていることを確認

**注意**: 検証後は修正を元に戻すこと

**記録**:
- [ ] 合格 / [ ] 不合格
- 確認内容: 

---

### 検証項目4: refresh を意図的に失敗させた場合、session.error が立ち、最終的に 401 → /login に落ちる（fail fast）

**目的**: Token refresh 失敗時に適切に fail fast し、ログイン画面にリダイレクトされることを確認する。

**手順**:

1. Keycloak の refresh_token エンドポイントを一時的に無効化するか、不正な refresh_token を使用
   - 方法A: Keycloak の設定で refresh_token の有効期限を 0 にする
   - 方法B: NextAuth.js の JWT コールバックで `token.refreshToken` を不正な値に設定（検証用）
2. ログイン後、期限切れ間近で API 呼び出しを実行
3. Console タブで以下を実行：
   ```javascript
   fetch('/api/auth/session')
     .then(res => res.json())
     .then(session => {
       console.log('Session error:', session.error);
       console.log('AccessToken:', session.accessToken);
     });
   ```
4. 次の API 呼び出しで 401 が返り、`/login` にリダイレクトされることを確認

**期待結果**:
- refresh 失敗時に `token.error = 'RefreshAccessTokenError'` が設定される
- `session.error = 'RefreshAccessTokenError'` が露出される（状態を隠さない）
- `token.accessToken` はクリアされず保持される（状態を隠さない）
- 次の API 呼び出しで 401 Unauthorized が返る
- `signOut({ callbackUrl: '/login' })` が実行され、ログイン画面にリダイレクトされる（fail fast）

**確認方法**:
- Console に `Session error: RefreshAccessTokenError` が表示される
- Network タブで 401 レスポンスが確認される
- ログイン画面（`/login`）にリダイレクトされる

**記録**:
- [ ] 合格 / [ ] 不合格
- 確認内容: 

---

### 検証項目5: 403 は握りつぶさず UI 側で明示される（ApiError throw が維持されている）

**目的**: 権限不足時に 403 エラーが適切に表示され、握りつぶされないことを確認する。

**手順**:

1. 権限のないユーザーでログイン
   - Keycloak で `nexus_db_access` claim に該当する role がないユーザーを使用
   - または、`integration__ALL__GROUP` role がないユーザーを使用
2. Group Contract List 画面（`/group/contracts`）で検索 API を呼び出す
3. Network タブで 403 レスポンスを確認
4. UI 側でエラーメッセージが表示されることを確認

**期待結果**:
- 403 Forbidden レスポンスが返る
- `ApiError` が throw され、UI 側でエラーメッセージが表示される
- エラーメッセージ: "この操作を実行する権限がありません。"

**確認方法**:
- Network タブで 403 レスポンスが確認される
- UI 側（Group Contract List 画面）でエラーメッセージが表示される
- エラーメッセージは握りつぶされず、明示的に表示される

**記録**:
- [ ] 合格 / [ ] 不合格
- 確認内容: 

---

## 4. 検証結果記録

### 4.1 検証実施情報

- **検証日時**: YYYY-MM-DD HH:MM
- **検証者**: 
- **検証環境**:
  - Frontend: Next.js (localhost:3000)
  - Backend BFF: Spring Boot (localhost:8080)
  - Keycloak: (localhost:8180/realms/nexus)

### 4.2 検証項目結果サマリー

| 検証項目 | 結果 | 備考 |
|---------|------|------|
| 1. session.accessToken が存在する | [ ] 合格 / [ ] 不合格 | |
| 2. Authorization: Bearer が付与される | [ ] 合格 / [ ] 不合格 | |
| 3. Token refresh が動作する | [ ] 合格 / [ ] 不合格 | |
| 4. refresh 失敗時に fail fast する | [ ] 合格 / [ ] 不合格 | |
| 5. 403 が UI 側で明示される | [ ] 合格 / [ ] 不合格 | |

### 4.3 総合評価

- [ ] すべて合格
- [ ] 一部不合格（詳細を記載）

### 4.4 問題点・改善点

（問題点や改善点があれば記載）

---

## 5. トラブルシューティング

### 5.1 ログイン後、同じ画面に戻される

**原因**: Keycloak の redirect URI が正しく設定されていない

**解決方法**:
1. Keycloak 管理画面で `nexus-bff` クライアントの Valid Redirect URIs を確認
2. `http://localhost:3000/api/auth/callback/keycloak` が含まれていることを確認
3. 含まれていない場合は追加

### 5.2 Authorization ヘッダーが付与されない

**原因**: NextAuth.js のセッションから access token が取得できていない

**解決方法**:
1. Console で `session.accessToken` を確認
2. `undefined` の場合は、Keycloak の設定を確認
3. `nexus-bff` クライアントの `defaultClientScopes` に `nexus-db-access` が含まれていることを確認

### 5.3 Token refresh が動作しない

**原因**: `token.refreshToken` が設定されていない、または `accessTokenExpires` が設定されていない

**解決方法**:
1. NextAuth.js の JWT コールバックで `token.refreshToken` と `token.accessTokenExpires` が設定されていることを確認
2. Keycloak の設定で `refresh_token` が返されることを確認

### 5.4 403 エラーが表示されない

**原因**: ApiClient のエラーハンドリングが正しく動作していない

**解決方法**:
1. `frontend/src/services/api.ts` の 403 処理を確認
2. UI 側のエラーハンドリングを確認

---

## 6. 参照

- [p2-detailed-roadmap.md](./p2-detailed-roadmap.md)（P2-1 の詳細）
- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)（権限制御設計）
- [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md)（Keycloak 設定手順）
- [p1-a2-e2e-verification.md](./p1-a2-e2e-verification.md)（P1-A2 の E2E 検証結果）

---

## 補足

- 本検証は P2-1 の Done 条件確認のためのものである
- 検証結果は本ドキュメントに記録し、P2-1 完了宣言の根拠とする
- すべての検証項目が合格した場合、P2-1 は完了と判断できる

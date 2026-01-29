# P1-A0 Keycloak 検証手順書（A案：設計忠実）

本ドキュメントは、P1-A0 で設定した Keycloak の動作を検証するための手順書です。
A案では **nexus_db_access を発行できるのは nexus-bff client のみ**（nexus-frontend には割り当てない）とします。

---

## 1. 前提条件

- Docker / Docker Compose がインストールされていること
- ポート 8180 が空いていること（Keycloak 用）
- ポート 8080 が空いていること（BFF 用）
- `jq` がインストールされていること（JSON パース用）

### jq のインストール

**Windows (PowerShell / Git Bash)**:
```bash
# Chocolatey を使用する場合
choco install jq

# または、Scoop を使用する場合
scoop install jq

# または、手動インストール
# https://github.com/jqlang/jq/releases から最新版をダウンロード
```

**macOS**:
```bash
# Homebrew を使用する場合
brew install jq
```

**Linux (Ubuntu/Debian)**:
```bash
sudo apt-get update && sudo apt-get install -y jq
```

**Linux (CentOS/RHEL)**:
```bash
sudo yum install -y jq
```

**確認**:
```bash
jq --version
```

本 BFF は fail fast を前提とした設計であり、以下のリクエストヘッダーを必須とする。

- すべての API リクエストにおいて `X-NEXUS-REGION` を必須とする  
  - integration（統合 DB）向け API も例外ではない
  - 例: `X-NEXUS-REGION: integration`

- Region DB を参照する API においては、法人スキーマ決定のため `X-NEXUS-CORP` を必須とする  
  - 例: `X-NEXUS-CORP: musashino`

- 上記ヘッダーが不足している場合、BFF は fail fast によりエラーを返却する

※ 現状の BFF 実装では actuator エンドポイント（`/actuator/**`）にも同一のフィルタが適用されるため、
  `/actuator/health` 等の確認にも `X-NEXUS-REGION` ヘッダーが必要となる。
---

## 2. Keycloak 起動

```bash
cd infrastructure
docker compose -f docker-compose.dev.yml up -d
```

**確認**: `http://localhost:8180` でログイン画面が表示されること

**管理者ログイン**:
- URL: `http://localhost:8180/admin`
- Username: `admin`
- Password: `admin`

---

## 3. 設定の確認（GUI）

### 3.1 Client Scope の確認

1. 管理画面で `nexus` realm を選択
2. 左メニューから **Client scopes** を選択
3. `nexus-db-access` が存在することを確認
4. `nexus-db-access` をクリック → **Mappers** タブ
5. `nexus-db-access-mapper` が存在し、以下の設定であることを確認：
   - Token Claim name: `nexus_db_access`
   - Multivalued: ON
   - Add to access token: ON
   - (重要) client role mapping の clientId が `nexus-bff` であること

### 3.2 Client の確認

1. 左メニューから **Clients** を選択
2. `nexus-bff` が存在することを確認
3. `nexus-bff` をクリック → **Roles** タブ
4. 以下の client roles が存在することを確認：
   - `integration__ALL__GROUP`
   - `saitama__musashino__GOJO`
   - `saitama__musashino__FUNERAL`
5. **Client scopes** タブで `nexus-db-access` が Default client scopes に含まれていることを確認

(設計上のガード)
- `nexus-frontend` の Default client scopes に `nexus-db-access` が含まれていないことを確認（BFF の source-of-truth を壊さないため）

### 3.3 Group の確認

1. 左メニューから **Groups** を選択
2. `corp-musashino` が存在することを確認
3. `corp-musashino` をクリック → **Role mapping** タブ
4. 以下の client roles が割り当てられていることを確認：
   - `nexus-bff` / `integration__ALL__GROUP`
   - `nexus-bff` / `saitama__musashino__GOJO`
   - `nexus-bff` / `saitama__musashino__FUNERAL`

### 3.4 User の確認

1. 左メニューから **Users** を選択
2. `dev-user` を選択
3. **Groups** タブで `corp-musashino` に所属していることを確認

---

## 4. Access Token 取得（検証）

### 4.1 Password Grant で取得（nexus-bff client を使用）

```bash
curl -s -X POST "http://localhost:8180/realms/nexus/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=nexus-bff" \
  -d "client_secret=nexus-bff-secret" \
  -d "username=dev-user" \
  -d "password=password" \
  | jq -r '.access_token'
```

### 4.2 Token をデコードして確認

```bash
TOKEN="eyJhbGciOiJSUz..."
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq .
```

### 4.3 期待する claim

```json
{
  "nexus_db_access": [
    "integration__ALL__GROUP",
    "saitama__musashino__GOJO",
    "saitama__musashino__FUNERAL"
  ]
}
```

---

## 5. BFF との連携検証

### 5.1 BFF 起動

```bash
cd backend
./gradlew :nexus-bff:bootRun --args='--spring.profiles.active=local,jdbc'
```

### 5.2 Region API（GOJO）検証

```bash
TOKEN=$(curl -s -X POST "http://localhost:8180/realms/nexus/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=nexus-bff" \
  -d "client_secret=nexus-bff-secret" \
  -d "username=dev-user" \
  -d "password=password" \
  | jq -r '.access_token')

curl -v -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: musashino"
```

**期待結果**: `200 OK`

### 5.3 Integration API 検証（X-NEXUS-REGION 必須）

```bash
curl -v -X GET "http://localhost:8080/api/v1/group/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-NEXUS-REGION: integration"

```

**期待結果**: `200 OK`

### 5.4 403 検証（role 不一致）

```bash
curl -v -X GET "http://localhost:8080/api/v1/gojo/contracts/search?page=0&size=20" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-NEXUS-REGION: saitama" \
  -H "X-NEXUS-CORP: fukushisousai"
```

**期待結果**: `403 Forbidden`

---

以上。

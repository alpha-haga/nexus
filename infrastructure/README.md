# NEXUS Infrastructure

Keycloak認証基盤の環境構築用ファイル。

## 環境構成

| 環境 | Keycloak DB | 認証方式 |
|------|-------------|----------|
| Production | Oracle | Keycloak |
| Staging | Oracle（本番コピー） | Keycloak + Impersonation |
| Development | H2 | Keycloak |
| Local | - | モック（CredentialsProvider） |

## Development環境

### 起動

```bash
cd infrastructure
docker compose -f docker-compose.dev.yml up -d
```

### 停止

```bash
docker compose -f docker-compose.dev.yml down
```

### データ削除（リセット）

```bash
docker compose -f docker-compose.dev.yml down -v
```

### Keycloak管理画面

- URL: http://localhost:8180
- 管理者ID: `admin`
- パスワード: `admin`

### テストユーザー

| 項目 | 値 |
|------|-----|
| ユーザー名 | dev-user |
| パスワード | password |
| メール | dev-user@example.com |
| 社員ID | E001 |
| 法人ID | corp-001 |
| 法人名 | サンプル法人 |

## フロントエンド連携

### Keycloak使用時

```bash
cd frontend
cp .env.development .env.local
npm run dev
```

### モック使用時（ローカル開発）

`frontend/.env.local` で KEYCLOAK_* をコメントアウト:

```env
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=dev-secret-key-for-local-development-only

# KEYCLOAK_CLIENT_ID=
# KEYCLOAK_CLIENT_SECRET=
# KEYCLOAK_ISSUER=
```

## ファイル構成

```
infrastructure/
├── README.md
├── docker-compose.dev.yml    # Development用（H2）
└── keycloak/
    └── realm-nexus.json      # realm初期設定
```

## realm設定

`keycloak/realm-nexus.json` に以下を定義:

- realm名: `nexus`
- クライアント: `nexus-frontend`
- ユーザー属性マッピング:
  - `employeeId` - 社員ID
  - `corporationId` - 法人ID
  - `corporationName` - 法人名

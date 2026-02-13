# JWT トークンの sub claim 確認手順

## 目的
`/api/v1/auth/bootstrap` で `jwt.subject` が null になる原因を特定するため、JWT トークンのペイロードに `sub` claim が含まれているか確認する。

## 手順

### 1. ブラウザから JWT トークンを取得

ブラウザの開発者ツール（F12）の Console で以下を実行:

```javascript
fetch('/api/auth/session')
  .then(r => r.json())
  .then(s => {
    if (s.accessToken) {
      console.log('JWT Token:', s.accessToken);
      navigator.clipboard.writeText(s.accessToken);
      console.log('✓ トークンをクリップボードにコピーしました');
    }
  });
```

### 2. JWT トークンをデコード

取得したトークンを以下のコマンドでデコード:

```bash
# JWT トークンを環境変数に設定
TOKEN="<コピーしたトークン>"

# ペイロード部分を抽出してデコード
PAYLOAD=$(echo "$TOKEN" | cut -d'.' -f2)
# Base64URL のパディングを追加
case $((${#PAYLOAD} % 4)) in
    2) PAYLOAD="${PAYLOAD}==" ;;
    3) PAYLOAD="${PAYLOAD}=" ;;
esac
# Base64URL を Base64 に変換
PAYLOAD_B64=$(echo "$PAYLOAD" | tr '_-' '/+')
# デコード
echo "$PAYLOAD_B64" | base64 -d | jq '.'
```

### 3. sub claim の確認

デコード結果で以下を確認:
- `sub` が含まれているか
- `sub` の値が null でないか
- その他の claim（`iss`, `azp`, `aud`, `nexus_db_access` など）が含まれているか

### 4. 結果の解釈

- **sub が存在する場合**: Spring Security の JwtDecoder / Converter の問題の可能性
- **sub が存在しない場合**: NextAuth が送っているトークンが期待と違う（ID token / opaque token / 別clientのtoken 等）可能性

# ブラウザからJWTトークンを取得する方法

## 方法1: ブラウザの開発者ツール（Console）

1. ブラウザで http://localhost:3000 にアクセス
2. ログインする
3. 開発者ツールを開く（F12）
4. Console タブで以下を実行:

```javascript
// NextAuth のセッションから accessToken を取得
fetch('/api/auth/session')
  .then(r => r.json())
  .then(s => {
    if (s.accessToken) {
      console.log('JWT Token:', s.accessToken);
      // クリップボードにコピー
      navigator.clipboard.writeText(s.accessToken).then(() => {
        console.log('✓ トークンをクリップボードにコピーしました');
      });
    } else {
      console.error('❌ accessToken が見つかりません。ログインしてください。');
    }
  });
```

## 方法2: Application > Session Storage

1. 開発者ツールを開く（F12）
2. Application タブを開く
3. Session Storage > http://localhost:3000 を選択
4. `next-auth.session-token` の値を確認（ただし、これは暗号化されたセッションIDなので、直接は使えない）

## 方法3: Network タブ

1. 開発者ツールを開く（F12）
2. Network タブを開く
3. `/api/v1/auth/bootstrap` へのリクエストを探す
4. Request Headers の `Authorization: Bearer <token>` からトークンを取得

## 方法4: 環境変数で指定

```bash
# トークンを環境変数に設定
export JWT_TOKEN="your-jwt-token-here"

# スクリプトを実行
bash scripts/test_bootstrap_with_jwt.sh
```

## 方法5: ファイルで指定

```bash
# トークンをファイルに保存
echo "your-jwt-token-here" > .test-jwt-token

# スクリプトを実行（.gitignore に .test-jwt-token を追加することを推奨）
bash scripts/test_bootstrap_with_jwt.sh
```

## 注意事項

- JWTトークンは機密情報です。`.test-jwt-token` ファイルは `.gitignore` に追加してください
- トークンには有効期限があります。期限切れの場合は再ログインが必要です

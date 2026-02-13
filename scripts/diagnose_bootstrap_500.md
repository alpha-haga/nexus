# Bootstrap 500 エラー 調査レポート

## 調査結果サマリー

### ✅ 成功している点
1. **フィルターの除外は正常に動作**
   - `RegionContextFilter` が `/api/v1/auth/` を正しく除外
   - `NexusAuthorizationContextFilter` が `/api/v1/auth/` を正しく除外
   - `CompanyAuthorizationFilter` が `/api/v1/auth/` を正しく除外
   - X-NEXUS-REGION ヘッダーの有無で結果が変わらない

2. **JWTトークンは正常**
   - `nexus_db_access` claim が含まれている
   - 複数のロールが正しく設定されている

### ❌ 問題点
- HTTP 500 エラーが発生
- `GlobalExceptionHandler` の `handleGeneric` がキャッチ
- エラーメッセージ: "An unexpected error occurred"

## 考えられる原因

### 1. CompanyMasterQueryService の DI エラー（最有力）

**可能性:**
- `JdbcCompanyMasterQueryService` が `@Profile("jdbc")` で有効化されていない
- `CompanyResolverService` が `CompanyMasterQueryService` を注入できない

**確認方法:**
```bash
# バックエンドの起動ログで以下を確認
grep -i "NoSuchBeanDefinitionException\|BeanCreationException\|CompanyMasterQueryService" <起動ログ>
```

### 2. Integration DB への接続エラー

**可能性:**
- Integration DB が起動していない
- 接続設定（`application-jdbc.yml`）が正しくない
- 環境変数が設定されていない

**確認方法:**
```bash
# 環境変数の確認
echo $NEXUS_DB_INTEGRATION_HOST
echo $NEXUS_DB_INTEGRATION_PORT
echo $NEXUS_DB_INTEGRATION_SERVICE
echo $NEXUS_DB_INTEGRATION_USER
echo $NEXUS_DB_INTEGRATION_PASSWORD
```

### 3. SQL 実行エラー

**可能性:**
- SQL ファイルの読み込みエラー
- SQL の構文エラー
- テーブルが存在しない

**確認方法:**
- SQL ファイル: `backend/nexus-infrastructure/src/main/resources/sql/group/company_master_by_region_company.sql`
- テーブル: `NXCM_COMPANY`

### 4. CompanyResolverService.resolveAvailableCompanies の処理エラー

**可能性:**
- `RoleParser.getAvailableCompanies` の結果が不正
- `key.split("__")` でエラー（key の形式が不正）

**確認方法:**
- JWT トークンの `nexus_db_access` claim を確認
- ロールの形式が `region__company__domain` になっているか

## 次のステップ

### 1. バックエンドの起動ログを確認

バックエンドの起動ターミナルで、`/api/v1/auth/bootstrap` へのリクエスト後に出力されるエラーログを確認してください。

**確認すべきキーワード:**
- `Unexpected error occurred`（追加したログ）
- `CompanyResolverService`
- `JdbcCompanyMasterQueryService`
- `NoSuchBeanDefinitionException`
- `BeanCreationException`
- `SQL`
- `Integration DB`

### 2. Profile の確認

起動ログで以下を確認:
```
The following profiles are active: local,jdbc
```

または、起動コマンドを確認:
```bash
./gradlew :nexus-bff:bootRun --args='--spring.profiles.active=local,jdbc'
```

### 3. ログレベルを上げて再テスト

`application-local.yml` に以下を追加（コメントアウトを解除）:

```yaml
logging:
  level:
    nexus.bff.controller.auth: DEBUG
    nexus.bff.security.CompanyResolverService: DEBUG
    nexus.infrastructure.identity.query.JdbcCompanyMasterQueryService: DEBUG
    org.springframework.jdbc: DEBUG
    org.springframework.beans.factory: DEBUG
```

その後、バックエンドを再起動して再度テストを実行してください。

### 4. エラーログの全文を共有

エラーログの全文を共有していただければ、より具体的な原因を特定できます。

## 修正内容

### GlobalExceptionHandler にログ出力を追加

`GlobalExceptionHandler.kt` の `handleGeneric` メソッドにログ出力を追加しました:

```kotlin
@ExceptionHandler(Exception::class)
fun handleGeneric(ex: Exception): ResponseEntity<ErrorResponse> {
    logger.error("Unexpected error occurred", ex)  // ← 追加
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(...)
}
```

これにより、エラーが発生したときにスタックトレースがログに出力されるようになります。

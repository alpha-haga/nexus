# BFF Controller 実装ルール（確定版）

本書は BFF 層の Controller 実装ルールを固定する。
目的は、今後 API が増えても迷子にならない配置ルールを確立し、責務境界を明確にすることである。

## 1. 目的と適用範囲

### 1.1 適用範囲

- **BFF 層の Controller / Response DTO / Mapper のみ**を対象とする
- domain 層（QueryService / DTO）や infrastructure 層（JDBC 実装）は対象外
- 既存実装（GroupContractSearchController / GroupCompaniesController）を正とする

### 1.2 目的

- Controller の責務を薄く保ち、HTTP レイヤとしての役割に集中する
- DTO / Mapper の配置ルールを固定し、一貫性を保つ
- 例外処理・バリデーションの方針を明確化する

---

## 2. ディレクトリ構成ルール（B構成）

### 2.1 基本構成

```
nexus-bff/src/main/kotlin/nexus/bff/controller/
├── <feature>/                    # 機能単位のディレクトリ（例: group）
│   ├── <Feature><UseCase>Controller.kt
│   ├── dto/
│   │   └── <UseCase>Response.kt
│   └── mapper/
│       └── <Entity>ResponseMapper.kt
└── <Feature><UseCase>Controller.kt  # 単一機能の場合は直下も可
※ ただし将来的に API が増える可能性がある場合は、最初から <feature>/ 配下に配置することを推奨する
```

### 2.2 具体例

**group 機能の companies 一覧:**
```
nexus-bff/src/main/kotlin/nexus/bff/controller/group/
├── GroupCompaniesController.kt
├── dto/
│   └── CompanyResponse.kt
└── mapper/
    └── CompanyResponseMapper.kt
```

**group 機能の contracts 検索（単一ファイル構成も可）:**
```
nexus-bff/src/main/kotlin/nexus/bff/controller/
└── GroupContractSearchController.kt  # Response DTO / Mapper は同一ファイル内
```

### 2.3 選択基準

- **複数 API が予定される機能**: `controller/<feature>/` 配下に配置（B構成）
- **単一 API のみの機能**: `controller/` 直下に配置（Response DTO / Mapper は同一ファイル内でも可）

---

## 3. 命名規約

### 3.1 Controller

- 命名: `<Feature><UseCase>Controller`
- 例: `GroupCompaniesController`, `GroupContractSearchController`
- パッケージ: `nexus.bff.controller.<feature>` または `nexus.bff.controller`

### 3.2 Response DTO

- 命名: `<UseCase>Response` または `<Entity>Response`
- 例: `CompanyResponse`, `GroupContractSearchResponse`
- パッケージ: `nexus.bff.controller.<feature>.dto` または Controller と同じパッケージ

### 3.3 Mapper

- 命名: `<Entity>ResponseMapper`（ファイル名）または extension function `toResponse()`
- 例: `CompanyResponseMapper.kt`（extension function を定義）
- パッケージ: `nexus.bff.controller.<feature>.mapper` または Controller と同じパッケージ

---

## 4. 責務境界（絶対）

### 4.1 Controller の責務

Controller は **HTTP レイヤとして薄く保つ**。以下のみを担当する：

1. **HTTP リクエストの受付**（`@GetMapping` 等）
2. **QueryService の呼び出し**
3. **DTO から Response への変換**（Mapper 経由）
4. **ResponseEntity の返却**
5. **基本的なバリデーション**（ページ番号、サイズ等の HTTP レベルの検証）

### 4.2 禁止事項

- **ドメイン/業務判断**: Controller 内で業務ロジックを実装しない
- **例外の握りつぶし**: 例外を catch して 200 で返す等の処理をしない
- **表示判断**: 「この値が null なら別の値を返す」等の表示ロジックを実装しない
- **データ補完**: 「この値が null ならデフォルト値を設定」等の補完処理を実装しない

### 4.3 バリデーション方針

- **HTTP レベルの検証のみ**: ページ番号、サイズ、必須パラメータの有無など
- **業務バリデーション**: QueryService 層または domain 層で行う
- **バリデーションエラー**: `ValidationException` を throw し、共通例外ハンドラに委譲

---

## 5. 戻り型・HTTP方針

### 5.1 原則: ResponseEntity を返す

**理由:**
- HTTP ステータスコードを明示的に制御できる
- 将来的にヘッダー追加等の拡張に対応しやすい
- Spring の標準的な実装パターンに準拠

**実装例:**
```kotlin
@GetMapping
fun list(): ResponseEntity<List<CompanyResponse>> {
    val companies = companyQueryService.findAll()
    return ResponseEntity.ok(companies.map { it.toResponse() })
}
```

### 5.2 HTTP ステータスコード

- **200 OK**: 正常終了時
- **400 Bad Request**: バリデーションエラー（`ValidationException` を throw）
- **500 Internal Server Error**: 予期しないエラー（共通例外ハンドラで処理）
- **その他**: Controller 内で明示的に設定しない（共通例外ハンドラに委譲）

### 5.3 エラーハンドリング

- Controller 内で `try-catch` を使用しない
- 例外は共通例外ハンドラに委譲し、適切な HTTP ステータスコードとエラーレスポンスを返す
- 「エラーを握りつぶして 200 で返す」等の処理は禁止

---

## 6. DTO（Response）の方針

### 6.1 命名と構造

- **frontend がそのまま使える名前**で定義する
- 例: `cmpCd`, `cmpShortNm`, `regionCd`（camelCase）
- 表示判断や補完は基本しない（必要なら別途合意）

### 6.2 フィールド設計

- **必要最小限の項目のみ**を含める
- 「便利そうだから」という理由で項目を増やさない
- frontend 未確定でも耐えられる安定した返却契約を維持する

### 6.3 null 許容

- 業務的に必須でない項目は nullable とする
- 「null なら別の値を返す」等の変換は Mapper で行わない（そのまま返す）

---

## 7. Mapper 方針

### 7.1 実装方式

- **自動マッピング禁止**: フィールド名の一致に頼らない
- **明示的な変換**: extension function または mapper object で実装

### 7.2 Extension Function 方式（推奨）

```kotlin
// mapper/CompanyResponseMapper.kt
fun CompanyDto.toResponse(): CompanyResponse =
    CompanyResponse(
        cmpCd = cmpCd,
        cmpShortNm = cmpShortNm,
        regionCd = regionCd
    )
```

### 7.3 同一ファイル内定義（単一 API の場合）

```kotlin
// Controller ファイル内
private fun CompanyDto.toResponse(): CompanyResponse =
    CompanyResponse(...)
```

### 7.4 変換ロジック

- **単純なフィールドマッピングのみ**: 表示判断や補完は行わない
- **null の扱い**: DTO の null は Response でも null として返す

---

## 8. Profile/Bean競合方針

### 8.1 @Profile("jdbc") の使用

**現状の採用方針:**
- JDBC Read 実装の Controller には `@Profile("jdbc")` を付与する
- 理由: JPA 実装との Bean 競合を回避するため

**実装例:**
```kotlin
@Profile("jdbc")
@RestController
@RequestMapping("/api/group/companies")
class GroupCompaniesController(...)
```

### 8.2 非 JDBC 実装の場合

- JPA 実装やその他の実装方式を使用する場合は、適切な Profile を付与する
- または Profile を付けずに常に有効化する（既存の GojoContractController 等）

---

## 9. 実装例（最小構成）

### 9.1 B構成（複数 API 予定）

**Controller:**
```kotlin
@Profile("jdbc")
@RestController
@RequestMapping("/api/group/companies")
class GroupCompaniesController(
    private val companyQueryService: CompanyQueryService
) {
    @GetMapping
    fun list(): ResponseEntity<List<CompanyResponse>> {
        val companies = companyQueryService.findAll()
        return ResponseEntity.ok(companies.map { it.toResponse() })
    }
}
```

**Response DTO:**
```kotlin
data class CompanyResponse(
    val cmpCd: String,
    val cmpShortNm: String,
    val regionCd: String?
)
```

**Mapper:**
```kotlin
fun CompanyDto.toResponse(): CompanyResponse =
    CompanyResponse(
        cmpCd = cmpCd,
        cmpShortNm = cmpShortNm,
        regionCd = regionCd
    )
```

### 9.2 単一ファイル構成（単一 API のみ）

**Controller + Response DTO + Mapper（同一ファイル）:**
```kotlin
@Profile("jdbc")
@RestController
@RequestMapping("/api/v1/group/contracts/search")
class GroupContractSearchController(...) {
    @GetMapping
    fun search(...): ResponseEntity<PaginatedGroupContractResponse> {
        // ...
    }
}

// Response DTOs
data class PaginatedGroupContractResponse(...)
data class GroupContractSearchResponse(...)

// Mapper
private fun PaginatedResult<GroupContractSearchDto>.toResponse(): ...
private fun GroupContractSearchDto.toResponse(): ...
```

---

## 10. Future Considerations（将来の拡張案）

以下の項目は現時点では未確定であり、必要に応じて検討する：

1. **共通 Response ラッパー**: エラー情報やメタデータを含む共通構造の導入
2. **バージョニング**: API バージョン管理の方針（URL パス vs ヘッダー）
3. **OpenAPI 生成**: Swagger/OpenAPI の自動生成対応

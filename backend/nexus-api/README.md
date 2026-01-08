# nexus-api

## 役割

**外部公開 API の Facade（薄い調整層）**。

- **HTTP エンドポイント提供**: REST API の公開
- **リクエスト/レスポンス変換**: DTO とドメインオブジェクトの変換
- **認証・認可**: 将来的に Keycloak 連携
- **エラーハンドリング**: 例外の HTTP ステータス変換

## やってよいこと

| 許可 | 例 |
|------|-----|
| Controller の定義 | `@RestController class PersonController` |
| DTO の定義 | `data class RegisterPersonRequest` |
| 入力バリデーション | `@Valid`, `@NotBlank` 等 |
| 認証・認可チェック | `@PreAuthorize`, Security 設定 |
| 例外ハンドリング | `@RestControllerAdvice` |
| ドメイン Service の呼び出し | `personService.register(command)` |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| **Repository の定義** | データアクセスは各ドメインモジュールの責務 |
| **Entity の定義** | Entity は各ドメインモジュールで定義済み |
| **ビジネスロジック** | 計算・判定ロジックはドメインモジュールで |
| **直接 DB アクセス** | JdbcTemplate, EntityManager の直接利用禁止 |
| **業務モジュールへの依存** | 現時点では group/identity/household のみ |

## ディレクトリ構成

```
src/main/kotlin/nexus/api/
├── NexusApiApplication.kt  # Spring Boot エントリーポイント
├── controller/            # REST コントローラー（Thin Controller）
│   ├── PersonController.kt
│   └── HouseholdController.kt
├── dto/                   # リクエスト/レスポンス DTO のみ
├── config/                # アプリケーション設定
│   └── GlobalExceptionHandler.kt
├── security/              # 認証・認可設定
└── interceptor/           # リクエストインターセプター
```

## 設計指針

### Thin Controller パターン

Controller はリクエストの受け取りとレスポンスの返却のみを担当。
ビジネスロジックは一切書かない。

```kotlin
// Good: Thin Controller
@RestController
@RequestMapping("/api/v1/persons")
class PersonController(
    private val personService: PersonService  // ドメインサービスを注入
) {
    @PostMapping
    fun register(@RequestBody request: RegisterPersonRequest): ResponseEntity<PersonResponse> {
        // 1. リクエスト → コマンド変換
        val command = request.toCommand()

        // 2. ドメインサービス呼び出し（ロジックはここにない）
        val person = personService.register(command)

        // 3. レスポンス変換
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(person.toResponse())
    }
}

// Bad: Fat Controller（ロジックが Controller にある）
@RestController
class PersonController(
    private val personRepository: PersonRepository  // NG: Repository 直接参照
) {
    @PostMapping
    fun register(@RequestBody request: RegisterPersonRequest): ResponseEntity<PersonResponse> {
        // NG: ビジネスロジックが Controller に
        if (personRepository.existsByEmail(request.email)) {
            throw DuplicateException("Email already exists")
        }
        val person = Person(
            id = UUID.randomUUID().toString(),
            // ... 組み立てロジック
        )
        personRepository.save(person)  // NG: 直接 save
        return ResponseEntity.ok(person.toResponse())
    }
}
```

### Repository を持たない理由

1. **責務の分離**: データアクセスはドメインモジュールの責務
2. **テスト容易性**: API 層はドメインサービスのモックでテスト可能
3. **再利用性**: 同じロジックを Batch や他チャネルでも利用可能
4. **変更影響の局所化**: DB スキーマ変更が API 層に波及しない

### DTO と Entity の分離

```kotlin
// API 層: DTO（外部表現）
data class PersonResponse(
    val id: String,
    val fullName: String,  // 表示用に加工
    val age: Int?          // 計算済みの値
)

// ドメイン層: Entity（内部表現）
@Entity
class Person(
    val id: String,
    val firstName: String,
    val lastName: String,
    val birthDate: LocalDate?
)

// 変換は API 層で行う
fun Person.toResponse() = PersonResponse(
    id = this.id,
    fullName = this.fullName,
    age = this.birthDate?.let { /* 年齢計算 */ }
)
```

### 将来の BFF 分離

現在 nexus-api は group/identity/household のみに依存。
業務モジュール（gojo/funeral/bridal/point）へのアクセスは将来 BFF として分離予定。

```
現在:
  nexus-api → group, identity, household

将来:
  nexus-api-core      → group, identity, household  (顧客管理系)
  nexus-api-gojo      → gojo                             (互助会系)
  nexus-api-ceremony  → funeral, bridal                  (式典系)
  nexus-api-point     → point                            (ポイント系)
```

## 依存関係

```
nexus-api
    ├── nexus-core
    ├── nexus-group       （法人横断検索）
    ├── nexus-identity    （人物管理）
    └── nexus-household   （世帯管理）
```

**現時点で禁止される依存**:
- nexus-gojo
- nexus-funeral
- nexus-bridal
- nexus-point

## セキュリティ（将来実装）

```kotlin
// 認証: Keycloak 連携
@Configuration
class SecurityConfig {
    // OAuth2 Resource Server 設定
}

// 認可: メソッドレベル
@PreAuthorize("hasRole('ADMIN')")
fun deleteHousehold(id: HouseholdId) { ... }
```

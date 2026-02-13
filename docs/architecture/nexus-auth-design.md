# NEXUS 認証・認可設計書

## 1. 概要

### 1.1 設計思想

```
┌─────────────────────────────────────────────────────────┐
│ 認可の第1層 = Keycloak のロール（認可の源泉）           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ ・ロールがなければアクセス不可（これが認可の源泉）      │
│ ・ロールから法人・ドメインが一意に決まる                │
│ ・法人マスタは接続情報・表示情報を保持する              │
│                                                         │
│ 【最終認可の判定】                                       │
│ アクセス可能 = ロールで許可 AND is_active = '1'        │
│                                                         │
│ → ロールがなければ使えない                              │
│ → is_active = '0' ならロールがあっても使えない         │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 1.2 認証・認可フロー

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  1. Keycloak ログイン                                   │
│     └─ ユーザー認証、JWT トークン発行                  │
│                         │                               │
│                         ↓                               │
│  2. ロール付与（認可の源泉）                            │
│     └─ nexus_db_access クレームにロール一覧            │
│        例: ["integration__ALL__GROUP",                 │
│             "saitama__musashino__GOJO",                │
│             "saitama__musashino__FUNERAL"]             │
│                         │                               │
│                         ↓                               │
│  3. Frontend: bootstrap API 呼び出し                    │
│     └─ ユーザー情報・利用可能法人を一括取得            │
│                         │                               │
│                         ↓                               │
│  4. Frontend: ヘッダーに法人選択を表示                  │
│     └─ 選択した法人（cmpCd）を Context に保持          │
│                         │                               │
│                         ↓                               │
│  5. 以降のAPI呼び出し（cmpCd を送信）                   │
│     └─ BFF が必ず検証 → 通過後に DB接続先を確定        │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 2. 認可検証の原則【重要】

### 2.1 判断の主体は常に BFF

```
┌─────────────────────────────────────────────────────────┐
│ 認可検証の原則                                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【Frontend の責務】                                      │
│ ├─ ユーザーが選択した cmpCd を送信するのみ             │
│ ├─ 判断・検証は一切行わない                            │
│ └─ BFF からの 403 エラーをハンドリングする             │
│                                                         │
│ 【BFF の責務（判断の主体）】                             │
│ 1. Frontend から受け取った cmpCd を検証                 │
│ 2. JWT の nexus_db_access ロールに該当法人が含まれるか  │
│ 3. 含まれていなければ 403 Forbidden を返却              │
│ 4. 含まれていれば、法人マスタから接続情報を取得         │
│ 5. DB接続先を確定し、クエリを実行                       │
│                                                         │
│ 【原則】                                                 │
│ ・判断は常に BFF が行う                                 │
│ ・Frontend は「選択」を送るだけ                         │
│ ・BFF は「検証」して「確定」する                        │
│ ・ロールに含まれない法人へのアクセスは、               │
│   いかなる経路でも拒否                                  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 2.2 cmpCd の送信方法

```
┌─────────────────────────────────────────────────────────┐
│ cmpCd 送信方法                                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【採用方式】HTTP ヘッダ                                  │
│ X-Company-Code: 01                                      │
│                                                         │
│ 【重要な原則】                                           │
│ ・cmpCd はユーザー入力であり、BFF が毎回ロールで        │
│   検証して初めて有効となる                              │
│ ・Frontend が送ってよいのは「選択値」まで               │
│ ・正しさは BFF が保証する                               │
│                                                         │
│ 【送信ルール】                                           │
│ ・全 API で cmpCd を常に送る（Frontend は判断しない）   │
│ ・BFF 側でドメインに応じた制御を行う:                   │
│   - group/integration 系 → cmpCd を無視                │
│   - gojo/funeral 系 → cmpCd を検証して使用             │
│                                                         │
│ 【理由】                                                 │
│ ・Frontend がドメインごとの仕様を知らなくていい         │
│ ・将来 group が法人絞り込みに対応しても変更不要         │
│ ・監査ログに「どの法人を選択中だったか」が残る          │
│ ・「判断は BFF」の原則と一致                            │
│                                                         │
│ 【検討した他の方式】                                     │
│ ・URL パス: /api/bff/{cmpCd}/gojo/contracts             │
│   → 可観測性は高いが URL 設計に影響                    │
│ ・クエリパラメータ: ?cmpCd=01                           │
│   → シンプルだが全 API に必須パラメータ                │
│                                                         │
│ ※ どの方式でも「BFF が検証して確定」は不変             │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 2.3 BFF 側の cmpCd 処理ルール

```
┌─────────────────────────────────────────────────────────┐
│ ドメイン別 cmpCd 処理ルール                              │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【cmpCd 検証必須】法人スキーマにアクセスするドメイン    │
│ ├─ /gojo/*       → 検証し、該当法人スキーマへ接続      │
│ ├─ /funeral/*    → 検証し、該当法人スキーマへ接続      │
│ ├─ /payment/*    → 検証し、該当法人スキーマへ接続      │
│ ├─ /accounting/* → 検証し、該当法人スキーマへ接続      │
│ └─ /reporting/*  → 検証し、該当法人スキーマへ接続      │
│                                                         │
│ 【cmpCd 無視】Integration 固定のドメイン                │
│ ├─ /group/*      → 無視、Integration DB へ接続         │
│ ├─ /identity/*   → 無視、Integration DB へ接続         │
│ └─ /household/*  → 無視、Integration DB へ接続         │
│                                                         │
│ 【cmpCd 無視】共有インスタンス                          │
│ └─ /point/*      → 無視、Point DB へ接続               │
│                                                         │
│ 【認証系】cmpCd 不要                                    │
│ └─ /auth/*       → 検証対象外                          │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

#### BFF 実装例

```kotlin
/**
 * ドメイン別 cmpCd 処理判定
 */
enum class CmpCdHandling {
    VALIDATE_AND_USE,  // 検証し、法人スキーマへ接続
    IGNORE,            // 無視（Integration/Point 等の固定接続先）
    NOT_REQUIRED,      // 不要（認証系 API）
}

fun determineCmpCdHandling(uri: String): CmpCdHandling {
    return when {
        // 認証系: cmpCd 不要
        uri.startsWith("/api/bff/auth/") -> CmpCdHandling.NOT_REQUIRED
        
        // Integration 固定: cmpCd 無視
        uri.contains("/group/") -> CmpCdHandling.IGNORE
        uri.contains("/identity/") -> CmpCdHandling.IGNORE
        uri.contains("/household/") -> CmpCdHandling.IGNORE
        
        // Point DB 固定: cmpCd 無視
        uri.contains("/point/") -> CmpCdHandling.IGNORE
        
        // その他（gojo, funeral 等）: cmpCd 検証必須
        else -> CmpCdHandling.VALIDATE_AND_USE
    }
}
```

#### 監査ログへの記録

```kotlin
/**
 * cmpCd を無視するドメインでも、監査ログには記録する
 * → 「どの法人を選択中に操作したか」がトレース可能
 */
fun recordAuditLog(
    jwt: Jwt,
    cmpCd: String?,        // 送られてきた cmpCd（無視した場合も記録）
    handling: CmpCdHandling,
    action: String,
    resource: String,
) {
    val log = AuditLog(
        timestamp = Instant.now(),
        keycloakSub = jwt.subject,
        username = jwt.getClaimAsString("preferred_username"),
        cmpCd = cmpCd,
        cmpCdHandling = handling.name,  // どう処理したか
        action = action,
        resource = resource,
    )
    auditLogRepository.save(log)
}
```

---

## 3. 認可の二層構造【重要】

### 3.1 二層構造の定義

```
┌─────────────────────────────────────────────────────────┐
│ 認可の二層構造                                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【第1層】ロール（Keycloak）= 認可の源泉                 │
│ ├─ 「誰が」「どの法人の」「どのドメインを」使えるか    │
│ ├─ ユーザー単位の認可                                  │
│ └─ ロールがなければアクセス不可（これは不変）          │
│                                                         │
│ 【第2層】マスタ（NXCM_COMPANY.is_active）= 運用の安全弁│
│ ├─ 「その法人が」「現在利用可能か」                    │
│ ├─ システム全体の制御                                  │
│ └─ is_active = '0' なら、ロールがあってもアクセス不可 │
│                                                         │
│ 【最終認可の判定】                                       │
│ アクセス可能 = ロールで許可 AND is_active = '1'        │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 3.2 言い換え

| 層 | 意味 | 例 |
|----|------|-----|
| ロール | 「あなたは使っていい」 | ユーザーAは武蔵野GOJOを使える |
| is_active | 「この法人は今使える状態」 | 武蔵野は現在稼働中 |

**両方揃って初めてアクセス可能**

### 3.3 運用シナリオ

```
┌─────────────────────────────────────────────────────────┐
│ is_active が必要な理由                                   │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【ケース1】法人の一時停止                               │
│ ・システムメンテナンス中                                │
│ ・データ移行中                                          │
│ ・障害対応中                                            │
│ → Keycloak のロールを全員分外すのは非現実的            │
│ → is_active = '0' で一括停止                           │
│                                                         │
│ 【ケース2】法人の廃止・統合                             │
│ ・A法人がB法人に吸収                                   │
│ ・A法人へのアクセスは完全に停止したい                  │
│ → ロールは順次削除するが、漏れがあるかもしれない       │
│ → is_active = '0' で確実に止める                       │
│                                                         │
│ 【ケース3】新法人の準備                                 │
│ ・マスタは先に作っておく                                │
│ ・でもまだ本番稼働前                                    │
│ → is_active = '0' で準備中を表現                       │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 3.4 エラーの区別

| 状況 | HTTPステータス | メッセージ |
|------|---------------|-----------|
| ロールがない | 403 Forbidden | 「この法人へのアクセス権限がありません」 |
| is_active = '0' | 503 Service Unavailable | 「この法人は現在利用できません」 |

→ エラーを区別することで、ユーザーに適切なメッセージを表示できる

### 3.5 BFF 実装

```kotlin
/**
 * 認可の二層構造を実装
 */
fun validateAndResolve(roles: List<String>, cmpCd: String, domain: String?): ConnectionInfo {
    // =========================================
    // 第1層: ロール検証（認可の源泉）
    // =========================================
    val availableCompanies = resolveAvailableCompanies(roles)
    val company = availableCompanies.find { it.cmpCd == cmpCd }
        ?: throw AccessDeniedException("Access denied to company: $cmpCd")  // 403

    // ドメイン検証
    if (domain != null && !company.availableDomains.contains(domain)) {
        throw AccessDeniedException("Access denied to domain: $domain")  // 403
    }

    // =========================================
    // 第2層: マスタ有効性検証（運用の安全弁）
    // =========================================
    val companyMaster = companyRepository.findByCmpCd(cmpCd)
        ?: throw IllegalStateException("Company master not found: $cmpCd")

    if (companyMaster.isActive != "1") {
        throw CompanyNotAvailableException("Company is not available: $cmpCd")  // 503
    }

    // =========================================
    // 両方通過 → 接続情報を返す
    // =========================================
    return ConnectionInfo(
        cmpCd = cmpCd,
        regionCd = companyMaster.regionCd,
        companyCd = companyMaster.companyCd,
        dbHost = companyMaster.dbHost,
        dbPort = companyMaster.dbPort,
        dbServiceName = companyMaster.dbServiceName,
        schemaName = companyMaster.schemaName,
    )
}

/**
 * 権限エラー（403）
 */
class AccessDeniedException(message: String) : RuntimeException(message)

/**
 * 法人利用不可エラー（503）
 */
class CompanyNotAvailableException(message: String) : RuntimeException(message)
```

### 3.6 例外ハンドラー

```kotlin
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(e: AccessDeniedException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ErrorResponse(
                code = "ACCESS_DENIED",
                message = "この法人へのアクセス権限がありません",
            ))
    }

    @ExceptionHandler(CompanyNotAvailableException::class)
    fun handleCompanyNotAvailable(e: CompanyNotAvailableException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(ErrorResponse(
                code = "COMPANY_NOT_AVAILABLE",
                message = "この法人は現在利用できません",
            ))
    }
}

data class ErrorResponse(
    val code: String,
    val message: String,
)
```

### 3.7 bootstrap API での反映

```kotlin
/**
 * bootstrap API では is_active = '1' の法人のみ返す
 */
fun resolveAvailableCompanies(roles: List<String>): List<AvailableCompany> {
    val companyDomains = roleParserService.getAvailableCompanies(roles)

    return companyDomains.mapNotNull { (key, domains) ->
        val (region, company) = key.split("__")
        companyRepository.findByRegionAndCompany(region, company)?.let { cmp ->
            // is_active = '0' の法人は除外
            if (cmp.isActive != "1") {
                return@let null
            }

            val masterDomains = cmp.availableDomains.split(",").toSet()
            val effectiveDomains = domains.intersect(masterDomains)

            if (effectiveDomains.isNotEmpty()) {
                AvailableCompany(
                    cmpCd = cmp.cmpCd,
                    companyName = cmp.companyName,
                    companyNameShort = cmp.companyNameShort,
                    regionCd = cmp.regionCd,
                    companyCd = cmp.companyCd,
                    availableDomains = effectiveDomains,
                    displayOrder = cmp.displayOrder,
                )
            } else {
                null
            }
        }
    }.sortedBy { it.displayOrder }
}
```

→ **利用可能法人一覧に is_active = '0' の法人は含まれない**
→ **ユーザーはそもそも選択できない**
→ **直接APIを叩いた場合は 503 で拒否**

---

## 4. Tenant と RegionContext の分離【重要】

### 4.1 概念の定義

```
┌─────────────────────────────────────────────────────────┐
│ Tenant と RegionContext                                  │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【Tenant（cmpCd）】                                      │
│ ├─ 定義: ユーザーの業務スコープ選択                    │
│ ├─ 意味: 「どの法人のデータを見たいか」                │
│ ├─ 例: "01" (武蔵野)                                   │
│ └─ 誰が扱う: Frontend（選択）、BFF（検証）             │
│                                                         │
│ 【RegionContext】                                        │
│ ├─ 定義: 技術的な DB ルーティングコンテキスト          │
│ ├─ 意味: 「どの DB / スキーマに接続するか」            │
│ ├─ 例: saitama-db.internal / ZEBRA_AREA1               │
│ └─ 誰が扱う: BFF のみ（Frontend は知らない）           │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 4.2 分離の原則

```
┌─────────────────────────────────────────────────────────┐
│ 分離の原則                                               │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 1. Tenant から Region を「導出」しない                  │
│    ├─ Frontend は Region を知らない・計算しない        │
│    └─ BFF も「導出」ではなく「ロール + マスタで解決」  │
│                                                         │
│ 2. Frontend は RegionContext を扱わない                 │
│    ├─ DB ホスト名、スキーマ名は Frontend に渡さない    │
│    └─ cmpCd のみを送信                                 │
│                                                         │
│ 3. 将来の Tenant 統合に備える                           │
│    ├─ UI 上で複数法人をまとめて表示（Tenant 統合）     │
│    ├─ DB ルーティング構造は変更しない                  │
│    └─ 複数の RegionContext へ並列アクセスし結果を統合  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 4.3 解決フロー

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│  Frontend                                               │
│  ├─ ユーザーが法人を選択                               │
│  └─ cmpCd: "01" を送信                                 │
│                         │                               │
│                         ↓                               │
│  BFF: 検証                                              │
│  ├─ JWT から roles 取得                                │
│  │   ["saitama__musashino__GOJO", ...]                 │
│  ├─ cmpCd: "01" が許可されているか？                   │
│  │   → roles をパースして確認                          │
│  └─ 許可されていなければ 403                           │
│                         │                               │
│                         ↓ 許可された                    │
│  BFF: RegionContext 解決                                │
│  ├─ NXCM_COMPANY から接続情報取得                      │
│  │   cmpCd: "01"                                       │
│  │   → region: saitama                                 │
│  │   → dbHost: saitama-db.internal                     │
│  │   → schema: ZEBRA_AREA1                             │
│  └─ DataSource を選択して接続                          │
│                         │                               │
│                         ↓                               │
│  BFF: クエリ実行・結果返却                              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

---

## 5. Keycloak 設定

### 5.1 ロール命名規則

```
{region}__{company}__{domain}

例:
├─ integration__ALL__GROUP      # Integration DB（横断検索）
├─ saitama__musashino__GOJO     # 埼玉地区 武蔵野法人 互助会
├─ saitama__musashino__FUNERAL  # 埼玉地区 武蔵野法人 葬祭
├─ fukushima__fukushima__GOJO   # 福島地区 福島法人 互助会
└─ ...
```

### 5.2 ロール一覧

| region | company | domain | 説明 |
|--------|---------|--------|------|
| integration | ALL | GROUP | Integration DB 横断検索 |
| saitama | musashino | GOJO | 武蔵野 互助会 |
| saitama | musashino | FUNERAL | 武蔵野 葬祭 |
| saitama | saikan | GOJO | サイカン 互助会 |
| saitama | saikan | FUNERAL | サイカン 葬祭 |
| saitama | fukushiso | FUNERAL | 中央福祉 葬祭のみ |
| fukushima | fukushima | GOJO | 福島 互助会 |
| fukushima | fukushima | FUNERAL | 福島 葬祭 |
| fukushima | touhoku | GOJO | 東北 互助会 |
| fukushima | touhoku | FUNERAL | 東北 葬祭 |
| fukushima | gifu | GOJO | 岐阜 互助会 |
| fukushima | gifu | FUNERAL | 岐阜 葬祭 |
| tochigi | tochigi | GOJO | 栃木 互助会 |
| tochigi | tochigi | FUNERAL | 栃木 葬祭 |
| tochigi | shizuoka | GOJO | 静岡 互助会 |
| tochigi | shizuoka | FUNERAL | 静岡 葬祭 |
| tochigi | tochigitksr | GOJO | 栃木タクセル 互助会 |
| tochigi | tochigitksr | FUNERAL | 栃木タクセル 葬祭 |

※ POINT, PAYMENT 等は追加時に作成

### 5.3 Client Scope 設定

```json
{
  "name": "nexus-db-access",
  "description": "NEXUS DB Access Scope - client roles を nexus_db_access claim として出力",
  "protocol": "openid-connect",
  "protocolMappers": [
    {
      "name": "nexus-db-access-mapper",
      "protocol": "openid-connect",
      "protocolMapper": "oidc-usermodel-client-role-mapper",
      "config": {
        "multivalued": "true",
        "id.token.claim": "true",
        "access.token.claim": "true",
        "claim.name": "nexus_db_access",
        "usermodel.clientRoleMapping.clientId": "nexus-bff"
      }
    }
  ]
}
```

### 5.4 JWT トークン例

```json
{
  "sub": "550e8400-e29b-41d4-a716-446655440000",
  "preferred_username": "haga",
  "email": "haga@example.com",
  "nexus_db_access": [
    "integration__ALL__GROUP",
    "saitama__musashino__GOJO",
    "saitama__musashino__FUNERAL"
  ],
  "iat": 1700000000,
  "exp": 1700003600
}
```

---

## 6. NEXUS マスタテーブル

### 6.1 法人マスタ（NXCM_COMPANY）

ロールから法人を特定した後、表示名やDB接続先を取得するためのマスタ。

```sql
CREATE TABLE NXCM_COMPANY (
    cmp_cd              CHAR(2)       NOT NULL,  -- 法人コード (01, 02, ...)
    region_cd           VARCHAR2(20)  NOT NULL,  -- 地区コード (saitama, fukushima, ...)
    company_cd          VARCHAR2(20)  NOT NULL,  -- 法人コード (musashino, saikan, ...)
    company_name        VARCHAR2(100) NOT NULL,  -- 法人名 (武蔵野互助会)
    company_name_short  VARCHAR2(50),            -- 法人名略称 (武蔵野)
    available_domains   VARCHAR2(200) NOT NULL,  -- 利用可能ドメイン (GOJO,FUNERAL,POINT)
    display_order       NUMBER(3)     NOT NULL,  -- 表示順
    is_active           CHAR(1)       DEFAULT '1',
    create_ymdhmi       CHAR(17),
    update_ymdhmi       CHAR(17),
    PRIMARY KEY (cmp_cd),
    UNIQUE (region_cd, company_cd)
);

CREATE INDEX NXCM_COMPANY_I01 ON NXCM_COMPANY(region_cd, company_cd);
```

#### 初期データ

```sql
-- 埼玉地区
INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('01', 'saitama', 'musashino', '武蔵野互助会', '武蔵野', 
    'GOJO,FUNERAL', 1, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('02', 'saitama', 'saikan', 'サイカン互助会', 'サイカン', 
    'GOJO,FUNERAL', 2, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('03', 'saitama', 'fukushiso', '中央福祉葬祭', '中央福祉', 
    'FUNERAL', 3, '1');

-- 福島地区
INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('05', 'fukushima', 'fukushima', '福島互助会', '福島', 
    'GOJO,FUNERAL', 1, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('06', 'fukushima', 'touhoku', '東北互助会', '東北', 
    'GOJO,FUNERAL', 2, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('07', 'fukushima', 'gifu', '岐阜互助会', '岐阜', 
    'GOJO,FUNERAL', 3, '1');

-- 栃木地区
INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('09', 'tochigi', 'tochigi', '栃木互助会', '栃木', 
    'GOJO,FUNERAL', 1, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('10', 'tochigi', 'shizuoka', '静岡互助会', '静岡', 
    'GOJO,FUNERAL', 2, '1');

INSERT INTO NXCM_COMPANY (cmp_cd, region_cd, company_cd, company_name, company_name_short, 
    available_domains, display_order, is_active)
VALUES ('11', 'tochigi', 'tochigitksr', '栃木タクセル互助会', '栃木タクセル', 
    'GOJO,FUNERAL', 3, '1');
```

### 6.2 ユーザーマッピング（NXCM_USER_MAPPING）

Keycloak ユーザーと社員マスタを紐付けるテーブル。
※ 第一弾では後回し可能

```sql
CREATE TABLE NXCM_USER_MAPPING (
    keycloak_sub      VARCHAR2(36)  NOT NULL,  -- Keycloak の sub (UUID)
    cmp_cd            CHAR(2)       NOT NULL,  -- 法人コード
    bosyu_cd          CHAR(7)       NOT NULL,  -- 社員マスタの募集コード
    is_primary        CHAR(1)       NOT NULL,  -- 主たる所属 ('1'/'0')
    valid_from        DATE          NOT NULL,
    valid_to          DATE,
    create_ymdhmi     CHAR(17),
    update_ymdhmi     CHAR(17),
    PRIMARY KEY (keycloak_sub, cmp_cd, bosyu_cd)
);

CREATE INDEX NXCM_USER_MAPPING_I01 ON NXCM_USER_MAPPING(keycloak_sub);
CREATE INDEX NXCM_USER_MAPPING_I02 ON NXCM_USER_MAPPING(cmp_cd, bosyu_cd);
```

---

## 7. Backend (BFF) 実装

### 7.1 ロール解析サービス

```kotlin
package com.example.nexus.bff.auth

import org.springframework.stereotype.Service

@Service
class RoleParserService {

    /**
     * パース済みロール
     */
    data class ParsedRole(
        val region: String,      // saitama, fukushima, tochigi, integration
        val company: String,     // musashino, saikan, ALL, ...
        val domain: String,      // GOJO, FUNERAL, GROUP, ...
    )

    /**
     * ロール文字列をパース
     */
    fun parseRoles(roles: List<String>): List<ParsedRole> {
        return roles.mapNotNull { role ->
            val parts = role.split("__")
            if (parts.size == 3) {
                ParsedRole(
                    region = parts[0],
                    company = parts[1],
                    domain = parts[2]
                )
            } else {
                null
            }
        }
    }

    /**
     * Integration DB へのアクセス権があるか
     */
    fun hasIntegrationAccess(roles: List<String>): Boolean {
        return roles.any { it.startsWith("integration__") }
    }

    /**
     * 利用可能な法人とドメインを取得
     * @return Map<"region__company", Set<domain>>
     */
    fun getAvailableCompanies(roles: List<String>): Map<String, Set<String>> {
        return parseRoles(roles)
            .filter { it.region != "integration" }
            .groupBy { "${it.region}__${it.company}" }
            .mapValues { (_, parsedRoles) ->
                parsedRoles.map { it.domain }.toSet()
            }
    }

    /**
     * 指定した法人・ドメインへのアクセス権があるか
     */
    fun hasAccess(roles: List<String>, regionCd: String, companyCd: String, domain: String): Boolean {
        val targetRole = "${regionCd}__${companyCd}__${domain}"
        return roles.contains(targetRole)
    }

    /**
     * 指定した法人へのアクセス権があるか（ドメイン問わず）
     */
    fun hasCompanyAccess(roles: List<String>, regionCd: String, companyCd: String): Boolean {
        val prefix = "${regionCd}__${companyCd}__"
        return roles.any { it.startsWith(prefix) }
    }
}
```

### 7.2 法人解決サービス

```kotlin
package com.example.nexus.bff.auth

import org.springframework.stereotype.Service

@Service
class CompanyResolverService(
    private val roleParserService: RoleParserService,
    private val companyRepository: CompanyRepository,
) {

    /**
     * 利用可能法人情報
     */
    data class AvailableCompany(
        val cmpCd: String,
        val companyName: String,
        val companyNameShort: String?,
        val regionCd: String,
        val companyCd: String,
        val availableDomains: Set<String>,
        val displayOrder: Int,
    )

    /**
     * ロールから利用可能法人を解決
     */
    fun resolveAvailableCompanies(roles: List<String>): List<AvailableCompany> {
        val companyDomains = roleParserService.getAvailableCompanies(roles)

        return companyDomains.mapNotNull { (key, domains) ->
            val (region, company) = key.split("__")
            companyRepository.findByRegionAndCompany(region, company)?.let { cmp ->
                // マスタで定義されているドメインとロールの交差
                val masterDomains = cmp.availableDomains.split(",").toSet()
                val effectiveDomains = domains.intersect(masterDomains)

                if (effectiveDomains.isNotEmpty()) {
                    AvailableCompany(
                        cmpCd = cmp.cmpCd,
                        companyName = cmp.companyName,
                        companyNameShort = cmp.companyNameShort,
                        regionCd = cmp.regionCd,
                        companyCd = cmp.companyCd,
                        availableDomains = effectiveDomains,
                        displayOrder = cmp.displayOrder,
                    )
                } else {
                    null
                }
            }
        }.sortedBy { it.displayOrder }
    }

    /**
     * cmpCd からアクセス可能か検証し、接続情報を返す
     * @throws AccessDeniedException ロールに含まれない場合
     */
    fun validateAndResolve(roles: List<String>, cmpCd: String, domain: String? = null): ConnectionInfo {
        val availableCompanies = resolveAvailableCompanies(roles)
        val company = availableCompanies.find { it.cmpCd == cmpCd }
            ?: throw AccessDeniedException("Access denied to company: $cmpCd")

        // ドメイン指定がある場合はドメインもチェック
        if (domain != null && !company.availableDomains.contains(domain)) {
            throw AccessDeniedException("Access denied to domain: $domain in company: $cmpCd")
        }

        // 法人マスタから接続情報を取得
        val companyMaster = companyRepository.findByCmpCd(cmpCd)
            ?: throw IllegalStateException("Company master not found: $cmpCd")

        return ConnectionInfo(
            cmpCd = cmpCd,
            regionCd = companyMaster.regionCd,
            companyCd = companyMaster.companyCd,
            dbHost = companyMaster.dbHost,
            dbPort = companyMaster.dbPort,
            dbServiceName = companyMaster.dbServiceName,
            schemaName = companyMaster.schemaName,
        )
    }

    data class ConnectionInfo(
        val cmpCd: String,
        val regionCd: String,
        val companyCd: String,
        val dbHost: String,
        val dbPort: Int,
        val dbServiceName: String,
        val schemaName: String,
    )
}

class AccessDeniedException(message: String) : RuntimeException(message)
```

### 7.3 認証コントローラー（bootstrap API）

```kotlin
package com.example.nexus.bff.controller

import com.example.nexus.bff.auth.CompanyResolverService
import com.example.nexus.bff.auth.RoleParserService
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/bff/auth")
class AuthController(
    private val roleParserService: RoleParserService,
    private val companyResolverService: CompanyResolverService,
) {

    /**
     * 初期化用API（1回で必要な情報を全て返す）
     * 
     * ログイン後にフロントエンドから最初に呼び出し、
     * ユーザー情報・利用可能法人を一括取得する。
     * 
     * Fail Fast: この API が失敗したら初期化失敗として扱う
     */
    @GetMapping("/bootstrap")
    fun bootstrap(
        @AuthenticationPrincipal jwt: Jwt
    ): BootstrapResponse {
        val roles = jwt.getClaimAsStringList("nexus_db_access") ?: emptyList()
        val companies = companyResolverService.resolveAvailableCompanies(roles)

        return BootstrapResponse(
            user = UserInfo(
                sub = jwt.subject,
                username = jwt.getClaimAsString("preferred_username"),
                email = jwt.getClaimAsString("email"),
            ),
            roles = roles,
            availableCompanies = companies.map { cmp ->
                AvailableCompanyDto(
                    cmpCd = cmp.cmpCd,
                    companyName = cmp.companyName,
                    companyNameShort = cmp.companyNameShort,
                    availableDomains = cmp.availableDomains.toList(),
                )
            },
            hasIntegrationAccess = roleParserService.hasIntegrationAccess(roles),
        )
    }
}

data class BootstrapResponse(
    val user: UserInfo,
    val roles: List<String>,
    val availableCompanies: List<AvailableCompanyDto>,
    val hasIntegrationAccess: Boolean,
)

data class UserInfo(
    val sub: String,
    val username: String?,
    val email: String?,
)

data class AvailableCompanyDto(
    val cmpCd: String,
    val companyName: String,
    val companyNameShort: String?,
    val availableDomains: List<String>,
)
```

### 7.4 認可フィルター（BFF での検証）

```kotlin
package com.example.nexus.bff.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 認可フィルター
 * 
 * 【重要】判断の主体は BFF
 * - Frontend から受け取った cmpCd を検証
 * - ロールに含まれない法人へのアクセスは 403
 * - 検証を通過した場合のみ、後続の処理へ
 */
@Component
class CompanyAuthorizationFilter(
    private val companyResolverService: CompanyResolverService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        // 認証不要パスはスキップ
        if (isPublicPath(request.requestURI)) {
            filterChain.doFilter(request, response)
            return
        }

        val authentication = SecurityContextHolder.getContext().authentication
        val jwt = authentication?.principal as? Jwt

        if (jwt == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication required")
            return
        }

        // cmpCd を取得（ヘッダー or パラメータ）
        val cmpCd = request.getHeader("X-Company-Code")
            ?: request.getParameter("cmpCd")

        if (cmpCd != null) {
            val roles = jwt.getClaimAsStringList("nexus_db_access") ?: emptyList()
            val domain = extractDomainFromPath(request.requestURI)

            try {
                // 【ここが判断のポイント】
                // BFF がロールを検証し、通過した場合のみ接続情報を返す
                val connectionInfo = companyResolverService.validateAndResolve(roles, cmpCd, domain)

                // リクエスト属性に接続情報を設定（後続処理で使用）
                request.setAttribute("connectionInfo", connectionInfo)
            } catch (e: AccessDeniedException) {
                // ロールに含まれない法人へのアクセスは拒否
                response.sendError(HttpServletResponse.SC_FORBIDDEN, e.message)
                return
            }
        }

        filterChain.doFilter(request, response)
    }

    private fun isPublicPath(uri: String): Boolean {
        return uri.startsWith("/api/bff/auth/") ||
               uri.startsWith("/actuator/")
    }

    private fun extractDomainFromPath(uri: String): String? {
        return when {
            uri.contains("/gojo/") -> "GOJO"
            uri.contains("/funeral/") -> "FUNERAL"
            uri.contains("/point/") -> "POINT"
            uri.contains("/group/") -> "GROUP"
            else -> null
        }
    }
}
```

### 7.5 動的データソース設定

```kotlin
package com.example.nexus.bff.config

import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class DynamicDataSourceConfig(
    private val companyRepository: CompanyRepository,
) {

    /**
     * 法人ごとのDataSourceをプールとして保持
     * 
     * 起動時に全法人分の接続プールを作成し、
     * リクエスト時に ConnectionInfo.cmpCd で選択する
     */
    @Bean
    fun companyDataSources(): Map<String, DataSource> {
        return companyRepository.findAllActive()
            .associate { company ->
                company.cmpCd to createDataSource(company)
            }
    }

    /**
     * Integration DB用のDataSource
     */
    @Bean
    fun integrationDataSource(): DataSource {
        return HikariDataSource().apply {
            jdbcUrl = "jdbc:oracle:thin:@integration-db.internal:1521/ORCL"
            username = "INTEGRATION_DATA"
            password = getPassword("integration")
            maximumPoolSize = 20
            poolName = "integration-pool"
        }
    }

    private fun createDataSource(company: Company): DataSource {
        return HikariDataSource().apply {
            jdbcUrl = "jdbc:oracle:thin:@${company.dbHost}:${company.dbPort}/${company.dbServiceName}"
            username = company.schemaName
            password = getPassword(company.cmpCd)
            maximumPoolSize = 5  // 法人ごとに小さめのプール
            poolName = "company-${company.cmpCd}-pool"
        }
    }

    private fun getPassword(key: String): String {
        // 実際は Vault や AWS Secrets Manager から取得
        return "password"
    }
}
```

---

## 8. Frontend 実装

### 8.1 型定義

```typescript
// types/auth.ts

export interface UserInfo {
  sub: string;
  username: string | null;
  email: string | null;
}

export interface AvailableCompany {
  cmpCd: string;
  companyName: string;
  companyNameShort: string | null;
  availableDomains: string[];
}

export interface BootstrapResponse {
  user: UserInfo;
  roles: string[];
  availableCompanies: AvailableCompany[];
  hasIntegrationAccess: boolean;
}

export interface AuthContext {
  user: UserInfo | null;
  roles: string[];
  availableCompanies: AvailableCompany[];
  hasIntegrationAccess: boolean;
  selectedCompany: AvailableCompany | null;
  selectCompany: (cmpCd: string) => void;
  isLoading: boolean;
  isInitialized: boolean;
  error: Error | null;
}
```

### 8.2 認証コンテキスト

```typescript
// contexts/AuthContext.tsx

import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useSession } from 'next-auth/react';

const AuthContext = createContext<AuthContext | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { data: session, status } = useSession();
  
  // 状態
  const [user, setUser] = useState<UserInfo | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [availableCompanies, setAvailableCompanies] = useState<AvailableCompany[]>([]);
  const [hasIntegrationAccess, setHasIntegrationAccess] = useState(false);
  const [selectedCompany, setSelectedCompany] = useState<AvailableCompany | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isInitialized, setIsInitialized] = useState(false);
  const [error, setError] = useState<Error | null>(null);

  // ログイン後に bootstrap API を呼び出し
  useEffect(() => {
    if (status === 'authenticated' && session?.accessToken) {
      bootstrap();
    } else if (status === 'unauthenticated') {
      setIsLoading(false);
    }
  }, [status, session]);

  const bootstrap = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const response = await fetch('/api/bff/auth/bootstrap', {
        headers: {
          Authorization: `Bearer ${session?.accessToken}`,
        },
      });

      if (!response.ok) {
        throw new Error(`Bootstrap failed: ${response.status}`);
      }

      const data: BootstrapResponse = await response.json();

      // 状態を一括設定
      setUser(data.user);
      setRoles(data.roles);
      setAvailableCompanies(data.availableCompanies);
      setHasIntegrationAccess(data.hasIntegrationAccess);

      // 保存されている選択法人を復元、なければ最初の法人を選択
      const savedCmpCd = localStorage.getItem('selectedCmpCd');
      const savedCompany = data.availableCompanies.find(c => c.cmpCd === savedCmpCd);
      setSelectedCompany(savedCompany ?? data.availableCompanies[0] ?? null);

      setIsInitialized(true);
    } catch (err) {
      console.error('Bootstrap failed:', err);
      setError(err instanceof Error ? err : new Error('Unknown error'));
    } finally {
      setIsLoading(false);
    }
  };

  const selectCompany = useCallback((cmpCd: string) => {
    const company = availableCompanies.find(c => c.cmpCd === cmpCd);
    if (company) {
      setSelectedCompany(company);
      localStorage.setItem('selectedCmpCd', cmpCd);
    }
  }, [availableCompanies]);

  return (
    <AuthContext.Provider
      value={{
        user,
        roles,
        availableCompanies,
        hasIntegrationAccess,
        selectedCompany,
        selectCompany,
        isLoading,
        isInitialized,
        error,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
```

### 8.3 ヘッダーコンポーネント（法人選択）

```typescript
// components/Header/CompanySelector.tsx

import React from 'react';
import { useAuth } from '@/contexts/AuthContext';

export const CompanySelector: React.FC = () => {
  const { availableCompanies, selectedCompany, selectCompany, isLoading, error } = useAuth();

  if (isLoading) {
    return <div className="animate-pulse h-10 w-40 bg-gray-200 rounded" />;
  }

  if (error) {
    return <div className="text-red-500 text-sm">初期化エラー</div>;
  }

  if (availableCompanies.length === 0) {
    return <div className="text-gray-500 text-sm">利用可能な法人がありません</div>;
  }

  // 1法人のみの場合は選択不要
  if (availableCompanies.length === 1) {
    return (
      <div className="px-3 py-2 text-sm font-medium text-gray-700">
        {selectedCompany?.companyNameShort ?? selectedCompany?.companyName}
      </div>
    );
  }

  return (
    <select
      value={selectedCompany?.cmpCd ?? ''}
      onChange={(e) => selectCompany(e.target.value)}
      className="block w-full px-3 py-2 text-sm border border-gray-300 rounded-md 
                 focus:outline-none focus:ring-2 focus:ring-blue-500"
    >
      {availableCompanies.map((company) => (
        <option key={company.cmpCd} value={company.cmpCd}>
          {company.companyNameShort ?? company.companyName}
        </option>
      ))}
    </select>
  );
};
```

### 8.4 API クライアント（cmpCd 自動付与）

```typescript
// lib/apiClient.ts

import { useSession } from 'next-auth/react';
import { useAuth } from '@/contexts/AuthContext';

class ApiClient {
  private baseUrl: string;
  private getAccessToken: () => string | null;
  private getSelectedCmpCd: () => string | null;

  constructor(
    baseUrl: string,
    getAccessToken: () => string | null,
    getSelectedCmpCd: () => string | null
  ) {
    this.baseUrl = baseUrl;
    this.getAccessToken = getAccessToken;
    this.getSelectedCmpCd = getSelectedCmpCd;
  }

  private async request<T>(
    path: string,
    options: RequestInit = {},
    includeCmpCd: boolean = true
  ): Promise<T> {
    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...options.headers,
    };

    // 認証トークン
    const token = this.getAccessToken();
    if (token) {
      headers['Authorization'] = `Bearer ${token}`;
    }

    // 法人コード（Frontend は選択した値を送るだけ、判断は BFF）
    const cmpCd = this.getSelectedCmpCd();
    if (includeCmpCd && cmpCd) {
      headers['X-Company-Code'] = cmpCd;
    }

    const response = await fetch(`${this.baseUrl}${path}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      if (response.status === 403) {
        throw new ApiError(403, 'この法人へのアクセス権限がありません');
      }
      throw new ApiError(response.status, await response.text());
    }

    return response.json();
  }

  async get<T>(path: string, includeCmpCd: boolean = true): Promise<T> {
    return this.request<T>(path, { method: 'GET' }, includeCmpCd);
  }

  async post<T>(path: string, body: unknown, includeCmpCd: boolean = true): Promise<T> {
    return this.request<T>(
      path,
      { method: 'POST', body: JSON.stringify(body) },
      includeCmpCd
    );
  }

  async put<T>(path: string, body: unknown, includeCmpCd: boolean = true): Promise<T> {
    return this.request<T>(
      path,
      { method: 'PUT', body: JSON.stringify(body) },
      includeCmpCd
    );
  }

  async delete<T>(path: string, includeCmpCd: boolean = true): Promise<T> {
    return this.request<T>(path, { method: 'DELETE' }, includeCmpCd);
  }
}

class ApiError extends Error {
  constructor(public status: number, message: string) {
    super(message);
    this.name = 'ApiError';
  }
}

// React Hook として使用
export const useApiClient = () => {
  const { selectedCompany } = useAuth();
  const { data: session } = useSession();

  return new ApiClient(
    '/api/bff',
    () => (session as any)?.accessToken ?? null,
    () => selectedCompany?.cmpCd ?? null
  );
};
```

---

## 9. DB接続構成

### 9.1 接続パターン

```
┌─────────────────────────────────────────────────────────┐
│ DB接続パターン                                           │
├─────────────────────────────────────────────────────────┤
│                                                         │
│ 【パターン1】Integration DB（読み取り中心）             │
│ 接続先: integration-db.internal                         │
│ スキーマ: INTEGRATION_DATA                              │
│ 用途:                                                   │
│ ├─ nexus-group      全法人横断検索                     │
│ ├─ nexus-identity   人物マスタ（将来）                 │
│ ├─ nexus-household  世帯マスタ（将来）                 │
│ └─ NXCM_*, NXCC_*   共通マスタ・設定                   │
│                                                         │
│ 【パターン2】地区DB + 法人スキーマ指定                  │
│ 接続先: 法人マスタの db_host                            │
│ スキーマ: 法人マスタの schema_name                      │
│ 用途:                                                   │
│ ├─ nexus-gojo       互助会業務                         │
│ ├─ nexus-funeral    葬祭業務                           │
│ ├─ nexus-bridal     冠婚業務                           │
│ ├─ nexus-payment    決済・収納                         │
│ ├─ nexus-agent      収納代行                           │
│ ├─ nexus-accounting 経理                               │
│ └─ nexus-reporting  帳票                               │
│                                                         │
│ 【パターン3】共有インスタンス（法人切替なし）           │
│ 接続先: point-db.internal（別インスタンス）             │
│ スキーマ: NEXUS_POINT                                   │
│ 用途:                                                   │
│ └─ nexus-point      ポイントシステム                   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 9.2 地区・法人・スキーマ対応表

| 地区 | 法人コード | 法人名 | DBホスト | スキーマ |
|------|-----------|--------|----------|----------|
| saitama | 01 | 武蔵野 | saitama-db.internal | ZEBRA_AREA1 |
| saitama | 02 | サイカン | saitama-db.internal | ZEBRA_AREA2 |
| saitama | 03 | 中央福祉 | saitama-db.internal | ZEBRA_SOUSAI3 |
| fukushima | 05 | 福島 | fukushima-db.internal | ZEBRA_AREA1 |
| fukushima | 06 | 東北 | fukushima-db.internal | ZEBRA_AREA2 |
| fukushima | 07 | 岐阜 | fukushima-db.internal | ZEBRA_AREA3 |
| tochigi | 09 | 栃木 | tochigi-db.internal | ZEBRA_AREA1 |
| tochigi | 10 | 静岡 | tochigi-db.internal | ZEBRA_AREA2 |
| tochigi | 11 | 栃木タクセル | tochigi-db.internal | ZEBRA_AREA3 |

---

## 10. 監査ログ

### 10.1 第一弾での監査ログ

社員マスタ紐付け（NXCM_USER_MAPPING）は後回しにするため、
Keycloak の情報のみで監査ログを記録。

```kotlin
data class AuditLog(
    val timestamp: Instant,
    val keycloakSub: String,       // JWT の sub
    val username: String,           // JWT の preferred_username
    val cmpCd: String?,             // 選択中の法人
    val action: String,             // CREATE, UPDATE, DELETE, READ
    val resource: String,           // contracts, receipts, ...
    val resourceId: String?,        // 対象のID
    val details: Map<String, Any>?, // 詳細情報
)
```

### 10.2 将来（社員マスタ紐付け後）

```kotlin
data class AuditLog(
    val timestamp: Instant,
    val keycloakSub: String,
    val username: String,
    val cmpCd: String?,
    val bosyuCd: String?,           // 社員マスタの募集コード
    val staffName: String?,         // 社員名
    val action: String,
    val resource: String,
    val resourceId: String?,
    val details: Map<String, Any>?,
)
```

---

## 11. 実装優先度

### 第一弾で必要

| 優先度 | 項目 | 内容 |
|--------|------|------|
| 1 | NXCM_COMPANY | 法人マスタ（ロール→法人情報の解決に必須） |
| 2 | RoleParserService | ロール解析サービス |
| 3 | CompanyResolverService | 法人解決サービス（検証 + 接続情報取得） |
| 4 | CompanyAuthorizationFilter | 認可フィルター（BFF での検証） |
| 5 | /api/bff/auth/bootstrap | 初期化API（1回で全情報取得） |
| 6 | 動的DB接続 | 法人スキーマ切替 |
| 7 | Frontend AuthContext | 認証コンテキスト |
| 8 | ヘッダー法人選択 | 法人選択UI |

### 後回し可能

| 項目 | 理由 |
|------|------|
| NXCM_USER_MAPPING | 社員マスタ紐付けなしでも動作可能 |
| 社員情報表示 | Keycloak の username で代用可能 |

---

## 12. チェックリスト

### 設計原則の確認

- [ ] 認可の源泉は Keycloak のロールのみ
- [ ] 判断の主体は BFF（Frontend は選択を送るだけ）
- [ ] 認可の二層構造（ロール AND is_active）
- [ ] Tenant（cmpCd）と RegionContext は分離
- [ ] Tenant から Region を導出しない
- [ ] Frontend は RegionContext を知らない
- [ ] 全 API で cmpCd を送る（Frontend は判断しない）
- [ ] group/integration 系は cmpCd を無視

### 実装の確認

- [ ] bootstrap API で初期化が 1 回で完結する
- [ ] bootstrap API は is_active = '1' の法人のみ返す
- [ ] CompanyAuthorizationFilter でロール検証を行う
- [ ] ロール検証失敗時は 403 を返す
- [ ] is_active = '0' の場合は 503 を返す
- [ ] 検証通過後に ConnectionInfo を設定する
- [ ] Frontend の ApiClient は cmpCd をヘッダーに設定する
- [ ] 監査ログに cmpCd を記録する（無視した場合も）

---

## 13. 参考資料

- Keycloak 設定: `realm-nexus-complete.json`
- P2-11 不変事項: 認可憲法

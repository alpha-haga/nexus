# P0-3b: DataSource / RegionContext 最終固定（ルーティング方針）

## 0. 目的
P0-3b は「地区DB + integration のルーティング配線」を確定し、以降の実装で迷わない土台を作る。

- JPA は当面 MIN（JDBC への全面移行はしない）
- Read=JDBC / Write=JPA は将来方針（P0-3bではルーティングの共通基盤のみ確定）
- ルーティングは JPA/JDBC どちらでも同じ仕組みに乗ること（将来の差し替え自由度確保）

## 1. DataSource 構成（固定）
DataSource は 4つとする。

- region: saitama / fukushima / tochigi（地区DB）
- integration: integration（統合DB）

## 2. ルーティングキー（固定）
ルーティングキーは **Region** とする。

- Region enum（固定）: SAITAMA / FUKUSHIMA / TOCHIGI / INTEGRATION
- ルーティングの実体は `RoutingDataSource` によって決定される
- 将来 tenant 等の拡張が必要でも、P0段階では Region に固定し、RegionContext を唯一の入口にする

## 3. RegionContext（責務と境界）
### 3.1 責務
- RegionContext は「現在の処理がどの Region DB に向くべきか」を保持する
- RegionContext を “読む” のは infrastructure（DataSource 層）
- RegionContext を “セット/クリア” するのは app 層（bff/api/batch）
- RegionContext は app / infrastructure の双方から参照されるため、
  特定レイヤに属さない共通実行コンテキストとして nexus-core に配置する

### 3.2 置き場（固定）
- RegionContext は `nexus-core` に置く（domain / infrastructure / app が参照する共通概念）
  - 例: `nexus.core.region.RegionContext`

### 3.3 実装方式（固定）
- ThreadLocal を採用（P0段階の最小・堅牢案）
- app 層は必ず finally で clear する（リーク防止）

## 4. Region 未設定時の挙動（固定）
- RegionContext が未設定の場合は **FAIL（例外）** とする
  - 事故（意図しない integration 参照）を早期に検知するため

## 5. ドメイン→DB の割当（固定）
| domain | default region | 備考 |
|---|---|---|
| group | INTEGRATION | 統合DB |
| identity | INTEGRATION | 基盤データは統合寄り |
| household | INTEGRATION | 基盤データは統合寄り |
| gojo | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| funeral | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| bridal | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| point | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| agent | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| payment | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| accounting | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |
| reporting | SAITAMA/FUKUSHIMA/TOCHIGI | 地区DB（Region必須） |

※ “default” は「通常の参照先」であり、実際の参照は RegionContext の値に従う。

## 6. app 層での RegionContext セット方針
### 6.1 BFF/API（Web）
- Request ごとに RegionContext をセットし、レスポンス後に clear する
- セット方法は以下のどちらか（採用方式を固定する）
  - Filter（OncePerRequestFilter）
  - HandlerInterceptor（preHandle/afterCompletion）

### 6.2 Batch
- Job/Step の開始点で RegionContext をセットし、終了時に clear する

## 7. Region の決め方（入力ソース）
P0-3b の段階では「Region をどこから決めるか」も固定する。

- 原則: 認証 Claim（Keycloak）から取り出す（将来）
- 現段階の local/dev は暫定:
  - HTTP Header `X-NEXUS-REGION`（例: saitama/fukushima/tochigi/integration）
  - Header が無い場合は FAIL（P0段階は事故防止優先）

## 8. infrastructure 側ルーティング（JPA/JDBC共通）
- `RoutingDataSource` を 1つ定義し、JPA/JDBC 共にそれを参照する
- JPA:
  - EntityManagerFactory / TransactionManager は RoutingDataSource を DataSource として使用する
- JDBC:
  - JdbcTemplate / NamedParameterJdbcTemplate も RoutingDataSource を使用する（P0では未使用でも配線だけ行う）

## 9. 検証項目（P0-3b Done 定義）
- `./gradlew build` が通る
- local プロファイルで `:nexus-bff:bootRun` が起動し、RegionContext のセット/クリアが動いている（ログ or 簡易テスト）
- Region 未設定時に FAIL することが確認できる（例外が出る）

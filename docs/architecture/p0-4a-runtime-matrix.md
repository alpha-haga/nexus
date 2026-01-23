# P0-4a 起動確認マトリクス（local/dev/stg/prod）

本書は P0-4a として、環境切替（local/dev/stg/prod）が
「env 切替だけで起動できる」状態であることを確認するための手順を固定する。

- 対象：nexus-bff / nexus-api / nexus-batch
- 前提：設定ファイルの構造（キー名・階層）は確定済み（P0-4で変更しない）
- 注意：本書は機能確認ではなく「起動成立」の確認に限る（P1以降で機能確認）

---

## 1. 目的

- `SPRING_PROFILES_ACTIVE` の切替だけで起動できること
- DB / Keycloak など外部依存は環境変数で差し替えできること
- P0-3b の Region ルーティング基盤（RegionContext / RoutingDataSource）を壊さないこと

---

## 2. 対象モジュールと起動コマンド

共通：
- backend ディレクトリで実行する

### 2-1. nexus-bff
```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew :nexus-bff:bootRun
SPRING_PROFILES_ACTIVE=dev   ./gradlew :nexus-bff:bootRun
SPRING_PROFILES_ACTIVE=stg   ./gradlew :nexus-bff:bootRun
SPRING_PROFILES_ACTIVE=prod  ./gradlew :nexus-bff:bootRun
```

### 2-2. nexus-api
```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew :nexus-api:bootRun
SPRING_PROFILES_ACTIVE=dev   ./gradlew :nexus-api:bootRun
SPRING_PROFILES_ACTIVE=stg   ./gradlew :nexus-api:bootRun
SPRING_PROFILES_ACTIVE=prod  ./gradlew :nexus-api:bootRun
```

### 2-3. nexus-batch
```bash
cd backend
SPRING_PROFILES_ACTIVE=local ./gradlew :nexus-batch:bootRun
SPRING_PROFILES_ACTIVE=dev   ./gradlew :nexus-batch:bootRun
SPRING_PROFILES_ACTIVE=stg   ./gradlew :nexus-batch:bootRun
SPRING_PROFILES_ACTIVE=prod  ./gradlew :nexus-batch:bootRun
```

---

## 3. 起動成立の判定基準（P0-4a）

### 3-1. 共通

- Spring Boot が起動完了ログまで到達すること
- 起動直後に例外で落ちないこと

### 3-2. nexus-bff / nexus-api

- `/actuator/health` が 200 を返すこと（local で確認できれば十分）
- Swagger UI が表示できること（local で確認できれば十分）

※dev/stg/prod はネットワーク制約がある場合、HTTP到達は必須条件にしない。
起動ログ到達を優先する。

### 3-3. nexus-batch

- 起動が成立すること（job 実行の成否は P0-4a の対象外）
- 起動時に勝手に job が動く／動かないはここでは議論しない（P1で扱う）

---

## 4. 必須の環境変数（テンプレ）

※値は環境ごとに異なる。設定キーの"名前"だけをここで固定する。

### 4-1. DB（integration + regions）

- `NEXUS_DB_DRIVER`（未指定時は `oracle.jdbc.OracleDriver` を想定）
- `NEXUS_DB_POOL_MAX` / `NEXUS_DB_POOL_MIN`（任意）

**integration：**

- `NEXUS_DB_INTEGRATION_URL`
- `NEXUS_DB_INTEGRATION_USER`
- `NEXUS_DB_INTEGRATION_PASS`

**regions：**

- `NEXUS_DB_SAITAMA_URL`
- `NEXUS_DB_SAITAMA_USER`
- `NEXUS_DB_SAITAMA_PASS`
- `NEXUS_DB_FUKUSHIMA_URL`
- `NEXUS_DB_FUKUSHIMA_USER`
- `NEXUS_DB_FUKUSHIMA_PASS`
- `NEXUS_DB_TOCHIGI_URL`
- `NEXUS_DB_TOCHIGI_USER`
- `NEXUS_DB_TOCHIGI_PASS`

### 4-2. OIDC / Keycloak

- `NEXUS_OIDC_ISSUER_URI`

---

## 5. 既知の注意（P0-3b 前提の再掲）

- RegionContext は core（確定）
- RegionContext の責務：
  - app（bff/api/batch）が set/clear
  - infrastructure が get（読むだけ）
  - domain は触らない
- 起動成立のため、RoutingDataSource の `defaultTargetDataSource=integration` は存在してよい（起動フェーズ限定）
- 業務処理中の暗黙フォールバックは禁止（fail-fast を維持）

---

## 6. P0-4a の Done 条件

以下が満たされていれば P0-4a を「完了扱い」とする。

1. 本書が `docs/architecture/` に追加されている
2. 既存のビルド・アーキテクチャテストが通る

```bash
cd backend
./gradlew build
./gradlew :nexus-architecture-tests:test
```

# P0-3e 循環依存の再発防止（運用固定）

本書は P0 フェーズにおける「循環依存の再発防止」を目的として、
依存追加時の禁止事項・確認手順を固定する。

---

## 1. 位置づけ（重要）

P0-3e は「循環が起きたら解く」フェーズではない。  
**循環が再発しない状態を維持し続けるためのルール固定**である。

- 「今、循環が無い」＝「P0-3e 不要」ではない
- 循環は **依存追加・責務混入・query系の置き場ブレ**で再発する
- 議論ではなく **事実（依存ツリー）**で判断する

---

## 2. 現時点の事実（P0-3e 着手時点の前提）

- Gradle の project 依存として循環は発生していない
- 依存方向は P0-3a の原則に一致している

許可される依存方向:

- app（bff/api/batch）→ domain → core
- infrastructure → domain + core

禁止される依存方向:

- domain → infrastructure / app
- domain → domain（ドメイン間の直接参照）

---

## 3. 再発しやすいパターン（禁止）

以下は禁止。発見したら差分を戻す（例外運用なし）。

### 3-1. 「参照が欲しい」理由で domain 間依存を追加する
例: group が gojo / identity / household を import し始める等。

- Read の都合で domain 間依存を入れると、依存肥大・循環の起点になる
- ReadModel 共有の議論は P1 以降（P0 ではやらない）

### 3-2. query/readmodel を理由に core に置く
- core に置けるのは RegionContext のような **業務と無関係な横断コンテキストのみ**
- ReadModel / QueryService / Reader は core に置かない

### 3-3. 「便利だから」で build.gradle.kts に project 依存を追加する
- 依存追加は最も再発原因になりやすい
- 追加するなら **必ず依存ツリーで影響を確認してから**（手順は次章）

---

## 4. 循環疑い時の即時確認手順（運用ルール）

循環・依存肥大が疑われた場合、以下で事実確認する。

```bash
cd backend
./gradlew :nexus-group:dependencies --configuration compileClasspath
./gradlew :nexus-infrastructure:dependencies --configuration compileClasspath
```

**重要:**
- 「なんとなくおかしい」議論は禁止
- 依存ツリーに現れたものだけを根拠に判断する

---

## 5. P0-3b 完成品の扱い（再発防止の重要前提）

以下は完成品として扱い、循環回避のために移動・責務変更しない。

**RegionContext は core（確定）**

RegionContext の責務分離は厳守:

- app: set/clear
- infrastructure: get（読むだけ）
- domain: 一切触らない

業務処理中の暗黙フォールバックは禁止（fail-fast 維持）

---

## 6. P0-3e の Done 条件

以下が満たされていれば P0-3e を「完了扱い」とする。

1. 本書（再発防止ルール）が docs/architecture/ に追加されている
2. ビルドとアーキテクチャテストが通る

```bash
cd backend
./gradlew build
./gradlew :nexus-architecture-tests:test
```

---

## 実行コマンド（apply後に必ず実行）
```bash
cd backend
./gradlew build
./gradlew :nexus-architecture-tests:test
```

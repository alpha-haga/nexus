# P0-3e: 循環依存 再発防止フェーズ（設計固定・運用ルール）

## 1. このフェーズの位置づけ

P0-3e は「循環依存を解消するフェーズ」ではない。

- 現時点で **循環依存は発生していない**
- しかし過去の経緯から、
  - モジュール追加
  - query 責務の混入
  - Gradle 依存の安易な追加
  によって **再発しやすい構造ポイント**が存在する

そのため P0-3e は、

> 循環依存が **再発しない状態を維持・固定するフェーズ**

として扱う。

---

## 2. 現在の依存関係の事実（再議論不要）

### 2.1 Gradle project 依存

現時点で循環は存在しない。

#### nexus-group
```
implementation(project(":nexus-core"))
```

#### nexus-infrastructure
```
implementation(project(":nexus-core"))
implementation(project(":nexus-identity"))
implementation(project(":nexus-household"))
implementation(project(":nexus-gojo"))
implementation(project(":nexus-group"))
```

依存方向は以下の通り：

```
infrastructure → domain + core
```

これは P0-3a で固定したルールと一致している。

---

### 2.2 nexus-group 内コードの参照状況

以下の確認結果より：

```
grep -R "import nexus\." -n nexus-group/src/main/kotlin \
  | grep -v "import nexus\.group\."
```

- nexus-group は **nexus-core のみを参照**
- 他ドメイン（identity / household / gojo 等）への直接参照は存在しない

→ **domain → domain の循環は現時点では存在しない**

---

## 3. 重要な注意点（誤解防止）

### 3.1 「循環が無い」＝「P0-3e が不要」ではない

P0-3e の目的は以下：

- 現在循環が無いことを確認する
- 循環が **再発しやすい構造ポイントを固定する**
- 再発時に「議論」ではなく「事実」で即判断できる状態を作る

そのため、P0-3e は
- 何かを「解く」フェーズではなく
- 「壊れない状態を維持する」フェーズである

---

## 4. 循環が疑われた場合の即時確認手順（運用ルール）

循環・依存肥大が疑われた場合は、必ず以下で事実確認を行う。

```bash
cd backend

./gradlew :nexus-group:dependencies --configuration compileClasspath
./gradlew :nexus-infrastructure:dependencies --configuration compileClasspath
```

- project 依存・jar 依存の循環は **ここで必ず可視化される**
- 「なんとなく設計がおかしい」という議論は禁止
- 常に **依存グラフの事実**を根拠に判断する

---

## 5. P0-3e の作業スタンス（前提固定）

### 5.1 完成品として扱うもの
- RegionContext
- DataSource ルーティング（P0-3b）

これらは **P0-3e 以降で触らない**。

---

### 5.2 依存方向ルール（再確認）
- app → domain → core
- infrastructure → domain + core
- domain → infrastructure / app 禁止

このルールは **ArchUnit により機械的に強制**されている。

---

### 5.3 再発防止の禁止事項
以下は禁止とする：

- 「便利だから」という理由での依存追加
- domain に他ドメインの query / ReadModel を混入させること
- core に ReadModel / Reader / query を置くこと
- app / domain から RegionContext を参照すること
- 業務処理中の integration への暗黙フォールバック

---

## 6. query / reader / readmodel の扱い（P0-3e 時点）

- query / reader / readmodel は **責務分離前提で整理する**
- ReadModel の配置先は：
  - 原則：domain 内
  - もしくは BFF DTO
- core に置けるのは RegionContext のような
  **業務と無関係なシステム横断コンテキストのみ**

---

## 7. P0-3e の完了条件（Done）

P0-3e は、以下が満たされていれば完了とする：

- 循環依存が発生していないことが事実として確認できる
- ArchUnit（P0-3a）が CI 上で常に通る
- 循環再発時の即時確認手順が固定されている
- 依存追加に対する「近道」が運用上封じられている

---

## 8. 次フェーズとの接続

P0-3e 完了後は、以下に進む：

- **P0-3c**：Read / Write 切替可能な導線の最小設計
  - Repository / Reader / ReadModel の役割整理
  - 実装は当面 JPA（JPA MIN）
  - JDBC 共通基盤は必要最小のみ

P0-3e は、
**P0-3c を安全に開始するための最終的な土台固定フェーズ**
である。

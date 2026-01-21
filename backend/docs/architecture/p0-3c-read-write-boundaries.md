# P0-3c: Read / Write 導線の最小設計（JPA MIN / 将来差し替え可能）

## 0. 目的

P0-3c は「Read=JDBC / Write=JPA」を**今すぐ全面導入するフェーズではない**。  
当面は **JPA MIN** で実装しつつ、将来 “速度問題が出た箇所だけ” JDBC に差し替えられるように、**導線（境界・置き場・命名）だけを最小で固定**する。

本フェーズの成果は「実装量」ではなく、**将来の自由度を確保する禁止事項と型の枠**である。

---

## 1. 前提（再議論不要）

- P0-3a（境界固定）完了済み（ArchUnit により機械強制）
- P0-3b（RegionContext / DataSource ルーティング）完了済み（完成品扱い）
- nexus-query は前提にしない（必要条件が揃うまで作らない）
- domain には現状 `@Service/@Transactional` が存在し得る  
  → P0-3c では技術純化を目的としない（P1 以降）

---

## 2. 何をやらないか（重要）

P0-3c では以下を行わない（勝手に進めない）：

- Read を JDBC で実装し始める（P0-3c では “導線” のみ）
- 共通 ReadModel を core に置く、あるいは共有モジュール化する
- nexus-query を新設する
- 既存の Repository を過度に再設計・分割する
- パフォーマンス議論（速度問題が出た箇所のみ後で対応）

---

## 3. 用語定義（P0-3c 固定）

### 3.1 Write 導線
- **Repository**：永続化を伴う更新（Write）の入口
- domain に interface を置き、infrastructure に実装を置く

### 3.2 Read 導線
- **Reader / QueryService**：取得・検索・一覧（Read）の入口
- domain に interface を置き、infrastructure に実装を置く
- Repository とは **別導線** とする（将来 JDBC 差し替えのため）

### 3.3 ReadModel
- Read の戻り値に使う DTO（表示・検索向け）
- **所属ドメイン内**に閉じる（他ドメインと共有しない）
- 共有が必要になった場合は BFF DTO で吸収し、必要条件が揃うまで nexus-query は作らない

---

## 4. ルール（最小で効く固定事項）

### 4.1 Write（Repository）ルール
- Write は **domain の `*Repository` interface** に集約する
- Repository は “集約単位” を基本とし、戻り値は Entity/Aggregate でよい（P1 で鈍化）

例（domain）：
```kotlin
interface GroupRepository {
    fun save(group: Group): Group
}
```

実装（infrastructure）：
- `JpaGroupRepository` など、技術を名前で表現してよい
- JPA MIN のため当面は Spring Data JPA / EntityManager いずれでも可

---

### 4.2 Read（Reader / QueryService）ルール
Read は用途で入口名を固定する。

| 用途 | 入口名 | 例 |
|---|---|---|
| 単純取得・一覧 | `*Reader` | `GroupReader` |
| 条件検索・集計 | `*QueryService` | `GroupQueryService` |

- Read は **Repository を直接使わない**
- Read の戻り値は **ReadModel**（or BFF DTO）とし、Entity を返さない  
  （P0-3a の Controller 返却禁止とは別に、Read 側も “Entity 直返し” を避ける土台を作る）

例（domain）：
```kotlin
interface GroupReader {
    fun findByPersonId(personId: PersonId): List<GroupSummary>
}
```

---

### 4.3 ReadModel ルール（配置と禁止）
- ReadModel は **domain 内**（例：`nexus.group.readmodel..`）に置く
- core に置かない（core はシステム横断コンテキストのみ）
- 他ドメインと共有しない（共有したい場合は BFF DTO で吸収）

例（domain）：
```kotlin
data class GroupSummary(
    val groupId: GroupId,
    val name: String
)
```

---

## 5. 置き場ルール（パッケージ指針）

P0-3c では以下を推奨する（固定）。

### domain（例：nexus.group..）
- `nexus.<domain>.repository..`（Write interface）
- `nexus.<domain>.reader..`（Read interface）
- `nexus.<domain>.query..`（Read interface：条件検索・集計）
- `nexus.<domain>.readmodel..`（ReadModel）

### infrastructure（例：nexus.infrastructure..）
- `nexus.infrastructure.<domain>.repository..`（Write 実装）
- `nexus.infrastructure.<domain>.reader..` / `query..`（Read 実装）

※ 実装技術（JPA/JDBC）は infrastructure 側の class 名や package で表現してよい。

---

## 6. 依存関係（固定）

依存方向は P0-3a の通り：

- app → domain → core
- infrastructure → domain + core
- domain → infrastructure/app 禁止

P0-3c では特に以下を守る：

- domain の Reader/Repository interface は **infrastructure を一切参照しない**
- ReadModel は domain 内に閉じ、core へ置かない

---

## 7. 例外（P0-3c 時点で許容するもの）

- domain に `@Service/@Transactional` が存在することは許容（段階導入）
- Read 実装が JPA であってもよい（JPA MIN）
- JDBC 共通基盤（RowMapper / Pagination 等）は P0-3d で必要最小のみ整備する

---

## 8. Done（完了条件）

P0-3c は以下が満たされていれば完了とする：

1. Repository（Write）と Reader/QueryService（Read）の**導線が分離**されている  
2. ReadModel の配置ルール（domain 内 / 非共有 / core禁止）が固定されている  
3. infrastructure に Read/Write 実装の置き場が決まり、将来 JDBC 差し替えが可能な形になっている  
4. `./gradlew build` と `./gradlew :nexus-architecture-tests:test` が通る

---

## 9. 次フェーズとの接続

- **P0-3d**：JDBC 共通基盤（必要最小）
- **P0-4**：環境切替（local/dev/stg/prod）

P0-3c は “導線の固定” が目的であり、JDBC 導入や性能最適化は後続フェーズで扱う。

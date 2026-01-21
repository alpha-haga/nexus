# P0-3a: 実開発前の土台固め（境界ルール固定）

## 0. 目的 / フェーズ位置づけ
P0-3a は「将来の自由度を確保するための禁止事項の固定」フェーズである。

- PoC は完了しており、ここからは壊れない土台を最優先とする
- JPA は当面 MIN 利用（全面 JDBC 化はしない）
- Read=JDBC / Write=JPA は将来拡張の設計方針であり、P0-3a では導線（境界）だけを固定する
- 本フェーズでは新機能追加はしない／JDBC 実装は最小（または未実装で可）

本フェーズの成果物：
1. モジュール境界と責務を文章で明確化する
2. 境界違反を ArchUnit で機械的に検知し CI で FAIL させる
3. group ↔ infrastructure の循環依存を「技術対応」ではなく「設計ルールの帰結」として解消方針を確定する（実装は P0-3e）

## 1. レイヤ定義（用語の固定）
本プロジェクトにおける「domain / infrastructure / app / core」は、パッケージで以下の通り定義する。

- **domain（ドメイン群）**: `nexus.<domain>..`
  - 例: `nexus.gojo..`, `nexus.group..`, `nexus.identity..`, `nexus.household..`, `nexus.payment..`, `nexus.accounting..` ...
  - 注: domain は `..domain..` のような共通パッケージを持たないため、「domain集合」を `nexus.<domain>..` として扱う
- **infrastructure**: `nexus.infrastructure..`
- **app**: `nexus.bff..` / `nexus.api..` / `nexus.batch..`
- **core**: `nexus.core..`

## 2. 依存方向ルール（最小で効く）
依存方向は以下に固定する。

- app -> domain -> core
- infrastructure -> domain + core
- domain -> infrastructure/app は禁止
- core ->（原則どこにも依存しない）

目的：
- domain を実装技術から独立させ、将来の差し替え自由度を確保する
- 循環依存を「設計で潰す」ことを可能にする

違反例：
- domain が infrastructure の DataSource / Repository 実装クラスを参照する
- domain が app の Controller / DTO を参照する

## 3. domain 技術依存禁止（段階導入）
P0-3a では domain から以下の技術依存（JPA/JDBC/SQL系）を禁止する。

禁止：
- `jakarta.persistence..`（JPA）
- `org.springframework.data.jpa..`（Spring Data JPA）
- `org.springframework.jdbc..`（Spring JDBC）
- `javax.sql..`（DataSource 等）

許可（既存コード互換のため、P0-3a では禁止しない）：
- `org.springframework.transaction..`（@Transactional 等）
- `org.springframework.stereotype..`（@Service 等）

目的：
- domain を JPA/JDBC どちらにも寄せない形に保ち、将来の差し替えを可能にする
- 既存の domain 内 @Service/@Transactional を壊さずに段階導入する

違反例：
- domain の class に @Entity を付与する
- domain が NamedParameterJdbcTemplate / DataSource を直接使う

## 4. Controller 境界（BFF/API）
BFF/API の Controller は domain の entity を返さない（返却型に含めない）。

- `nexus.bff..controller..` の public endpoint は `nexus.*.entity.*` を返却型に含めない
- `nexus.api..controller..` の public endpoint は `nexus.*.entity.*` を返却型に含めない

返却として許可される例：
- BFF/API 側 DTO（Response Model）
- domain の ReadModel（ただし Controller 返却として適切に整形されている場合）

## 5. Read / Write の役割分担（導線のみ固定）
P0-3a では Read=JDBC を強制しない。ただし将来切替可能な導線を固定する。

- Write:
  - domain の `*Repository`（interface）に閉じる
  - 実装は infrastructure（当面 JPA MIN）
- Read:
  - domain の `*Reader` または `*QueryService`（interface）を許可する
  - 戻り値は ReadModel（読み取り専用 DTO）
  - ReadModel は所属ドメイン内に閉じ、ドメイン間共有しない

ReadModel 共有が必要になった場合の扱い：
- まず BFF DTO で吸収する
- それでも破綻する条件が揃った場合のみ設計理由付きで提案する（P0-3a では前提にしない）

## 6. group 循環依存の解消方針（設計ルールとして確定）
循環依存は「技術で回避」しない。責務境界の帰結として設計で潰す。

- group が他ドメイン情報を必要とする場合は用途で分類する：
  - Write（整合性が必要）: group 集約内で完結させ、外部参照は最小化。必要なら Reader 経由で取得する
  - Read（表示/検索/集計）: group の Reader が ReadModel を返すことで吸収する
- 統合読み取り（JOIN/集計）は infrastructure 実装の責務とする
  - 実装技術は当面 JPA でも可（JPA MIN）
  - 速度問題が出た箇所のみ、後から JDBC 実装へ差し替え可能であれば十分

P0-3a では方針を確定し、実際の循環解消（移動/実装修正）は P0-3e で行う。

## 7. ArchUnit による機械強制（P0-3a）
P0-3a では以下を最低限強制する（L1必須 + 可能ならL2）。

### L1（必須）
- L1-1: domain は JPA/JDBC/SQL 系に依存してはならない（transaction/stereotype は許可）
- L1-2: domain -> infrastructure/app 依存禁止（domain集合で判定）
- L1-3: bff/api controller の public メソッドは `nexus.*.entity.*` を返却型に含めない（ジェネリクス含む）

### L2（任意）
- L2-1: repository と reader の相互参照禁止（責務混濁防止）
- L2-2: readmodel -> entity 参照禁止（ReadModel の純度確保）

## 8. nexus-query について（P0-3aでは作らない）
P0-3a の前提として nexus-query は新設しない。
必要条件（例：ReadModel 重複が顕著、BFF DTO 肥大化、複数アプリで統合検索の再利用が常態化）が揃った場合のみ、設計理由付きで提案する。

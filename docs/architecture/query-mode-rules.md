# QueryMode 設計・利用規約（NEXUS / P2-5 以降 共通）

## 0. 本規約の位置づけ（最重要）

本規約は **NEXUS プロジェクトにおいて QueryMode を使用する際の「設計原則と利用ルールを固定するための正本」**である。

- QueryMode は SQL を wrap して外側で WHERE/ORDER/OFFSET を付与する方針を採用している
- この方針により、COUNT/SEARCH の母集団一致を構造で担保し、パフォーマンス劣化を防ぐ
- 本規約に反する実装は **未完了（NG）** とみなす
- **SQL は人間が正として作成する**。AI は SQL の設計提案を行わず、Kotlin 側を機械的に追従させる

---

## 1. 概要

### 1.1 なぜ QueryMode があるか

JOIN 増加による一覧検索のパフォーマンス劣化に対し、「検索対象キーを最小構成で先に確定し、そのキーで一覧本体を取得する」構造へ移行するため。

- **COUNT**: 軽量な target SQL で母集団を確定（表示専用 JOIN を含めない）
- **SELECT_***: 表示に必要な JOIN を含む search SQL で一覧を取得
- **RAW**: SQL をそのまま返す（フォーム用マスタ取得など）

### 1.2 何を防ぐか

- COUNT と SELECT_* の母集団不一致（WHERE 条件の手動同期ミス）
- ORDER BY での内側テーブルエイリアス参照エラー（`contract_search.xxx` が外側から見えない）
- COUNT への ORDER BY/OFFSET 混入によるパフォーマンス劣化
- Kotlin 側での結果ソートによる順序保証（SQL の ORDER BY に寄せる）

---

## 2. QueryMode 一覧

### 2.1 COUNT

**責務**: 件数取得のみ

**入力**:
- `targetSqlPath`: 対象抽出SQL（検索条件に必要なJOINのみ、表示専用JOINは含めない）
- `condition`: 検索条件
- `sortKey`: 無視される（null で固定）
- `sortDir`: 無視される（null で固定）
- `page`: 無視される（null で固定）

**出力**:
- SQL: `SELECT COUNT(1) FROM (targetSql + where) t`
- パラメータ: `where.params()` + 追加パラメータ（例: `businessYmd`）

**生成されるSQL形**:
```sql
SELECT COUNT(1) FROM (
    -- target SQL（例: group_contract_target.sql）
    SELECT
        contract_search.cmp_cd AS cmp_cd
        , contract_search.contract_no AS contract_no
        -- WHERE句で使用する列も含める必要がある
        , contract_search.contract_receipt_ymd AS contract_receipt_ymd
        , contract_search.family_nm_kana AS family_name_kana
        , contract_search.search_tel_no AS search_tel_no
        , contract_search.course_cd AS course_cd
    FROM zgot_contract_search_key contract_search
    INNER JOIN zgot_contract_info_all contract_info 
        ON ...
    INNER JOIN zgot_status_rec_all status_rec
        ON ...
    -- 表示専用JOINは含めない
) t
WHERE
    contract_receipt_ymd >= :contractReceiptYmdFrom
    AND family_name_kana LIKE '%' || :familyNmKana || '%'
    -- 動的に生成されたWHERE句
```

**禁止事項**:
- ORDER BY を混入させること（COUNT には ORDER BY を付けない）
- OFFSET/FETCH を混入させること（COUNT には OFFSET/FETCH を付けない）

### 2.2 SELECT_ALL

**責務**: 全件取得（ページングなし）

**入力**:
- `searchSqlPath`: 一覧取得SQL（表示JOINすべて含む、WHERE/ORDER BY/OFFSET-FETCH は外側で付与）
- `condition`: 検索条件
- `sortKey`: ソートキー（null の場合はデフォルトソート）
- `sortDir`: ソート方向（"asc"/"desc"、null の場合は ASC）
- `page`: 無視される（null で固定）

**出力**:
- SQL: `SELECT * FROM (searchSql) s + where + orderBy`
- パラメータ: `where.params()` + 追加パラメータ（例: `businessYmd`）

**生成されるSQL形**:
```sql
SELECT * FROM (
    -- search SQL（例: group_contract_search.sql）
    WITH base AS (
        SELECT
            contract_search.cmp_cd AS cmp_cd
            , contract_search.contract_no AS contract_no
            -- 表示に必要な列すべて
        FROM zgot_contract_search_key contract_search
        LEFT JOIN zgom_course_cd_all course
            ON ...
        -- 表示専用JOINすべて含む
    )
    SELECT
        base.cmp_cd AS cmp_cd
        , base.contract_no AS contract_no
        , base.contract_receipt_ymd AS contract_receipt_ymd
        , base.family_name_kana AS family_name_kana
        , base.course_cd AS course_cd
        -- ORDER BYで使用する列は必ずSELECTで露出させる（ASで列名固定）
    FROM base base
    INNER JOIN zgot_contract_info_all contract_info 
        ON ...
    -- 表示に必要なJOINすべて含む
) s
WHERE
    contract_receipt_ymd >= :contractReceiptYmdFrom
    AND family_name_kana LIKE '%' || :familyNmKana || '%'
    -- 動的に生成されたWHERE句
ORDER BY
    contract_receipt_ymd DESC
    , contract_no
    -- 外側で参照可能な列名（内側テーブルエイリアス参照禁止）
```

### 2.3 SELECT_PAGED

**責務**: ページング付き取得

**入力**:
- `searchSqlPath`: 一覧取得SQL（表示JOINすべて含む、WHERE/ORDER BY/OFFSET-FETCH は外側で付与）
- `condition`: 検索条件
- `sortKey`: ソートキー（null の場合はデフォルトソート）
- `sortDir`: ソート方向（"asc"/"desc"、null の場合は ASC）
- `page`: ページネーション仕様（必須）

**出力**:
- SQL: `SELECT * FROM (searchSql) s + where + orderBy + offset/fetch`
- パラメータ: `where.params()` + `offset` + `limit` + 追加パラメータ（例: `businessYmd`）

**生成されるSQL形**:
```sql
SELECT * FROM (
    -- search SQL（SELECT_ALL と同じ）
) s
WHERE
    -- 動的に生成されたWHERE句
ORDER BY
    -- 動的に生成されたORDER BY句
OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
```

### 2.4 RAW

**責務**: SQL をそのまま返す（加工禁止）

**入力**:
- `rawSqlPath`: RAW SQL のパス（必須）
- `condition`: 無視される（null で固定）
- `sortKey`: 無視される（null で固定）
- `sortDir`: 無視される（null で固定）
- `page`: 無視される（null で固定）

**出力**:
- SQL: `rawSql` をそのまま返す（where/order/offset 付与禁止）
- パラメータ: 空のMap（または rawSql 内で定義されたパラメータを手動で追加）

**生成されるSQL形**:
```sql
-- raw SQL をそのまま返す（例: フォーム用マスタ取得）
SELECT
    course_cd
    , course_nm
FROM zgom_course_cd_all
WHERE delete_flg = '0'
ORDER BY course_cd
-- WHERE/ORDER/OFFSET は builder 側で付与しない
```

**禁止事項**:
- where/order/offset を builder 側で付与すること
- COUNT 用途に使うこと（COUNT 用途には COUNT モードを使用）

**注意事項**:
- PII（個人識別情報）を含む可能性があるため、ログ方針は P2-6 で決める（ここでは最小限の注意喚起のみ）

---

## 3. 母集団一致ルール（最重要）

### 3.1 原則

COUNT と SELECT_* の母集団（FROM/JOIN/WHERE）は **完全に一致** しなければならない。

### 3.2 実装方針

- COUNT は `targetSql` を使用し、SELECT_* は `searchSql` を使用する
- 両方とも **同一の `ConditionApplier`** を通して WHERE 句を生成する
- WHERE 句の生成ロジックは **1箇所に集約** し、手動同期を避ける

### 3.3 責務分界

- **target SQL**: 検索条件に必要なJOINのみを含む（軽量）
- **search SQL**: 表示に必要なJOINすべてを含む（重い）
- **WHERE句**: 両方とも同一の `ConditionApplier` で生成（母集団一致を担保）

### 3.4 検証方法

- COUNT と SELECT_* で同一の `condition` を渡し、件数が一致することを確認する
- 検索条件を変えて複数パターンで検証する

---

## 4. ORDER BY ルール（最重要）

### 4.1 外側wrap前提の制約

SELECT_* モードでは、search SQL を `SELECT * FROM (searchSql) s` としてwrapしている。

このため、**内側テーブルエイリアス（例: `contract_search.xxx`）は外側 ORDER BY から見えない**。

### 4.2 ORDER BY の書き方

**正しい例**:
```sql
-- search SQL の最終SELECT句で列名を固定
SELECT
    base.contract_receipt_ymd AS contract_receipt_ymd
    , base.contract_no AS contract_no
FROM base base
...
) s
ORDER BY
    contract_receipt_ymd DESC  -- 外側で参照可能な列名
    , contract_no              -- 外側で参照可能な列名
```

**間違った例**:
```sql
-- 内側テーブルエイリアスを参照（エラー）
ORDER BY
    contract_search.contract_receipt_ymd DESC  -- 外側から見えない
    , contract_search.contract_no              -- 外側から見えない
```

### 4.3 search SQL の責務

- ORDER BY で使用する列は **必ず SELECT で露出させる**（AS で列名固定）
- 列名は **外側で参照可能な形式** にする（例: `contract_receipt_ymd`、`contract_no`）
- 内側テーブルエイリアス（例: `contract_search.xxx`）を ORDER BY で使用しない

### 4.4 OrderByBuilder の責務

- 許可されたソートキーのみを受け付ける（ホワイトリスト制）
- ソートキーから「外側で参照可能な列名」へのマッピングを提供する
- デフォルトソートと安定ソート（二次キー）を提供する

**実装例**:
```kotlin
OrderByBuilder(
    allowed = mapOf(
        "contractReceiptYmd" to "contract_receipt_ymd",  // 外側で参照可能な列名
        "contractNo" to "contract_no"                    // 外側で参照可能な列名
    ),
    defaultOrderBy = "ORDER BY contract_receipt_ymd DESC, contract_no",
    stableSecondaryOrderBy = "contract_no ASC"
)
```

### 4.5 stable sort（二次キー）の扱い

- 同一の一次キー値が複数ある場合、順序を安定化するために二次キーを使用する
- `stableSecondaryOrderBy` で指定する（例: `contract_no ASC`）
- 二次キーも「外側で参照可能な列名」で指定する

---

## 5. OFFSET/FETCH ルール

### 5.1 SELECT_PAGED のみが offset/limit を持つ

- SELECT_PAGED モードのみが `OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY` を付与する
- SELECT_ALL モードには OFFSET/FETCH を付けない（全件取得）
- COUNT モードには OFFSET/FETCH を付けない（件数取得のみ）

### 5.2 COUNT への混入禁止

COUNT モードに ORDER BY や OFFSET/FETCH を混入させてはならない。

理由:
- COUNT は件数取得のみであり、順序やページングは不要
- ORDER BY や OFFSET/FETCH を付与すると、パフォーマンスが劣化する可能性がある

---

## 6. RAW ルール

### 6.1 加工禁止の定義

RAW モードでは、SQL を **一切加工しない**。

禁止事項:
- WHERE 句を付与すること
- ORDER BY 句を付与すること
- OFFSET/FETCH を付与すること
- パラメータを自動的に追加すること（rawSql 内で定義されたパラメータは手動で追加可能）

### 6.2 COUNT 用途に使わない

RAW モードは COUNT 用途に使わない。

理由:
- COUNT 用途には COUNT モードを使用する
- RAW モードは SQL をそのまま返すため、COUNT 用の wrap ができない

### 6.3 使用例

RAW モードは以下の用途で使用する:
- フォーム用マスタ取得（例: コース一覧、法人一覧、募集担当者一覧）
- 固定の SQL をそのまま実行したい場合

### 6.4 ログ方針

PII（個人識別情報）を含む可能性があるため、ログ方針は P2-6 で決める（ここでは最小限の注意喚起のみ）。

---

## 7. よくある事故と対策

### 7.1 ラップでエイリアスが見えない

**症状**:
- ORDER BY で `contract_search.contract_receipt_ymd` を参照するとエラーになる
- 「列が見つからない」というエラーメッセージが表示される

**原因**:
- search SQL を `SELECT * FROM (searchSql) s` としてwrapしているため、内側テーブルエイリアス（`contract_search`）が外側から見えない

**対策**:
- ORDER BY では「外側で参照可能な列名」を使用する（例: `contract_receipt_ymd`）
- search SQL の最終SELECT句で、ORDER BY で使用する列を必ず SELECT で露出させる（AS で列名固定）
- OrderByBuilder の `allowed` マップで「外側で参照可能な列名」を指定する

### 7.2 COUNT と SELECT_* の母集団不一致

**症状**:
- COUNT で取得した件数と、SELECT_* で取得した件数が一致しない
- ページング表示が正しく動作しない

**原因**:
- COUNT と SELECT_* で異なる WHERE 条件を使用している
- 手動で WHERE 条件を同期しているため、ミスが発生しやすい

**対策**:
- COUNT と SELECT_* で **同一の `ConditionApplier`** を使用する
- WHERE 句の生成ロジックを **1箇所に集約** する
- 検索条件を変えて複数パターンで検証する

### 7.3 COUNT に ORDER BY が混入する

**症状**:
- COUNT クエリの実行時間が長くなる
- パフォーマンスが劣化する

**原因**:
- COUNT モードに ORDER BY を付与している
- COUNT は件数取得のみであり、順序は不要

**対策**:
- COUNT モードには ORDER BY を付けない（SqlQueryBuilder の実装で担保）
- COUNT モードでは `orderByBuilder.build()` を呼び出さない

### 7.4 WHERE句で列名が見つからない

**症状**:
- WHERE 句で `family_name_kana` を参照するとエラーになる
- 「列が見つからない」というエラーメッセージが表示される

**原因**:
- target SQL または search SQL の最終SELECT句に、WHERE 句で使用する列が含まれていない
- 列名のエイリアスが一致していない

**対策**:
- target SQL と search SQL の最終SELECT句に、WHERE 句で使用する列を必ず含める
- 列名のエイリアスを統一する（例: `family_name_kana`）
- ConditionApplier で使用する列名が、SQL の最終SELECT句のエイリアスと一致していることを確認する

---

## 8. 実装側の責務

### 8.1 SqlQueryBuilder

**責務**:
- SQL ファイルを読み込む（`sqlLoader.load()`）
- WHERE 句を構築する（`WhereBuilder` + `ConditionApplier`）
- ORDER BY 句を構築する（`OrderByBuilder`、COUNT モードでは呼び出さない）
- OFFSET/FETCH を付与する（SELECT_PAGED モードのみ）
- パラメータを集約する（`where.params()` + 追加パラメータ）

**禁止事項**:
- SQL の JOIN 構成・WHERE 条件・計算式を AI 判断で変更すること
- COUNT モードに ORDER BY や OFFSET/FETCH を混入させること

### 8.2 OrderByBuilder

**責務**:
- 許可されたソートキーのみを受け付ける（ホワイトリスト制）
- ソートキーから「外側で参照可能な列名」へのマッピングを提供する
- デフォルトソートと安定ソート（二次キー）を提供する

**禁止事項**:
- 内側テーブルエイリアス（例: `contract_search.xxx`）を ORDER BY で使用すること
- 許可されていないソートキーを受け付けること

### 8.3 ConditionApplier

**責務**:
- 検索条件を WHERE 句に適用する
- 値が存在する条件のみ WHERE に追加する（NULL吸収目的の OR は禁止）
- COUNT と SELECT_* で **同一の WHERE 句を生成する**（母集団一致を担保）

**禁止事項**:
- NULL吸収目的の OR（`:p IS NULL OR col = :p`）を使用すること
- COUNT と SELECT_* で異なる WHERE 条件を生成すること

### 8.4 QueryService（Jdbc...QueryService）

**責務**:
- COUNT クエリを生成・実行する（`QueryMode.COUNT`）
- SELECT_PAGED クエリを生成・実行する（`QueryMode.SELECT_PAGED`）
- 追加パラメータ（例: `businessYmd`）を設定する
- 結果を DTO にマッピングする（`RowMapper`）

**禁止事項**:
- Kotlin 側で結果をソートして順序保証すること（SQL の ORDER BY に寄せる）
- COUNT と SELECT_* で異なる `ConditionApplier` を使用すること

---

## 9. 運用ガイド

### 9.1 どのモードをいつ使うか

**フォーム用マスタ取得**:
- RAW モードを使用する
- 例: コース一覧、法人一覧、募集担当者一覧

**一覧検索**:
- COUNT モード + SELECT_PAGED モードを使用する
- 例: 契約一覧、顧客一覧

**全件取得（ページングなし）**:
- SELECT_ALL モードを使用する
- 例: エクスポート機能、レポート生成

### 9.2 SQL ファイルの配置

**target SQL**:
- `backend/nexus-infrastructure/src/main/resources/sql/{domain}/{entity}_target.sql`
- 例: `group/group_contract_target.sql`

**search SQL**:
- `backend/nexus-infrastructure/src/main/resources/sql/{domain}/{entity}_search.sql`
- 例: `group/group_contract_search.sql`

**RAW SQL**:
- `backend/nexus-infrastructure/src/main/resources/sql/{domain}/{entity}_{purpose}.sql`
- 例: `group/group_course_master.sql`

### 9.3 SQL ファイルの作成方針

**SQL は人間が正として作成する**。AI は SQL の設計提案を行わず、Kotlin 側を機械的に追従させる。

**target SQL**:
- 検索条件に必要なJOINのみを含む（軽量）
- WHERE 句で使用する列を必ず SELECT で含める（AS で列名固定）
- 表示専用JOINは含めない

**search SQL**:
- 表示に必要なJOINすべてを含む（重い）
- ORDER BY で使用する列を必ず SELECT で露出させる（AS で列名固定）
- WHERE/ORDER BY/OFFSET-FETCH は外側で付与するため、SQL ファイルには含めない

---

## 10. まとめ

### 10.1 最重要ルール

1. **COUNT と SELECT_* の母集団一致**: 同一の `ConditionApplier` を使用する
2. **ORDER BY の列名**: 外側で参照可能な列名を使用する（内側テーブルエイリアス参照禁止）
3. **COUNT への ORDER BY/OFFSET 混入禁止**: パフォーマンス劣化を防ぐ
4. **SQL は人間が正**: AI は SQL の設計提案を行わず、Kotlin 側を機械的に追従させる

### 10.2 禁止事項（再掲）

- COUNT に ORDER BY/OFFSET を混入させること
- 外側wrap後に `contract_search.xxx` のような内側エイリアスを ORDER BY で参照すること
- RAW で where/order/offset を builder 側で付与すること
- Kotlin 側で結果をソートして順序保証すること（SQL の ORDER BY に寄せる）

---

## 11. 関連ドキュメント

- `p2-5-jdbc-querybuilder-guideline.md`: JDBC QueryBuilder の基本方針
- `cursor-output-rules.md`: Cursor 出力規約
- `ai-rules.md`: AI 実装ルール

# P2-5: JDBC QueryBuilder / Count-Search 整合ガイド

本書は P2-5（P1-B3：パフォーマンス）で扱う「JDBC検索のSQL生成方針」を固定する。
目的は、性能劣化と count/search の条件不一致事故を防ぎ、Fail Fast を維持することである。

## 1. 背景

- JDBC + バインド変数自体が悪いのではなく、
  `(:p IS NULL OR col = :p)` のような **OR吸収**が実行計画を悪化させ、索引利用を阻害しやすい。
- NEXUSでは JPA は業務ロジック系取得が主、JDBC は一覧/検索など重い Read が主となる。
  利用比率は固定しないが、JDBC側は性能・事故防止を最優先とする。

## 2. 結論（固定方針）

### 2.1 WHERE は動的に組み立てる
- **OR吸収は禁止**（`:p IS NULL OR ...` の形を廃止）
- 値が存在する条件のみ WHERE に追加する（全条件NULLの場合は "条件なし" として明示的に扱う）

### 2.2 count と select は実行として分ける
- `count` と `select` を **同一SQLに統合しない**
- 理由：ページング用途等で `COUNT(*) OVER()` による不要コストが発生し得るため

### 2.3 count/search の FROM/JOIN/WHERE は構造で一致させる
- 手作業で2SQLを同期し続ける運用は禁止
- 共通の「base（FROM/JOIN/WHERE）」を QueryBuilder が生成し、
  `count` / `select` / `select+offset` はラッパーで差分のみを付与する

### 2.4 OR の扱い

OR は実務要件（住所/氏名/電話番号などの複数条件のいずれか一致）で必要となるため**許可**する。
ただし、NULL吸収目的の OR は性能劣化要因のため**禁止**する。

**許可される OR**:
- 検索要件として明示された条件同士の結合のみ
- 例：住所1 OR 住所2、電話番号 OR 携帯電話番号 など

**禁止される OR（NULL吸収目的）**:
- NG例: `(:param IS NULL OR col = :param)`
- NG例: `(col = :param OR :param IS NULL)`
- 理由：実行計画を悪化させ、索引利用を阻害する

**OR の実装方針**:
- OR は WhereBuilder の `orGroup { ... }` を使って構造化する
- `orGroup` 内では `orIfNotNull(name, value) { "... :name ..." }` を使用し、値があるものだけを積む
- `orGroup` が空（OR条件が1つも積まれない）場合は WHERE に追加しない（状態を隠さない）
- OR の導入により実行計画が悪化する可能性があるため、P2-5 で必ず計測根拠を残す（推測禁止）

## 3. 3パターン（全件 / 件数 / offsetあり）の吸収

QueryMode を定義し、同一の QuerySpec（condition/sort/page）から SQL を生成する。

- COUNT: `SELECT COUNT(*) FROM ( base )`
- SELECT_ALL: `SELECT cols FROM ( base ) ORDER BY ...`
- SELECT_PAGED: `SELECT cols FROM ( base ) ORDER BY ... OFFSET ... FETCH ...`

## 4. ソート（将来の動的指定に備える）

- UI からソートキーが指定され得るため、**許可ソートキーのホワイトリスト制**とする
- 許可キー・デフォルト・不正時の扱いは `p1-b0-group-contract-display-requirements.md` を正とする
- SQL組み立てで列名を文字列結合しない
  - `sortKey -> (安全な ORDER BY 断片)` のマップで解決する
- 許可されない sortKey / direction は **400 Bad Request（Fail Fast）**

## 5. 責務分離

- QueryBuilder：SQL文字列と bind param を生成する（組み立て責務）
- QueryService（Jdbc...QueryService）：生成された SQL を実行し DTO に詰める（実行責務）
- Controller：入力値の形式チェック・400境界を守る（業務判断はしない）

### 5.1 WhereBuilder API（擬似コード）

WhereBuilder は以下の API を提供する：

```kotlin
// AND条件（値がある場合のみ追加）
andIfNotNull(name: String, value: Any?) { condition: String }

// ORグループ（複数のOR条件を構造化）
orGroup { 
  orIfNotNull(name: String, value: Any?) { condition: String }
  // ... 複数の orIfNotNull を記述
}
```

**使用例**:
- 住所検索（住所1 OR 住所2）: `orGroup { orIfNotNull("addr1", condition.addr1) { "addr1 LIKE :addr1" }; orIfNotNull("addr2", condition.addr2) { "addr2 LIKE :addr2" } }`
- `orGroup` 内に1つも条件が積まれない場合は、WHERE 句に追加されない

## 6. P2-5 Done（本書に基づく）

- OR吸収が廃止されている（動的WHERE）
- OR が実務要件として必要な場合のみ使用され、NULL吸収目的の OR が存在しない
- OR は `orGroup` により構造化され、読みやすさ/事故防止が担保されている
- count/search の FROM/JOIN/WHERE が構造で一致している
- 動的ソートがホワイトリストで安全に適用される（不正は400）
- 改善前後の計測が docs に残っている（推測禁止）

# P2-5: Group 契約一覧 JOIN 段階的復活計画

本ドキュメントは、P2-5 における **Group 契約一覧（法人横断契約一覧）の JOIN 段階的復活計画** を定義する。

**本書の位置づけ**: P2-5 の JOIN 実装計画の正本。P2-5 の JOIN 実装はこのドキュメントに従って進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（P1-B0 最低限の根拠）
- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）
- [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md)（QueryBuilder規律）

---

## 1. 前提（P2-4 を土台とする）

**P2-4 で成立した機能は成立済み（再実装禁止）**:
- P1-B0 最低限の一覧表示項目は P2-4 で実装済み
- P1-B1 で復活した JOIN（`cmp_short_name`, `course_name`, `monthly_premium`）は維持
- 認証/認可/権限制御/表示制御は P2-3 で成立済み

**設計憲法の再確認**:
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）

---

## 2. 目的（What）

P2-5 で追加する一覧表示項目の根拠となるJOINを段階的に復活させ、業務成立へ寄せる。

**JOIN 段階的復活の位置づけ**: P1-B1 の延長として、P2-5 で追加する項目に必要なJOINを段階的に復活させる。

---

## 3. JOIN 段階的復活計画（Wave 1 / Wave 2 / Wave 3）

### 3.1 Wave 1（最小・最軽量）

**追加される一覧表示項目**:
- 契約状態（contractStatus）
- 口数（shareNum）
- 契約金額（contractGaku）
- 都道府県名（prefName）
- 市区町村名（cityTownName）
- 募集担当者姓（漢字）（bosyuFamilyNameKanji）
- 募集担当者名（漢字）（bosyuFirstNameKanji）
- 加入担当者姓（漢字）（entryFamilyNameKanji）
- 加入担当者名（漢字）（entryFirstNameKanji）

**追加されるテーブル/ビュー/結合キー**:
- 契約状態テーブル（仮名: `zgom_contract_status`）
  - 結合キー: `contract_no`（想定）
  - 取得項目: `contract_status`
  - JOIN種別: LEFT JOIN（NULL許容）
- コース詳細テーブル（仮名: `zgom_course_detail`）
  - 結合キー: `cmp_cd`, `course_cd`（想定）
  - 取得項目: `share_num`, `contract_gaku`
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: `zgom_course_cd_all` との関係を確認（既存JOINとの整合）
- 住所マスタ（仮名: `zgom_address_master`）
  - 結合キー: `zip_cd` または `addr_key`（想定）
  - 取得項目: `pref_name`, `city_town_name`
  - JOIN種別: LEFT JOIN（NULL許容）
- 担当者マスタ（仮名: `zgom_staff_master`）
  - 結合キー: `staff_cd`（想定）
  - 取得項目: `family_name_kanji`, `first_name_kanji`
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: 募集担当者と加入担当者で2回JOINが必要（別エイリアス）

**性能リスク**: 中
- 回避策: 結合キーにインデックスが存在することを確認、実測SQLで計測

**計測方法**:
- 実測SQL: `group_contract_search.sql`（Wave 1 JOIN追加後）
- 対象条件: 実データ量での検索（約470万件前提）
- 許容値: P1-B2 の結果（平均1.6秒、最長2.6秒）を基準として、2倍以内を目標
- 計測項目: 実行時間、実行計画（explain plan）

### 3.2 Wave 2（中規模）

**追加される一覧表示項目**:
- タスク名（taskName）
- 積立回数（totalSaveNum）
- 積立金額（totalGaku）
- 郵便番号（zipCd）
- 大字町名（oazaTownName）
- 字丁目名（azaChomeName）

**追加されるテーブル/ビュー/結合キー**:
- タスクテーブル（仮名: `zgom_task`）
  - 結合キー: `task_cd` または `contract_no`（想定）
  - 取得項目: `task_name`
  - JOIN種別: LEFT JOIN（NULL許容）
- 積立テーブル（仮名: `zgom_savings`）
  - 結合キー: `contract_no`（想定）
  - 取得項目: `total_save_num`, `total_gaku`
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: 集計が必要な場合はサブクエリまたは集計関数を使用
- 住所マスタ（拡張）
  - 結合キー: 住所マスタ（Wave 1で追加済み）
  - 取得項目: `zip_cd`, `oaza_town_name`, `aza_chome_name`
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: Wave 1で追加した住所マスタJOINを拡張

**性能リスク**: 中〜高
- 回避策: 積立テーブルの集計はサブクエリまたは集計関数で最適化、実測SQLで計測

**計測方法**:
- 実測SQL: `group_contract_search.sql`（Wave 2 JOIN追加後）
- 対象条件: 実データ量での検索（約470万件前提）
- 許容値: Wave 1 の結果を基準として、1.5倍以内を目標
- 計測項目: 実行時間、実行計画（explain plan）

### 3.3 Wave 3（大規模・組織情報）

**追加される一覧表示項目**:
- 元請支給ランク組織コード（motoSupplyRankOrgCd）
- 元請支給ランク組織名（motoSupplyRankOrgName）
- 支給ランク組織コード（supplyRankOrgCd）
- 支給ランク組織名（supplyRankOrgName）
- 部署コード（sectCd）
- 部署名（sectName）
- 最終入金日（lastReceiptYmd）

**追加されるテーブル/ビュー/結合キー**:
- 組織マスタ（仮名: `zgom_org_master`）
  - 結合キー: `org_cd`（想定）
  - 取得項目: `moto_supply_rank_org_cd`, `moto_supply_rank_org_name`, `supply_rank_org_cd`, `supply_rank_org_name`
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: 元請と支給で2回JOINが必要（別エイリアス）
- 部署マスタ（仮名: `zgom_sect_master`）
  - 結合キー: `sect_cd`（想定）
  - 取得項目: `sect_name`
  - JOIN種別: LEFT JOIN（NULL許容）
- 入金テーブル（仮名: `zgom_receipt`）
  - 結合キー: `contract_no`（想定）
  - 取得項目: `last_receipt_ymd`（MAX集計）
  - JOIN種別: LEFT JOIN（NULL許容）
  - 注意: 集計が必要（MAX関数またはサブクエリ）

**性能リスク**: 高
- 回避策: 組織マスタのJOINはインデックス確認、入金テーブルの集計は最適化、実測SQLで計測

**計測方法**:
- 実測SQL: `group_contract_search.sql`（Wave 3 JOIN追加後）
- 対象条件: 実データ量での検索（約470万件前提）
- 許容値: Wave 2 の結果を基準として、1.5倍以内を目標
- 計測項目: 実行時間、実行計画（explain plan）

---

## 4. Search/Count の FROM/WHERE 完全一致ルール（P04規律）

**必須ルール**:
- COUNT SQL は FROM/WHERE を search SQL と完全一致させる（P04-2 のルール）
- JOIN の追加は search SQL と count SQL の両方に反映する
- WHERE 句の条件は search SQL と count SQL で完全一致する

**実装方針**:
- QueryBuilder 導入により、FROM/JOIN/WHERE を構造で一致させる（P2-5 のスコープ）
- 共通の「base（FROM/JOIN/WHERE）」を QueryBuilder が生成し、`count` / `select` / `select+offset` はラッパーで差分のみを付与する

**検証方法**:
- 各Wave完了時に、search SQL と count SQL の FROM/JOIN/WHERE を比較
- 同一条件で検索した場合、search の件数と count の件数が一致することを確認

---

## 5. 計測方法（実測SQL、対象条件、許容値）

### 5.1 計測方針

**計測根拠**: 推測禁止。実データ量での計測を必須とする。

**計測タイミング**:
- Wave 1 完了時
- Wave 2 完了時
- Wave 3 完了時
- QueryBuilder 導入完了時

**計測項目**:
- 実行時間（平均、最小、最大）
- 実行計画（explain plan）
- インデックス利用状況

### 5.2 実測SQL

**対象SQL**:
- `group_contract_search.sql`（各Wave完了後のバージョン）
- `group_contract_count.sql`（各Wave完了後のバージョン）

**対象条件**:
- 実データ量での検索（約470万件前提）
- 代表的な検索条件パターン（3〜5パターン）
  - 全件検索（条件なし）
  - 契約受付年月日範囲指定
  - 契約番号前方一致
  - 家族名カナ中間一致
  - 電話番号中間一致

**許容値（暫定）**:
- Wave 1: P1-B2 の結果（平均1.6秒、最長2.6秒）を基準として、2倍以内を目標
- Wave 2: Wave 1 の結果を基準として、1.5倍以内を目標
- Wave 3: Wave 2 の結果を基準として、1.5倍以内を目標
- 最終目標: 平均3秒以内、最長5秒以内（業務合意後に確定）

**注意**: 許容値は業務合意後に確定する。現時点では暫定値として記録する。

### 5.3 計測結果の記録

**記録項目**:
- 計測日時
- 計測対象SQL（Wave番号）
- 計測条件（検索条件パターン）
- 実行時間（平均、最小、最大）
- 実行計画（explain plan）
- インデックス利用状況
- 改善前後の比較（該当する場合）

**記録場所**:
- パフォーマンス測定結果ドキュメント（p2-5-performance-measurement-results.md、新規作成予定）

---

## 6. 実装順序（各Wave共通）

各Waveは以下の順で実装する（設計憲法に従う）:

1. **SQL 修正**（JOIN 追加、CAST(NULL...) を実列に置換）
   - `group_contract_search.sql` の修正
   - `group_contract_count.sql` の修正（FROM/JOIN/WHERE を search SQL と完全一致）
2. **DTO 確認**（変更不要の想定、既に全項目に対応済み）
3. **RowMapper 確認**（変更不要の想定、既に全項目に対応済み）
4. **Controller 確認**（変更不要の想定）
5. **Frontend 表示拡張**（追加項目の表示枠追加、業務判断なし）
6. **API 動作確認**（200/400/403/404 の境界確認）
7. **回帰テスト**（既存機能が壊れていないことを確認）
8. **性能計測**（実測SQL、対象条件、許容値）

---

## 7. Done 条件（チェックリスト形式）

### 7.1 Wave 1

- [ ] Wave 1 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] Search/Count整合が担保されている（FROM/WHERE一致）
- [ ] 性能計測が実施されている（実測SQL、対象条件、許容値）
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 7.2 Wave 2

- [ ] Wave 2 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] Search/Count整合が担保されている（FROM/WHERE一致）
- [ ] 性能計測が実施されている（実測SQL、対象条件、許容値）
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 7.3 Wave 3

- [ ] Wave 3 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] Search/Count整合が担保されている（FROM/WHERE一致）
- [ ] 性能計測が実施されている（実測SQL、対象条件、許容値）
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 7.4 全体

- [ ] 各Waveで性能計測が実施されている（推測禁止）
- [ ] 計測結果が docs に記録されている
- [ ] 性能リスクの高いJOINは段階導入になっている
- [ ] 上記がすべて docs に明文化されている

---

## 8. 参照

- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）
- [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md)（QueryBuilder規律）
- [p1-b2-completion.md](./p1-b2-completion.md)（現状のパフォーマンス測定結果を参照）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

## 9. 注意事項

- P2-4 で成立した P1-B0 最低限を土台とする
- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）
- Search/Count整合を必ず維持する（FROM/WHERE完全一致）
- 性能計測は推測禁止（実測SQL、対象条件、許容値を記録）

---

以上。

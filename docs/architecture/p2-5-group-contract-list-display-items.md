# P2-5: Group 契約一覧 表示項目正本（実務項目の精査・追加）

本ドキュメントは、P2-5 における **Group 契約一覧（法人横断契約一覧）の表示項目** を正本として定義する。

**本書の位置づけ**: P2-5 の一覧表示項目の正本。P2-5 の実装はこのドキュメントに従って進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（P1-B0 最低限の根拠）
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 完了後の土台）

---

## 1. 前提（P2-4 を土台とする）

**P2-4 で成立した機能は成立済み（再実装禁止）**:
- P1-B0 最低限（MUST）の一覧表示項目は P2-4 で実装済み
- 認証/認可/権限制御/表示制御は P2-3 で成立済み
- Frontend の権限制御ロジックは変更しない

**設計憲法の再確認**:
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 暗黙補完（デフォルト値の自動付与等）を禁止
- 失敗・未設定・未対応を隠さない

---

## 2. 目的（What）

P1-B0 最低限を土台として、実務に合わせた一覧表示項目を精査・追加し、業務成立へ寄せる。

**P2-5 の位置づけ**: P2-4 で成立した P1-B0 最低限を土台として、実務に合わせた一覧表示項目の精査・追加をスコープインするフェーズ。

---

## 3. 一覧表示項目の一覧（表形式）

### 3.1 項目定義表

以下の表で、各項目の表示名、データ定義、MUST/SHOULD/FUTURE、データソース、表示形式を定義する。

**凡例**:
- **MUST**: P2-5 終了までに実装必須
- **SHOULD**: 実務上重要だが、P2-5 で実装可能な範囲で対応
- **FUTURE**: P2-5 では実装しない（将来の拡張）

| 表示名 | APIキー | SQL列名 | 型 | null可否 | データ定義（意味/単位/例） | P1-B0分類 | P2-5分類 | データソース | 表示形式 | 備考 |
|--------|---------|---------|-----|----------|---------------------------|-----------|----------|--------------|----------|------|
| 法人コード | companyCd | company_cd | String | 不可 | 法人識別コード（例: "001"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 基本識別情報 |
| 法人名（略称） | companyShortName | company_short_name | String? | 可 | 法人名の略称（例: "ムサシノ"） | MUST | MUST（P2-4完了） | JOIN必要（zgom_cmp） | 文字列そのまま | P1-B1でJOIN復活済み |
| 契約番号 | contractNo | contract_no | String | 不可 | 契約番号（例: "12345678"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 基本識別情報、検索条件 |
| 家族番号 | familyNo | family_no | String | 不可 | 家族番号（例: "12345"） | SHOULD | SHOULD | 現状SQLで取得可 | 文字列そのまま | 基本識別情報 |
| 世帯番号 | houseNo | house_no | String? | 可 | 世帯番号（例: "01"） | SHOULD | SHOULD | 現状SQLで取得可 | 文字列そのまま | 基本識別情報 |
| 姓（外字） | familyNameGaiji | family_name_gaiji | String? | 可 | 姓（外字）（例: "山田"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 氏名情報 |
| 名（外字） | firstNameGaiji | first_name_gaiji | String? | 可 | 名（外字）（例: "太郎"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 氏名情報 |
| 姓（カナ） | familyNameKana | family_name_kana | String? | 可 | 姓（カナ）（例: "ヤマダ"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 氏名情報、検索条件 |
| 名（カナ） | firstNameKana | first_name_kana | String? | 可 | 名（カナ）（例: "タロウ"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 氏名情報 |
| 契約受付年月日 | contractReceiptYmd | contract_receipt_ymd | String? | 可 | 契約受付年月日（YYYYMMDD形式、例: "20240101"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | YYYY/MM/DD形式で表示 | 日付情報、検索条件、ソートキー |
| 生年月日 | birthday | birthday | String? | 可 | 生年月日（YYYYMMDD形式、例: "19800101"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | YYYY/MM/DD形式で表示 | 日付情報 |
| 契約状態 | contractStatus | contract_status | String? | 可 | 契約状態（文字列、例: "有効"） | MUST | MUST | JOIN必要（契約状態テーブル想定） | 文字列そのまま | 契約状態関連、Wave 1候補 |
| タスク名 | taskName | task_name | String? | 可 | タスク名（例: "入会処理"） | SHOULD(仮) | SHOULD | JOIN必要（タスクテーブル想定） | 文字列そのまま | 契約状態関連、Wave 2候補 |
| コースコード | courseCd | course_cd | String? | 可 | コースコード（例: "C001"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | コース情報、検索条件 |
| コース名 | courseName | course_name | String? | 可 | コース名（例: "基本コース"） | MUST | MUST（P2-4完了） | JOIN必要（zgom_course_cd_all） | 文字列そのまま | P1-B1でJOIN復活済み |
| 口数 | shareNum | share_num | Int? | 可 | 口数（例: 10） | MUST | MUST | JOIN必要（コース詳細テーブル想定） | 数値そのまま | コース詳細、Wave 1候補 |
| 月掛金 | monthlyPremium | monthly_premium | Long? | 可 | 月掛金（円、例: 5000） | MUST | MUST（P2-4完了） | JOIN必要（zgom_course_cd_all） | 数値（カンマ区切り、単位: 円） | P1-B1でJOIN復活済み |
| 契約金額 | contractGaku | contract_gaku | Long? | 可 | 契約金額（円、例: 100000） | MUST | MUST | JOIN必要（コース詳細テーブル想定） | 数値（カンマ区切り、単位: 円） | コース詳細、Wave 1候補 |
| 積立回数 | totalSaveNum | total_save_num | Long? | 可 | 積立回数（例: 120） | MUST | MUST | JOIN必要（積立テーブル想定） | 数値そのまま | コース詳細、Wave 2候補 |
| 積立金額 | totalGaku | total_gaku | Long? | 可 | 積立金額（円、例: 600000） | MUST | MUST | JOIN必要（積立テーブル想定） | 数値（カンマ区切り、単位: 円） | コース詳細、Wave 2候補 |
| 郵便番号 | zipCd | zip_cd | String? | 可 | 郵便番号（7桁、例: "1234567"） | SHOULD | SHOULD | JOIN必要（住所マスタ想定） | 文字列（ハイフン区切り、例: "123-4567"） | 住所詳細、Wave 2候補 |
| 都道府県名 | prefName | pref_name | String? | 可 | 都道府県名（例: "東京都"） | MUST | MUST | JOIN必要（住所マスタ想定） | 文字列そのまま | 住所詳細、Wave 1候補 |
| 市区町村名 | cityTownName | city_town_name | String? | 可 | 市区町村名（例: "渋谷区"） | MUST | MUST | JOIN必要（住所マスタ想定） | 文字列そのまま | 住所詳細、Wave 1候補 |
| 大字町名 | oazaTownName | oaza_town_name | String? | 可 | 大字町名（例: "神南"） | MUST | MUST | JOIN必要（住所マスタ想定） | 文字列そのまま | 住所詳細、Wave 2候補 |
| 字丁目名 | azaChomeName | aza_chome_name | String? | 可 | 字丁目名（例: "1丁目"） | MUST | MUST | JOIN必要（住所マスタ想定） | 文字列そのまま | 住所詳細、Wave 2候補 |
| 住所1 | addr1 | addr1 | String? | 可 | 住所1（例: "神南1-2-3"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 住所情報 |
| 住所2 | addr2 | addr2 | String? | 可 | 住所2（例: "マンション101"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 住所情報 |
| 電話番号 | telNo | tel_no | String? | 可 | 電話番号（例: "0312345678"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 連絡先、検索条件 |
| 携帯電話番号 | mobileNo | mobile_no | String? | 可 | 携帯電話番号（例: "09012345678"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 連絡先 |
| 募集担当者コード | recruitRespBosyuCd | recruit_resp_bosyu_cd | String? | 可 | 募集担当者コード（例: "B001"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 募集情報、検索条件 |
| 募集担当者姓（漢字） | bosyuFamilyNameKanji | bosyu_family_name_kanji | String? | 可 | 募集担当者姓（漢字）（例: "田中"） | MUST | MUST | JOIN必要（担当者マスタ想定） | 文字列そのまま | 募集情報、Wave 1候補 |
| 募集担当者名（漢字） | bosyuFirstNameKanji | bosyu_first_name_kanji | String? | 可 | 募集担当者名（漢字）（例: "太郎"） | MUST | MUST | JOIN必要（担当者マスタ想定） | 文字列そのまま | 募集情報、Wave 1候補 |
| 加入担当者コード | entryRespBosyuCd | entry_resp_bosyu_cd | String? | 可 | 加入担当者コード（例: "E001"） | MUST | MUST（P2-4完了） | 現状SQLで取得可 | 文字列そのまま | 募集情報 |
| 加入担当者姓（漢字） | entryFamilyNameKanji | entry_family_name_kanji | String? | 可 | 加入担当者姓（漢字）（例: "佐藤"） | MUST | MUST | JOIN必要（担当者マスタ想定） | 文字列そのまま | 募集情報、Wave 1候補 |
| 加入担当者名（漢字） | entryFirstNameKanji | entry_first_name_kanji | String? | 可 | 加入担当者名（漢字）（例: "花子"） | MUST | MUST | JOIN必要（担当者マスタ想定） | 文字列そのまま | 募集情報、Wave 1候補 |
| 元請支給ランク組織コード | motoSupplyRankOrgCd | moto_supply_rank_org_cd | String? | 可 | 元請支給ランク組織コード（例: "ORG001"） | MUST | MUST | JOIN必要（組織マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 元請支給ランク組織名 | motoSupplyRankOrgName | moto_supply_rank_org_name | String? | 可 | 元請支給ランク組織名（例: "本社"） | MUST | MUST | JOIN必要（組織マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 支給ランク組織コード | supplyRankOrgCd | supply_rank_org_cd | String? | 可 | 支給ランク組織コード（例: "ORG002"） | SHOULD(仮) | SHOULD | JOIN必要（組織マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 支給ランク組織名 | supplyRankOrgName | supply_rank_org_name | String? | 可 | 支給ランク組織名（例: "支社"） | SHOULD(仮) | SHOULD | JOIN必要（組織マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 部署コード | sectCd | sect_cd | String? | 可 | 部署コード（例: "SECT01"） | SHOULD | SHOULD | JOIN必要（部署マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 部署名 | sectName | sect_name | String? | 可 | 部署名（例: "営業部"） | SHOULD | SHOULD | JOIN必要（部署マスタ想定） | 文字列そのまま | 組織情報、Wave 3候補 |
| 最終入金日 | lastReceiptYmd | last_receipt_ymd | String? | 可 | 最終入金日（YYYYMMDD形式、例: "20240101"） | MUST | MUST | JOIN/集計必要（入金テーブル想定） | YYYY/MM/DD形式で表示 | 金銭状況把握用、Wave 3候補 |
| 年齢 | age | age | Int? | 可 | 年齢（満年齢、例: 44） | SHOULD | SHOULD | 生年月日から算出（Frontendで算出可） | 数値そのまま（単位: 歳） | 生年月日から算出、Frontend実装可 |

**注意**: 
- 「P2-4完了」と記載された項目は P2-4 で実装済み
- 「Wave X候補」は [p2-5-group-contract-list-join-plan.md](./p2-5-group-contract-list-join-plan.md) を参照
- データソースが「現状SQLで取得可」の項目は JOIN 不要
- データソースが「JOIN必要」の項目は JOIN 段階的復活が必要

### 3.2 P1-B0 最低限との差分（追加項目）

**P2-5 で追加する項目（P1-B0 最低限を超える項目）**:

**Wave 1 で追加**:
- 契約状態（contractStatus）
- 口数（shareNum）
- 契約金額（contractGaku）
- 都道府県名（prefName）
- 市区町村名（cityTownName）
- 募集担当者姓（漢字）（bosyuFamilyNameKanji）
- 募集担当者名（漢字）（bosyuFirstNameKanji）
- 加入担当者姓（漢字）（entryFamilyNameKanji）
- 加入担当者名（漢字）（entryFirstNameKanji）

**Wave 2 で追加**:
- タスク名（taskName）
- 積立回数（totalSaveNum）
- 積立金額（totalGaku）
- 郵便番号（zipCd）
- 大字町名（oazaTownName）
- 字丁目名（azaChomeName）

**Wave 3 で追加**:
- 元請支給ランク組織コード（motoSupplyRankOrgCd）
- 元請支給ランク組織名（motoSupplyRankOrgName）
- 支給ランク組織コード（supplyRankOrgCd）
- 支給ランク組織名（supplyRankOrgName）
- 部署コード（sectCd）
- 部署名（sectName）
- 最終入金日（lastReceiptYmd）

**P2-5 では実装しない（FUTURE）**:
- 契約状態区分（contractStatusKbn）等の `_kbn` 系項目（マスタJOINが必要、将来の拡張）
- ポイント系項目（saPoint, aaPoint 等）（集計が必要、将来の拡張）
- 入会区分名（entryKbnName）（マスタJOINが必要、将来の拡張）
- カード番号（cardNo）（取得可否・JOIN有無が未確定）

### 3.3 表示形式の規則（業務判断禁止）

**日付項目（YYYYMMDD形式）**:
- 表示形式: `YYYY/MM/DD`（スラッシュ区切り）
- 空値（NULL）: 「-」または「未設定」と表示
- 業務判断: 禁止（表示フォーマット規則のみ）

**数値項目（金額）**:
- 表示形式: カンマ区切り（例: `1,000,000`）、単位: 円
- 空値（NULL）: 「-」または「0円」と表示
- 業務判断: 禁止（表示フォーマット規則のみ）

**数値項目（回数・口数・年齢）**:
- 表示形式: 数値そのまま（例: `10`、`44`）
- 空値（NULL）: 「-」と表示
- 業務判断: 禁止（表示フォーマット規則のみ）

**文字列項目**:
- 表示形式: 文字列そのまま
- 空値（NULL）: 「-」または「未設定」と表示
- 桁あふれ: 省略表示（例: `...`）またはツールチップで全文表示
- 業務判断: 禁止（表示フォーマット規則のみ）

**郵便番号**:
- 表示形式: ハイフン区切り（例: `123-4567`）
- 空値（NULL）: 「-」と表示
- 業務判断: 禁止（表示フォーマット規則のみ）

---

## 4. 非スコープ（今やらないこと）

**必須：明示**:

- **名寄せ/同義語/ゆらぎ吸収などの高度検索**: 将来の拡張として検討
- **UI側での自動補完・推測**: Frontend は業務判断をしない（設計憲法準拠）
- **編集・更新系**: 別フェーズで検討
- **`_kbn` 系項目の名称化（マスタJOIN）**: 将来の拡張として検討
- **ポイント系項目（集計が必要）**: 将来の拡張として検討
- **カード番号（取得可否・JOIN有無が未確定）**: 将来の拡張として検討

---

## 5. Done 条件（チェックリスト形式）

### 5.1 項目表の確定

- [ ] 項目表が確定している（P1-B0 最低限 + P2-5 追加項目）
- [ ] 各項目がどのJOINで取得されるかが説明できる
- [ ] 表示形式の規則が確定している（業務判断禁止）

### 5.2 実装反映

- [ ] Wave 1 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] Wave 2 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] Wave 3 の項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 5.3 Search/Count整合

- [ ] Search/Count整合が壊れない計画になっている
- [ ] COUNT SQL は FROM/WHERE を search SQL と完全一致させている（P04-2 のルール）

### 5.4 性能リスク管理

- [ ] 性能リスクの高いJOINは段階導入になっている
- [ ] 各Waveで性能計測が実施されている（推測禁止）

### 5.5 設計憲法準拠

- [ ] 暗黙補完をしていない（設計憲法準拠）
- [ ] エラーを握りつぶしていない（設計憲法準拠）
- [ ] Frontend は業務判断をしていない（設計憲法準拠）

### 5.6 ドキュメント化

- [ ] 上記がすべて docs に明文化されている

---

## 6. 参照

- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（P1-B0 最低限の根拠）
- [p2-5-group-contract-list-join-plan.md](./p2-5-group-contract-list-join-plan.md)（JOIN 段階的復活計画）
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 完了後の土台）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

## 7. 注意事項

- P2-4 で成立した P1-B0 最低限を土台とする
- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）
- 表示形式は「表示フォーマット規則のみ」で、業務判断は禁止

---

以上。

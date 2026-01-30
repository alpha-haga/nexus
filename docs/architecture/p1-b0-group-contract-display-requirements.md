# P1-B0: Group 契約一覧 表示要件確定

本ドキュメントは、P1-B0 における **Group 契約一覧（法人横断契約一覧）の表示要件確定** を目的とする。

**本書の位置づけ**: P1-B0 の成果物。P1-B1 以降の実装は、本書を正として進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)

---

## 1. 目的とスコープ

### 1.1 P1-B0 の目的

- Group 契約一覧の表示項目を確定する（MUST/SHOULD/WON'T）
- 検索条件を確定する（query param、バリデーション、400条件）
- ソート要件を確定する（許可キー、デフォルト、方向、不正時の扱い）
- ページング要件を確定する（page/size のデフォルトと上限、上限超過時の扱い）
- エラーの意味を確定する（200/400/403/404）
- JOIN 段階的復活の順序を確定する（P1-B1 でやる順番を箇条書きで固定）

### 1.2 スコープ（含む）

- Group 契約一覧 API（`/api/v1/group/contracts/search`）の表示要件
- 検索条件、ソート、ページングの仕様
- エラーレスポンスの意味

### 1.3 スコープ外（含まない）

- Frontend 実装
- 認証/認可方式の変更（P04-5 / P1-A1 で確定済み）
- Keycloak 設定変更
- Context 仕様変更
- JOIN の具体実装（P1-B1 で実施）

---

## 2. 表示項目定義（MUST/SHOULD/WON'T）

### 2.1 表示項目一覧

以下の表で、各項目の表示名、APIキー、SQL列名、型、null可否、MUST/SHOULD/WON'T、備考を定義する。

**注意**: MUST/SHOULD/WON'T は業務合意済みの内容を反映している。

| 表示名 | APIキー | SQL列名 | 型 | null可否 | 現状 | MUST/SHOULD/WON'T | 備考 |
|--------|---------|---------|-----|----------|------|-------------------|------|
| 法人コード | companyCd | company_cd | String | 不可 | 取得可能 | MUST | 基本識別情報 |
| 法人名（略称） | companyShortName | company_short_name | String? | 可 | CAST(NULL) | MUST | JOIN復活候補（Step1） |
| 契約番号 | contractNo | contract_no | String | 不可 | 取得可能 | MUST | 基本識別情報、検索条件 |
| 家族番号 | familyNo | family_no | String | 不可 | 取得可能 | SHOULD | 基本識別情報 |
| 世帯番号 | houseNo | house_no | String? | 可 | 取得可能 | SHOULD | 基本識別情報 |
| 姓（外字） | familyNameGaiji | family_name_gaiji | String? | 可 | 取得可能 | MUST | 氏名情報 |
| 名（外字） | firstNameGaiji | first_name_gaiji | String? | 可 | 取得可能 | MUST | 氏名情報 |
| 姓（カナ） | familyNameKana | family_name_kana | String? | 可 | 取得可能 | MUST | 氏名情報、検索条件 |
| 名（カナ） | firstNameKana | first_name_kana | String? | 可 | 取得可能 | MUST | 氏名情報 |
| 契約受付年月日 | contractReceiptYmd | contract_receipt_ymd | String? | 可 | 取得可能 | MUST | 日付情報（YYYYMMDD）、検索条件、ソートキー |
| 生年月日 | birthday | birthday | String? | 可 | 取得可能 | MUST | 日付情報（YYYYMMDD） |
| 契約状態区分 | contractStatusKbn | contract_status_kbn | String? | 可 | CAST(NULL) | WON'T(仮) | JOIN復活候補（Step1）、検索条件（現状無効） |
| 請求停止理由区分 | dmdStopRasonKbn | dmd_stop_rason_kbn | String? | 可 | CAST(NULL) | WON'T(仮) | 契約状態関連 |
| 解約理由区分 | cancelReasonKbn | cancel_reason_kbn | String? | 可 | CAST(NULL) | WON'T(仮) | 契約状態関連 |
| 解約状態区分 | cancelStatusKbn | cancel_status_kbn | String? | 可 | CAST(NULL) | WON'T(仮) | 契約状態関連 |
| 雑収理由区分 | zashuReasonKbn | zashu_reason_kbn | String? | 可 | CAST(NULL) | WON'T(仮) | 契約状態関連 |
| 契約状態 | contractStatus | contract_status | String? | 可 | CAST(NULL) | MUST | 契約状態関連 |
| タスク名 | taskName | task_name | String? | 可 | CAST(NULL) | SHOULD(仮) | 契約状態関連 |
| 状態更新年月日 | statusUpdateYmd | status_update_ymd | String? | 可 | CAST(NULL) | WON'T| 契約状態関連（YYYYMMDD） |
| コースコード | courseCd | course_cd | String? | 可 | 取得可能 | MUST | コース情報、検索条件 |
| コース名 | courseName | course_name | String? | 可 | CAST(NULL) | MUST | JOIN復活候補（Step1） |
| 口数 | shareNum | share_num | Int? | 可 | CAST(NULL) | MUST | コース詳細 |
| 月掛金 | monthlyPremium | monthly_premium | Long? | 可 | CAST(NULL) | MUST | コース詳細 |
| 契約金額 | contractGaku | contract_gaku | Long? | 可 | CAST(NULL) | MUST | コース詳細 |
| 積立回数 | totalSaveNum | total_save_num | Long? | 可 | CAST(NULL) | MUST | コース詳細 |
| 積立金額 | totalGaku | total_gaku | Long? | 可 | CAST(NULL) | MUST | コース詳細 |
| 郵便番号 | zipCd | zip_cd | String? | 可 | CAST(NULL) | SHOULD | 住所詳細 |
| 都道府県名 | prefName | pref_name | String? | 可 | CAST(NULL) | MUST | 住所詳細 |
| 市区町村名 | cityTownName | city_town_name | String? | 可 | CAST(NULL) | MUST | 住所詳細 |
| 大字町名 | oazaTownName | oaza_town_name | String? | 可 | CAST(NULL) | MUST | 住所詳細 |
| 字丁目名 | azaChomeName | aza_chome_name | String? | 可 | CAST(NULL) | MUST | 住所詳細 |
| 住所1 | addr1 | addr1 | String? | 可 | 取得可能 | MUST | 住所情報 |
| 住所2 | addr2 | addr2 | String? | 可 | 取得可能 | MUST | 住所情報 |
| 電話番号 | telNo | tel_no | String? | 可 | 取得可能 | MUST | 連絡先、検索条件 |
| 携帯電話番号 | mobileNo | mobile_no | String? | 可 | 取得可能 | MUST | 連絡先 |
| SAポイント | saPoint | sa_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| AAポイント | aaPoint | aa_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| Aポイント | aPoint | a_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| 新規ポイント | newPoint | new_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| 増口ポイント | addPoint | add_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| 手当なしポイント | noallwPoint | noallw_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| S新規ポイント | ssPoint | ss_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| UPポイント | upPoint | up_point | Int? | 可 | CAST(NULL) | SHOULD(仮) | ポイント情報 |
| 入会区分名 | entryKbnName | entry_kbn_name | String? | 可 | CAST(NULL) | SHOULD(仮) | 募集情報 |
| 募集担当者コード | recruitRespBosyuCd | recruit_resp_bosyu_cd | String? | 可 | 取得可能 | MUST | 募集情報、検索条件（bosyuCd） |
| 募集担当者姓（漢字） | bosyuFamilyNameKanji | bosyu_family_name_kanji | String? | 可 | CAST(NULL) | MUST | 募集情報 |
| 募集担当者名（漢字） | bosyuFirstNameKanji | bosyu_first_name_kanji | String? | 可 | CAST(NULL) | MUST | 募集情報 |
| 加入担当者コード | entryRespBosyuCd | entry_resp_bosyu_cd | String? | 可 | 取得可能 | MUST | 募集情報 |
| 加入担当者姓（漢字） | entryFamilyNameKanji | entry_family_name_kanji | String? | 可 | CAST(NULL) | MUST | 募集情報 |
| 加入担当者名（漢字） | entryFirstNameKanji | entry_first_name_kanji | String? | 可 | CAST(NULL) | MUST | 募集情報 |
| 元請支給ランク組織コード | motoSupplyRankOrgCd | moto_supply_rank_org_cd | String? | 可 | CAST(NULL) | MUST | 組織情報 |
| 元請支給ランク組織名 | motoSupplyRankOrgName | moto_supply_rank_org_name | String? | 可 | CAST(NULL) | MUST | 組織情報 |
| 支給ランク組織コード | supplyRankOrgCd | supply_rank_org_cd | String? | 可 | CAST(NULL) | SHOULD(仮) | 組織情報 |
| 支給ランク組織名 | supplyRankOrgName | supply_rank_org_name | String? | 可 | CAST(NULL) | SHOULD(仮) | 組織情報 |
| 部署コード | sectCd | sect_cd | String? | 可 | CAST(NULL) | SHOULD | 組織情報 |
| 部署名 | sectName | sect_name | String? | 可 | CAST(NULL) | SHOULD | 組織情報 |
| あんサポフラグ | anspFlg | ansp_flg | String? | 可 | CAST(NULL) | WON'T | その他 |
| 約款種別区分 | agreementKbn | agreement_kbn | String? | 可 | CAST(NULL) | WON'T | その他 |
| 集金・営業所コード | collectOfficeCd | collect_office_cd | String? | 可 | CAST(NULL) | WON'T | その他 |
| 差押フラグ | foreclosureFlg | foreclosure_flg | String? | 可 | CAST(NULL) | WON'T(仮) | その他 |
| 登録年月日 | registYmd | regist_ymd | String? | 可 | CAST(NULL) | WON'T | その他（YYYYMMDD） |
| EC申込受付番号 | receptionNo | reception_no | String? | 可 | CAST(NULL) | SHOULD(仮) | その他 |
| カード番号 | cardNo | card_no(仮) | String? | 可 | 不明 | MUST | 会員識別用。取得可否・JOIN有無は P1-B1 で確定 |
| 年齢 | age | age | Int? | 可 | CAST(NULL) | SHOULD | 生年月日から算出。原則：検索実行日時点の満年齢（例外がある場合は P1-B1 開始前に再合意する） |
| 最終入金日 | lastReceiptYmd | last_receipt_ymd | String? | 可 | CAST(NULL) | MUST | 金銭状況把握用。JOIN/集計が必要なため Step2以降候補 |

### 2.2 既存SQLの CAST(NULL...) 項目（JOIN復活候補）

既存SQL（`group_contract_search.sql`）に `CAST(NULL AS ...)` として定義されている項目は、JOIN 復活の候補として以下に列挙する。

**候補項目（概念レベル）**:
- `company_short_name` (VARCHAR2(20)) - 法人名表示
- `contract_status_kbn` (CHAR(1)) - 契約状態区分（既存検索条件で使用、現状無効）
- `dmd_stop_rason_kbn` (CHAR(1)) - 請求停止理由区分
- `cancel_reason_kbn` (CHAR(1)) - 解約理由区分
- `cancel_status_kbn` (CHAR(1)) - 解約状態区分
- `zashu_reason_kbn` (CHAR(1)) - 雑収理由区分
- `contract_status` (VARCHAR2(1)) - 契約状態
- `task_name` (VARCHAR2(30)) - タスク名
- `status_update_ymd` (CHAR(8)) - 状態更新年月日
- `course_name` (VARCHAR2(15)) - コース名
- `share_num` (NUMBER(2)) - 口数
- `monthly_premium` (NUMBER(7)) - 月掛金
- `contract_gaku` (NUMBER(7)) - 契約金額
- `total_save_num` (NUMBER(7)) - 積立回数
- `total_gaku` (NUMBER(7)) - 積立金額
- `zip_cd` (CHAR(7)) - 郵便番号
- `pref_name` (VARCHAR2(4)) - 都道府県名
- `city_town_name` (VARCHAR2(12)) - 市区町村名
- `oaza_town_name` (VARCHAR2(18)) - 大字町名
- `aza_chome_name` (VARCHAR2(18)) - 字丁目名
- `sa_point` (NUMBER(5)) - SAポイント
- `aa_point` (NUMBER(5)) - AAポイント
- `a_point` (NUMBER(5)) - Aポイント
- `new_point` (NUMBER(5)) - 新規ポイント
- `add_point` (NUMBER(5)) - 増口ポイント
- `noallw_point` (NUMBER(5)) - 手当なしポイント
- `ss_point` (NUMBER(5)) - S新規ポイント
- `up_point` (NUMBER(5)) - UPポイント
- `entry_kbn_name` (CHAR(1)) - 入会区分名
- `bosyu_family_name_kanji` (VARCHAR2(30)) - 募集担当者姓（漢字）
- `bosyu_first_name_kanji` (VARCHAR2(30)) - 募集担当者名（漢字）
- `entry_family_name_kanji` (VARCHAR2(15)) - 加入担当者姓（漢字）
- `entry_first_name_kanji` (VARCHAR2(15)) - 加入担当者名（漢字）
- `moto_supply_rank_org_cd` (CHAR(6)) - 元請支給ランク組織コード
- `moto_supply_rank_org_name` (VARCHAR2(15)) - 元請支給ランク組織名
- `supply_rank_org_cd` (CHAR(6)) - 支給ランク組織コード
- `supply_rank_org_name` (VARCHAR2(15)) - 支給ランク組織名
- `sect_cd` (CHAR(6)) - 部署コード
- `sect_name` (VARCHAR2(25)) - 部署名
- `ansp_flg` (CHAR(1)) - あんサポフラグ
- `agreement_kbn` (VARCHAR2(10)) - 約款種別区分
- `collect_office_cd` (CHAR(6)) - 集金・営業所コード
- `foreclosure_flg` (CHAR(1)) - 差押フラグ
- `regist_ymd` (CHAR(8)) - 登録年月日
- `reception_no` (CHAR(16)) - EC申込受付番号

**注意**: 上記は候補枠であり、P1-B0 で業務と合意してから MUST/SHOULD/WON'T を確定する。

**補足（区分値 `_kbn` の扱い）**:
- `_kbn` 系項目（例: contractStatusKbn など）は、将来マスタ JOIN により名称取得する前提とする
- P1-B0 / P1-B1 では名称 JOIN を行わないため、表示項目としては WON'T(仮) とする
- ただし、業務上「状態表示」が必要な場合は、別の表現（例: contractStatus）で満たす
- 状態表示用の文字列。区分値（_kbn）とは別物として扱う

---

## 3. 検索条件（query param）

### 3.1 検索条件一覧

以下の表で、各検索条件の param、型、必須、バリデーション、例、400条件を定義する。

**注意**: バリデーション詳細は業務合意後に確定する。現時点では既存実装を参考として記載。

| param | 型 | 必須 | バリデーション | 例 | 400条件 | 備考 |
|-------|-----|------|----------------|-----|---------|------|
| contractReceiptYmdFrom | String? | 任意 | YYYYMMDD形式（8桁） | "20240101" | 形式不正時 | 契約受付年月日範囲（開始） |
| contractReceiptYmdTo | String? | 任意 | YYYYMMDD形式（8桁） | "20241231" | 形式不正時 | 契約受付年月日範囲（終了） |
| contractNo | String? | 任意 | 文字列（前方一致） | "12345" | なし | 契約番号（前方一致） |
| familyNmKana | String? | 任意 | 文字列（中間一致） | "ヤマダ" | なし | 家族名カナ（中間一致） |
| telNo | String? | 任意 | 文字列（中間一致） | "03" | なし | 電話番号検索。tel_no / mobile_no を横断して中間一致検索 |
| bosyuCd | String? | 任意 | 文字列（完全一致） | "B001" | なし | 募集担当者コード（完全一致） |
| courseCd | String? | 任意 | 文字列（完全一致） | "C001" | なし | コースコード（完全一致） |
| contractStatusKbn | String? | 任意 | 文字列（完全一致） | "1" | なし | 契約状態区分（完全一致、現状CAST(NULL)のため無効） |

**注意**: 上記は既存実装の参考情報であり、P1-B0 で業務と合意してから確定する。

**補足（氏名カナ検索の扱い）**:
- 将来、氏名（カナ）検索を統合的に追加する場合は `familyNmKana` 単独指定は WON'T とする
- P1 では互換性維持のため既存 `familyNmKana` を維持（廃止は別フェーズで判断）

### 3.2 追加検索条件の候補（業務合意が必要）

以下の検索条件は既存実装にはないが、業務要件として追加の可能性がある。

**追加候補（概念レベル）**:
- 法人コード検索（`companyCd`）
- 氏名（漢字）検索（`familyNameGaiji`, `firstNameGaiji`）
- 氏名（カナ）検索（`familyNameKana`, `firstNameKana`）
- 住所検索（`addr1`, `addr2`, `zipCd` 等）
- 契約状態での絞り込み（`contractStatusKbn` が有効化された場合の拡張）

**注意**: 上記は候補であり、P1-B0 で業務と合意してから確定する。

---

## 4. ソート

### 4.1 ソート仕様

| 項目 | 内容 |
|------|------|
| 許可キー | 業務合意待ち（既存SQL: `contract_receipt_ymd`, `contract_no`） |
| デフォルト | 業務合意待ち（既存SQL: `contract_receipt_ymd DESC, contract_no`） |
| 方向 | 業務合意待ち（既存SQL: `DESC` / `ASC`） |
| 不正時の扱い | 400 Bad Request |

**注意**: 本段階では既存実装を参考として記載。具体の項目値は業務合意後に確定する。

### 4.2 既存ソート（参考）

既存SQL（`group_contract_search.sql`）では以下のソートが定義されている（参考情報）:
- `contract_receipt_ymd DESC`
- `contract_no`

**注意**: 上記は既存実装の参考情報であり、P1-B0 で業務と合意してから確定する。

**補足（画面操作との関係）**:
- 画面での列クリック等の操作は frontend 実装スコープとする
- API 側では、許可されたソートキーと複数指定可否のみを仕様として固定する

---

## 5. ページング

### 5.1 ページング仕様

| 項目 | 内容 |
|------|------|
| page のデフォルト | 0（既存実装） |
| size のデフォルト | 20（既存実装） |
| size の上限 | 100（P1-B2 時点での確定値） |
| 上限超過時の扱い | 400 Bad Request |
| page のバリデーション | `>= 0`（既存実装） |
| size のバリデーション | `> 0` かつ `<= 100`（P1-B2 で追加） |

**注意**: 
- P1-B2 で `size` の上限を 100 として確定した（実装済み）。
- 現時点では上限 100 を正とするが、今後のパフォーマンス測定や業務要件により変更の可能性がある。
- 上限値の変更が必要な場合は、P1-B2 以降のフェーズで再検討する。

### 5.2 既存ページング（参考）

既存実装（`GroupContractSearchController`）では以下のページングが定義されている（参考情報）:
- `page`: デフォルト 0, バリデーション: `>= 0`
- `size`: デフォルト 20, バリデーション: `> 0`（P1-B2 以前は上限なし、P1-B2 で上限 100 を追加）

**注意**: 上記は既存実装の参考情報であり、P1-B0 で業務と合意してから確定する。

---

## 6. エラーの意味（200/400/403/404）

### 6.1 エラーレスポンス定義

| HTTPステータス | 意味 | 発生条件 |
|----------------|------|----------|
| 200 OK | 正常終了 | 検索条件が正しく、結果が返る |
| 400 Bad Request | 不正なリクエスト | バリデーションエラー、不正なパラメータ、上限超過等 |
| 403 Forbidden | 未許可 | 認可エラー（P1-A1 で実装済み） |
| 404 Not Found | 存在しないAPI | 存在しないAPIパス（P1-A1 で実装済み） |

### 6.2 400 Bad Request の具体条件（業務合意後に詳細化）

以下の条件で 400 Bad Request を返す（業務合意後に確定）:
- `page < 0`（既存実装）
- `size <= 0`（既存実装）
- `size > 上限値`（業務合意後に上限値を確定）
- `contractReceiptYmdFrom` / `contractReceiptYmdTo` の形式不正（YYYYMMDD形式でない）
- ソートキーが不正（許可キー以外を指定）
- その他バリデーションエラー

**注意**: 400 の具体条件は、検索条件・ソート・ページングの確定後に詳細化する。

---

## 7. JOIN 段階的復活の順序（B1でやる順番を箇条書きで固定）

### 7.1 JOIN 復活順序

以下の順序で JOIN を段階的に復活させる。各 Step の具体内容は P1-B0 で業務と合意してから確定する。

**Step 1（最小・最軽量）**
- JOIN先: 業務合意待ち（概念レベル: 法人マスタ想定）
- 復活する項目: `company_short_name`（法人名表示）
- 理由: 基本識別情報として必須度が高い、単一テーブルJOIN想定で複雑度が低い

**Step 2**
- JOIN先: 業務合意待ち（概念レベル: コースマスタ想定）
- 復活する項目: `course_name`（コース名表示）
- 理由: コースコードのみでは識別しにくい、表示上重要

**Step 3以降**: 業務合意待ち
- 業務合意後に順序を確定

### 7.1.2 WON'T（P1では実施しない）/ Future JOIN 候補

以下は将来マスタ JOIN により名称取得する前提だが、P1 では実施しない（WON'T）。
- 対象: `_kbn` 系（例: contractStatusKbn, dmdStopRasonKbn, cancelReasonKbn, cancelStatusKbn, zashuReasonKbn など）
- 理由: P1 では一覧業務成立に必要な最小項目に絞る。名称化は別フェーズで合意して実施する。
- 

**注意**: 上記は候補であり、P1-B0 で業務と合意してから確定する。JOIN先やテーブル構造は前提にしない（概念レベルで止める）。

### 7.2 実装順序（P1-B1 で実施）

各 Step は以下の順で実装する:
1. SQL 修正（JOIN 追加、CAST(NULL...) を実列に置換）
2. DTO 確認（変更不要の想定）
3. RowMapper 確認（変更不要の想定）
4. API 動作確認（200/400/403/404 の境界確認）
5. 回帰テスト（既存機能が壊れていないことを確認）

---

## 8. 補足

- 本ドキュメントは P1-B0 の成果物であり、業務と合意してから確定する
- 確定後は P1-B1 以降の実装の正として扱う
- 設計の再定義・再解釈は禁止（既存の設計の正に従う）
- 推測で項目を埋めない（業務合意が必要な項目は「業務合意待ち」として明記）
- JOIN先やテーブル構造は前提にしない（概念レベルで止める）

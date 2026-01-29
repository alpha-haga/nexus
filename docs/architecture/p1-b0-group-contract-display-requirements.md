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

以下の表で、各項目の表示名、APIキー、SQL列名、型、null可否、備考を定義する。

| 表示名 | APIキー | SQL列名 | 型 | null可否 | MUST/SHOULD/WON'T | 備考 |
|--------|---------|---------|-----|----------|-------------------|------|
| （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） |

**注意**: 本段階ではテンプレートのみ。具体の項目値は推測で埋めない。

### 2.2 既存SQLの CAST(NULL...) 項目（候補枠）

既存SQL（`group_contract_search.sql`）に `CAST(NULL AS ...)` として定義されている項目は、JOIN 復活の候補として以下に列挙する。ただし、値やJOIN先は推測で確定しない。

**候補項目（例）**:
- `company_short_name` (VARCHAR2(20))
- `contract_status_kbn` (CHAR(1))
- `dmd_stop_rason_kbn` (CHAR(1))
- `cancel_reason_kbn` (CHAR(1))
- `cancel_status_kbn` (CHAR(1))
- `zashu_reason_kbn` (CHAR(1))
- `contract_status` (VARCHAR2(1))
- `task_name` (VARCHAR2(30))
- `status_update_ymd` (CHAR(8))
- `course_name` (VARCHAR2(15))
- `share_num` (NUMBER(2))
- `monthly_premium` (NUMBER(7))
- `contract_gaku` (NUMBER(7))
- `total_save_num` (NUMBER(7))
- `total_gaku` (NUMBER(7))
- `zip_cd` (CHAR(7))
- `pref_name` (VARCHAR2(4))
- `city_town_name` (VARCHAR2(12))
- `oaza_town_name` (VARCHAR2(18))
- `aza_chome_name` (VARCHAR2(18))
- `sa_point` (NUMBER(5))
- `aa_point` (NUMBER(5))
- `a_point` (NUMBER(5))
- `new_point` (NUMBER(5))
- `add_point` (NUMBER(5))
- `noallw_point` (NUMBER(5))
- `ss_point` (NUMBER(5))
- `up_point` (NUMBER(5))
- `entry_kbn_name` (CHAR(1))
- `bosyu_family_name_kanji` (VARCHAR2(30))
- `bosyu_first_name_kanji` (VARCHAR2(30))
- `entry_family_name_kanji` (VARCHAR2(15))
- `entry_first_name_kanji` (VARCHAR2(15))
- `moto_supply_rank_org_cd` (CHAR(6))
- `moto_supply_rank_org_name` (VARCHAR2(15))
- `supply_rank_org_cd` (CHAR(6))
- `supply_rank_org_name` (VARCHAR2(15))
- `sect_cd` (CHAR(6))
- `sect_name` (VARCHAR2(25))
- `ansp_flg` (CHAR(1))
- `agreement_kbn` (VARCHAR2(10))
- `collect_office_cd` (CHAR(6))
- `foreclosure_flg` (CHAR(1))
- `regist_ymd` (CHAR(8))
- `reception_no` (CHAR(16))

**注意**: 上記は候補枠であり、P1-B0 で業務と合意してから MUST/SHOULD/WON'T を確定する。

---

## 3. 検索条件（query param）

### 3.1 検索条件一覧

以下の表で、各検索条件の param、型、必須、バリデーション、例、400条件を定義する。

| param | 型 | 必須 | バリデーション | 例 | 400条件 |
|-------|-----|------|----------------|-----|---------|
| （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） | （今後B0で確定） |

**注意**: 本段階ではテンプレートのみ。具体の項目値は推測で埋めない。

### 3.2 既存検索条件（参考）

既存の `GroupContractSearchCondition` には以下の条件が定義されている（参考情報）:
- `contractReceiptYmdFrom` (String?, YYYYMMDD形式)
- `contractReceiptYmdTo` (String?, YYYYMMDD形式)
- `contractNo` (String?, 前方一致)
- `familyNmKana` (String?, 中間一致)
- `telNo` (String?, 中間一致)
- `bosyuCd` (String?, 完全一致)
- `courseCd` (String?, 完全一致)
- `contractStatusKbn` (String?, 完全一致)

**注意**: 上記は既存実装の参考情報であり、P1-B0 で業務と合意してから確定する。

---

## 4. ソート

### 4.1 ソート仕様

| 項目 | 内容 |
|------|------|
| 許可キー | （今後B0で確定） |
| デフォルト | （今後B0で確定） |
| 方向 | （今後B0で確定） |
| 不正時の扱い | 400 Bad Request |

**注意**: 本段階ではテンプレートのみ。具体の項目値は推測で埋めない。

### 4.2 既存ソート（参考）

既存SQL（`group_contract_search.sql`）では以下のソートが定義されている（参考情報）:
- `contract_receipt_ymd DESC`
- `contract_no`

**注意**: 上記は既存実装の参考情報であり、P1-B0 で業務と合意してから確定する。

---

## 5. ページング

### 5.1 ページング仕様

| 項目 | 内容 |
|------|------|
| page のデフォルト | （今後B0で確定） |
| size のデフォルト | （今後B0で確定） |
| size の上限 | （今後B0で確定） |
| 上限超過時の扱い | 400 Bad Request |

**注意**: 本段階ではテンプレートのみ。具体の項目値は推測で埋めない。

---

## 6. エラーの意味（200/400/403/404）

### 6.1 エラーレスポンス定義

| HTTPステータス | 意味 | 発生条件 |
|----------------|------|----------|
| 200 OK | 正常終了 | 検索条件が正しく、結果が返る |
| 400 Bad Request | 不正なリクエスト | バリデーションエラー、不正なパラメータ、上限超過等 |
| 403 Forbidden | 未許可 | 認可エラー（P1-A1 で実装済み） |
| 404 Not Found | 存在しないAPI | 存在しないAPIパス（P1-A1 で実装済み） |

**注意**: 400 の具体条件は、検索条件・ソート・ページングの確定後に詳細化する。

---

## 7. JOIN 段階的復活の順序（B1でやる順番を箇条書きで固定）

### 7.1 JOIN 復活順序

以下の順序で JOIN を段階的に復活させる。各 Step の具体内容は P1-B0 で業務と合意してから確定する。

**Step 1**: （今後B0で確定）
- JOIN先: （今後B0で確定）
- 復活する項目: （今後B0で確定）

**Step 2**: （今後B0で確定）
- JOIN先: （今後B0で確定）
- 復活する項目: （今後B0で確定）

**Step 3**: （今後B0で確定）
- JOIN先: （今後B0で確定）
- 復活する項目: （今後B0で確定）

**注意**: 本段階では枠のみ。具体の JOIN 先や項目は推測で確定しない。

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

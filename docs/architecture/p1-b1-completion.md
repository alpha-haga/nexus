# P1-B1 完了宣言（Step 1 & Step 2）

## 1. フェーズ前提（確定事項・再議論禁止）

- フェーズ：P1-B1（JOIN 段階的復活）
- P1-B0（表示要件確定）：完了済み
- P1-A2（E2E 検証）：完了済み
- 本フェーズでは P1-B0 で固定した JOIN 復活順序に従い、Step 1 & Step 2 を実施
- 設計の正は以下ドキュメントである：
  - docs/architecture/p1-b0-group-contract-display-requirements.md
  - docs/architecture/nexus-design-constitution.md
  - docs/architecture/nexus-project-roadmap.md

---

## 2. P1-B1 の目的（再掲）

P1-B1 は以下を目的として実施した：

- P1-B0 で固定した JOIN 復活順序に従い、段階的に JOIN を復活させる
- SQL → DTO → Mapper → API の順で実装
- 各段階で回帰テストを実施

---

## 3. 実施内容（事実のみ）

### 3.1 Step 1: 法人名（cmp_short_name）JOIN復活

- JOIN先: `zgom_cmp` テーブル
- JOIN条件: `cmp.cmp_cd = contract_search.cmp_cd AND cmp.delete_flg = '0'`
- 復活項目: `cmp_short_name`（`cmp.cmp_short_nm` から取得）
- SQL修正: `CAST(NULL AS VARCHAR2(20)) AS cmp_short_name` → `cmp.cmp_short_nm AS cmp_short_name`
- DTO確認: 変更不要（既存の `cmpShortName: String?` で対応可能）
- RowMapper確認: 変更不要（既存の `rs.getString("cmp_short_name")` で対応可能）

### 3.2 Step 2: コース名・月額保険料（course_name, monthly_premium）JOIN復活

- JOIN先: `zgom_course_cd_all` テーブル
- JOIN条件:
  - `contract_search.cmp_cd = course.cmp_cd`
  - `contract_search.course_cd = course.course_cd`
  - 期間条件（`tekiyo_start_ymd`, `tekiyo_end_ymd`）
  - `course.delete_flg = '0'`
- 復活項目:
  - `course_name`（`course.course_nm` から取得）
  - `monthly_premium`（`NVL(course.monthly_premium, 0)` から取得）
- SQL修正:
  - `CAST(NULL AS VARCHAR2(15)) AS course_name` → `course.course_nm AS course_name`
  - `CAST(NULL AS NUMBER(7)) AS monthly_premium` → `NVL(course.monthly_premium, 0) AS monthly_premium`
- DTO確認: 変更不要（既存の `courseName: String?`, `monthlyPremium: Long?` で対応可能）
- RowMapper確認: 変更不要（既存のマッピングで対応可能）

### 3.3 GlobalExceptionHandler 追加

- `nexus-bff` モジュールに `GlobalExceptionHandler` を追加
- `ValidationException` を 400 Bad Request として適切にハンドリング
- Spring のバリデーション例外（`MethodArgumentNotValidException`, `ConstraintViolationException`, `BindException`）も 400 として処理
- `nexus-api` の `ErrorResponse` 形式と統一

---

## 4. 検証結果

### 4.1 JOIN復活項目の取得確認

- `cmpShortName`: 正常に取得できていることを確認
- `courseName`: 正常に取得できていることを確認
- `monthlyPremium`: 正常に取得できていることを確認（0または正の値）

### 4.2 エラーハンドリング確認

- `page < 0`: 400 Bad Request を返却（`ValidationException` が適切にハンドリング）
- `size <= 0`: 400 Bad Request を返却（`ValidationException` が適切にハンドリング）
- 無効なトークン: 401 Unauthorized を返却（正常動作）

### 4.3 回帰テスト結果

すべての既存機能が正常に動作することを確認：

- **検索条件**:
  - `contractNo`（前方一致）: 正常動作
  - `familyNmKana`（中間一致）: 正常動作
  - `telNo`（中間一致）: 正常動作
  - `bosyuCd`（完全一致）: 正常動作
  - `courseCd`（完全一致）: 正常動作
  - `contractReceiptYmdFrom/To`（日付範囲）: 正常動作
- **ページネーション**: 正常動作（page=0, page=1 ともに確認）
- **ソート**: 正常動作（`contract_receipt_ymd DESC, contract_no`）
- **複数条件の組み合わせ**: 正常動作

### 4.4 ステータスコードの確認

- 200 OK: 正常終了（JOIN復活項目も含めて正常に取得）
- 400 Bad Request: バリデーションエラー（`page < 0`, `size <= 0` 等）
- 403 Forbidden: 認可エラー（P1-A1 で実装済み）
- 404 Not Found: 存在しないAPI（P1-A1 で実装済み）

---

## 5. スコープ外事項の明確化

以下は P1-B1（Step 1 & Step 2）のスコープ外である：

- Step 3以降のJOIN復活（業務合意後に実施）
- `share_num`, `contract_gaku` 等の他のコース詳細項目の復活（Step 2では実施しない）
- `_kbn` 系の名称化（P1では実施しない、WON'T）
- パフォーマンスチューニング（P1-B2以降の責務）

---

## 6. Done 判定

以下の条件を満たすため、P1-B1（Step 1 & Step 2）は完了と判断できる：

- ✅ P1-B0 の MUST が 200 で返る（`cmpShortName`, `courseName`, `monthlyPremium` が取得できている）
- ✅ 400/403/404 の境界が明確（`GlobalExceptionHandler` により適切にハンドリング）
- ✅ 回帰が取れている（既存機能が壊れていないことを確認）
- ✅ SQL → DTO → Mapper → API の順で実装が完了している

---

## 7. 次フェーズへの引き継ぎ

### 7.1 次フェーズ

- 次フェーズは P1-B2（Count/Search 最適化）または Step 3以降のJOIN復活
- Step 3以降のJOIN復活は業務合意後に実施

### 7.2 引き継ぎ事項

- Step 1 & Step 2 のJOIN復活が完了している
- `GlobalExceptionHandler` により適切なエラーハンドリングが実装されている
- 既存機能の回帰テストが完了している
- SQL修正内容:
  - `group_contract_search.sql`: `zgom_cmp` と `zgom_course_cd_all` への LEFT JOIN を追加
  - `cmp_short_name`, `course_name`, `monthly_premium` が実列から取得可能

---

## 8. 成果物

- SQL修正: `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql`
- 例外ハンドラー追加: `backend/nexus-bff/src/main/kotlin/nexus/bff/config/GlobalExceptionHandler.kt`
- 回帰テスト結果: 本ドキュメントの「4.3 回帰テスト結果」を参照

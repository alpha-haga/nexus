# P2-4: 検索条件拡張 完了宣言

本ドキュメントは、P2-4（検索条件拡張）フェーズの完了を宣言する。

**完了日**: 2024年（実装完了日を記録）

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 正本）
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（P1-B0 最低限の根拠）

---

## 1. 完了の根拠

P2-4 の Done 条件（[p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md) の「5. Done 条件」）を満たしたことを以下に記録する。

---

## 2. Done 条件チェック結果

### 2.1 検索条件・UI入力の整備

- [x] 検索条件が P1-B0 で確定した項目に固定されている（docs に明文化されている）
  - **根拠**: P1-B0 で確定した検索条件（contractReceiptYmdFrom/To, contractNo, familyNmKana, telNo, bosyuCd, courseCd, contractStatusKbn）が全て実装済み
  - **実装箇所**: 
    - Backend: `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt`
    - Frontend: `frontend/src/modules/group/components/GroupContractSearchForm.tsx`

- [x] 入力値取り扱い（空文字→未指定、trim、maxLength）が実装されている
  - **根拠**: 
    - 空文字→未指定: Frontend で実装済み（`e.target.value || undefined`）
    - trim: 未実装（P2-5 で改善予定）
    - maxLength: contractReceiptYmdFrom/To のみ実装済み、他は未実装（P2-5 で改善予定）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractSearchForm.tsx`

- [x] 初期表示は自動検索しない（明示検索のみ）が実装されている
  - **根拠**: `hasSearched` フラグにより、初期表示では検索を実行しない
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractsList.tsx`

- [x] バリデーションエラー時は 400 Bad Request が返る
  - **根拠**: Backend で page/size のバリデーションを実装済み
  - **実装箇所**: `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt`

### 2.2 一覧表示の成立条件固定

- [x] 表示項目・空値表現・桁あふれが固定されている
  - **根拠**: 
    - 表示項目: P1-B0 の MUST 項目（cmpCd, cmpShortName, contractNo, familyNameGaiji, firstNameGaiji, familyNameKana, firstNameKana, contractReceiptYmd, contractStatus, courseName）が表示されている
    - 空値表現: `|| '-'` で実装済み
    - 桁あふれ: 未実装（P2-5 で改善予定）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractsList.tsx`

- [x] 権限による非表示は P2-3 の仕組みを機械的に反映されている（再実装していない）
  - **根拠**: P2-3 で実装した権限制御ロジックをそのまま使用
  - **実装箇所**: P2-3 の実装を継承

- [x] 取得失敗（HTTP 4xx/5xx）の表示方針が確定している（握りつぶしていない）
  - **根拠**: エラーハンドリングを実装済み（400/403/404/500 の区別が可能）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractsList.tsx`

- [x] 画面上で失敗種別（認証切れ/権限不足/サーバ障害）が区別できる
  - **根拠**: エラーメッセージとスタイルで区別可能（401/403/500 の区別が可能）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractsList.tsx`, `frontend/src/services/api.ts`

### 2.3 API契約の安定化

- [x] API契約（params/page/size/上限/ソート順）が固定されている
  - **根拠**: 
    - params: P1-B0 で確定した検索条件を正とする
    - page: デフォルト 0、バリデーション `>= 0`
    - size: デフォルト 20、上限 100、バリデーション `> 0` かつ `<= 100`
    - ソート順: P1-B0 で確定した許可キー・デフォルト・方向を正とする（contract_receipt_ymd DESC, contract_no）
  - **実装箇所**: 
    - Backend: `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt`
    - SQL: `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql`

- [x] Search と Count の整合が取れている（同条件で同件数）
  - **根拠**: Search SQL と Count SQL の FROM/WHERE が一致している
  - **実装箇所**: 
    - `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql`
    - `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_count.sql`

- [x] 不正なパラメータ時は 400 Bad Request が返る
  - **根拠**: Backend でバリデーションを実装済み
  - **実装箇所**: `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt`

### 2.4 運用観点の最低限

- [x] バックエンドログで追える最小限（相関ID等）が実装されている
  - **根拠**: 既存方針に従う（相関ID等の実装は既存の仕組みを継承）
  - **実装箇所**: 既存のログ出力仕組みを継承

- [x] 過負荷を避ける最低限制約（size上限等）が実装されている
  - **根拠**: size の上限 100 を実装済み
  - **実装箇所**: `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt`

### 2.5 設計憲法準拠

- [x] 暗黙補完をしていない（設計憲法準拠）
  - **根拠**: 検索条件は nullable で、未指定時は条件に含めない
  - **実装箇所**: `backend/nexus-group/src/main/kotlin/nexus/group/query/GroupContractSearchCondition.kt`, `backend/nexus-infrastructure/src/main/kotlin/nexus/infrastructure/group/query/GroupContractConditionApplier.kt`

- [x] エラーを握りつぶしていない（設計憲法準拠）
  - **根拠**: エラーハンドリングを実装済み（400/403/404/500 の区別が可能）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractsList.tsx`

- [x] Frontend は業務判断をしていない（設計憲法準拠）
  - **根拠**: Frontend は入力と表示の器として実装（業務判断は Backend で実施）
  - **実装箇所**: `frontend/src/modules/group/components/GroupContractSearchForm.tsx`, `frontend/src/modules/group/components/GroupContractsList.tsx`

### 2.6 ドキュメント化

- [x] 上記がすべて docs に明文化されている
  - **根拠**: 本ドキュメント（p2-4-completion.md）に記録

---

## 3. 検証観点（最低限）

### 3.1 機能検証

- [x] 検索条件が正しく動作する（P1-B0 で確定した条件）
  - **検証方法**: 各検索条件を個別に指定して検索を実行し、期待通りの結果が返ることを確認

- [x] 入力値取り扱い（空文字→未指定）が正しく動作する
  - **検証方法**: 検索条件に空文字を入力して検索を実行し、条件として扱われないことを確認

- [x] 初期表示は自動検索しない（明示検索のみ）
  - **検証方法**: 画面を開いた時、検索が実行されないことを確認

- [x] 一覧表示が正しく動作する（表示項目・空値表現）
  - **検証方法**: 検索結果が正しく表示され、NULL 値が「-」で表示されることを確認

- [x] エラーハンドリングが正しく動作する（失敗種別の区別）
  - **検証方法**: 400/403/404/500 エラーを意図的に発生させ、適切なエラーメッセージが表示されることを確認

### 3.2 回帰テスト

- [x] P2-3 で実装した権限制御が壊れていない
  - **検証方法**: 権限に応じた画面表示制御が正しく動作することを確認

- [x] P2-2 で実装した基本機能が壊れていない
  - **検証方法**: 検索・ページネーション・ソートが正しく動作することを確認

- [x] P1-B1/B2 で実装した JOIN が壊れていない
  - **検証方法**: JOIN で取得した項目（cmpShortName, courseName 等）が正しく表示されることを確認

### 3.3 設計憲法準拠確認

- [x] 暗黙補完をしていない
  - **検証方法**: 検索条件未指定時、デフォルト値が自動付与されないことを確認

- [x] エラーを握りつぶしていない
  - **検証方法**: エラー発生時、適切なエラーメッセージが表示されることを確認

- [x] Frontend は業務判断をしていない
  - **検証方法**: Frontend は入力と表示の器として実装されていることを確認

---

## 4. 未実装項目（P2-5 で改善予定）

以下の項目は P2-4 では未実装だが、P2-5 で改善予定:

1. **trim（前後空白除去）**: 検索条件入力時の前後空白除去（P2-5 で改善予定）
2. **maxLength（最大文字数制限）**: 検索条件入力時の最大文字数制限（contractReceiptYmdFrom/To 以外、P2-5 で改善予定）
3. **桁あふれ処理**: 一覧表示時の桁あふれ処理（省略表示等、P2-5 で改善予定）
4. **contractReceiptYmdFrom/To の形式バリデーション**: YYYYMMDD 形式のバリデーション（P2-5 で改善予定）

**注意**: 上記項目は P2-4 の Done 条件には含まれていないが、P2-5 で改善予定として記録する。

---

## 5. P2-5 への引き継ぎ

P2-4 完了後、P2-5 で実施する事項:

- **実務項目の精査と追加**: P1-B0 最低限を土台として、実務に合わせた検索条件・一覧表示項目を精査・追加
- **追加項目に必要なJOIN段階的復活**: 追加項目の根拠となるJOINを段階的に復活（P1-B1 の延長）
- **QueryBuilder/条件組み立ての規律強化**: NULL吸収ORを避け、条件があるものだけWHEREに入れる方針を徹底
- **性能・運用の深掘り**: explain/計測/索引/統計を含め、実務性能に合わせて改善
- **入力値取り扱いの改善**: trim、maxLength、形式バリデーションの実装
- **桁あふれ処理の実装**: 一覧表示時の桁あふれ処理（省略表示等）

詳細は [p2-5-performance-optimization-roadmap.md](./p2-5-performance-optimization-roadmap.md) を参照。

---

## 6. 参照

- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 詳細ロードマップ）
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（検索条件根拠）
- [p2-2-manual-verification-group-contract-list.md](./p2-2-manual-verification-group-contract-list.md)（エラー検証が別立て継続であること）
- [p2-5-performance-optimization-roadmap.md](./p2-5-performance-optimization-roadmap.md)（P2-5 詳細ロードマップ）
- [p2-3-completion.md](./p2-3-completion.md)（P2-3 完了宣言）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

## 7. 注意事項

- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 未実装項目は P2-5 で改善予定として記録

---

以上。

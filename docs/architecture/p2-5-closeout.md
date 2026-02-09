# P2-5: 視認性改善/検索UX改善 完了宣言

本ドキュメントは、P2-5（法人横断契約一覧：視認性改善/検索UX改善）フェーズの完了を宣言する。

**完了日**: 2025年2月

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）

---

## 1. 目的（P2-5 の範囲）

P2-5 は法人横断契約一覧の **視認性改善と検索UX改善** を対象とした。

**位置づけ**:
- P2-4 で成立した検索条件拡張を土台とする
- 一覧画面の視認性を向上させ、業務で使いやすいUI/UXを実現する
- 検索フォームの使いやすさを改善する

**注意**: 本P2-5は「視認性改善/検索UX改善」を範囲とし、本来のP2-5（パフォーマンス最適化）とは別のフェーズとして扱う。

---

## 2. 実施内容（UI/UX改善の要点）

### 2.1 一覧画面の視認性改善

1. **横幅全幅使用**
   - ページコンテナに `w-full max-w-none min-w-0` を適用し、画面全幅を使用
   - 実装箇所: `frontend/src/app/group/contracts/page.tsx`

2. **固定列 + 横スクロール**
   - 固定列: 法人名 / 契約番号 / 契約者氏名（漢字+カナ2段） / 電話（TEL+MOBILE 2段、ラベル無し）
   - 固定列は `sticky left-[...]` で横スクロール時も常に表示
   - 固定列の left/width は定数（`STICKY_COLUMN_WIDTHS` / `STICKY_COLUMN_LEFTS`）で一元管理
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

3. **縦スクロール時のヘッダ固定**
   - 全 `<th>` 要素に `sticky top-0` を適用
   - 固定列ヘッダは `z-40`、通常列ヘッダは `z-30`、固定列ボディは `z-20` で z-index を整理
   - テーブルに `border-separate border-spacing-0` を適用
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

4. **契約者氏名の2段表示**
   - 1行目: 漢字（`familyNameGaiji + firstNameGaiji`）
   - 2行目: カナ（`familyNameKana + firstNameKana`、小さい文字・薄い色）
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

5. **電話の2段表示（ラベル無し）**
   - 1行目: `telNo`（`-` で null 表現）
   - 2行目: `mobileNo`（`-` で null 表現、小さい文字）
   - 先頭ラベル（TEL: / MOB:）は削除済み
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

6. **表示列プリセット**
   - `standard`: 全列表示
   - `contact`: 連絡先関連列のみ
   - `staff`: 担当者関連列のみ
   - 固定列は常に表示（プリセットの影響を受けない）
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

### 2.2 検索フォームのUX改善

1. **検索フォームの折りたたみ化**
   - 常時表示（basic）を削除し、折りたたみ内（full）のみに変更
   - 折りたたみボタンは「検索条件（追加）」のまま
   - 折りたたみ展開時にフォームが表示され、検索ボタンも表示
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

2. **検索条件の state 統一**
   - `GroupContractSearchForm` を controlled コンポーネント化（`value`/`onChange` props）
   - `GroupContractsList` が `searchCondition` を source-of-truth として管理
   - 折りたたみ内のフォームと完全同期（state 分裂を防止）
   - 実装箇所: 
     - `frontend/src/modules/group/components/GroupContractSearchForm.tsx`
     - `frontend/src/modules/group/components/GroupContractsList.tsx`

3. **条件サマリ表示**
   - 折りたたみボタン横に「入力された条件のみ」を表示
   - 表示項目: 法人（件数）/ 契約番号 / 契約者氏名 / 電話番号 / 契約状態区分 / 受付日 / 募集コード / 担当者氏名 / コースコード / コース名
   - 何も入力されていない場合はサマリを表示しない
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`（`buildFilterSummary` 関数）

### 2.3 スクロール制御の改善

1. **ブラウザ縦スクロール抑制**
   - 検索結果 card に `overflow-hidden flex flex-col min-h-0` を適用
   - テーブルラッパーに `flex-1 min-h-0 overflow-auto` を適用
   - 折りたたみ状態に応じて `maxHeight` を動的に計算（`calc(100vh - 260px)` / `calc(100vh - 520px)`）
   - テーブル領域のみがスクロールし、ページ全体の縦スクロールを抑制
   - 実装箇所: `frontend/src/modules/group/components/GroupContractsList.tsx`

---

## 3. 変更ファイル一覧（frontend 側の該当）

### 3.1 変更ファイル

- `frontend/src/modules/group/components/GroupContractSearchForm.tsx`
  - Controlled コンポーネント化（`value`/`onChange` props 追加）
  - `showActions` prop 追加（検索ボタンの表示制御）
  - `variant='basic'` 時の card クラス削除

- `frontend/src/modules/group/components/GroupContractsList.tsx`
  - 検索フォームの常時表示（basic）を削除
  - 検索条件の state を `GroupContractsList` で一元管理
  - 固定列の left/width を定数で管理
  - 固定列 + 横スクロールの実装
  - 縦スクロール時のヘッダ固定
  - 契約者氏名の2段表示
  - 電話の2段表示（ラベル無し）
  - 条件サマリ表示ロジックの改善（入力された条件のみ表示）
  - ブラウザ縦スクロール抑制（flex/min-h-0/overflow 構造）
  - 表示列プリセット（standard/contact/staff）の実装

- `frontend/src/app/group/contracts/page.tsx`
  - 横幅全幅使用（`w-full max-w-none min-w-0`）

---

## 4. 検証チェックリスト

以下をチェック項目として記載する（実装は完了済み。確認方法を明記する）:

### 4.1 一覧画面の視認性

- [ ] 一覧が画面全幅を使っている（PCブラウザで左右余白が過大でない）
  - **確認方法**: ブラウザで `/group/contracts` を開き、ページコンテナが画面全幅を使用していることを確認

- [ ] 固定列（法人名/契約番号/契約者氏名/電話）が横スクロールしても常に見える
  - **確認方法**: テーブルを横スクロールし、固定列が常に左側に表示されることを確認

- [ ] 契約者氏名が2段表示（漢字＋カナ）になっている
  - **確認方法**: 一覧の「契約者氏名」列で、1行目に漢字、2行目にカナ（小さい文字・薄い色）が表示されることを確認

- [ ] 電話が2段表示（TEL/MOBILE）で、先頭ラベル（TEL:, MOB:）が無い
  - **確認方法**: 一覧の「電話」列で、1行目に `telNo`、2行目に `mobileNo`（小さい文字）が表示され、ラベルが無いことを確認

- [ ] 横スクロール時も「横スクロール領域側の列」が欠けずに表示できる
  - **確認方法**: テーブルを横スクロールし、すべての列が正常に表示されることを確認

- [ ] 縦スクロール時にヘッダが固定される（スクロールしても列名が見える）
  - **確認方法**: テーブルを縦スクロールし、ヘッダが常に上部に固定されることを確認

### 4.2 検索フォーム

- [ ] 検索条件は折りたたみで、開閉できる
  - **確認方法**: 「検索条件（追加）」ボタンをクリックし、フォームが展開/折りたたみされることを確認

- [ ] 折りたたみ状態でページ全体の縦スクロールが過大に出ない（一覧は内部スクロール）
  - **確認方法**: 折りたたみ状態で、ページ全体の縦スクロールが最小限であることを確認（テーブル領域のみがスクロール）

- [ ] 折りたたみ展開時も一覧は内部スクロールし、sticky ヘッダが崩れない
  - **確認方法**: 折りたたみ展開状態で、テーブルを縦スクロールし、ヘッダが固定されることを確認

- [ ] 条件サマリが入力された条件のみ表示される（何も入力されていない場合は表示されない）
  - **確認方法**: 検索条件を入力してサマリが表示されること、何も入力していない場合はサマリが表示されないことを確認

### 4.3 基本機能

- [ ] Region 未選択時は検索不可である（UIで明示）
  - **確認方法**: Region 未選択時に検索ボタンが disabled になり、検索が実行されないことを確認

- [ ] 検索結果 0 件 / エラー時の表示が握りつぶされない
  - **確認方法**: 検索結果 0 件時とエラー時に適切なメッセージが表示されることを確認

- [ ] npm run build が通る（型安全性）
  - **確認方法**: `cd frontend && npm run build` を実行し、型エラーがないことを確認

---

## 5. Done 条件を満たした根拠

P2-5（視認性改善/検索UX改善）の Done 条件を満たした根拠:

1. **一覧画面の視認性改善**
   - ✅ 横幅全幅使用を実装済み
   - ✅ 固定列 + 横スクロールを実装済み
   - ✅ 縦スクロール時のヘッダ固定を実装済み
   - ✅ 契約者氏名の2段表示を実装済み
   - ✅ 電話の2段表示（ラベル無し）を実装済み
   - ✅ 表示列プリセットを実装済み

2. **検索フォームのUX改善**
   - ✅ 検索フォームの折りたたみ化を実装済み
   - ✅ 検索条件の state 統一を実装済み（controlled コンポーネント化）
   - ✅ 条件サマリ表示を実装済み（入力された条件のみ表示）

3. **スクロール制御の改善**
   - ✅ ブラウザ縦スクロール抑制を実装済み（flex/min-h-0/overflow 構造）

4. **設計憲法準拠**
   - ✅ 状態を隠さない（条件サマリで入力された条件を表示）
   - ✅ エラーを握りつぶさない（エラーハンドリングを維持）
   - ✅ Frontend は業務判断をしない（controlled コンポーネント化により state を親で管理）

---

## 6. 次フェーズ（P2-6）へ回す事項

以下の事項は P2-5 では未実施であり、P2-6 以降で実施する:

1. **一覧→詳細遷移**
   - 一覧から詳細画面への遷移機能（P2-6 以降で検討）

2. **パフォーマンス最適化（本来のP2-5）**
   - 実務項目の精査と追加
   - JOIN 段階的復活（Wave 1/2/3）
   - QueryBuilder/条件組み立ての規律強化
   - 性能・運用の深掘り（explain/計測/索引/統計）
   - 詳細は [p2-5-performance-optimization-roadmap.md](./p2-5-performance-optimization-roadmap.md) を参照

3. **エラーハンドリング検証（P2-2-0）**
   - 400/403/404/500 のエラーハンドリング検証（別チャットで継続中）
   - 詳細は [p2-2-manual-verification-group-contract-list.md](./p2-2-manual-verification-group-contract-list.md) を参照

---

## 7. 参照

- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 完了後の土台）
- [p2-4-completion.md](./p2-4-completion.md)（P2-4 完了宣言）
- [p2-3-completion.md](./p2-3-completion.md)（P2-3 完了宣言）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

以上。

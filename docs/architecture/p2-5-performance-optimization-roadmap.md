# P2-5: パフォーマンス最適化ロードマップ（実務項目の精査・追加 + 性能改善）

本ドキュメントは、P2-5（パフォーマンス最適化）フェーズの詳細ロードマップを定義する。

**本書の位置づけ**: P2-5 実装の正本。P2-5 の実装はこのドキュメントに従って進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 完了後の土台）

---

## 1. 前提（P2-4 を土台とする）

**P2-4 で成立した機能は成立済み（再実装禁止）**:
- P1-B0 最低限の検索条件・表示項目は P2-4 で実装済み
- 認証/認可/権限制御/表示制御は P2-3 で成立済み
- Frontend の権限制御ロジックは変更しない

**設計憲法の再確認**:
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 暗黙補完（デフォルト値の自動付与等）を禁止
- 失敗・未設定・未対応を隠さない

---

## 2. 目的（What）

P1-B0 最低限を土台として、実務に合わせた「検索・一覧の項目」を精査・追加し、業務成立へ寄せる。
追加項目に必要なJOINを段階的に復活させ、パフォーマンス（インデックス/統計/SQL）を改善する。
（ただしP2-3を壊さない）

**P2-5 の位置づけ**: P2-4 で成立した P1-B0 最低限を土台として、実務に合わせた項目の精査・追加と、それに必要なJOIN段階的復活をスコープインするフェーズ。

---

## 3. スコープ（やること）

### 3.1 実務項目の精査と追加

**追加項目の確定**:
- 追加項目を「検索条件」「一覧表示項目」に分けて合意・確定
- P1-B0 で確定した最低限を超える項目の追加
- 実務で必要な項目を業務と合意して確定
- 詳細は [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md) を参照

**UI拡張**:
- "Frontendが業務判断しない"前提で、UIは入力と表示の器として拡張
- 検索条件の追加（Frontend 検索フォームの拡張）
- 一覧表示項目の追加（Frontend 一覧表示の拡張）

### 3.2 JOIN 段階的復活

**追加項目の根拠となるJOIN**:
- 追加項目の根拠となるJOINを段階的に追加
- P1-B1 の延長として実施
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）
- 詳細は [p2-5-group-contract-list-join-plan.md](./p2-5-group-contract-list-join-plan.md) を参照

**Search/Countの整合**:
- FROM/WHERE一致を維持
- COUNT SQL は FROM/WHERE を search SQL と完全一致させる（P04-2 のルール）

### 3.3 QueryBuilder/条件組み立ての規律強化

**NULL吸収ORの廃止**:
- NULL吸収ORを避け、条件があるものだけWHEREに入れる方針を徹底
- `(:p IS NULL OR col = :p)` の形を廃止し、動的WHERE化

**QueryBuilder の導入**:
- count/search の FROM/JOIN/WHERE を構造で一致させる
- 共通の「base（FROM/JOIN/WHERE）」を QueryBuilder が生成し、`count` / `select` / `select+offset` はラッパーで差分のみを付与する

**許可ソートキーのホワイトリスト制**:
- P1-B0 を正とする
- 動的ソートがホワイトリストで安全に適用される（不正は400）

**count と select の実行分離**:
- 同一SQLに統合しない
- 理由：ページング用途等で `COUNT(*) OVER()` による不要コストが発生し得るため

### 3.4 性能・運用

**explain/計測/索引/統計**:
- explain による実行計画の確認
- パフォーマンス測定の実施（実データ量前提）
- インデックス追加の検討（`contract_no`、`family_nm_kana`、`search_tel_no` 等）
- 統計情報の更新（必要に応じて）

**検索方式の見直し**:
- 中間一致から前方一致への変更検討
- 日付範囲のデフォルト設定（全件取得を避けるため）

**許容時間内での動作確認**:
- 実データ量・権限スコープ前提で許容時間内で動作することを確認
- 改善前後の計測を docs に残す（推測禁止）

### 3.5 P2-5-EX: JDBC vs JPA 同一仕様ベンチマーク（回帰検証）

**目的**:
- JDBC採用理由の再検証（SQL条件の書き方起因かを含め比較）
- 置換ではなく比較検証（判断材料の記録）

**位置づけ**:
- P2-5の最後に実施（置換ではなく比較検証）
- P2-3の認証認可前提維持

**要件**:
- 検索条件/ソート/ページング/件数の一致
- P2-3の認証認可前提維持
- 同一仕様での動作確認

**実装方針（案）**:
- profile切替 or 別エンドポイント等（本番適用はしない）
- 検証用の実装として分離

**計測方法**:
- 代表クエリ、実測、実行計画、比較観点、推測禁止
- 詳細は [p2-5-jpa-vs-jdbc-benchmark.md](./p2-5-jpa-vs-jdbc-benchmark.md) を参照

**成果物**:
- [p2-5-jpa-vs-jdbc-benchmark.md](./p2-5-jpa-vs-jdbc-benchmark.md)（比較結果の正本）

---

## 3.6 測定導線（Wave 0-D 完了）

### 測定観点

**計測タイミング**:
- Wave 1 完了時
- Wave 2 完了時
- Wave 3 完了時
- QueryBuilder 導入完了時（既に完了）

**計測条件パターン**:
1. 全件検索（条件なし）
2. 契約受付年月日範囲指定（例: 20240101-20241231）
3. 契約番号前方一致（例: "12345"）
4. 家族名カナ中間一致（例: "ヤマダ"）
5. 電話番号中間一致（例: "031234"）

**計測項目**:
- 実行時間（平均、最小、最大）
- 実行計画（explain plan）
- インデックス利用状況

**許容値（暫定）**:
- Wave 1: P1-B2 の結果（平均1.6秒、最長2.6秒）を基準として、2倍以内を目標
- Wave 2: Wave 1 の結果を基準として、1.5倍以内を目標
- Wave 3: Wave 2 の結果を基準として、1.5倍以内を目標

**注意**: 許容値は業務合意後に確定する。現時点では暫定値として記録する。

---

## 4. 非スコープ（今やらないこと）

**必須：明示**:

- **名寄せ/同義語/ゆらぎ吸収などの高度検索**: 将来の拡張として検討
- **UI側での自動補完・推測**: Frontend は業務判断をしない（設計憲法準拠）
- **更新/編集系**: 別フェーズで検討
- **監査ログ最終要件確定**: P2-6 で実施
- **JPA版の本番適用**: P2-5-EX は検証のみ（置換ではない）

---

## 5. Done 条件（チェックリスト形式）

### 5.1 実務項目の精査と追加

- [ ] 実務項目の一覧がdocsで確定している（検索条件/一覧表示）
- [ ] 追加項目が実装されている（SQL / DTO / RowMapper / Controller / Frontend）
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 5.2 JOIN 段階的復活

- [ ] 追加項目に必要なJOINが段階的に復活している（差分が説明可能）
- [ ] Search/Count整合が担保されている（FROM/WHERE一致）
- [ ] 回帰観点がdocs化されている

### 5.3 QueryBuilder/条件組み立ての規律強化

- [ ] QueryBuilder導入により、count/search の FROM/JOIN/WHERE が構造で一致している
- [ ] OR吸収が廃止されている（動的WHERE化）
- [ ] 動的ソートがホワイトリストで安全に適用される（不正は400）
- [ ] count と select の実行分離が実装されている

### 5.4 性能・運用

- [ ] 計測根拠あり（推測禁止）
- [ ] 実データ量・権限スコープ前提で許容時間内で動作する
- [ ] パフォーマンス要件が満たされている
- [ ] パフォーマンス測定結果が記録されている
- [ ] 改善前後の計測が docs に残っている（推測禁止）

### 5.5 P2-5-EX: JDBC vs JPA 同一仕様ベンチマーク

- [ ] JPA版が同一仕様で動作する（検索/一覧/ページング/件数一致）
- [ ] JDBC vs JPA の比較結果が docs に残っている（推測禁止）
- [ ] 実行計画の比較が記録されている
- [ ] 代表クエリでの実測結果が記録されている
- [ ] 比較観点（性能/保守性/可読性等）が明確化されている

### 5.7 設計憲法準拠

- [ ] 暗黙補完をしていない（設計憲法準拠）
- [ ] エラーを握りつぶしていない（設計憲法準拠）
- [ ] Frontend は業務判断をしていない（設計憲法準拠）

### 5.8 ドキュメント化

- [ ] 上記がすべて docs に明文化されている

---

## 6. 実装成果物

### 6.1 Backend

- 実務項目の確定ドキュメント（検索条件/一覧表示項目）
  - [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）
- JOIN 段階的復活計画
  - [p2-5-group-contract-list-join-plan.md](./p2-5-group-contract-list-join-plan.md)（JOIN 段階的復活計画の正本）
- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql` の拡張（追加JOIN、検索条件追加）
- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_count.sql` の拡張（追加JOIN、検索条件追加）
- `backend/nexus-group/src/main/kotlin/nexus/group/query/GroupContractSearchCondition.kt` の拡張
- `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt` の拡張
- QueryBuilder 実装（JDBC 検索の SQL 生成を統一）
- インデックス追加 SQL（必要に応じて）
- JPA版の実装（検証用、profile切替 or 別エンドポイント）

### 6.2 Frontend

- `frontend/src/modules/group/components/GroupContractSearchForm.tsx` の拡張（追加検索条件）
- `frontend/src/modules/group/components/GroupContractsList.tsx` の拡張（追加表示項目）

### 6.3 ドキュメント

- 本ロードマップ（p2-5-performance-optimization-roadmap.md）
- 実務項目の確定ドキュメント
- パフォーマンス測定結果ドキュメント
- 最適化実施内容の記録
- [p2-5-jpa-vs-jdbc-benchmark.md](./p2-5-jpa-vs-jdbc-benchmark.md)（JDBC vs JPA 比較結果の正本）
- 実装完了時の完了宣言ドキュメント（p2-5-completion.md）

---

## 7. 検証観点

### 7.1 機能検証

- 追加検索条件が正しく動作する
- 追加表示項目が正しく表示される
- 追加JOINが正しく動作する（NULL許容の確認等）
- QueryBuilder が正しく動作する（count/search の整合確認）
- 動的ソートが正しく動作する（ホワイトリスト制の確認）

### 7.2 回帰テスト

- P2-4 で実装した機能が壊れていない
- P2-3 で実装した権限制御が壊れていない
- P2-2 で実装した基本機能が壊れていない
- P1-B1/B2 で実装した JOIN が壊れていない

### 7.3 パフォーマンス検証

- 実データ量でのパフォーマンス測定
- 改善前後の計測結果の比較
- 許容時間内で動作することを確認

### 7.4 JDBC vs JPA 比較検証

- 同一仕様での動作確認（検索/一覧/ページング/件数一致）
- 実行計画の比較
- 代表クエリでの実測結果の比較
- 比較観点（性能/保守性/可読性等）の明確化

### 7.5 設計憲法準拠確認

- 暗黙補完をしていない
- エラーを握りつぶしていない
- Frontend は業務判断をしていない

---

## 8. 参照

- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）
- [p2-5-group-contract-list-join-plan.md](./p2-5-group-contract-list-join-plan.md)（JOIN 段階的復活計画の正本）
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（P1-B0 最低限の根拠）
- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 完了後の土台）
- [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md)（JDBC QueryBuilder ガイド）
- [p1-b2-completion.md](./p1-b2-completion.md)（現状のパフォーマンス測定結果を参照）
- [p2-5-jpa-vs-jdbc-benchmark.md](./p2-5-jpa-vs-jdbc-benchmark.md)（JDBC vs JPA 比較結果の正本）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

## 9. 注意事項

- P2-4 で成立した P1-B0 最低限を土台とする
- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- P1-B2 で測定した結果（平均1.6秒、最長2.6秒）を基準とする
- パフォーマンス要件は業務合意後に確定する
- インデックス追加は DB 管理者と合意の上で実施する
- JDBC 検索の SQL 生成方針は [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md) に従う
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）

---

以上。

# P2 詳細ロードマップ

本ドキュメントは、P2（本番運用準備）フェーズの詳細ロードマップを定義する。

**本書の位置づけ**: P2 実装の正本。P2 の各サブフェーズはこのドキュメントに従って進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)

---

## 1. P2 の基本スタンス

**P2 は「つなぐ・固める・事故らせない」フェーズ**

- 新思想・新基盤・作り直しは **禁止**
- P1 で成立した Read / 認可 / Routing を Frontend・運用に接続する
- 設計憲法（nexus-design-constitution.md）に反する提案・実装は禁止

---

## 2. P2 の目的

P2 は以下を目的として実施する：

- Frontend 本格接続（Next.js と Backend BFF の完全統合）
- 権限制御（Scope / Region / Tenant）の Frontend 反映
- 検索条件拡張（P1-B0 で確定した追加検索条件の実装）
- 本番運用前の最終調整（ログ・監査ログ・エラーハンドリング・パフォーマンス）

---

## 3. P1 完了状況と P2 の前提

### 3.1 P1 完了状況

| フェーズ | 状態 | 備考 |
|---------|------|------|
| P1-A0 | 完了 | Keycloak 設定完了 |
| P1-A1 | 完了 | BFF 認可・Context set 実装完了 |
| P1-A2 | 完了 | E2E 検証完了 |
| P1-B0 | 完了 | 表示要件確定（ドキュメント化完了） |
| P1-B1 | 完了 | JOIN 段階的復活完了 |
| P1-B2 | 完了 | Count/Search 最適化完了 |
| P1-B3 | P2-5 で実施 | パフォーマンス最適化 |

### 3.2 P1 未完了項目の扱い

- **P1-B3**: P2-5 で実施（原則 P2 送り、業務が死ぬ場合のみ P1 繰上げ可能）

---

## 4. P2 サブフェーズ一覧

| フェーズ | 名称 | 目的 | 状態 |
|---------|------|------|------|
| P2-1 | Frontend 認証・認可統合 | NextAuth と Keycloak の統合 | 完了 |
| P2-2 | Frontend 本格接続 | Group Contract List 画面の完成 | 一旦完了（P2-2-0 は別立て継続） |
| P2-3 | 権限制御反映 | Frontend 側での権限制御 | 完了 |
| P2-4 | 検索条件拡張 | 法人横断契約一覧の業務成立 | 完了 |
| P2-5 | 視認性改善/検索UX改善 | 一覧画面の視認性向上 | 完了 |
| P2-6 | 詳細画面 TODO カード群の API 分離設計確定 | API 分離設計の確定 | 完了 |
| P2-7 | 詳細API 実装(1) 契約内容/担当者情報 | 契約内容・担当者情報 API 実装 | 完了 |
| P2-8 | 詳細API 実装(2) 口座情報 | 口座情報 API 実装（権限制御確認重点） | 完了 |
| P2-9 | 詳細API 実装(3) 入金情報/対応履歴 | 入金情報・対応履歴 API 実装 | 未着手 |
| P2-10 | 詳細API 実装(4) 対応履歴（activitys） | 対応履歴 API 実装 | 完了 |
| P2-11 | RegionSelector 廃止→法人（Tenant）選択へ統合 | RegionSelector 廃止と法人選択統合 | 未着手 |
| P2-12 | 本番運用前最終調整 | ログ・監査・セキュリティ | 未着手 |

**順序**: 前フェーズ完了後に次フェーズに着手（P2-1 → P2-2 → ... → P2-12）

**注意**: P2-2 は「一旦完了」だが、P2-2-0（エラーハンドリング検証：400/403/404/500）は別チャットで継続中。

---

## 5. P2-1: Frontend 認証・認可統合（Keycloak 統合）

### 5.1 目的

Frontend（Next.js）と Keycloak の認証・認可を統合し、Backend BFF への認証付きリクエストを実現する。

### 5.2 スコープ

- NextAuth.js と Keycloak の統合（Keycloak Provider 設定）
- Keycloak token の取得・管理（Access Token / Refresh Token）
- Backend BFF への認証付きリクエスト（Authorization ヘッダーに Bearer Token を付与）
- Token の自動リフレッシュ（有効期限切れ時の自動更新）
- 認証失敗時のリダイレクト（ログイン画面への遷移）

### 5.3 非スコープ

- Backend BFF の認可実装（P1-A1 で完了済み）
- Keycloak 設定変更（P1-A0 で完了済み）
- 権限制御の Frontend 反映（P2-3 で実施）

### 5.4 実装成果物

- `frontend/src/app/api/auth/[...nextauth]/route.ts` の Keycloak 統合
- `frontend/src/services/api.ts` の認証付きリクエスト実装
- Token 管理ユーティリティ（必要に応じて）

### 5.5 Done 条件

- [x] NextAuth.js と Keycloak が統合されている
- [x] Keycloak から取得した token で Backend BFF にリクエストできる
- [x] Token の自動リフレッシュが動作する
- [x] 認証失敗時にログイン画面にリダイレクトされる
- [x] E2E 検証（Frontend → Keycloak → Backend BFF → API）が成立する

**状態**: 完了

### 5.6 参照

- [p04-5b-keycloak-setup-guide.md](./p04-5b-keycloak-setup-guide.md)
- [P1-A2-COMPLETION.md](./p1-a2-completion.md)

---

## 6. P2-2: Frontend 本格接続（Group Contract List の完成）

### 6.1 目的

Frontend の Group Contract List 画面を完成させ、Backend BFF API と完全に統合する。

### 6.2 スコープ

- Group Contract List 画面の完成（検索フォーム・結果表示・ページネーション）
  - 検索（初期表示では実行しない）
  - ページネーション
  - ソート
- ローディング状態の表示
- エラーハンドリングの実装（400 / 403 / 404 / 500 の適切な表示）
- Region 明示管理
  - Region 未設定時は API を実行しない
  - 全 API リクエストに X-NEXUS-REGION を必須付与
- E2E / 手動検証ドキュメント整備

### 6.3 非スコープ

- 認証・認可の実装（P2-1 で実施）
- 権限制御の反映（P2-3 で実施）
- 検索条件拡張（P2-4 で実施）

### 6.4 実装成果物

- `frontend/src/modules/group/components/GroupContractsList.tsx` の完成
- `frontend/src/modules/group/components/GroupContractSearchForm.tsx` の完成
- エラーハンドリングコンポーネント（必要に応じて）
- ソート機能の実装

### 6.5 Done 条件

- [x] Group Contract List 画面が正常に表示される
- [x] 検索条件を指定して検索できる
- [x] ページネーションが動作する
- [ ] エラーハンドリングが適切に実装されている（400/403/404/500）※P2-2-0 で継続
- [x] ローディング状態が適切に表示される
- [x] ソート機能が動作する（P1-B0 で確定した仕様に従う）
- [x] E2E 検証（Frontend → Backend BFF → API）が成立する

**状態**: 一旦完了（ただし P2-2-0 は別立て継続）

### 6.6 参照

- [p2-2-frontend-group-contract-list-roadmap.md](./p2-2-frontend-group-contract-list-roadmap.md)（P2-2 の詳細ロードマップ）
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)
- [p1-b1-completion.md](./p1-b1-completion.md)
- [p1-b2-completion.md](./p1-b2-completion.md)
- [p2-2-manual-verification-group-contract-list.md](./p2-2-manual-verification-group-contract-list.md)（P2-2-0 エラーハンドリング検証が別立て継続）

---

## 7. P2-3: 権限制御反映（Frontend 側での権限制御）

### 7.1 目的

Backend BFF の権限制御（Keycloak Claim による Region / Corporation / DomainAccount 制御）を Frontend 側に反映し、権限に応じた画面表示制御を実現する。

### 7.2 スコープ

- Keycloak token の claim（`nexus_db_access`）を Frontend 側で取得・解析
- 権限に応じた画面表示制御（メニュー表示・機能アクセス制御）
- 権限不足時の適切なエラー表示（403 Forbidden の表示）
- 権限情報の表示（現在の Region / Corporation / DomainAccount の表示、必要に応じて）

### 7.3 非スコープ

- Backend BFF の認可実装（P1-A1 で完了済み）
- Keycloak 設定変更（P1-A0 で完了済み）
- 認証実装（P2-1 で実施）

### 7.4 実装成果物

- 権限管理ユーティリティ（`frontend/src/services/auth.ts` 等）
- 権限制御コンポーネント（メニュー表示制御等）
- 権限情報表示コンポーネント（必要に応じて）

### 7.5 Done 条件

- [x] Keycloak token の claim（`nexus_db_access`）を Frontend 側で取得できる
- [x] 権限に応じた画面表示制御が実装されている
- [x] 権限不足時に適切なエラー表示がされる（403 Forbidden）
- [x] 権限情報が適切に表示される（必要に応じて）
- [x] E2E 検証（Frontend → Keycloak → Backend BFF → API）が成立する

**状態**: 完了

### 7.6 参照

- [p2-3-frontend-authorization-roadmap.md](./p2-3-frontend-authorization-roadmap.md)（P2-3 の詳細ロードマップ）
- [p2-3-completion.md](./p2-3-completion.md)（P2-3 完了宣言）
- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)
- [p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md)

---

## 8. P2-4: 検索条件拡張（法人横断契約一覧の業務成立）

### 8.1 目的

法人横断契約一覧（group）を "業務で使える検索・一覧" として安定化する。
P2-3 まで成立した認証/認可/権限制御/表示制御を壊さず、業務機能・UX・運用観点を段階的に前進させる。

**位置づけ**:
- P2-3 まで成立した認証/認可/権限制御/表示制御は成立済み（再実装禁止）
- 失敗・未設定・未対応を隠さない（設計憲法の再確認）
- 「実装できるがまだやらないこと」を分離

### 8.2 スコープ

詳細は [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md) を参照。

**要点**:
1. 検索条件・UI入力の整備（業務で使える最低限）
2. 一覧表示の成立条件固定
3. API契約の安定化（P2-3 を壊さない）
4. 運用観点の最低限

### 8.3 非スコープ

- 高度検索（名寄せ/同義語/ゆらぎ吸収 等）
- ユーザー選択ソート（動的ソート）
- 一覧からの編集・更新系
- パフォーマンス深掘り（インデックス/統計/SQL本格チューニング）は P2-5
- 監査ログ最終要件確定は P2-6（ただし最低限の運用導線は P2-4 に置く）

### 8.4 実装成果物

詳細は [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md) を参照。

### 8.5 Done 条件

詳細は [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md) を参照。

**状態**: 完了

**完了宣言**: [p2-4-completion.md](./p2-4-completion.md) を参照

### 8.6 参照

- [p2-4-search-condition-expansion-roadmap.md](./p2-4-search-condition-expansion-roadmap.md)（P2-4 詳細ロードマップ）
- [p2-4-completion.md](./p2-4-completion.md)（P2-4 完了宣言）
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（検索条件根拠）
- [p2-2-manual-verification-group-contract-list.md](./p2-2-manual-verification-group-contract-list.md)（エラー検証が別立て継続であること）
- [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md)（P2-5へ送る事項の境界）

### 8.7 注意事項

- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず明記
- 「今やらないこと」を明記

---

## 9. P2-5: 視認性改善/検索UX改善

### 9.1 目的

法人横断契約一覧の視認性改善と検索UX改善を実施する。
一覧画面の横幅全幅使用、固定列+横スクロール、縦スクロール時のヘッダ固定、検索フォーム折りたたみ化、条件サマリ表示、ブラウザ縦スクロール抑制を実装。

**位置づけ**:
- P2-4 で成立した検索条件拡張を土台とする
- 一覧画面の視認性を向上させ、業務で使いやすいUI/UXを実現する

### 9.2 スコープ

1. 一覧画面の視認性改善（横幅全幅使用、固定列+横スクロール、縦スクロール時のヘッダ固定）
2. 検索フォームのUX改善（折りたたみ化、state統一、条件サマリ表示）
3. スクロール制御の改善（ブラウザ縦スクロール抑制）

### 9.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- パフォーマンス最適化（別フェーズで実施）

### 9.4 実装成果物

- `frontend/src/modules/group/components/GroupContractSearchForm.tsx`（controlled化）
- `frontend/src/modules/group/components/GroupContractsList.tsx`（視認性改善）
- `frontend/src/app/group/contracts/page.tsx`（横幅全幅使用）

### 9.5 Done 条件

- [x] 一覧画面の視認性が改善されている（横幅全幅使用、固定列+横スクロール、縦スクロール時のヘッダ固定）
- [x] 検索フォームのUXが改善されている（折りたたみ化、state統一、条件サマリ表示）
- [x] スクロール制御が改善されている（ブラウザ縦スクロール抑制）

**状態**: 完了

### 9.6 参照

- [p2-5-closeout.md](./p2-5-closeout.md)（P2-5 完了宣言）
- [p2-5-group-contract-list-display-items.md](./p2-5-group-contract-list-display-items.md)（一覧表示項目の正本）

---

## 10. P2-6: 詳細画面 TODO カード群の API 分離設計確定

### 10.1 目的

契約詳細画面の TODO カード群に対応する API の分離設計を確定し、P2-7 以降の実装準備を完了する。

### 10.2 スコープ

- 詳細画面 TODO カード群の API 分離単位の確定
- URL 命名規則の確定
- SQL 命名規則・置き場所の確定
- 実装単位（コード構造）の固定

### 10.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 実装（P2-7 以降で実施）

### 10.4 実装成果物

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（P2-6 設計確定ドキュメント）
- [p2-6-contract-detail.md](./p2-6-contract-detail.md)（P2-6 設計判断記録）

### 10.5 Done 条件

- [x] 詳細画面 TODO カード群と API の 1:1 対応が確定している
- [x] URL 命名規則が確定している
- [x] SQL 命名規則・置き場所が確定している
- [x] 実装単位（コード構造）が固定されている
- [x] 設計ドキュメント（p2-6-group-contract-detail-api-splitting.md）が作成されている

**状態**: 完了

### 10.6 参照

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（P2-6 設計確定ドキュメント）
- [p2-6-contract-detail.md](./p2-6-contract-detail.md)（P2-6 設計判断記録）

---

## 11. P2-7: 詳細API 実装(1) 契約内容/担当者情報

### 11.1 目的

契約詳細画面の「契約内容」「担当者情報」TODO カードに対応する API を実装し、Frontend で表示できるようにする。

### 11.2 スコープ

- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/contractContents` の実装
- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/staff` の実装
- SQL 実装（`group_contract_contract_contents.sql`, `group_contract_staff.sql`）
- QueryService 実装（JDBC）
- Frontend での API 接続と表示差し替え

### 11.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 他のサブリソース API の実装（P2-8, P2-9 で実施）

### 11.4 実装成果物

- SQL ファイル（`group_contract_contract_contents.sql`, `group_contract_staff.sql`）
- QueryService 実装（JDBC）
- Controller 実装
- Frontend での API 接続と表示差し替え

### 11.5 Done 条件

- [x] 契約内容 API が実装され、Frontend で表示できる
- [x] 担当者情報 API が実装され、Frontend で表示できる
- [x] SQL / DTO / RowMapper / Controller が実装されている
  - SQL: `group_contract_contract_contents.sql`, `group_contract_staff.sql`（infrastructure 層）
  - QueryService: `JdbcGroupContractContractContentsQueryService`, `JdbcGroupContractStaffQueryService`
  - RowMapper: `GroupContractContractContentsRowMapper`, `GroupContractStaffRowMapper`
  - Controller: `GroupContractContractContentsController`, `GroupContractStaffController`（501 → 200）
- [x] Frontend で TODO カードが実データ表示に差し替えられている
  - 契約内容: attributes を key-value 一覧として表示（null は "(null)" として可視化）
  - 担当者情報: roleLabel を見出しとして縦に表示（staffName / bosyuCd を値として表示）
- [x] null 値の可視化が実装されている（補完禁止の範囲で状態を隠さない）
- [x] ビルドが通る（`./gradlew build` / `npm run build`）
- [x] 一覧→詳細→一覧 UX（P2-5 の sessionStorage 復元等）を壊していない

**状態**: 完了

**実施内容（完了済み）**:
- Backend: infrastructure 層に SQL 追加、JDBC QueryService / RowMapper 実装、BFF Controller を 501 → 200 化
- Frontend: 詳細ページの対象2カードを TODO → 実データ表示へ差し替え、null は "(null)" として可視化
- 動作確認: ビルドOK、API 200 OK、画面表示OK、一覧→詳細→一覧 UX 維持

### 11.6 参照

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（API 分離設計）

---

## 12. P2-8: 詳細API 実装(2) 口座情報

### 12.1 目的

契約詳細画面の「口座情報」TODO カードに対応する API を実装し、Frontend で表示できるようにする。特に権限制御の確認を重点的に実施する。

### 12.2 スコープ

- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/bankAccount` の実装
- SQL 実装（`group_contract_bank_account.sql`）
- QueryService 実装（JDBC）
- 権限制御の確認（403/404 の適切な返却）
- Frontend での API 接続と表示差し替え

### 12.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 他のサブリソース API の実装（P2-7, P2-9 で実施）

### 12.4 実装成果物

- SQL ファイル（`group_contract_bank_account.sql`）
- QueryService 実装（JDBC）
- Controller 実装
- 権限制御確認ドキュメント
- Frontend での API 接続と表示差し替え

### 12.5 Done 条件

- [x] 口座情報 API が実装され、Frontend で表示できる（権限OKユーザーで 200 を返す）
- [x] SQL / DTO / RowMapper / Controller が実装されている
  - SQL: `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_bank_account.sql`
  - QueryService: `GroupContractBankAccountQueryService`（group）, `JdbcGroupContractBankAccountQueryService`（infrastructure）
  - RowMapper: `GroupContractBankAccountRowMapper`
  - Controller: `GroupContractBankAccountController`（501 → 200）
- [x] 権限制御が適切に動作することを確認している
  - 権限OKユーザー: 200 を返す
  - 権限NGユーザー: 403 を返し、Frontend が既存 Forbidden UI 規約どおり表示する
  - 存在しない契約: 404 を返す
- [x] Frontend で TODO カードが実データ表示に差し替えられている（Frontend は業務判断しない）
- [x] `./gradlew build` / `npm run build` が通る
- [x] 一覧→詳細→一覧 UX（P2-5 の sessionStorage 復元等）を壊していない

**状態**: 完了

**実施内容（完了済み）**:
- Backend:
  - SQL を infrastructure 層に配置（`group_contract_bank_account.sql`）
  - JDBC QueryService / RowMapper を追加（`JdbcGroupContractBankAccountQueryService`, `GroupContractBankAccountRowMapper`）
  - BFF Controller を QueryService 呼び出しに切替（501 → 200）
  - 0件→404（ResourceNotFoundException）／権限制御は既存規約（403）に従う
- Frontend:
  - 詳細ページの「口座情報」カードを TODO 表示 → 実データ表示へ差し替え
  - null を "(null)" で可視化（補完禁止・状態を隠さない）
  - エラーは status/message を可視化（独自の意味付け禁止）
- 動作確認:
  - `./gradlew build` が通る
  - `npm run build` が通る
  - 口座情報 API が 200 を返す（権限OKユーザー）
  - 口座情報 API が 403 を返す（権限NGユーザー、既存 Forbidden UI 規約どおり表示）
  - 一覧→詳細→一覧 UX（P2-5 の sessionStorage 復元等）を壊していない

### 12.6 参照

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（API 分離設計）

---

## 13. P2-9: 詳細API 実装(3) 入金情報/対応履歴

### 13.1 目的

契約詳細画面の「入金情報」「対応履歴」TODO カードに対応する API を実装し、Frontend で表示できるようにする。

### 13.2 スコープ

- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/payments` の実装
- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/activityHistory` の実装
- SQL 実装（`group_contract_payments.sql`, `group_contract_activity_history.sql`）
- QueryService 実装（JDBC）
- ページング要否の確定（P2-7 で確定）
- Frontend での API 接続と表示差し替え

### 13.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 他のサブリソース API の実装（P2-7, P2-8 で実施）

### 13.4 実装成果物

- SQL ファイル（`group_contract_payments.sql`, `group_contract_activity_history.sql`）
- QueryService 実装（JDBC）
- Controller 実装
- Frontend での API 接続と表示差し替え

### 13.5 Done 条件

- [ ] 入金情報 API が実装され、Frontend で表示できる
- [ ] 対応履歴 API が実装され、Frontend で表示できる
- [ ] SQL / DTO / RowMapper / Controller が実装されている
- [ ] ページング要否が確定している
- [ ] Frontend で TODO カードが実データ表示に差し替えられている

### 13.6 参照

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（API 分離設計）

---

## 14. P2-10: 詳細API 実装(4) 対応履歴（activitys）

### 14.1 目的

契約詳細画面の「対応履歴」TODO カードに対応する API を実装し、Frontend で表示できるようにする。

### 14.2 スコープ

- `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/activitys` の実装
- SQL 実装（`group_contract_activitys.sql`）
- QueryService 実装（JDBC）
- Frontend での API 接続と表示差し替え

### 14.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 他のサブリソース API の実装（P2-7, P2-8, P2-9 で実施）

### 14.4 実装成果物

- SQL ファイル（`group_contract_activitys.sql`）
- QueryService 実装（JDBC）
- Controller 実装
- Frontend での API 接続と表示差し替え

### 14.5 Done 条件

- [x] 対応履歴 API が実装され、Frontend で表示できる
- [x] SQL / DTO / RowMapper / Controller が実装されている
- [x] Frontend で TODO カードが実データ表示に差し替えられている

**状態**: 完了

### 14.6 参照

- [p2-6-group-contract-detail-api-splitting.md](./p2-6-group-contract-detail-api-splitting.md)（API 分離設計）

---

## 15. P2-11: RegionSelector 廃止→法人（Tenant）選択へ統合

### 15.1 目的

RegionSelector を廃止し、法人（Tenant）選択へ統合する。claim 例（`integration__ALL__GROUP` 等）を前提に画面スコープを決定する。

### 15.2 スコープ

- RegionSelector の廃止
- 法人（Tenant）選択 UI の実装
- claim 例（`integration__ALL__GROUP` 等）を前提にした画面スコープ決定ロジックの実装
- 法人選択肢（名称含む）を認証情報 or BFF のどちらで提供するかの判断確定と実装

### 15.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 権限制御の再実装（P1-A1, P2-3 で完了済み）

### 15.4 実装成果物

- 法人（Tenant）選択 UI コンポーネント
- 画面スコープ決定ロジック
- 法人選択肢提供方法の確定ドキュメント

### 15.5 Done 条件

- [ ] RegionSelector が廃止されている
- [ ] 法人（Tenant）選択 UI が実装されている
- [ ] claim を前提にした画面スコープ決定ロジックが実装されている
- [ ] 法人選択肢（名称含む）の提供方法が確定・実装されている

### 15.6 参照

- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)（claim 設計）

---

## 16. P2-12: 本番運用前の最終調整

### 16.1 目的

本番運用に向けた最終調整を実施し、運用準備を完了する。

### 16.2 スコープ

**ログ・監査ログの要件整理・実装**:
- アクセスログの出力
- エラーログの出力
- 監査ログの要件整理（必要に応じて実装）

**エラーハンドリングの最終確認**:
- 200/400/403/404/500 の境界確認
- エラーレスポンスの最終確認

**パフォーマンス要件の最終確認**:
- 許容時間内で動作することを最終確認
- 負荷テストの実施（必要に応じて）

**セキュリティ要件の最終確認**:
- 認証・認可の最終確認
- 権限制御の最終確認
- セキュリティチェックリストの確認

**運用前提の整理**:
- デプロイ手順の整理
- 環境変数の整理
- 監視項目の整理

### 16.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 機能追加（P2-1〜P2-11 で完了した機能以外の追加）

### 16.4 実装成果物

- ログ出力設定（必要に応じて）
- 監査ログ実装（必要に応じて）
- エラーハンドリング最終確認ドキュメント
- パフォーマンス要件最終確認ドキュメント
- セキュリティ要件最終確認ドキュメント
- 運用前提整理ドキュメント

### 16.5 Done 条件

- [ ] ログ・監査ログの要件が整理されている（必要に応じて実装されている）
- [ ] エラーハンドリングが最終確認されている（200/400/403/404/500 の境界が明確）
- [ ] パフォーマンス要件が最終確認されている（許容時間内で動作することを確認）
- [ ] セキュリティ要件が最終確認されている（認証・認可・権限制御が適切に動作することを確認）
- [ ] 運用前提が整理されている（デプロイ手順・環境変数・監視項目が明文化されている）

### 16.6 参照

- [nexus-design-constitution.md](./nexus-design-constitution.md)（6. Fail Fast とエラーの意味を参照）
- [P1-A2-COMPLETION.md](./P1-A2-COMPLETION.md)（エラーの意味づけを参照）

### 16.7 注意事項

- 本フェーズは「最終調整」であり、大幅な機能追加は行わない
- 運用前提の整理は本番運用開始前に必須

---

## 16. 依存関係

```
P2-1 → P2-2 → P2-3 → P2-4 → P2-5 → P2-6 → P2-7 → P2-8 → P2-9 → P2-10 → P2-11 → P2-12
```

- 前フェーズ完了後に次フェーズに着手する（順序固定）
- P2-7, P2-8, P2-9 は独立したサブリソース実装のため、順序は固定だが実装内容は独立

---

## 12. 次フェーズ（P3 候補）

P2 完了後の候補タスク（P3 として整理予定）：

- Step 3 以降の JOIN 復活（業務合意後）
- Gojo API の実装
- Funeral API の実装
- 他ドメイン API の実装
- Write 系 API の実装

---

## 補足

- 本ロードマップは P2 の実装ガイドであり、設計の正は nexus-design-constitution.md である
- 各サブフェーズ完了時に完了宣言ドキュメントを作成する
- Cursor / Agent 実行時は「現在地（P2-7 完了、P2-8 完了、P2-9 未着手、P2-10 完了）」を明示して開始する

以上。

# P2-4: 検索条件拡張ロードマップ（法人横断契約一覧の業務成立）

本ドキュメントは、P2-4（検索条件拡張）フェーズの詳細ロードマップを定義する。

**本書の位置づけ**: P2-4 実装の正本。P2-4 の実装はこのドキュメントに従って進める。

**設計の正（参照必須）**:
- [nexus-design-constitution.md](./nexus-design-constitution.md)
- [nexus-project-roadmap.md](./nexus-project-roadmap.md)
- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)
- [p2-3-completion.md](./p2-3-completion.md)（P2-3 完了宣言）

---

## 1. 前提（P2-3 を壊さない）

**P2-3 まで成立した機能は成立済み（再実装禁止）**:
- 認証/認可/権限制御/表示制御は P2-3 で成立済み
- 権限による非表示は P2-3 の仕組みを機械的に反映（再実装禁止）
- Frontend の権限制御ロジックは変更しない

**設計憲法の再確認**:
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 暗黙補完（デフォルト値の自動付与等）を禁止
- 失敗・未設定・未対応を隠さない

---

## 2. 目的（What）

法人横断契約一覧（group）を **"業務で使える検索・一覧"** として安定化する。

- P2-3 まで成立した認証/認可/権限制御/表示制御を壊さず、業務機能・UX・運用観点を段階的に前進
- 失敗・未設定・未対応を隠さない（設計憲法の再確認）
- 「実装できるがまだやらないこと」を分離

**P2-4 の位置づけ**: P1-B0 で確定した最低限の検索条件・表示項目を実装・成立させるフェーズ。

---

## 3. スコープ（やること）

### 3.1 検索条件・UI入力の整備（業務で使える最低限）

**条件項目の固定**:
- P1-B0 で確定した検索条件を実装
- 検索条件の一覧は [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md) の「3. 検索条件（query param）」を正とする
- 追加検索条件の候補（3.2）は業務合意後に実装

**入力値取り扱い**:
- 空文字→未指定（空文字は検索条件として扱わない）
- trim（前後の空白を除去）
- maxLength（最大文字数制限、UIで補助）
- 文字種は「UIで補助」止まり（業務判断は禁止）
- バリデーションエラー時は 400 Bad Request

**初期表示**:
- 初期表示は自動検索しない（明示検索のみ）
- 検索条件未入力時は検索ボタンを disabled にする（任意）

### 3.2 一覧表示の成立条件固定

**表示項目・空値表現・桁あふれ**:
- 表示項目は P1-B0 で確定した MUST/SHOULD 項目を正とする
- 空値（NULL）は適切に表示する（「-」「未設定」等、UIで明示）
- 桁あふれは適切に処理する（省略表示等、UIで明示）

**権限による非表示**:
- 権限による非表示は P2-3 の仕組みを機械的に反映（再実装禁止）
- P2-3 で実装した権限制御ロジックをそのまま使用

**取得失敗（HTTP 4xx/5xx）の表示方針**:
- 握りつぶし禁止（エラーを隠さない）
- 画面上で失敗種別（認証切れ/権限不足/サーバ障害）が区別できる
- エラーメッセージは適切に表示する（相関ID等を含む）

### 3.3 API契約の安定化（P2-3 を壊さない）

**params/page/size/上限/ソート順固定**:
- params は P1-B0 で確定した検索条件を正とする
- page のデフォルト: 0、バリデーション: `>= 0`
- size のデフォルト: 20、上限: 100、バリデーション: `> 0` かつ `<= 100`
- 上限超過時は 400 Bad Request
- ソート順は P1-B0 で確定した許可キー・デフォルト・方向を正とする
- 不正なソートキー指定時は 400 Bad Request

**Search と Count の整合**:
- 同条件で同件数が返ることを保証
- COUNT SQL は FROM/WHERE を search SQL と完全一致させる（P04-2 のルール）

### 3.4 運用観点の最低限

**画面上で失敗種別が区別できる**:
- 認証切れ（401 Unauthorized）
- 権限不足（403 Forbidden）
- サーバ障害（500 Internal Server Error）
- その他（400 Bad Request、404 Not Found）

**バックエンドログで追える最小限**:
- 相関ID等、既存方針に従う
- エラーログは適切に出力する

**過負荷を避ける最低限制約**:
- size上限（100）を実装済み
- その他の制約は必要に応じて追加

---

## 4. 非スコープ（今やらないこと）

**必須：明示**:

- **実務項目の精査・追加**: P2-5 で実施（P1-B0 最低限を土台として、実務に合わせた項目を追加）
- **追加項目に必要なJOIN段階的復活**: P2-5 で実施
- **高度検索（名寄せ/同義語/ゆらぎ吸収 等）**: 将来の拡張として検討
- **ユーザー選択ソート（動的ソート）**: P2-5 で検討（QueryBuilder 導入時に）
- **一覧からの編集・更新系**: 別フェーズで検討
- **パフォーマンス深掘り（インデックス/統計/SQL本格チューニング）**: P2-5 で実施
- **監査ログ最終要件確定**: P2-6 で実施（ただし最低限の運用導線は P2-4 に置く）

---

## 5. Done 条件（チェックリスト形式）

### 5.1 検索条件・UI入力の整備

- [ ] 検索条件が P1-B0 で確定した項目に固定されている（docs に明文化されている）
- [ ] 入力値取り扱い（空文字→未指定、trim、maxLength）が実装されている
- [ ] 初期表示は自動検索しない（明示検索のみ）が実装されている
- [ ] バリデーションエラー時は 400 Bad Request が返る

### 5.2 一覧表示の成立条件固定

- [ ] 表示項目・空値表現・桁あふれが固定されている
- [ ] 権限による非表示は P2-3 の仕組みを機械的に反映されている（再実装していない）
- [ ] 取得失敗（HTTP 4xx/5xx）の表示方針が確定している（握りつぶしていない）
- [ ] 画面上で失敗種別（認証切れ/権限不足/サーバ障害）が区別できる

### 5.3 API契約の安定化

- [ ] API契約（params/page/size/上限/ソート順）が固定されている
- [ ] Search と Count の整合が取れている（同条件で同件数）
- [ ] 不正なパラメータ時は 400 Bad Request が返る

### 5.4 運用観点の最低限

- [ ] バックエンドログで追える最小限（相関ID等）が実装されている
- [ ] 過負荷を避ける最低限制約（size上限等）が実装されている

### 5.5 設計憲法準拠

- [ ] 暗黙補完をしていない（設計憲法準拠）
- [ ] エラーを握りつぶしていない（設計憲法準拠）
- [ ] Frontend は業務判断をしていない（設計憲法準拠）

### 5.6 ドキュメント化

- [ ] 上記がすべて docs に明文化されている

---

## 6. 実装成果物

### 6.1 Backend

- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql` の拡張（検索条件追加）
- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_count.sql` の拡張（検索条件追加）
- `backend/nexus-group/src/main/kotlin/nexus/group/query/GroupContractSearchCondition.kt` の拡張
- `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt` の拡張（バリデーション追加）

### 6.2 Frontend

- `frontend/src/modules/group/components/GroupContractSearchForm.tsx` の拡張（検索条件追加）
- エラーハンドリングの改善（失敗種別の区別）

### 6.3 ドキュメント

- 本ロードマップ（p2-4-search-condition-expansion-roadmap.md）
- 実装完了時の完了宣言ドキュメント（p2-4-completion.md）

---

## 7. 検証観点

### 7.1 機能検証

- 検索条件が正しく動作する（P1-B0 で確定した条件）
- 入力値取り扱い（空文字→未指定、trim、maxLength）が正しく動作する
- 初期表示は自動検索しない（明示検索のみ）
- 一覧表示が正しく動作する（表示項目・空値表現・桁あふれ）
- 権限による非表示が正しく動作する（P2-3 の仕組みを機械的に反映）
- エラーハンドリングが正しく動作する（失敗種別の区別）

### 7.2 回帰テスト

- P2-3 で実装した権限制御が壊れていない
- P2-2 で実装した基本機能が壊れていない
- P1-B1/B2 で実装した JOIN が壊れていない

### 7.3 設計憲法準拠確認

- 暗黙補完をしていない
- エラーを握りつぶしていない
- Frontend は業務判断をしていない

---

## 8. 次フェーズ（P2-5）への引き継ぎ

P2-4 完了後、P2-5 で実施する事項：

- **実務項目の精査と追加**: P1-B0 最低限を土台として、実務に合わせた検索条件・一覧表示項目を精査・追加
- **追加項目に必要なJOIN段階的復活**: 追加項目の根拠となるJOINを段階的に復活（P1-B1 の延長）
- **QueryBuilder/条件組み立ての規律強化**: NULL吸収ORを避け、条件があるものだけWHEREに入れる方針を徹底
- **性能・運用の深掘り**: explain/計測/索引/統計を含め、実務性能に合わせて改善

詳細は [p2-5-performance-optimization-roadmap.md](./p2-5-performance-optimization-roadmap.md) を参照。

---

## 9. 参照

- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（検索条件根拠）
- [p2-2-manual-verification-group-contract-list.md](./p2-2-manual-verification-group-contract-list.md)（エラー検証が別立て継続であること）
- [p2-5-performance-optimization-roadmap.md](./p2-5-performance-optimization-roadmap.md)（P2-5 詳細ロードマップ）
- [p2-5-jdbc-querybuilder-guideline.md](./p2-5-jdbc-querybuilder-guideline.md)（P2-5へ送る事項の境界）
- [p2-3-completion.md](./p2-3-completion.md)（P2-3 完了宣言）
- [nexus-design-constitution.md](./nexus-design-constitution.md)（設計憲法）

---

## 10. 注意事項

- P2-3 を壊さない前提（認証/認可/権限制御/表示制御は成立済み）
- 「Frontendは業務判断しない」「状態を隠さない」「エラーを握りつぶさない」を必ず遵守
- 「今やらないこと」を明記（実務項目の精査・追加、追加JOIN、高度検索、動的ソート、編集・更新系、パフォーマンス深掘り、監査ログ最終要件確定）
- 実装順序は **SQL → DTO → RowMapper → Controller → Frontend** の順で実施（設計憲法に従う）

---

以上。

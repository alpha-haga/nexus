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
| P1-B0 | 完了 | 表示要件確定（ドキュメント化済み） |
| P1-B1 | 完了 | JOIN 段階的復活完了 |
| P1-B2 | 完了 | Count/Search 最適化完了 |
| P1-B3 | P2-5 で実施 | パフォーマンス最適化 |

### 3.2 P1 未完了項目の扱い

- **P1-B0**: 完了済み（`p1-b0-group-contract-display-requirements.md` が存在）
- **P1-B3**: P2-5 で実施（原則 P2 送り、業務が死ぬ場合のみ P1 繰上げ可能）

---

## 4. P2 サブフェーズ一覧

| フェーズ | 名称 | 目的 | 状態 |
|---------|------|------|------|
| P2-1 | Frontend 認証・認可統合 | NextAuth と Keycloak の統合 | 完了 |
| P2-2 | Frontend 本格接続 | Group Contract List 画面の完成 | 次着手 |
| P2-3 | 権限制御反映 | Frontend 側での権限制御 | 未着手 |
| P2-4 | 検索条件拡張 | 追加検索条件の実装 | 未着手 |
| P2-5 | パフォーマンス最適化 | P1-B3 の実施 | 未着手 |
| P2-6 | 本番運用前最終調整 | ログ・監査・セキュリティ | 未着手 |

**順序**: 前フェーズ完了後に次フェーズに着手（P2-1 → P2-2 → ... → P2-6）

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
- エラーハンドリングの実装（400/403/404/500 の適切な表示）
- ローディング状態の表示
- 検索条件のバリデーション（Frontend 側）
- ソート機能の実装（P1-B0 で確定したソート仕様に従う）

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

- [ ] Group Contract List 画面が正常に表示される
- [ ] 検索条件を指定して検索できる
- [ ] ページネーションが動作する
- [ ] エラーハンドリングが適切に実装されている（400/403/404/500）
- [ ] ローディング状態が適切に表示される
- [ ] ソート機能が動作する（P1-B0 で確定した仕様に従う）
- [ ] E2E 検証（Frontend → Backend BFF → API）が成立する

### 6.6 参照

- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)
- [p1-b1-completion.md](./p1-b1-completion.md)
- [p1-b2-completion.md](./p1-b2-completion.md)

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

- [ ] Keycloak token の claim（`nexus_db_access`）を Frontend 側で取得できる
- [ ] 権限に応じた画面表示制御が実装されている
- [ ] 権限不足時に適切なエラー表示がされる（403 Forbidden）
- [ ] 権限情報が適切に表示される（必要に応じて）
- [ ] E2E 検証（Frontend → Keycloak → Backend BFF → API）が成立する

### 7.6 参照

- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)
- [p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md)

---

## 8. P2-4: 検索条件拡張

### 8.1 目的

P1-B0 で確定した追加検索条件を実装し、検索機能を拡張する。

### 8.2 スコープ

P1-B0 で確定した追加検索条件の実装（業務合意後に確定）：
- 法人コード検索（`companyCd`）
- 氏名（漢字）検索（`familyNameGaiji`, `firstNameGaiji`）
- 氏名（カナ）検索（`familyNameKana`, `firstNameKana`）
- 住所検索（`addr1`, `addr2`, `zipCd` 等）
- 契約状態での絞り込み（`contractStatusKbn` が有効化された場合）

実装順序：
- SQL の拡張（WHERE 句の追加）
- DTO / RowMapper の確認（変更不要の想定）
- Controller の拡張
- Frontend 検索フォームの拡張

### 8.3 非スコープ

- P1-B0 で確定していない検索条件の追加（業務合意が必要）
- JOIN 復活（P1-B1 で完了済み、追加 JOIN は別フェーズ）

### 8.4 実装成果物

- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_search.sql` の拡張
- `backend/nexus-infrastructure/src/main/resources/sql/group/group_contract_count.sql` の拡張
- `backend/nexus-group/src/main/kotlin/nexus/group/query/GroupContractSearchCondition.kt` の拡張
- `backend/nexus-bff/src/main/kotlin/nexus/bff/controller/GroupContractSearchController.kt` の拡張
- `frontend/src/modules/group/components/GroupContractSearchForm.tsx` の拡張

### 8.5 Done 条件

- [ ] P1-B0 で確定した追加検索条件が実装されている
- [ ] SQL / DTO / RowMapper / Controller の整合が取れている
- [ ] Frontend 検索フォームに追加検索条件が反映されている
- [ ] 追加検索条件での検索が正常に動作する
- [ ] 回帰テストが完了している（既存機能が壊れていない）

### 8.6 参照

- [p1-b0-group-contract-display-requirements.md](./p1-b0-group-contract-display-requirements.md)（3.2 追加検索条件の候補を参照）

### 8.7 注意事項

- 追加検索条件は業務合意後に確定する
- 検索条件の追加は **SQL → DTO → RowMapper → Controller → Frontend** の順で実装する（設計憲法に従う）

---

## 9. P2-5: パフォーマンス最適化（P1-B3 の実施）

### 9.1 目的

実データ量・権限スコープ前提でパフォーマンスを調整し、本番運用に耐える性能を確保する。

### 9.2 スコープ

実データ量を前提としたチューニング：
- インデックス追加の検討（`contract_no`、`family_nm_kana`、`search_tel_no` 等）
- 検索方式の見直し（中間一致から前方一致への変更検討）
- 日付範囲のデフォルト設定（全件取得を避けるため）

権限スコープを考慮した最適化：
- パフォーマンス測定の実施（実データ量前提）
- 許容時間内で動作することを確認

### 9.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- JOIN の削除（P1-B1 で復活した JOIN は維持）

### 9.4 実装成果物

- インデックス追加 SQL（必要に応じて）
- パフォーマンス測定結果ドキュメント
- 最適化実施内容の記録

### 9.5 Done 条件

- [ ] 計測根拠あり（推測禁止）
- [ ] 実データ量・権限スコープ前提で許容時間内で動作する
- [ ] パフォーマンス要件が満たされている
- [ ] パフォーマンス測定結果が記録されている

### 9.6 参照

- [p1-b2-completion.md](./p1-b2-completion.md)（現状のパフォーマンス測定結果を参照）

### 9.7 注意事項

- P1-B2 で測定した結果（平均1.6秒、最長2.6秒）を基準とする
- パフォーマンス要件は業務合意後に確定する
- インデックス追加は DB 管理者と合意の上で実施する

---

## 10. P2-6: 本番運用前の最終調整

### 10.1 目的

本番運用に向けた最終調整を実施し、運用準備を完了する。

### 10.2 スコープ

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

### 10.3 非スコープ

- アーキテクチャの変更（設計憲法に反する変更は禁止）
- 機能追加（P2-1〜P2-5 で完了した機能以外の追加）

### 10.4 実装成果物

- ログ出力設定（必要に応じて）
- 監査ログ実装（必要に応じて）
- エラーハンドリング最終確認ドキュメント
- パフォーマンス要件最終確認ドキュメント
- セキュリティ要件最終確認ドキュメント
- 運用前提整理ドキュメント

### 10.5 Done 条件

- [ ] ログ・監査ログの要件が整理されている（必要に応じて実装されている）
- [ ] エラーハンドリングが最終確認されている（200/400/403/404/500 の境界が明確）
- [ ] パフォーマンス要件が最終確認されている（許容時間内で動作することを確認）
- [ ] セキュリティ要件が最終確認されている（認証・認可・権限制御が適切に動作することを確認）
- [ ] 運用前提が整理されている（デプロイ手順・環境変数・監視項目が明文化されている）

### 10.6 参照

- [nexus-design-constitution.md](./nexus-design-constitution.md)（6. Fail Fast とエラーの意味を参照）
- [P1-A2-COMPLETION.md](./P1-A2-COMPLETION.md)（エラーの意味づけを参照）

### 10.7 注意事項

- 本フェーズは「最終調整」であり、大幅な機能追加は行わない
- 運用前提の整理は本番運用開始前に必須

---

## 11. 依存関係

```
P2-1 → P2-2 → P2-3 → P2-4 → P2-5 → P2-6
```

- 前フェーズ完了後に次フェーズに着手する（順序固定）
- P2-5 と P2-6 は並行可能（ただし P2-4 完了後）

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
- Cursor / Agent 実行時は「現在地（P2-X）」を明示して開始する

以上。

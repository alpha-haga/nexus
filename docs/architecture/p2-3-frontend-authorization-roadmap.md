# P2-3: Frontend 権限制御ロードマップ

本ドキュメントは、P2-3（Frontend 権限制御反映）フェーズにおける詳細ロードマップを定義する。

**本書の位置づけ**: P2-3 実装の正本。P2-3 の実装は本書に従って進める。

**設計の正（参照必須）**:
- docs/architecture/nexus-design-constitution.md
- docs/architecture/nexus-project-roadmap.md
- docs/architecture/p04-5-keycloak-claims-db-routing.md
- docs/architecture/p1-1-bff-authorization-implementation.md

---

## 1. P2-3 の目的（What）

Backend BFF の権限制御（Keycloak Claim による Region / Corporation / DomainAccount 制御）を Frontend 側に反映し、権限に応じた画面表示制御を実現する。

---

## 2. 前提（事実・再解釈禁止）

- 認証（NextAuth×Keycloak）は P2-1 で成立済み
- Backend BFF の認可・Context set は P1-A1 で成立済み
- Keycloak token claim（`nexus_db_access`）の設計は P04-5 で確定済み
- Group Contract List 画面の基本機能は P2-2 で成立済み（ただし P2-2-0 は別チャットで継続）
- **Backend 仕様変更は行わない**（P2-3 は Frontend 側の実装のみ）
- **Frontend は認可判断しない**（Backend BFF の claim を機械的に表示制御へ反映するだけ）
- **状態と失敗を隠さない**（設計憲法に準拠）
- **Fail Fast**（未許可状態は UI で明示し、握りつぶさない）

---

## 3. 禁止事項（P2-3 固有）

- Backend BFF の仕様変更（P2-3 は Frontend 側の実装のみ）
- Region セレクターの変更（P2-2 で実装した暫定UIは P2-3 では触らない）
- 権限判定ロジックの再実装（Backend BFF の実装を正とする）
- 業務判断の実装（Frontend は業務判断をしない）

---

## 3.5 nexus_db_access claim の取り扱い

**Claim 形式**:
- Claim 名: `nexus_db_access`
- Claim 型: `List<String>`（配列）
- 各要素の形式: `"{region}__{corporation}__{domainAccount}"`（区切り文字は `__`（アンダースコア2つ））

**Claim 要素の例**:
- `saitama__musashino__GOJO`
- `saitama__musashino__FUNERAL`
- `fukushima__fukushima__GOJO`
- `integration__ALL__GROUP`

**DomainAccount の表記規則**:
- DomainAccount は大文字固定（GOJO / FUNERAL / GROUP）
- `nexus_db_access` の値は client role 名そのまま（変換/接頭辞付与なし）

**Parse 失敗時の扱い**:
- claim が存在しない、または空配列の場合 → errors に記録し、権限情報なしとして扱う
- claim 要素の形式が不正な場合（区切り文字が `__` でない、要素数が3でない等） → errors に記録し、該当要素を無視する
- parse 失敗は握りつぶさず、errors に残す（設計憲法「状態を隠さない」に準拠）

**参照**: [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) の「3. Keycloak Claim 設計（nexus_db_access）」を正とする

---

## 3.6 ルート→必要権限（DomainAccount）マッピング表

Frontend 側で権限表示制御を行う際の、ルート（URL prefix）と必要 DomainAccount のマッピング表。

| URL prefix | 必要 DomainAccount | 備考 |
|------------|-------------------|------|
| `/gojo/**` | `GOJO` | nexus-gojo ドメイン |
| `/funeral/**` | `FUNERAL` | nexus-funeral ドメイン |
| `/group/**` | `GROUP` | integration 専用（GROUP は role 文字列上の予約語） |
| `/points/**` | `POINTS` | 暫定（存在しない/実値不明なら「token実値に合わせる」を明記） |
| `/customers/points` | `POINTS` | 暫定（存在しない/実値不明なら「token実値に合わせる」を明記） |
| `/admin/**` | 暫定 | 存在しない/実値不明なら「token実値に合わせる」を明記 |
| `/agents/**` | 暫定 | 存在しない/実値不明なら「token実値に合わせる」を明記 |
| `/billing/**` | 暫定 | 存在しない/実値不明なら「token実値に合わせる」を明記 |
| `/reports/**` | 暫定 | 存在しない/実値不明なら「token実値に合わせる」を明記 |
| `/customers/search` | 暫定 | 後続で確定させることを明記 |
| `/customers/households` | 暫定 | 後続で確定させることを明記 |

**重要**: 
- このマッピング表は「フロント独自の権限ロジックを増殖させない」ための唯一の固定点として扱う
- "これから決める" ではなく "この docs が正" として固定する
- 暫定項目は後続フェーズで確定させる

**参照**: [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) の「5. DomainAccount 決定規則」を正とする

---

## 4. 非スコープ（P2-3 では実施しない）

- **P2-2-0（エラーハンドリング検証：400/403/404/500）**: 別チャットで継続
- **Region セレクターの変更**: P2-2 で実装した暫定UIは P2-3 では触らない
- **Backend BFF の仕様変更**: Backend 仕様変更は行わない
- **認証実装**: P2-1 で完了済み
- **Keycloak 設定変更**: P1-A0 で完了済み

---

## 5. P2-3 サブタスク一覧（依存順）

> 各サブタスクは「目的 / スコープ / Done 条件」を必ず満たす。
> Done を満たさないまま次へ進むことは禁止。

### P2-3-1: Keycloak token claim の取得・解析

**目的**: Frontend 側で Keycloak token の claim（`nexus_db_access`）を取得し、解析できるようにする。

**スコープ**:
- NextAuth.js の session から Keycloak token を取得
- token の claim（`nexus_db_access`）を取得・解析
- claim の形式（`List<String>`）を Frontend 側で扱える形に変換
- claim の解析ロジック（Region / Corporation / DomainAccount の抽出）を実装
- parse 失敗時の errors 記録（claim が存在しない、空配列、形式不正等）

**実装成果物**:
- `frontend/src/services/auth.ts` の claim 取得・解析機能
- 権限情報の型定義（必要に応じて）
- parse 失敗時の errors 記録ロジック

**Done 条件**:
- session から Keycloak token の claim（`nexus_db_access`）を Frontend 側で取得できる
- claim の解析ロジックが実装されている（Region / Corporation / DomainAccount の抽出）
- 解析結果が型安全に扱える（TypeScript の型定義が存在する）
- parse 失敗時は errors に記録され、握りつぶさない（設計憲法「状態を隠さない」に準拠）

---

### P2-3-2: 権限情報の表示（現在の権限状態の明示）

**目的**: ユーザーが現在の権限状態（Region / Corporation / DomainAccount）を把握できるようにする。

**スコープ**:
- 権限情報（Region / Corporation / DomainAccount）を画面に表示する
- 権限情報の表示場所・表示形式を決定する
- 権限情報が取得できない場合の表示を実装する

**実装成果物**:
- 権限情報表示コンポーネント（必要に応じて）
- 権限情報の表示ロジック

**Done 条件**:
- 権限情報（Region / Corporation / DomainAccount）が画面に表示される
- 権限情報が取得できない場合の表示が実装されている
- 権限情報の表示が設計憲法（状態を隠さない）に準拠している

**注意事項**:
- Region セレクター（P2-2 で実装した暫定UI）は変更しない
- 権限情報の表示は「現在の権限状態を明示する」目的であり、業務判断ではない

---

### P2-3-3: 権限に応じた画面表示制御

**目的**: 権限に応じて画面の表示を制御する（メニュー表示・機能アクセス制御）。

**スコープ**:
- 権限に応じたメニュー表示制御
- 権限に応じた機能アクセス制御（必要に応じて）
- 権限不足時の UI 表示制御

**実装成果物**:
- 権限制御コンポーネント（メニュー表示制御等）
- 権限制御フック（必要に応じて）

**Done 条件**:
- 権限に応じた画面表示制御が実装されている
- 権限不足時の UI 表示が適切に実装されている
- 権限制御が設計憲法（業務判断をしない）に準拠している

**注意事項**:
- Frontend は業務判断をしない（Backend BFF の権限制御を反映するのみ）
- 権限制御は「表示制御」であり、認可判定ではない（認可判定は Backend BFF で実施）

---

### P2-3-4: 権限不足時のエラー表示（403 Forbidden）

**目的**: Backend BFF から 403 Forbidden が返された場合に、適切なエラー表示を行う。

**スコープ**:
- 403 Forbidden エラーの検知
- 403 Forbidden エラーの表示（エラーメッセージ・UI 表示）
- 権限不足時のユーザーへの案内

**実装成果物**:
- 403 Forbidden エラー表示コンポーネント（必要に応じて）
- エラーハンドリングロジックの拡張

**Done 条件**:
- 403 Forbidden エラーが適切に検知される
- 403 Forbidden エラーが適切に表示される（エラーメッセージ・UI 表示）
- 権限不足時のユーザーへの案内が実装されている

**注意事項**:
- 403 Forbidden エラーの判定は Backend BFF で実施される（Frontend は表示のみ）
- エラー表示は設計憲法（状態を隠さない）に準拠する

---

### P2-3-5: 検証（E2E / 手動）とドキュメント更新

**目的**: 実装の成立を「検証可能な形」で固定する。

**スコープ**:
- 手動検証手順（権限情報の表示、権限制御、403 エラー）を docs に明文化
- E2E 観点（Frontend→Keycloak→BFF→DB）のチェック項目を docs に明文化

**Done 条件**:
- P2-3 の手動検証手順が docs に明文化されている
- P2-3 の E2E 観点のチェックが docs に明文化されている

---

## 6. P2-3 の完了条件（フェーズ Done）

- session から Keycloak token の claim（`nexus_db_access`）を取得できる
- claim の解析ロジックが実装されている（Region / Corporation / DomainAccount の抽出）
- parse 失敗時は errors に記録され、握りつぶさない
- 権限情報（Region / Corporation / DomainAccount）が画面に表示される
- 権限に応じた画面表示制御が実装されている（ルート→必要権限マッピング表に基づく）
- 権限不足時に 403 Forbidden エラーが適切に表示される
- Region セレクターは変更されていない（P2-2 暫定UIのまま）
- build が通過する（TypeScript の型エラーがない）
- 検証ドキュメント（手動/E2E）が更新されている

---

## 7. 推奨順序

1. P2-3-1（Keycloak token claim の取得・解析）
2. P2-3-2（権限情報の表示）
3. P2-3-3（権限に応じた画面表示制御）
4. P2-3-4（権限不足時のエラー表示）
5. P2-3-5（検証ドキュメント更新）

---

## 8. 次フェーズ引き継ぎ

P2-3 完了後の次フェーズ（P2-4 以降）への引き継ぎ事項：

- **権限情報の表示**: P2-3 で実装した権限情報表示は、P2-4 以降でも維持する
- **権限制御**: P2-3 で実装した権限制御は、P2-4 以降でも維持する
- **Region セレクター**: P2-2 で実装した暫定UIは、P2-3 では触らないが、P3 以降で変更の可能性がある（別フェーズで検討）

---

## 9. 参照

- [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md)（Keycloak Claim 設計）
- [p1-1-bff-authorization-implementation.md](./p1-1-bff-authorization-implementation.md)（BFF 認可実装）
- [p2-2-frontend-group-contract-list-roadmap.md](./p2-2-frontend-group-contract-list-roadmap.md)（P2-2 ロードマップ）

---

## 10. P2-3 実装完了確認チェックリスト

本チェックリストは、P2-3 の実装が完了条件を満たしていることを確認するための手動確認観点である。

### 10.1 Session と Claim 取得の確認

- [ ] NextAuth session に `accessToken` が存在する
- [ ] NextAuth session に `dbAccess` が存在する（`DbAccessClaims` 型）
- [ ] NextAuth session に `dbAccessRaw` が存在する（`string[]` 型）
- [ ] `accessToken` から `nexus_db_access` claim が正しく抽出される
- [ ] claim が存在しない場合、`dbAccess.errors` に「nexus_db_access claim が JWT payload に存在しません」が記録される
- [ ] claim が空配列の場合、`dbAccess.errors` に「nexus_db_access claim が空配列です」が記録される
- [ ] claim の parse 失敗時、`dbAccess.errors` に適切なエラーメッセージが記録される

### 10.2 権限判定ロジックの確認

- [ ] `permissionUtils.ts` の `getScreenPermissionFromSession` が正しく動作する
- [ ] `useScreenPermission` Hook が正しく動作する
- [ ] `SCREEN_PERMISSIONS` マッピングが正しく定義されている
- [ ] 未認証（`status === 'loading'` または `!session`）の場合、`canView: false` が返される
- [ ] 権限情報の取得エラーがある場合、`canView: false` と適切な `reason` が返される
- [ ] `any: true` の画面は認証済みユーザー全員がアクセス可能
- [ ] `denyAll: true` の画面は全ユーザーに拒否される
- [ ] `domain` 指定の画面は、該当 DomainAccount を持つ role が存在する場合のみアクセス可能
- [ ] `integrationOnly: true` の画面は、`integration` region かつ該当 DomainAccount を持つ role が存在する場合のみアクセス可能

### 10.3 メニュー表示制御の確認

- [ ] `navConfig.tsx` の各メニュー項目に `screenKey` が設定されている
- [ ] Sidebar で権限がないメニュー項目は `disabled` 表示（`opacity-50` または `opacity-75`）になる
- [ ] Sidebar で権限がないメニュー項目の `title` 属性に拒否理由（`reason`）が表示される
- [ ] Sidebar で親メニュー（children あり）は権限不足でも展開可能（`disabled` 属性を付けない）
- [ ] Sidebar で子メニューは権限がない場合 `Link` ではなく `div` で表示される
- [ ] Sidebar で子メニューのインデント（`ml-6`）が常に適用されている

### 10.4 ページ表示制御の確認

- [ ] `/group/contracts` ページで、権限がない場合 `Forbidden` コンポーネントが表示される
- [ ] `/group/persons` ページで、権限がない場合 `Forbidden` コンポーネントが表示される
- [ ] `/gojo` ページで、権限がない場合 `Forbidden` コンポーネントが表示される
- [ ] `/funeral` ページで、権限がない場合 `Forbidden` コンポーネントが表示される
- [ ] 認証中（`status === 'loading'`）の場合、ページでローディング表示がされる（`Forbidden` を出さない）
- [ ] 未認証（`!session`）の場合、ページで `null` を返し、NextAuth の導線に任せる
- [ ] 直接 URL アクセス時も、権限がない場合は `Forbidden` が表示される（メニュー抑止だけで終わらない）

### 10.5 403 Forbidden UI の確認

- [ ] `Forbidden` コンポーネントに「権限がありません（403）」が明示されている
- [ ] `Forbidden` コンポーネントに対象画面名（`screenName`）が表示される
- [ ] `Forbidden` コンポーネントに拒否理由（`reason`）が表示される（存在する場合）
- [ ] `Forbidden` コンポーネントに「管理者に連絡してください」の案内が表示される
- [ ] `Forbidden` コンポーネントに「ダッシュボードに戻る」の導線が表示される

### 10.6 ビルドと型安全性の確認

- [ ] `npm run build` が成功する
- [ ] TypeScript の型エラーがない
- [ ] ESLint エラーがない（設定されている場合）

### 10.7 Region セレクターの確認

- [ ] Region セレクター（P2-2 暫定UI）に変更が入っていない
- [ ] Region セレクター関連のファイルに差分がない（git diff で確認）

### 10.8 設計憲法準拠の確認

- [ ] Frontend は認可判断をしていない（Backend claim を機械的に反映するだけ）
- [ ] 権限制御の判定ロジックが 1箇所（`permissionUtils.ts`）に集約されている
- [ ] エラーを握りつぶしていない（`console.log` のみ等は禁止）
- [ ] 状態を隠していない（権限不足・未設定を UI で明示）

---

## 補足

- 本ロードマップは P2-3 の実装ガイドであり、設計の正は nexus-design-constitution.md である
- P2-3 完了時に完了宣言ドキュメントを作成する
- Cursor / Agent 実行時は「現在地（P2-3）」を明示して開始する

以上。

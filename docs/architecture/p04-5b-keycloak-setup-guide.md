# P04-5b Keycloak 実設定手順書

本ドキュメントは、Keycloak 初学者でも手順通りに進められる **`nexus_db_access` claim の実設定手順** を提供するものです。

**前提**: 権限 Claim 設計の正は [p04-5-keycloak-claims-db-routing.md](./p04-5-keycloak-claims-db-routing.md) を参照してください。

---

## 1. 目的

本手順書の目的は以下です：

- Access token に `nexus_db_access`（配列）を出力する
- 値は client role の一覧を使う（運用が壊れにくい）
- BFF が claim を見て 403 / Context set するための前提を整える

**重要**: 設定値（claim 名、role 命名規約等）は本書で固定し、ブレさせない。

---

## 2. 用語（最小）

Keycloak 初学者向けに、本手順で使用する用語を一行ずつ説明します。

- **Realm**: Keycloak における「テナント」のような概念。ユーザー・クライアント・ロール等を管理する単位
- **Client**: アプリケーション（例: nexus-bff）を表す。OAuth2/OIDC の「クライアント」に相当
- **Client Role**: 特定の client に紐づくロール。本設計では DB アクセス権限を表現する
- **Group**: ユーザーをまとめる単位。group に role を付与することで、ユーザー管理を簡素化
- **Client Scope**: 複数の client で共有する設定（mapper 等）をまとめる単位
- **Protocol Mapper**: token に claim を追加する仕組み。本設計では client roles を claim に変換する
- **Access Token**: リソースサーバー（BFF）が受け取る token。本設計では `nexus_db_access` claim を含む

---

## 3. 事前確認チェックリスト

設定を開始する前に、以下を確認してください：

- [ ] 対象 Realm 名（例: `nexus`）
- [ ] 対象 Client 名（例: `nexus-bff`）
- [ ] どの環境（local/dev/stg/prod）を触っているか
- [ ] 既存の mapper / scope があるか（衝突防止）

**注意**: 本番環境のバックアップ・運用管理は本手順書の範囲外です。各環境の運用方針に従ってください。

---

## 4. Client Role 命名規約（固定）

**フォーマット**: `{region}__{corporation}__{domainAccount}`

**区切り文字**: `__`（アンダースコア2つ）

**例**:
- `saitama__musashino__GOJO`
- `saitama__musashino__FUNERAL`
- `integration__ALL__GROUP`

**禁止文字**: `:` `/` 空白（事故防止のため）

**DomainAccount の表記**: domainAccount は大文字固定（GOJO/FUNERAL/GROUP）

**ワイルドカード方針**:
- `ALL` は integration の corporation 不要表現としてのみ許可
- region 側で wildcard 禁止の理由: 事故防止のため。region 側で wildcard を許可すると、意図しない法人へのアクセスが発生する可能性がある

---

## 5. Client Role の作成（事前準備）

Group に role を割り当てる前に、Client Role を作成する必要があります。

### 5.1 Client Role を作成

1. 左メニューから **Clients** を選択
2. 対象 Client（例: `nexus-bff`）を選択
3. **Roles** タブを選択
4. **Create role** をクリック
5. 以下の設定を入力:
   - **Role name**: 命名規約に従って入力（例: `saitama__musashino__GOJO`）
6. **Save** をクリック

**確認ポイント**: Roles タブの一覧に、作成した role が表示されること

### 5.2 必要な Client Roles の例

最低限、以下の client roles を作成してください：

- `integration__ALL__GROUP`
- `saitama__musashino__GOJO`
- `saitama__musashino__FUNERAL`

**注意**: 実際の運用に合わせて、必要な role を追加してください。

---

## 6. 手順A（推奨）: Client Scope 方式で Mapper を追加

### 6.1 Client Scope を作成

1. Keycloak 管理画面にログイン
2. 対象 Realm（例: `nexus`）を選択
3. 左メニューから **Client scopes** を選択
4. **Create client scope** をクリック
5. 以下の設定を入力:
   - **Name**: `nexus-db-access`
   - **Protocol**: `openid-connect`
6. **Save** をクリック

**確認ポイント**: Client scopes の一覧に `nexus-db-access` が表示されること

### 6.2 Protocol Mapper を作成（目的: client roles を claim に出す）

**重要**: `nexus_db_access` の値は client role 名そのまま（変換/接頭辞付与なし）

1. 作成した `nexus-db-access` scope を開く
2. **Mappers** タブを選択
3. **Add mapper** → **By configuration** を選択
4. Mapper の種類を選択:
   - **選ぶ条件（2つとも満たすこと）**:
     1. 「ユーザーに割り当てられた *client roles* を token claim として出力する」機能を持つもの
     2. Mapper の説明に "client role" または "client roles" と明記されているもの
   - **採用しないもの**:
     - "User Realm Role" mapper（realm roles を出すため）
     - "Realm Role" mapper（realm roles を出すため）
     - "User Attribute" mapper（client roles を出さないため）
   - **UI 名称の例**（バージョンにより異なる）:
     - "User Client Role" mapper
     - "Client Role" mapper
     - "User Client Roles" mapper

5. 以下の設定値を入力（固定）:
   - **Name**: `nexus-db-access-mapper`（任意の名前で可）
   - **Claim name**: `nexus_db_access`（固定）
   - **Add to access token**: `ON`（必須）
   - **Add to ID token**: `OFF` または `ON`（BFF は access token を見るため、ID token は任意）
   - **Multivalued**: `ON`（配列として出力するため必須）
   - **Token Claim Name**: `nexus_db_access`（Claim name と同じ値）
   - **Client ID**: `nexus-bff`（選べる UI の場合）
   - **JSON type**: `String`（選べる UI の場合）

6. **Save** をクリック

**ありがちな間違いと回避**:
- ❌ Realm role を出す mapper を選ばない（Client role 用の mapper を選ぶ）
- ❌ ID token だけに出して access token に出さない（Add to access token を ON にする）
- ❌ Multivalued が OFF になっている（配列として出力するため ON にする）
- ❌ "Full scope allowed" 等の設定に依存しない（本手順では scope を明示的に割り当てる）

### 6.3 Client に scope を割り当て

1. 左メニューから **Clients** を選択
2. 対象 Client（例: `nexus-bff`）を選択
3. **Client scopes** タブを選択
4. **Default client scopes** セクションで **Add client scope** をクリック
5. `nexus-db-access` を選択して **Add** をクリック

**確認ポイント**: Default client scopes に `nexus-db-access` が表示されること

**注意**: Optional client scopes ではなく Default client scopes に追加すること（検証優先のため）

---

## 7. 手順B（代替）: Client 直付け Mapper

手順A（Client Scope 方式）が使えない場合（権限や運用事情）のみ、この手順を使用してください。

### 7.1 Client 直付け Mapper を作成

1. 左メニューから **Clients** を選択
2. 対象 Client（例: `nexus-bff`）を選択
3. **Mappers** タブを選択
4. **Add mapper** → **By configuration** を選択
5. 手順A の 6.2 と同じ mapper 種類を選択（Client role を出すもの）
6. 手順A の 6.2 と同じ設定値を入力

### 7.2 手順A との差

- **手順A**: Client Scope に mapper を定義し、複数の client で共有可能
- **手順B**: 特定の client に直接 mapper を定義（他の client では使えない）

### 7.3 なぜ代替か

- Client Scope 方式の方が運用しやすい（複数 client で共有可能）
- ただし、Keycloak の権限設定や運用方針により、Client Scope を作成できない場合がある
- その場合は Client 直付け方式を使用する

---

## 8. Role 付与（運用推奨は Group）

### 8.1 Group を作る（例）

1. 左メニューから **Groups** を選択
2. **Create group** をクリック
3. 以下の設定を入力:
   - **Name**: `corp-musashino`（例）
4. **Save** をクリック

**目的**: 人の入れ替えを group 管理に寄せる。ユーザー個別に role を付与するのではなく、group に role を付与し、ユーザーを group に所属させる。

### 8.2 Group に client role を割り当てる

1. 作成した group（例: `corp-musashino`）を選択
2. **Role mapping** タブを選択
3. **Assign role** をクリック
4. **Filter by clients** を選択
5. 対象 Client（例: `nexus-bff`）を選択
6. 以下の client roles を選択（例として最低限）:
   - `integration__ALL__GROUP`
   - `saitama__musashino__GOJO`
   - `saitama__musashino__FUNERAL`
7. **Assign** をクリック

**確認ポイント**: Role mapping の一覧に、選択した client roles が表示されること

### 8.3 User を group に所属させる

1. 左メニューから **Users** を選択
2. 対象ユーザーを選択
3. **Groups** タブを選択
4. **Join group** をクリック
5. 作成した group（例: `corp-musashino`）を選択
6. **Join** をクリック

**確認ポイント**: Groups タブに、選択した group が表示されること

---

## 9. 動作確認（必須）

### 9.1 Access Token を取得する

既存のログイン/認証導線を利用して access token を取得してください。

**例**: OAuth2/OIDC の認証フローを経由して access token を取得

### 9.2 Token をデコードして `nexus_db_access` を確認

1. 取得した access token をデコード（JWT デコーダーを使用）
2. 以下の点を確認:
   - `nexus_db_access` claim が存在すること
   - `nexus_db_access` が配列であること（文字列1個ではない）
   - role 名がそのまま入っていること（例: `["saitama__musashino__GOJO", "integration__ALL__GROUP"]`）

**確認ポイント**:
- Claim が配列形式で出力されていること
- Client role 名がそのまま claim 値として含まれていること

### 9.3 失敗時の典型原因と切り分け

#### Claim が出ない

**原因**: Scope/mapper が client に割り当てられていない

**確認**:
- Client scopes に `nexus-db-access` が追加されているか
- Mapper の "Add to access token" が ON になっているか

#### Claim が文字列1個（配列ではない）

**原因**: Multivalued が OFF

**確認**:
- Mapper の "Multivalued" 設定が ON になっているか

#### Realm roles が出る（Client roles ではない）

**原因**: Mapper 種別ミス（Realm role mapper を選んでいる）

**確認**:
- Mapper の種類が "Client Role" を出すものになっているか
- "User Realm Role" mapper を選んでいないか

#### Access token ではなく ID token にだけ出ている

**原因**: "Add to access token" が OFF

**確認**:
- Mapper の "Add to access token" 設定が ON になっているか

#### Role が claim に含まれない

**原因**: User に role が割り当てられていない、または group に role が割り当てられていない

**確認**:
- User が group に所属しているか
- Group に client role が割り当てられているか
- Client role が正しく作成されているか

---

## 10. 運用ルール（最低限）

### 10.1 Role 追加時のルール

- Role 追加は「命名規約」に従うこと
- 区切り文字は `__`（アンダースコア2つ）を使用
- 禁止文字（`:` `/` 空白）は使用しない

### 10.2 Integration 権限の扱い

- Integration 権限も明示的に付与すること（`integration__ALL__GROUP`）
- `ALL` 付与の乱用禁止（region 側では `ALL` は使用しない）

### 10.3 監査

- 付与した group/role の記録を残すこと
- 権限変更時は変更履歴を記録すること

**注意**: バックアップ・運用管理の詳細は本手順書の範囲外です。各環境の運用方針に従ってください。

---

## 11. Done 条件

以下の条件を満たした場合、設定完了とします：

- [ ] 任意ユーザーで token に `nexus_db_access` が配列で出力される
- [ ] 例の role（例: `saitama__musashino__GOJO`、`integration__ALL__GROUP`）が claim に含まれることを確認済み
- [ ] BFF 側で claim を読み取り、403 / Context set が正常に動作する（P1-1 実装後）

---

以上。本書が Keycloak 実設定手順の正本です。
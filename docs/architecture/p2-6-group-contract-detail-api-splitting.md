# P2-6: Group 契約詳細 TODO カードの API 分離設計（確定）

本書は P2-6 の成果物であり、P2-7 以降で契約詳細の TODO カード群を段階的に実装するための
**API 分離単位 / 命名規則 / SQL 粒度** を固定する。

設計憲法：

- Frontend は業務判断をしない（Backend が決めた結果をそのまま表示）
- 未実装を隠さない（TODO カードは常に表示）
- 「動いている風」禁止（未実装 API は 501 を返す、またはフロントから呼ばない）

---

## 1. 現状（P2-5 時点）

### 1-1. 既存 API

- 一覧検索
  - `GET /api/v1/group/contracts/search`
  - 返却：`PaginatedGroupContractResponse`

- 詳細（骨格：基本情報/契約者/連絡先/住所/契約状態）
  - `GET /api/v1/group/contracts/{cmpCd}/{contractNo}`
  - 返却：`GroupContractDetailResponse`

### 1-2. Frontend の TODO カード（未実装領域）

詳細画面 `frontend/src/app/group/contracts/[cmpCd]/[contractNo]/page.tsx` の未実装カード：

1. 契約内容
2. 担当者情報
3. 口座情報
4. 入金情報
5. 対応履歴

---

## 2. 分離方針（P2-6 で固定）

### 2-1. API 分離の基本

- **1責務 = 1エンドポイント**
- 既存の「詳細（骨格）」は維持する（責務を壊さない）
- TODO カードごとに **独立した API** を追加し、段階的に実装可能にする

### 2-2. URL 命名規則

- ベース：`/api/v1/group/contracts/{cmpCd}/{contractNo}`
- サブリソース：`/api/v1/group/contracts/{cmpCd}/{contractNo}/{subresource}`
- `subresource` は **snake / kebab を使わず lowerCamel**（既存の BFF route と統一）

例：

- `/api/v1/group/contracts/{cmpCd}/{contractNo}/contractContents`
- `/api/v1/group/contracts/{cmpCd}/{contractNo}/staff`

---

## 3. TODO カード ↔ API 対応（確定）

| UI カード | Endpoint | Response DTO（BFF） | Query DTO（group） | SQL（infrastructure） |
|---|---|---|---|---|
| 契約内容 | `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/contractContents` | `GroupContractContractContentsResponse` | `GroupContractContractContentsDto` | `group_contract_contract_contents.sql` |
| 担当者情報 | `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/staff` | `GroupContractStaffResponse` | `GroupContractStaffDto` | `group_contract_staff.sql` |
| 口座情報 | `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/bankAccount` | `GroupContractBankAccountResponse` | `GroupContractBankAccountDto` | `group_contract_bank_account.sql` |
| 入金情報 | `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/payments` | `GroupContractPaymentsResponse` | `GroupContractPaymentDto`（List） | `group_contract_payments.sql` |
| 対応履歴 | `GET /api/v1/group/contracts/{cmpCd}/{contractNo}/activityHistory` | `GroupContractActivityHistoryResponse` | `GroupContractActivityHistoryDto`（List） | `group_contract_activity_history.sql` |

補足：

- 「入金情報」「対応履歴」は **List 形式**（ページング要否は P2-7 で確定）
- 実装順序は任意（UI カードは常に表示し、該当 API が未実装の間は 501 または未接続のまま）

---

## 4. SQL 命名規則（確定）

### 4-1. 置き場所

- `backend/nexus-infrastructure/src/main/resources/sql/group/` のみ

### 4-2. 命名

- 既存と同じく `group_contract_*.sql`
- 単数/複数は「返却構造」に合わせる
  - 1 行想定：単数（例：`group_contract_staff.sql`）
  - 複数行想定：複数（例：`group_contract_payments.sql`）

---

## 5. 実装単位（コード構造の固定）

### 5-1. group（query）側

- 各サブリソースごとに DTO と QueryService Interface を定義する（P2-6）
- 実装は infrastructure に置く（P2-7 以降）

### 5-2. BFF 側

- Controller は 1 サブリソース 1 Controller（または 1 Controller 内で明確に分割）
- 未実装時の挙動は以下のどちらかで **必ず明示**する
  - Controller 未作成（Frontend が呼ばない）
  - Controller は作成し 501 Not Implemented を返す

---

## 6. 本書の変更禁止事項

- TODO カードと API の 1:1 対応を崩すこと
- SQL の置き場所を infrastructure 以外へ移すこと
- 既存の `GET /api/v1/group/contracts/{cmpCd}/{contractNo}` の責務を肥大化させること

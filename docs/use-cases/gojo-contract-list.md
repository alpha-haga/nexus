# GOJO 契約一覧 API 仕様（Local / All）

本ドキュメントは、互助会（GOJO）における  
**契約一覧機能（Local / All）**の仕様を定義するものである。

本仕様は **読み取り専用（Read Only）** であり、  
**振る舞い（Behavior）のみ**を定義する。

DB 構成、永続化方式、フレームワーク詳細は **意図的に含めない**。

---

## 目的（Purpose）

以下 2 つのスコープで契約一覧を提供する。

- **Local（地区単位）**  
  業務現場向けのオペレーションビュー
- **All（法人横断）**  
  グループ全体での参照・分析・管理用途

---

## スコープ定義

### 1. Local 契約一覧（地区単位）

#### 意図（Intent）
単一地区（region）に属する契約を一覧表示する。  
主に **現場業務向け** の画面。

#### スコープ特性
- 地区単位
- 法人は地区により暗黙的に決定される
- オペレーション用途

#### UI ルート
/gojo/contracts/local

#### API
GET /api/v1/gojo/contracts/local

**API提供元（方針/移行先）**: nexus-bff（社内UI向けBFF）

#### リクエストパラメータ

| 名称 | 型 | 必須 | 備考 |
|----|----|----|----|
| regionId | string | 必須 | 地区ID |
| page | int | 必須 | 0始まり |
| size | int | 必須 | 許可値: 20 / 50 / 100 |

#### ルール
- `page` は `0以上` であること
- `size` は `20 / 50 / 100` のいずれか
- regionId は必須
- **Read Only**

---

### 2. All 契約一覧（法人横断）

#### 意図（Intent）
複数法人にまたがる契約を一覧表示する。  
**管理・分析用途** を想定。

#### スコープ特性
- 法人横断
- 法人指定は任意
- 管理・分析用途

#### UI ルート
/gojo/contracts/all

#### API
GET /api/v1/gojo/contracts/all

**API提供元（方針/移行先）**: nexus-bff（社内UI向けBFF）

#### リクエストパラメータ

| 名称 | 型 | 必須 | 備考 |
|----|----|----|----|
| corporationId | string | 任意 | 未指定時は全法人 |
| page | int | 必須 | 0始まり |
| size | int | 必須 | 許可値: 20 / 50 / 100 |

#### ルール
- `page` は `0以上` であること
- `size` は `20 / 50 / 100` のいずれか
- `corporationId` は **任意**
- **追加のガード条件は禁止（ALL は明示的に許可）**
- **Read Only**

---

## 共通ページネーションルール

- ページネーションは **必須**
- ページサイズは固定値のみ許可  
- 20 / 50 / 100
- 最大 size は `100`
- ソート順は実装依存（本仕様では定義しない）

---

## アーキテクチャ制約（必須）

以下のルールは **必ず遵守すること**。

- Domain 層は Spring / JPA / JDBC に依存してはならない
- Repository インターフェースは Domain 層に配置する
- Repository 実装は Infrastructure 層に配置する
- API / Application 層で Spring Data JPA を直接使用しない
- nexus-api は DataSource / JPA 設定を所有しない
- 本ユースケースは **Read Only** とする

関連ドキュメント：
- `docs/architecture/design-principles.md`
- `docs/architecture/ai-rules.md`

---

## 非対象（Out of Scope）

以下は本仕様の対象外とする。

- 契約詳細画面
- 更新 / 解約 / 保留 等の操作
- 認可・権限制御
- 法人以外の検索条件
- DB スキーマ定義

---

## ステータス

- Local 契約一覧: 実装済み
- All 契約一覧: 実装済み
- **開発者への委譲可能状態**

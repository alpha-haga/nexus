# 契約一覧（Local / All / Group）概要仕様

## 目的
契約一覧を「地区業務」と「法人横断（統合DB）」で分け、接続先とスコープを明確化する。
本書は挙動仕様のみを定義し、DBテーブル定義や実装方式（View/Union）は別途とする。

---

## 一覧の種類と接続先

| 種別 | UI | API | 接続先 | スコープ |
|---|---|---|---|---|
| gojo/local | /gojo/contracts/local | GET /api/v1/gojo/contracts/local | 地区DB（REGION） | 単一法人スキーマ（地区内の1法人） |
| gojo/all | /gojo/contracts/all | GET /api/v1/gojo/contracts/all | 地区DB（REGION） | 地区DBインスタンス内の複数法人スキーマ横断（地区内ALL） |
| group/contracts | /group/contracts | GET /api/v1/group/contracts | 統合DB（INTEGRATION） | 全法人横断（統合ALL） |

---

## 共通パラメータ（必須）
- page: Int（0始まり、必須、page >= 0）
- size: Int（必須、20 / 50 / 100 のみ、最大100）

---

## フィルタ仕様
### gojo/local
- regionId: 必須

### gojo/all
- regionId: 必須
- corporationId: 任意（null の場合は ALL を許可、追加ガード禁止）

### group/contracts
- corporationId: 任意（null の場合は ALL を許可、追加ガード禁止）

---

## アーキテクチャ制約（必須）
- Domain/Application/API は JPA/JDBC を知らない（技術非依存）
- Repository interface は Domain、実装は Infrastructure
- nexus-api は DataSource/JPA を所有しない
- gojo は DbContext.forRegion(regionId) を使用（integration 禁止）
- group は DbContext.forIntegration() を使用（region 禁止）

参照：
- docs/architecture/design-principles.md
- docs/architecture/ai-rules.md
- docs/use-cases/gojo-contract-list.md
- docs/spec/group-contract-list.md

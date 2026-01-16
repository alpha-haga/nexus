# 契約一覧機能仕様（Group Contracts List）

## 概要

法人横断の契約一覧を統合DB（integration DB）から取得する機能。

## エンドポイント

- **UI**: `/group/contracts`
- **API**: `GET /api/v1/group/contracts`

## 接続先とスコープ

| 機能 | 接続先 | スコープ | 説明 |
|------|--------|---------|------|
| gojo/local | 地区DB（Region） | 単一法人スキーマ | 指定された regionId の地区DB内の単一法人スキーマから取得 |
| gojo/all | 地区DB（Region） | 同一地区DBインスタンス内の複数法人スキーマ集約 | 同一地区DBインスタンス内の複数法人スキーマを横断して取得（地区内ALL） |
| group/contracts | 統合DB（Integration） | 全法人の契約 | 統合DBから全法人の契約を取得 |

**重要**: 
- gojo/local と gojo/all は地区DB（Region）を使用
- gojo/all は integration DB ではない（同一地区DBインスタンス内の複数法人スキーマ集約）
- group/contracts は必ず統合DB（DbContext.forIntegration）を使用する

## パラメータ

### 必須パラメータ

- `page` (Int): ページ番号（0始まり）
- `size` (Int): ページサイズ（20, 50, 100 のみ）

### オプショナルパラメータ

- `corporationId` (String?): 法人ID（指定時のみ絞り込み、未指定は全法人）

## ルール

1. **ページネーション**: page と size は必須
2. **サイズ制限**: size は 20, 50, 100 のみ（最大100）
3. **法人フィルタ**: corporationId は optional（null の場合は全法人を返す）
4. **ALL許可**: 追加のガード/必須フィルタは不要（ALL を禁止しない）
5. **Read-only**: 更新操作は提供しない

## レスポンス形式

{
  "items": [
    {
      "id": "contract-001",
      "corporationId": "corp-001",
      "contractorPersonId": "person-001",
      "beneficiaryPersonId": "person-002",
      "planCode": "PLAN-A",
      "planName": "プランA",
      "monthlyFee": 10000,
      "maturityAmount": 1000000,
      "contractDate": "2024-01-01",
      "maturityDate": "2029-01-01",
      "status": "ACTIVE"
    }
  ],
  "page": 0,
  "size": 20,
  "total": 150
}

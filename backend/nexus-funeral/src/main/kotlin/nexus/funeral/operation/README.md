# funeral/operation

## 責務

**葬祭の施行管理**。

- 施行スケジュール管理
- 式場・祭壇の手配
- スタッフ配置
- 施行当日の進行管理

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| funeral/reception | 連携 | 受付から案件引継 |
| funeral/logistics | 連携 | 車両・搬送手配 |
| funeral/finance | 連携 | 費用・請求連携 |
| funeral/master | 参照 | プラン・商品参照 |

## 禁止事項

| 禁止 | 理由 |
|------|------|
| 会計仕訳の作成 | accounting の責務 |
| 顧客情報の更新 | identity の責務 |
| 入金処理 | finance → payment の責務 |
| 車両管理 | logistics の責務 |

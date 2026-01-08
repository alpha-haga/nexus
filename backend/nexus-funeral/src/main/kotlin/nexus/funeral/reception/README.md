# funeral/reception

## 責務

**葬祭の受付・CRM**。

- 問い合わせ対応
- 事前相談管理
- 顧客接点の記録

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| funeral/operation | 連携 | 受注後に施行へ引継 |
| identity | 参照 | 顧客情報の確認 |
| household | 参照 | 世帯情報の確認 |
| gojo | 参照 | 互助会契約の確認 |

## 禁止事項

| 禁止 | 理由 |
|------|------|
| 顧客情報の更新 | identity の責務 |
| 会計仕訳の作成 | accounting の責務 |
| 施行管理 | operation の責務 |
| 入金処理 | finance → payment の責務 |

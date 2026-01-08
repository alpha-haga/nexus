# identity/person

## 責務

**人物マスタの管理**。

- 人物の登録・更新・検索
- 名寄せ（重複人物の統合）
- FEDERATION アーキテクチャの中核

## 他ドメインとの関係

| ドメイン | 関係 | 説明 |
|---------|------|------|
| gojo | 参照される | 契約者・受益者の PersonId |
| funeral | 参照される | 故人・喪主の PersonId |
| bridal | 参照される | 新郎新婦の PersonId |
| household | 連携 | 世帯構成員の PersonId |

## 禁止事項

| 禁止 | 理由 |
|------|------|
| 業務モジュールからの直接更新 | API 層経由でのみアクセス |
| 他ドメインへの依存 | Single Source of Truth |

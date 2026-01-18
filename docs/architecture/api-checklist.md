# API作成時チェックリスト

本チェックリストは、新規API（Controller）を作成する際に、  
**nexus-api** と **nexus-bff** のどちらに配置すべきかを  
**5分で判定する**ためのものです。

---

## A. 3分で判定するフローチャート

このAPIは「社内UI（Web / Tablet）向け」か？

├─ Yes → **nexus-bff**  
└─ No → 2へ  

このAPIは「外部公開 / 基盤用途」か？

├─ Yes → **nexus-api**  
└─ No → 3へ  

このAPIは「業務ドメイン  
（gojo / funeral / bridal / point / agent / payment / accounting / reporting）」  
に関連するか？

├─ Yes → **nexus-bff（必ず）**  
└─ No → 4へ  

このAPIは「group / identity / household の UI専用集約API」か？

├─ Yes → **nexus-bff**  
└─ No → 5へ  

### 迷った場合の質問事項

- 想定クライアントは？（社内UI / 外部システム / パートナー）
- 認証境界は？（社内認証 / 外部認証 / 公開）
- 公開範囲は？（社内のみ / 外部公開 / 基盤用途）

→ 回答に基づいて **nexus-api** または **nexus-bff** を選択

---

## B. 判定表（Decision Table）

| APIの性質 | 配置先 | 理由 | 例 |
|-----------|--------|------|-----|
| 外部公開・基盤用途 | **nexus-api** | 外部システム・パートナー向け、安定性重視 | `GET /api/v1/persons/{id}` |
| 社内UI向け（Web / Tablet） | **nexus-bff** | 社内UI専用、柔軟な変更対応 | `GET /api/v1/gojo/contracts/local` |
| 業務ドメインAPI | **nexus-bff** | 業務ロジック変更の影響を外部APIから分離 | `GET /api/v1/gojo/contracts/all` |
| UI専用の集約API | **nexus-bff** | BFFパターンに適している | `GET /api/v1/dashboard/summary` |
| 基盤ドメイン単体API | **nexus-api** | 基盤用途として外部公開 | `GET /api/v1/persons` |
| 基盤ドメインUI集約API | **nexus-bff** | UI向け最適化が必要 | `GET /api/v1/customers/search` |

---

## C. "やってはいけない" チェック（MUST NOT）

### ❌ nexus-api に追加してはいけないもの

- 業務ドメイン（gojo / funeral / bridal / point / agent / payment / accounting / reporting）の Controller
- 社内UI専用の集約API
- UI向けの最適化（ページネーション・フィルタリング・集計）を含むAPI

### ❌ nexus-bff に追加してはいけないもの

- 外部公開用途のAPI
- パートナー・他システム向けのAPI
- 基盤用途のAPI（group / identity / household の単体参照）

### ⚠️ 共通の注意事項

- DataSource Bean / 接続ルーティング / JPA設定  
  （@EntityScan / @EnableJpaRepositories 等）の定義は  
  **nexus-infrastructure に集約**する
- nexus-api / nexus-bff は **DataSource を直接定義しない**
- API層は DbConnectionProvider 等のインフラ基盤を利用し、  
  **Controller で JDBC / SQL を直接扱わない**

---

## D. PRレビュー用チェック項目

- [ ] Controller は `nexus-api` または `nexus-bff` に配置されている
- [ ] 業務ドメインAPIは `nexus-bff` に配置されている
- [ ] 外部公開APIは `nexus-api` に配置されている
- [ ] `nexus-api` に業務ドメイン依存が追加されていない
- [ ] `scanBasePackages` に禁止ドメインが含まれていない
- [ ] DataSource / JPA 設定を API 層で定義していない
- [ ] 想定クライアントが明確である

---

## E. 参考リンク

- [API構成ポリシー（design-principles）](./design-principles.md)
- [API Placement Rules（AI Rules）](./ai-rules.md)
- [Controller 生成前チェック（10行ガード）](./ai-rules.md)

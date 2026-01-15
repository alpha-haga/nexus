# Context Map：契約一覧のデータスコープ定義（local / all / group）

本資料は、契約一覧機能における  
**local / all / group の違いと責務境界**を明確にするための設計資料である。

本ドキュメントは **開発者・設計者向け**であり、AI向けルールではない。

---

## 用語定義（結論）

| 区分 | 意味 |
|-----|-----|
| **local** | 単一スキーマ（単一地区DB） |
| **all** | 同一DBインスタンスに含まれる複数スキーマの集合 |
| **group** | 統合DB（integration DB） |

---

## 全体構成図

```mermaid
flowchart LR
  UI[UI: 契約一覧画面]

  %% Gojo Local
  subgraph GOJO_LOCAL["GOJO / Local（単一スキーマ）"]
    UI --> L1[/gojo/contracts/local]
    L1 --> API_L[API: /api/v1/gojo/contracts/local]
    API_L --> APP_L[ContractQueryService]
    APP_L --> DOM_L[GojoContractRepository.findByRegion]
    DOM_L --> INF_L[RepositoryImpl]
    INF_L --> DB_L[(Region DB<br/>1 schema)]
  end

  %% Gojo All
  subgraph GOJO_ALL["GOJO / All（同一インスタンス内の複数スキーマ）"]
    UI --> A1[/gojo/contracts/all]
    A1 --> API_A[API: /api/v1/gojo/contracts/all]
    API_A --> APP_A[ContractQueryService]
    APP_A --> DOM_A[GojoContractRepository.findAll]
    DOM_A --> INF_A[RepositoryImpl]
    INF_A --> UNION["スキーマ横断（UNION）"]
    UNION --> S1[(Schema: Tokyo)]
    UNION --> S2[(Schema: Osaka)]
    UNION --> S3[(Schema: Fukuoka)]
  end

  %% Group
  subgraph GROUP["GROUP（統合DB）"]
    UI --> G1[/group/contracts]
    G1 --> API_G[API: /api/v1/group/contracts]
    API_G --> APP_G[GroupContractQueryService]
    APP_G --> DOM_G[GroupContractRepository]
    DOM_G --> INF_G[RepositoryImpl]
    INF_G --> INTDB[(Integration DB)]
  end

各スコープの詳細
1. local（GOJO / Local）
意味

単一地区・単一スキーマの契約一覧

特徴

強い整合性

業務オペレーション向け

regionId 必須

データ範囲
1 DBインスタンス
└─ 1 schema
2. all（GOJO / All）
意味

同一DBインスタンスに含まれる複数スキーマの集合

「地区をまたぐが、GOJOの業務コンテキスト内」

重要な誤解防止

❌ integration DB ではない
❌ group の責務ではない

データ範囲

1 DBインスタンス
├─ schema_tokyo
├─ schema_osaka
└─ schema_fukuoka
実装方針（未確定・どちらでも良い）

DB VIEW による UNION ALL

もしくは SQL で動的にスキーマ指定して UNION

※ このフェーズでは 実装方式は固定しない

3. group（GROUP / Integration）
意味

統合DB（integration DB）を参照する契約一覧

法人横断・集計・参照用途

特徴

Read Only

正規化・名寄せ済みデータ

業務処理は行わない

データ範囲

別DBインスタンス
└─ integration schema
共通制約（重要）
page / size は必須

size は {20, 50, 100} のみ許可

ALL（corporationId 未指定）は常に許可

Domain / Application / API 層は Spring Data JPA を使用しない

DataSource / JPA 設定は nexus-api が所有しない
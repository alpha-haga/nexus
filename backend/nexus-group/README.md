# nexus-group

## 役割

複数法人を横断した **検索専用** モジュール。

グループ法人間で共有される人・世帯・会員識別情報を扱う基盤ドメインであり、
業務ロジックは持たず、参照・連携を目的とする。

- **法人横断検索**: 全法人のデータを統合的に検索
- **名寄せ候補表示**: 同一人物の可能性がある候補を提示（実行は identity）
- **サマリー取得**: 特定人物の全法人にまたがる情報サマリー

## やってよいこと

| 許可 | 例 |
|------|-----|
| Query クラスの定義 | `GroupSearchCriteria`, `GroupSearchResult` |
| 検索用 Service | `GroupQueryService.searchPersons()` |
| Read Only な Repository | `@Query` による SELECT のみ |
| 集約・変換ロジック | 複数データソースの結果をマージ |

## やってはいけないこと

| 禁止 | 理由 |
|------|------|
| INSERT / UPDATE / DELETE | group は Read Only。データ変更権限を持たない |
| Command クラスの定義 | 更新指示を表すオブジェクトは存在してはならない |
| 書き込み用 Repository | `save()`, `delete()` を呼び出すコードは禁止 |
| identity への更新呼び出し | 名寄せ実行は identity の責務。group は候補表示のみ |
| 業務モジュールへの依存 | group は core のみに依存する |

## ディレクトリ構成

```
src/main/kotlin/nexus/group/
├── controller/   # REST エンドポイント（検索API）
├── dto/          # リクエスト/レスポンス DTO
├── query/        # 検索条件・検索結果の定義
│   └── GroupQuery.kt
├── service/      # 検索ロジック
│   └── GroupQueryService.kt
└── config/       # モジュール設定
```

## 設計指針

### なぜ Read Only か

1. **データ主権の保護**: 法人Aのデータを法人Bの操作で変更させない
2. **監査証跡の一元化**: 更新は identity で行い、履歴を集中管理
3. **責務の分離**: 「横断検索」と「データ管理」は異なる関心事

### 名寄せの役割分担

```
┌─────────────────┐     ┌─────────────────┐
│     group       │────▶│    identity     │
│  候補を検索・表示 │     │  名寄せを実行    │
│   (Read Only)   │     │  (Write権限あり) │
└─────────────────┘     └─────────────────┘
```

- group: 「この人物と同一かもしれない候補」を返す
- identity: 「この2つを同一人物として統合する」を実行

### 検索パフォーマンス

複数法人のデータを検索するため、以下に注意：

- インデックスの適切な設計
- ページネーションの実装
- 検索条件の絞り込み推奨

## 依存関係

```
nexus-group
    └── nexus-core （ID, VO, 例外）
```

**禁止される依存**:
- nexus-identity（参照は可能だが更新呼び出しは禁止）
- nexus-gojo / nexus-funeral / nexus-bridal / nexus-point

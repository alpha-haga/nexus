P04: 法人横断契約一覧（Group） JDBC Read 方針
目的

法人横断契約一覧の Read 導線を JDBC 実装に差し替え可能な形で整備する

P0/P04 の段階では 起動・DI・導線固定を最優先し、性能最適化や JOIN 復活は後続に回す

層の責務

domain（nexus-group）

技術詳細（SQL/JDBC/JPA/DB接続情報）を一切持たない

Query の “型（DTO/Condition）” と “インターフェース” のみを持つ

infrastructure（nexus-infrastructure）

SQL は nexus-infrastructure/src/main/resources/sql/ 配下にのみ置く

JDBC/JPA 実装クラス、RowMapper、SqlLoader を持つ

bff（nexus-bff）

HTTP API のみ（validation / request param → condition 変換 / response 変換）

“動いている風” 禁止：失敗は握りつぶさず 500/4xx で返す（Fail Fast）

SQL の置き場所

SQL は infrastructure 層にのみ配置
backend/nexus-infrastructure/src/main/resources/sql/group/

呼び出し側は SqlLoader.load("group/group_contract_search.sql") のように 相対パス固定

SqlLoader 側が sql/ プレフィックス等を吸収する（既存仕様を変更しない）

Profile / Bean 競合回避

JDBC 実装は @Profile("jdbc") で有効化

JPA MIN 実装は @Profile("!jdbc") で残す（非 jdbc 環境の compile/起動保持）

BFF の新エンドポイントも @Profile("jdbc") を付与し、既存 API と URL 衝突しないようにする

目的：jdbc プロファイル時のみ JDBC 導線が有効になり、DI 競合を起こさない

命名ルール（SQL alias / Kotlin DTO）

SQL alias：lower_snake_case

省略は基本しない（略すと迷子になりやすい）

長くても “意味が分かる” を優先

Kotlin：lowerCamelCase

snake_case → camelCase の変換は RowMapper で明示的に行う（自動マッピング禁止）

取得できない列は CAST(NULL AS ...) AS <alias> で 列は保持し、DTO 側は nullable にする（JOIN を後で復活できる）

現行のテーブル前提

integration 側の検索キー：zgot_contract_search_key contract_search

当面は JOIN を外し、取れない項目は CAST(NULL ...) で埋める

WHERE は (:param IS NULL OR column ...) 形式で nullable condition をそのまま bind する

API

jdbc プロファイル時の検索 API（例）

GET /api/v1/group/contracts/search?page=0&size=20&contractNo=...

既存の /api/v1/group/contracts は “旧導線” として残すが、P04 では jdbc profile で無効化する

Done 条件（P04-2）

./gradlew build が通る

SPRING_PROFILES_ACTIVE=jdbc ./gradlew :nexus-bff:bootRun が起動する

curl で endpoint 到達し、少なくとも「SQL 実行 → 例外が見える」状態になる（env 未設定なら “設定不足で落ちる” を確認できれば良い）
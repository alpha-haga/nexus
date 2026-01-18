# nexus-bff（Internal UI Backend / BFF）

## 役割
- 社内UI（Web / Tablet）向けの Backend For Frontend
- 業務ドメインの API（gojo / funeral / bridal / point / agent / payment / accounting / reporting）を束ねる
- UI向けのデータ集約・整形・ページング等を提供する

## 依存
- nexus-core
- nexus-infrastructure（DB接続基盤。DataSource設定の所有はしない）
- group / identity / household（必要に応じて参照）
- 業務ドメイン（段階的に追加）

## 禁止事項
- 外部公開用途の API をここに実装しない（それは nexus-api）
- DataSource/JPA 設定をこのモジュールで所有しない（infrastructure の責務）

## Controller 配置ルール
- 社内UI向け Controller は nexus-bff にのみ置く
- nexus-api に業務ドメイン Controller を追加しない
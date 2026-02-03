# P2-5 パフォーマンス測定スクリプト

## 概要

P2-5 のパフォーマンス測定用スクリプトです。5つの計測条件パターンを実行し、実行時間（平均、最小、最大）を計測します。

## 前提条件

- `curl` がインストールされていること
- `jq` がインストールされていること
- `awk` がインストールされていること（計算用、通常は標準でインストール済み）
- Keycloak が起動していること
- BFF が起動していること

## インストール

```bash
chmod +x scripts/measure_performance.sh
```

## 使用方法

### 基本的な使用方法

```bash
./scripts/measure_performance.sh
```

### 環境変数で設定を変更

```bash
# Keycloak設定
export KEYCLOAK_URL="http://localhost:8180"
export KEYCLOAK_REALM="nexus"
export KEYCLOAK_CLIENT_ID="nexus-bff"
export KEYCLOAK_CLIENT_SECRET="nexus-bff-secret"
export KEYCLOAK_USERNAME="dev-user"
export KEYCLOAK_PASSWORD="password"

# BFF設定
export BFF_URL="http://localhost:8080"
export REGION="integration"

# 計測設定
export ITERATIONS=5  # 各パターンの実行回数
export PAGE=0
export SIZE=20

# CSV出力（オプション）
export OUTPUT_CSV=1  # 1を設定するとCSVファイルに出力

./scripts/measure_performance.sh
```

## 計測条件パターン

1. 全件検索（条件なし）
2. 契約受付年月日範囲指定（例: 20240101-20241231）
3. 契約番号前方一致（例: "12345"）
4. 家族名カナ中間一致（例: "ヤマダ"）
5. 電話番号中間一致（例: "031234"）

## 出力

各パターンについて以下を出力します：
- 平均実行時間（秒）
- 最小実行時間（秒）
- 最大実行時間（秒）

`OUTPUT_CSV=1` を設定すると、`performance_results.csv` に結果が保存されます。

## 注意事項

- 実データ量での計測を前提とします（推測禁止）
- 計測前にBFFが正常に起動していることを確認してください
- Keycloak の認証情報が正しいことを確認してください
- Windows環境では Git Bash または WSL を使用してください

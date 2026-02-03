#!/bin/bash
# P2-5 パフォーマンス測定スクリプト
# 計測条件パターンを実行し、実行時間（平均、最小、最大）を計測する

set -euo pipefail

# 設定（環境変数で上書き可能）
KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
KEYCLOAK_REALM="${KEYCLOAK_REALM:-nexus}"
KEYCLOAK_CLIENT_ID="${KEYCLOAK_CLIENT_ID:-nexus-bff}"
KEYCLOAK_CLIENT_SECRET="${KEYCLOAK_CLIENT_SECRET:-nexus-bff-secret}"
KEYCLOAK_USERNAME="${KEYCLOAK_USERNAME:-dev-user}"
KEYCLOAK_PASSWORD="${KEYCLOAK_PASSWORD:-password}"
BFF_URL="${BFF_URL:-http://localhost:8080}"
REGION="${REGION:-integration}"
ITERATIONS="${ITERATIONS:-5}"
PAGE="${PAGE:-0}"
SIZE="${SIZE:-20}"

# 色付き出力用
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ログ出力
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1" >&2
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1" >&2
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1" >&2
}

# Keycloak から Access Token を取得
get_access_token() {
    log_info "Keycloak から Access Token を取得中..."
    local token_url="${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token"
    
    local response=$(curl -s -X POST "$token_url" \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "grant_type=password" \
        -d "client_id=${KEYCLOAK_CLIENT_ID}" \
        -d "client_secret=${KEYCLOAK_CLIENT_SECRET}" \
        -d "username=${KEYCLOAK_USERNAME}" \
        -d "password=${KEYCLOAK_PASSWORD}")
    
    if ! command -v jq &> /dev/null; then
        log_error "jq がインストールされていません。jq をインストールしてください。"
        exit 1
    fi
    
    local token=$(echo "$response" | jq -r '.access_token')
    
    if [ "$token" == "null" ] || [ -z "$token" ]; then
        log_error "Access Token の取得に失敗しました。"
        echo "$response" | jq . >&2
        exit 1
    fi
    
    # トークンのみを標準出力に出力（ログは標準エラー出力に送る）
    echo "$token"
}

# URLエンコード関数（jqを使用）
url_encode() {
    if command -v jq &> /dev/null; then
        echo -n "$1" | jq -sRr @uri
    else
        # jqがない場合のフォールバック（簡単なエンコード）
        echo -n "$1" | sed 's/ /%20/g; s/!/%21/g; s/"/%22/g; s/#/%23/g; s/\$/%24/g; s/&/%26/g; s/'\''/%27/g; s/(/%28/g; s/)/%29/g; s/*/%2A/g; s/+/%2B/g; s/,/%2C/g; s/:/%3A/g; s/;/%3B/g; s/=/%3D/g; s/?/%3F/g; s/@/%40/g; s/\[/%5B/g; s/\]/%5D/g'
    fi
}

# API リクエストを実行し、実行時間を計測
measure_api_call() {
    local token="$1"
    local query_params="$2"
    local description="$3"
    
    # URL構築（query_paramsが空の場合は?を付けない）
    # 日本語などの特殊文字を含む場合はURLエンコードが必要
    local url
    if [ -z "$query_params" ]; then
        url="${BFF_URL}/api/v1/group/contracts/search?page=${PAGE}&size=${SIZE}"
    else
        # クエリパラメータをURLエンコード
        local encoded_params=""
        IFS='&' read -ra PARAMS <<< "$query_params"
        for param in "${PARAMS[@]}"; do
            if [[ "$param" == *"="* ]]; then
                local key="${param%%=*}"
                local value="${param#*=}"
                local encoded_value=$(url_encode "$value")
                if [ -z "$encoded_params" ]; then
                    encoded_params="${key}=${encoded_value}"
                else
                    encoded_params="${encoded_params}&${key}=${encoded_value}"
                fi
            else
                if [ -z "$encoded_params" ]; then
                    encoded_params="$param"
                else
                    encoded_params="${encoded_params}&${param}"
                fi
            fi
        done
        url="${BFF_URL}/api/v1/group/contracts/search?${encoded_params}&page=${PAGE}&size=${SIZE}"
    fi
    
    local times=()
    local total_time=0
    
    log_info "計測中: ${description}"
    log_info "URL: ${url}"
    
    # 最初の1回だけ詳細ログを出力（デバッグ用）
    if [ "${DEBUG:-}" == "1" ]; then
        log_info "デバッグ: リクエスト詳細"
        log_info "  Method: GET"
        log_info "  Headers:"
        log_info "    Authorization: Bearer ${token:0:20}..."
        log_info "    X-NEXUS-REGION: ${REGION}"
        log_info "    Accept: application/json"
    fi
    
    for i in $(seq 1 ${ITERATIONS}); do
        local start_time=$(date +%s.%N)
        
        # レスポンスボディを一時ファイルに保存（Windows対応）
        local response_file
        if command -v mktemp &> /dev/null; then
            response_file=$(mktemp)
        else
            response_file="/tmp/measure_perf_$$_$i"
        fi
        
        # 最初の1回だけ詳細ログを出力（デバッグ用）
        local curl_opts=()
        if [ "$i" == "1" ] && [ "${DEBUG:-}" == "1" ]; then
            curl_opts+=("-v")
        fi
        
        local http_code=$(curl "${curl_opts[@]}" -s -o "$response_file" -w "%{http_code}" \
            -X GET "$url" \
            -H "Authorization: Bearer ${token}" \
            -H "X-NEXUS-REGION: ${REGION}" \
            -H "Accept: application/json" \
            -H "Content-Type: application/json")
        
        local end_time=$(date +%s.%N)
        # awk を使って浮動小数点計算（bcの代わり）
        local elapsed=$(awk "BEGIN {printf \"%.3f\", $end_time - $start_time}")
        
        # http_codeが空の場合はエラー
        if [ -z "$http_code" ]; then
            log_error "HTTP リクエストに失敗しました（ステータスコードが取得できませんでした）。"
            log_error "リクエストURL: ${url}"
            if [ -f "$response_file" ]; then
                log_error "レスポンス:"
                cat "$response_file" >&2
            fi
            rm -f "$response_file" 2>/dev/null || true
            return 1
        fi
        
        if [ "$http_code" != "200" ]; then
            log_error "HTTP ${http_code} エラーが発生しました。"
            log_error "リクエストURL: ${url}"
            log_error "エラーレスポンス:"
            if command -v jq &> /dev/null; then
                cat "$response_file" | jq . 2>/dev/null || cat "$response_file"
            else
                cat "$response_file"
            fi
            log_error ""
            log_error "デバッグ用: 以下のコマンドで直接リクエストを確認してください"
            log_error "curl -v -X GET \"${url}\" -H \"Authorization: Bearer ${token:0:20}...\" -H \"X-NEXUS-REGION: ${REGION}\" -H \"Accept: application/json\""
            rm -f "$response_file" 2>/dev/null || true
            return 1
        fi
        
        rm -f "$response_file" 2>/dev/null || true
        
        times+=("$elapsed")
        # awk を使って加算
        total_time=$(awk "BEGIN {printf \"%.3f\", $total_time + $elapsed}")
        
        printf "  [%d/%d] %.3f秒\n" "$i" "${ITERATIONS}" "$elapsed" >&2
    done
    
    # 平均、最小、最大を計算（awkを使用）
    local avg=$(awk "BEGIN {printf \"%.3f\", $total_time / ${ITERATIONS}}")
    
    # 最小・最大を計算（awkで配列を処理）
    local min_max=$(printf '%s\n' "${times[@]}" | awk 'BEGIN {min=999999; max=0} {if ($1 < min) min=$1; if ($1 > max) max=$1} END {printf "%.3f %.3f", min, max}')
    local min=$(echo "$min_max" | awk '{print $1}')
    local max=$(echo "$min_max" | awk '{print $2}')
    
    echo "" >&2
    log_success "計測結果: ${description}"
    echo "  平均: ${avg}秒" >&2
    echo "  最小: ${min}秒" >&2
    echo "  最大: ${max}秒" >&2
    echo "" >&2
    
    # CSV形式で出力（オプション）
    if [ "${OUTPUT_CSV:-}" == "1" ]; then
        echo "${description},${avg},${min},${max}" >> performance_results.csv
    fi
}

# メイン処理
main() {
    log_info "P2-5 パフォーマンス測定を開始します"
    log_info "設定:"
    echo "  Keycloak URL: ${KEYCLOAK_URL}" >&2
    echo "  BFF URL: ${BFF_URL}" >&2
    echo "  Region: ${REGION}" >&2
    echo "  実行回数: ${ITERATIONS}" >&2
    echo "  ページ: ${PAGE}, サイズ: ${SIZE}" >&2
    echo "" >&2
    
    # Access Token を取得
    local token=$(get_access_token)
    log_success "Access Token を取得しました"
    echo "" >&2
    
    # CSV出力ファイルを初期化
    if [ "${OUTPUT_CSV:-}" == "1" ]; then
        echo "パターン,平均(秒),最小(秒),最大(秒)" > performance_results.csv
    fi
    
    # 計測条件パターン1: 全件検索（条件なし）
    measure_api_call "$token" "" "1. 全件検索（条件なし）"
    
    # 計測条件パターン2: 契約受付年月日範囲指定
    measure_api_call "$token" "contractReceiptYmdFrom=20240101&contractReceiptYmdTo=20241231" "2. 契約受付年月日範囲指定（20240101-20241231）"
    
    # 計測条件パターン3: 契約番号前方一致
    measure_api_call "$token" "contractNo=12345" "3. 契約番号前方一致（12345）"
    
    # 計測条件パターン4: 家族名カナ中間一致
    measure_api_call "$token" "familyNmKana=ヤマダ" "4. 家族名カナ中間一致（ヤマダ）"
    
    # 計測条件パターン5: 電話番号中間一致
    measure_api_call "$token" "telNo=031234" "5. 電話番号中間一致（031234）"
    
    log_success "すべての計測が完了しました"
    
    if [ "${OUTPUT_CSV:-}" == "1" ]; then
        log_info "結果を performance_results.csv に保存しました"
    fi
}

# スクリプト実行
main "$@"

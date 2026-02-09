#!/bin/bash
# 法人横断契約詳細API テストスクリプト
# Token取得から詳細API実行までのテストを実行する

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

# 色付き出力用
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ログ出力（標準エラー出力に送る）
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
    
    echo "$token"
}

# 一覧から実際の契約を取得して cmpCd と contractNo を抽出
get_contract_from_list() {
    local token="$1"
    log_info "一覧APIから契約情報を取得中..."
    
    # HTTPステータスコードも取得
    local search_result=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BFF_URL}/api/v1/group/contracts/search?page=0&size=1&contractReceiptYmdFrom=20240101&contractReceiptYmdTo=20241231" \
        -H "Authorization: Bearer $token" \
        -H "X-NEXUS-REGION: ${REGION}")
    
    local http_status=$(echo "$search_result" | grep "HTTP_STATUS:" | cut -d: -f2)
    local body=$(echo "$search_result" | sed '/HTTP_STATUS:/d')
    
    log_info "一覧API HTTP Status: $http_status"
    
    # HTTPステータスが200以外の場合はエラー
    if [ "$http_status" != "200" ]; then
        log_error "一覧APIがエラーを返しました。HTTP Status: $http_status"
        log_error "レスポンス内容:"
        # jqでパースできるか試す、できない場合はそのまま表示
        echo "$body" | jq . >&2 2>/dev/null || echo "$body" >&2
        exit 1
    fi
    
    # JSONパースエラーチェック
    if ! echo "$body" | jq empty 2>/dev/null; then
        log_error "一覧APIのレスポンスがJSON形式ではありません。"
        log_error "レスポンス内容:"
        echo "$body" >&2
        exit 1
    fi
    
    # totalElements を確認
    local total_elements=$(echo "$body" | jq -r '.totalElements // 0')
    if [ "$total_elements" == "0" ] || [ "$total_elements" == "null" ]; then
        log_error "一覧APIに契約データが存在しません。totalElements: $total_elements"
        echo "$body" | jq . >&2
        exit 1
    fi
    
    log_info "契約データ件数: $total_elements"
    
    # content 配列の存在確認
    local content_length=$(echo "$body" | jq -r '.content | length')
    if [ "$content_length" == "0" ] || [ "$content_length" == "null" ]; then
        log_error "content 配列が空です。"
        echo "$body" | jq . >&2
        exit 1
    fi
    
    local cmp_cd=$(echo "$body" | jq -r '.content[0].cmpCd // empty')
    local contract_no=$(echo "$body" | jq -r '.content[0].contractNo // empty')
    
    if [ -z "$cmp_cd" ] || [ "$cmp_cd" == "null" ]; then
        log_error "一覧から契約情報を取得できませんでした。"
        log_error "レスポンス内容:"
        echo "$body" | jq . >&2
        exit 1
    fi
    
    if [ -z "$contract_no" ] || [ "$contract_no" == "null" ]; then
        log_error "契約番号を取得できませんでした。"
        log_error "レスポンス内容:"
        echo "$body" | jq . >&2
        exit 1
    fi
    
    log_info "取得した契約情報: cmpCd=$cmp_cd, contractNo=$contract_no"
    
    echo "$cmp_cd|$contract_no"
}

# 詳細APIを実行
test_detail_api() {
    local token="$1"
    local cmp_cd="$2"
    local contract_no="$3"
    local test_name="$4"
    
    log_info "$test_name"
    echo "  URL: ${BFF_URL}/api/v1/group/contracts/${cmp_cd}/${contract_no}" >&2
    
    local response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET "${BFF_URL}/api/v1/group/contracts/${cmp_cd}/${contract_no}" \
        -H "Authorization: Bearer $token" \
        -H "X-NEXUS-REGION: ${REGION}")
    
    local http_status=$(echo "$response" | grep "HTTP_STATUS:" | cut -d: -f2)
    local body=$(echo "$response" | sed '/HTTP_STATUS:/d')
    
    echo "  HTTP Status: $http_status" >&2
    echo "$body" | jq . 2>/dev/null || echo "$body"
    echo "" >&2
}

# メイン処理
main() {
    log_info "法人横断契約詳細API テストを開始します"
    echo "" >&2
    
    # Access Token を取得
    local token=$(get_access_token)
    log_success "Access Token を取得しました"
    echo "" >&2
    
    # 一覧から実際の契約を取得
    local contract_info=$(get_contract_from_list "$token")
    local cmp_cd=$(echo "$contract_info" | cut -d'|' -f1)
    local contract_no=$(echo "$contract_info" | cut -d'|' -f2)
    log_success "契約情報を取得しました: cmpCd=$cmp_cd, contractNo=$contract_no"
    echo "" >&2
    
    # テストケース1: 正常系（実際の契約データ）
    test_detail_api "$token" "$cmp_cd" "$contract_no" "=== テストケース1: 正常系（実際の契約データ） ==="
    
    # テストケース2: 400 Bad Request（cmpCd が空）
    test_detail_api "$token" "" "12345678" "=== テストケース2: 400 Bad Request（cmpCd が空） ==="
    
    # テストケース3: 404 Not Found（存在しない契約）
    test_detail_api "$token" "999" "99999999" "=== テストケース3: 404 Not Found（存在しない契約） ==="
    
    log_success "すべてのテストが完了しました"
}

# スクリプト実行
main "$@"

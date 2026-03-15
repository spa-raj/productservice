#!/bin/bash
# ==============================================================================
# Elasticsearch (OpenSearch) Integration Test Suite — EKS/K8s
# ==============================================================================
# Tests the full Elasticsearch/OpenSearch search pipeline on EKS:
#   1. Health checks (userservice, productservice)
#   2. OAuth2 token retrieval (admin)
#   3. Create test products
#   4. Trigger full reindex
#   5. Test search (full-text, fuzzy, filters, suggestions)
#   6. Test real-time sync (create → search, update → search, delete → search)
#
# Prerequisites:
#   - userservice port-forwarded to localhost:8081
#   - productservice port-forwarded to localhost:8080
#   - kubectl configured for the target cluster
#
# Usage:
#   ./test-elasticsearch-k8s.sh
#   ADMIN_PASSWORD="xxx" CLIENT_SECRET="yyy" ./test-elasticsearch-k8s.sh
#   TOKEN="xxx" ./test-elasticsearch-k8s.sh    # skip OAuth2 flow
# ==============================================================================

set -euo pipefail

USERSERVICE="http://localhost:8081"
PRODUCTSERVICE="http://localhost:8080"
NAMESPACE="${NAMESPACE:-vibevault}"

# Pull credentials from K8s secrets (fallback to env vars if set)
ADMIN_EMAIL="admin@gmail.com"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-$(kubectl get secret userservice-secret -n "$NAMESPACE" -o jsonpath='{.data.ADMIN_PASSWORD}' | base64 -d)}"
CLIENT_ID="vibevault-client"
CLIENT_SECRET="${CLIENT_SECRET:-$(kubectl get secret userservice-secret -n "$NAMESPACE" -o jsonpath='{.data.CLIENT_SECRET}' | base64 -d)}"
REDIRECT_URI="https://oauth.pstmn.io/v1/callback"
SCOPES="openid+profile+email+read+write"
export ADMIN_PASSWORD CLIENT_SECRET

PASS=0
FAIL=0
SKIP=0

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# ============================================================================
# Helpers
# ============================================================================

assert_status() {
    local description="$1"
    local expected="$2"
    local actual="$3"
    local body="${4:-}"

    if [ "$actual" = "$expected" ]; then
        echo -e "  ${GREEN}PASS${NC} [$actual] $description"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} [$actual expected $expected] $description"
        [ -n "$body" ] && echo "       Response: $(echo "$body" | head -c 300)"
        FAIL=$((FAIL + 1))
    fi
}

assert_body_contains() {
    local description="$1"
    local expected_substring="$2"
    local body="$3"

    if echo "$body" | grep -qi "$expected_substring"; then
        echo -e "  ${GREEN}PASS${NC} $description (contains '$expected_substring')"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} $description (expected to contain '$expected_substring')"
        echo "       Response: $(echo "$body" | head -c 300)"
        FAIL=$((FAIL + 1))
    fi
}

request() {
    local method="$1"
    local url="$2"
    local headers="${3:-}"
    local data="${4:-}"

    local curl_args=(-s -w "\n%{http_code}" -X "$method" "$url")
    if [ -n "$headers" ]; then
        while IFS= read -r header; do
            [ -n "$header" ] && curl_args+=(-H "$header")
        done <<< "$headers"
    fi
    if [ -n "$data" ]; then
        curl_args+=(-d "$data")
    fi

    local response
    response=$(curl "${curl_args[@]}")
    BODY=$(echo "$response" | head -n -1)
    STATUS=$(echo "$response" | tail -n 1)
}

section() {
    echo ""
    echo -e "${CYAN}--- $1 ---${NC}"
}

urlencode() {
    python3 -c "import urllib.parse,sys; print(urllib.parse.quote(sys.argv[1], safe=''))" "$1"
}

# ============================================================================
# OAuth2 Token Flow
# ============================================================================

get_oauth2_token() {
    set +e
    local username="$1"
    local password="$2"

    local COOKIE_JAR
    COOKIE_JAR=$(mktemp /tmp/es_k8s_test_cookies.XXXXXX)

    local AUTH_URL="${USERSERVICE}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPES}"

    curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" -L --max-redirs 1 -o /dev/null "$AUTH_URL"

    local LOGIN_PAGE
    LOGIN_PAGE=$(curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" "${USERSERVICE}/login")
    local CSRF
    CSRF=$(echo "$LOGIN_PAGE" | grep -oP 'name="_csrf".*?value="\K[^"]+')

    if [ -z "$CSRF" ]; then
        rm -f "$COOKIE_JAR"
        set -e
        echo ""
        return
    fi

    local ENCODED_PASSWORD
    ENCODED_PASSWORD=$(urlencode "$password")
    curl -s -D- -o /dev/null -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "${USERSERVICE}/login" \
        -d "username=${username}&password=${ENCODED_PASSWORD}&_csrf=${CSRF}" > /dev/null

    local AUTHORIZE_RESPONSE
    AUTHORIZE_RESPONSE=$(curl -s -D- -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
        "${USERSERVICE}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPES}&continue")

    local AUTHORIZE_LOCATION
    AUTHORIZE_LOCATION=$(echo "$AUTHORIZE_RESPONSE" | grep -i "^Location:" | tr -d '\r' || true)

    local AUTH_CODE=""

    if echo "$AUTHORIZE_LOCATION" | grep -q "code="; then
        AUTH_CODE=$(echo "$AUTHORIZE_LOCATION" | grep -oP 'code=\K[^&\s]+' || true)
    else
        local CONSENT_BODY
        CONSENT_BODY=$(echo "$AUTHORIZE_RESPONSE" | sed '1,/^\r$/d')
        local STATE
        STATE=$(echo "$CONSENT_BODY" | grep -oP 'name="state"[^>]*value="\K[^"]+' || true)

        if [ -z "$STATE" ]; then
            rm -f "$COOKIE_JAR"
            set -e
            echo ""
            return
        fi

        local CONSENT_RESPONSE
        CONSENT_RESPONSE=$(curl -s -D- -o /dev/null -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "${USERSERVICE}/oauth2/authorize" \
            -d "client_id=${CLIENT_ID}&state=${STATE}&scope=read&scope=profile&scope=write&scope=email")

        local CONSENT_LOCATION
        CONSENT_LOCATION=$(echo "$CONSENT_RESPONSE" | grep -i "^Location:" | tr -d '\r' || true)

        AUTH_CODE=$(echo "$CONSENT_LOCATION" | grep -oP 'code=\K[^&\s]+' || true)
    fi

    if [ -z "$AUTH_CODE" ]; then
        rm -f "$COOKIE_JAR"
        set -e
        echo ""
        return
    fi

    local TOKEN_RESPONSE
    TOKEN_RESPONSE=$(curl -s -X POST "${USERSERVICE}/oauth2/token" \
        -u "${CLIENT_ID}:${CLIENT_SECRET}" \
        -d "grant_type=authorization_code" \
        -d "code=${AUTH_CODE}" \
        -d "redirect_uri=${REDIRECT_URI}")

    local TOKEN
    TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])" 2>/dev/null || echo "")

    rm -f "$COOKIE_JAR"
    set -e
    echo "$TOKEN"
}

# ============================================================================
# Test Suite
# ============================================================================

echo "=============================================="
echo "  Elasticsearch/OpenSearch Integration Test Suite (EKS)"
echo "=============================================="
echo -e "  ${CYAN}Namespace: ${NAMESPACE}${NC}"

# --------------------------------------------------
section "1. Health Checks"
# --------------------------------------------------

request GET "$USERSERVICE/actuator/health"
assert_status "userservice health" "200" "$STATUS"

request GET "$PRODUCTSERVICE/actuator/health"
assert_status "productservice health" "200" "$STATUS"

# --------------------------------------------------
section "2. OAuth2 Token"
# --------------------------------------------------

if [ -n "${TOKEN:-}" ]; then
    echo -e "  ${GREEN}PASS${NC} Using provided TOKEN"
    PASS=$((PASS + 1))
else
    echo "  Obtaining admin OAuth2 token..."
    TOKEN=$(get_oauth2_token "$ADMIN_EMAIL" "$ADMIN_PASSWORD")
fi

if [[ "$TOKEN" =~ ^eyJ.*\..*\..*$ ]]; then
    echo -e "  ${GREEN}PASS${NC} Admin OAuth2 token obtained"
    PASS=$((PASS + 1))
    AUTH_HEADERS="$(printf 'Authorization: Bearer %s\nContent-Type: application/json' "$TOKEN")"
else
    echo -e "  ${RED}FAIL${NC} Could not obtain OAuth2 token"
    FAIL=$((FAIL + 1))
    echo ""
    echo "=============================================="
    printf "  Results: ${GREEN}%d passed${NC}, ${RED}%d failed${NC}, ${YELLOW}%d skipped${NC}\n" "$PASS" "$FAIL" "$SKIP"
    echo "=============================================="
    exit 1
fi

# --------------------------------------------------
section "3. Create Test Products"
# --------------------------------------------------

TIMESTAMP=$(date +%s)

request POST "$PRODUCTSERVICE/categories" "$AUTH_HEADERS" '{"name":"Electronics","description":"Electronic devices"}'
if [ "$STATUS" = "200" ] || [ "$STATUS" = "409" ]; then
    echo -e "  ${GREEN}OK${NC} Category 'Electronics' ready"
else
    echo -e "  ${YELLOW}WARN${NC} Category creation returned $STATUS"
fi

request POST "$PRODUCTSERVICE/categories" "$AUTH_HEADERS" '{"name":"Clothing","description":"Apparel and clothing"}'
if [ "$STATUS" = "200" ] || [ "$STATUS" = "409" ]; then
    echo -e "  ${GREEN}OK${NC} Category 'Clothing' ready"
else
    echo -e "  ${YELLOW}WARN${NC} Category creation returned $STATUS"
fi

request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Premium Leather Wallet ${TIMESTAMP}\",\"description\":\"Handcrafted genuine leather wallet with RFID blocking\",\"price\":1499.99,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (Premium Leather Wallet)" "200" "$STATUS"

request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Wireless Bluetooth Headphones ${TIMESTAMP}\",\"description\":\"Noise cancelling over-ear headphones with 30hr battery\",\"price\":3999.00,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (Wireless Bluetooth Headphones)" "200" "$STATUS"

request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Cotton Summer T-Shirt ${TIMESTAMP}\",\"description\":\"Breathable organic cotton t-shirt for summer\",\"price\":599.00,\"currency\":\"INR\",\"categoryName\":\"Clothing\"}"
assert_status "POST /products (Cotton Summer T-Shirt)" "200" "$STATUS"

# --------------------------------------------------
section "4. Wait for real-time indexing"
# --------------------------------------------------

# Reindex is handled separately via the Reindex OpenSearch workflow.
# Wait for the event-driven indexing of test products created above.
sleep 3

# --------------------------------------------------
section "5. Full-Text Search (scoped to test products)"
# --------------------------------------------------

# Search by product name (uses timestamp to scope to our test products)
request GET "$PRODUCTSERVICE/search/products?query=Leather+Wallet+${TIMESTAMP}&page=0&size=10"
assert_status "Search: 'Leather Wallet ${TIMESTAMP}'" "200" "$STATUS"
assert_body_contains "Search results contain wallet" "Wallet" "$BODY"

# Search by description content (ES-only — MySQL LIKE can't search CLOB)
request GET "$PRODUCTSERVICE/search/products?query=RFID+blocking&page=0&size=10"
assert_status "Search: 'RFID blocking' (description search)" "200" "$STATUS"
assert_body_contains "Description search finds RFID product" "RFID" "$BODY"

request GET "$PRODUCTSERVICE/search/products?query=noise+cancelling+${TIMESTAMP}&page=0&size=10"
assert_status "Search: 'noise cancelling' (description)" "200" "$STATUS"
assert_body_contains "Description search finds headphones" "Headphones" "$BODY"

# --------------------------------------------------
section "6. Fuzzy Search (Typo Tolerance, scoped to test products)"
# --------------------------------------------------

# Misspelled: "Lether Walet" instead of "Leather Wallet"
request GET "$PRODUCTSERVICE/search/products?query=Lether+Walet+${TIMESTAMP}&page=0&size=10"
assert_status "Fuzzy search: 'Lether Walet'" "200" "$STATUS"
assert_body_contains "Fuzzy search finds wallet despite typos" "Wallet" "$BODY"

# Misspelled: "Headhpones" instead of "Headphones"
request GET "$PRODUCTSERVICE/search/products?query=Bluetoth+Headhpones+${TIMESTAMP}&page=0&size=10"
assert_status "Fuzzy search: 'Bluetoth Headhpones'" "200" "$STATUS"
assert_body_contains "Fuzzy search finds headphones despite typo" "Headphones" "$BODY"

# --------------------------------------------------
section "7. Filtered Search (scoped to test products)"
# --------------------------------------------------

# Price range — our test wallet is 1499.99, headphones 3999.00
request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&minPrice=1000&maxPrice=5000&page=0&size=10"
assert_status "Search: price range 1000-5000" "200" "$STATUS"

# Currency filter
request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&currency=INR&page=0&size=10"
assert_status "Search: currency=INR" "200" "$STATUS"

# Category filter
request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&categoryName=Electronics&page=0&size=10"
assert_status "Search: categoryName=Electronics" "200" "$STATUS"
assert_body_contains "Electronics filter returns electronics" "Electronics" "$BODY"

# Combined: query + category + price
request GET "$PRODUCTSERVICE/search/products?query=Wireless+${TIMESTAMP}&categoryName=Electronics&minPrice=2000&page=0&size=10"
assert_status "Search: query + category + price combined" "200" "$STATUS"

# --------------------------------------------------
section "8. Suggestions (Typeahead, scoped to test products)"
# --------------------------------------------------

request GET "$PRODUCTSERVICE/search/products/suggest?prefix=Premium+Leather+Wallet+${TIMESTAMP}&limit=5"
assert_status "Suggest: prefix='Premium Leather Wallet ${TIMESTAMP}'" "200" "$STATUS"
assert_body_contains "Suggestions contain Premium product" "Premium" "$BODY"

request GET "$PRODUCTSERVICE/search/products/suggest?prefix=Wireless+Bluetooth+Headphones+${TIMESTAMP}&limit=5"
assert_status "Suggest: prefix='Wireless Bluetooth Headphones ${TIMESTAMP}'" "200" "$STATUS"
assert_body_contains "Suggestions contain Wireless product" "Wireless" "$BODY"

# --------------------------------------------------
section "9. Real-Time Sync (Event-Driven Indexing)"
# --------------------------------------------------

SYNC_PRODUCT_NAME="SyncTest-Laptop-${TIMESTAMP}"
request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"${SYNC_PRODUCT_NAME}\",\"description\":\"Real-time sync test product\",\"price\":49999.00,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (sync test product)" "200" "$STATUS"
SYNC_PRODUCT_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

sleep 3

request GET "$PRODUCTSERVICE/search/products?query=${SYNC_PRODUCT_NAME}&page=0&size=10"
assert_status "Search: newly created product appears" "200" "$STATUS"
TOTAL_RESULTS=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('totalElements', 0))" 2>/dev/null || echo "0")
if [ "$TOTAL_RESULTS" -gt 0 ] 2>/dev/null; then
    echo -e "  ${GREEN}PASS${NC} Real-time indexing: product found in search after create"
    PASS=$((PASS + 1))
else
    echo -e "  ${RED}FAIL${NC} Real-time indexing: product NOT found in search after create"
    FAIL=$((FAIL + 1))
fi

if [ -n "$SYNC_PRODUCT_ID" ]; then
    UPDATED_NAME="SyncTest-Updated-Laptop-${TIMESTAMP}"
    request PATCH "$PRODUCTSERVICE/products/$SYNC_PRODUCT_ID" "$AUTH_HEADERS" \
        "{\"name\":\"${UPDATED_NAME}\"}"
    assert_status "PATCH /products (update sync test product)" "200" "$STATUS"

    sleep 3

    request GET "$PRODUCTSERVICE/search/products?query=${SYNC_PRODUCT_NAME}&page=0&size=10"
    if ! echo "$BODY" | grep -q "$SYNC_PRODUCT_NAME"; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: old name no longer found in search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: old name still found in search"
        FAIL=$((FAIL + 1))
    fi

    request GET "$PRODUCTSERVICE/search/products?query=${UPDATED_NAME}&page=0&size=10"
    NEW_RESULTS=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('totalElements', 0))" 2>/dev/null || echo "0")

    if [ "$NEW_RESULTS" -gt 0 ] 2>/dev/null; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: updated name found in search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: updated name NOT found in search"
        FAIL=$((FAIL + 1))
    fi

    request DELETE "$PRODUCTSERVICE/products/$SYNC_PRODUCT_ID" "Authorization: Bearer $TOKEN"
    assert_status "DELETE /products (delete sync test product)" "200" "$STATUS"

    sleep 3

    request GET "$PRODUCTSERVICE/search/products?query=${UPDATED_NAME}&page=0&size=10"
    if ! echo "$BODY" | grep -q "$UPDATED_NAME"; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: deleted product removed from search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: deleted product still in search"
        FAIL=$((FAIL + 1))
    fi
else
    SKIP=$((SKIP + 4))
    echo -e "  ${YELLOW}SKIP${NC} Update/delete sync tests (no product ID)"
fi

# --------------------------------------------------
section "10. Pagination & Sorting"
# --------------------------------------------------

request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&page=0&size=2&sortBy=price&sortDir=asc"
assert_status "Search: page=0, size=2, sort=price asc" "200" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&page=0&size=5&sortBy=name&sortDir=desc"
assert_status "Search: sort=name desc" "200" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=${TIMESTAMP}&page=0&size=5&sortBy=createdAt&sortDir=desc"
assert_status "Search: sort=createdAt desc" "200" "$STATUS"

# --------------------------------------------------
section "11. Edge Cases"
# --------------------------------------------------

request GET "$PRODUCTSERVICE/search/products?query=&page=0&size=10"
assert_status "Search: empty query (returns all)" "200" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=xyznonexistentproduct12345&page=0&size=10"
assert_status "Search: non-existent query" "200" "$STATUS"
NO_RESULT_COUNT=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('totalElements', -1))" 2>/dev/null || echo "-1")
if [ "$NO_RESULT_COUNT" -eq 0 ] 2>/dev/null; then
    echo -e "  ${GREEN}PASS${NC} Non-existent query returns 0 results"
    PASS=$((PASS + 1))
else
    echo -e "  ${RED}FAIL${NC} Non-existent query returned $NO_RESULT_COUNT results"
    FAIL=$((FAIL + 1))
fi

request GET "$PRODUCTSERVICE/search/products?query=test&sortBy=invalidField&page=0&size=10"
assert_status "Search: invalid sort field returns 400" "400" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=test&page=0&size=200"
assert_status "Search: page size > 100 returns 400" "400" "$STATUS"

# --------------------------------------------------
echo ""
echo "=============================================="
printf "  Results: ${GREEN}%d passed${NC}, ${RED}%d failed${NC}, ${YELLOW}%d skipped${NC}\n" "$PASS" "$FAIL" "$SKIP"
echo "=============================================="

# Exit with failure if any tests failed
[ "$FAIL" -eq 0 ]

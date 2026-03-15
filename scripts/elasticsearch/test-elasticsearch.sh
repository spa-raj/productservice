#!/bin/bash
# ==============================================================================
# Elasticsearch Integration Test Suite
# ==============================================================================
# Tests the full Elasticsearch indexing and search pipeline:
#   1. Health checks (userservice, productservice, Elasticsearch)
#   2. OAuth2 token retrieval (admin)
#   3. Create test products
#   4. Trigger full reindex
#   5. Test search (full-text, fuzzy, filters, suggestions)
#   6. Test real-time sync (create → search, update → search, delete → search)
#
# Prerequisites:
#   - docker compose up -d (userservice + productservice + elasticsearch)
#   - vibevault-network exists
#
# Usage:
#   ./test-elasticsearch.sh
# ==============================================================================

set -euo pipefail

USERSERVICE="http://localhost:8081"
PRODUCTSERVICE="http://localhost:8080"
ELASTICSEARCH="http://localhost:9200"

# Local docker-compose credentials
ADMIN_EMAIL="admin@gmail.com"
ADMIN_PASSWORD="abcd@1234"
CLIENT_ID="vibevault-client"
CLIENT_SECRET="abc@12345"
REDIRECT_URI="https://oauth.pstmn.io/v1/callback"
SCOPES="openid+profile+email+read+write"

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

assert_body_not_contains() {
    local description="$1"
    local unexpected_substring="$2"
    local body="$3"

    if ! echo "$body" | grep -qi "$unexpected_substring"; then
        echo -e "  ${GREEN}PASS${NC} $description (does not contain '$unexpected_substring')"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} $description (should not contain '$unexpected_substring')"
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
# OAuth2 Token Flow (adapted for local docker-compose)
# ============================================================================

get_oauth2_token() {
    # Run the entire OAuth2 flow with errexit disabled to prevent script exit on grep failures
    set +e
    local username="$1"
    local password="$2"

    local COOKIE_JAR
    COOKIE_JAR=$(mktemp /tmp/es_test_cookies.XXXXXX)

    local AUTH_URL="${USERSERVICE}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPES}"

    # Hit authorize → login page
    curl -s -c "$COOKIE_JAR" -b "$COOKIE_JAR" -L --max-redirs 1 -o /dev/null "$AUTH_URL"

    # Get CSRF token
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

    # POST login
    local ENCODED_PASSWORD
    ENCODED_PASSWORD=$(urlencode "$password")
    curl -s -D- -o /dev/null -c "$COOKIE_JAR" -b "$COOKIE_JAR" -X POST "${USERSERVICE}/login" \
        -d "username=${username}&password=${ENCODED_PASSWORD}&_csrf=${CSRF}" > /dev/null

    # Follow redirect to authorize
    local AUTHORIZE_RESPONSE
    AUTHORIZE_RESPONSE=$(curl -s -D- -c "$COOKIE_JAR" -b "$COOKIE_JAR" \
        "${USERSERVICE}/oauth2/authorize?response_type=code&client_id=${CLIENT_ID}&redirect_uri=${REDIRECT_URI}&scope=${SCOPES}&continue")

    local AUTHORIZE_LOCATION
    AUTHORIZE_LOCATION=$(echo "$AUTHORIZE_RESPONSE" | grep -i "^Location:" | tr -d '\r' || true)

    local AUTH_CODE=""

    if echo "$AUTHORIZE_LOCATION" | grep -q "code="; then
        AUTH_CODE=$(echo "$AUTHORIZE_LOCATION" | grep -oP 'code=\K[^&\s]+' || true)
    else
        # Submit consent
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

    # Exchange code for token
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
echo "  Elasticsearch Integration Test Suite"
echo "=============================================="

# --------------------------------------------------
section "1. Health Checks"
# --------------------------------------------------

request GET "$USERSERVICE/actuator/health"
assert_status "userservice health" "200" "$STATUS"

request GET "$PRODUCTSERVICE/actuator/health"
assert_status "productservice health" "200" "$STATUS"

ES_RESPONSE=$(curl -s -w "\n%{http_code}" "$ELASTICSEARCH")
ES_BODY=$(echo "$ES_RESPONSE" | head -n -1)
ES_STATUS=$(echo "$ES_RESPONSE" | tail -n 1)
assert_status "elasticsearch health" "200" "$ES_STATUS"

if [ "$ES_STATUS" = "200" ]; then
    ES_VERSION=$(echo "$ES_BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['version']['number'])" 2>/dev/null || echo "unknown")
    echo -e "  ${CYAN}Elasticsearch version: ${ES_VERSION}${NC}"
fi

# --------------------------------------------------
section "2. OAuth2 Token"
# --------------------------------------------------

echo "  Obtaining admin OAuth2 token..."
TOKEN=$(get_oauth2_token "$ADMIN_EMAIL" "$ADMIN_PASSWORD")

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

# Create categories
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

# Create products with known searchable content
request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Premium Leather Wallet ${TIMESTAMP}\",\"description\":\"Handcrafted genuine leather wallet with RFID blocking\",\"price\":1499.99,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (Premium Leather Wallet)" "200" "$STATUS"
PRODUCT_1_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Wireless Bluetooth Headphones ${TIMESTAMP}\",\"description\":\"Noise cancelling over-ear headphones with 30hr battery\",\"price\":3999.00,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (Wireless Bluetooth Headphones)" "200" "$STATUS"
PRODUCT_2_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"Cotton Summer T-Shirt ${TIMESTAMP}\",\"description\":\"Breathable organic cotton t-shirt for summer\",\"price\":599.00,\"currency\":\"INR\",\"categoryName\":\"Clothing\"}"
assert_status "POST /products (Cotton Summer T-Shirt)" "200" "$STATUS"
PRODUCT_3_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

# --------------------------------------------------
section "4. Full Reindex"
# --------------------------------------------------

request POST "$PRODUCTSERVICE/admin/index/reindex" "Authorization: Bearer $TOKEN"
assert_status "POST /admin/index/reindex" "200" "$STATUS"

if [ "$STATUS" = "200" ]; then
    INDEXED_COUNT=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('productsIndexed', 'unknown'))" 2>/dev/null || echo "unknown")
    echo -e "  ${CYAN}Products indexed: ${INDEXED_COUNT}${NC}"
fi

# Wait for ES to refresh
sleep 2

# --------------------------------------------------
section "5. Elasticsearch Index Verification"
# --------------------------------------------------

ES_COUNT_RESPONSE=$(curl -s "$ELASTICSEARCH/products/_count")
ES_DOC_COUNT=$(echo "$ES_COUNT_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('count', 0))" 2>/dev/null || echo "0")
echo -e "  ${CYAN}Documents in ES index: ${ES_DOC_COUNT}${NC}"

if [ "$ES_DOC_COUNT" -gt 0 ] 2>/dev/null; then
    echo -e "  ${GREEN}PASS${NC} ES index has documents"
    PASS=$((PASS + 1))
else
    echo -e "  ${RED}FAIL${NC} ES index is empty"
    FAIL=$((FAIL + 1))
fi

# --------------------------------------------------
section "6. Full-Text Search"
# --------------------------------------------------

# Search by product name
request GET "$PRODUCTSERVICE/search/products?query=leather+wallet&page=0&size=10"
assert_status "Search: 'leather wallet'" "200" "$STATUS"
assert_body_contains "Search results contain wallet" "wallet" "$BODY"

# Search by description content (not possible with MySQL LIKE on CLOB)
request GET "$PRODUCTSERVICE/search/products?query=RFID+blocking&page=0&size=10"
assert_status "Search: 'RFID blocking' (description search)" "200" "$STATUS"
assert_body_contains "Description search finds RFID product" "RFID" "$BODY"

# Search by description — noise cancelling
request GET "$PRODUCTSERVICE/search/products?query=noise+cancelling&page=0&size=10"
assert_status "Search: 'noise cancelling' (description)" "200" "$STATUS"
assert_body_contains "Description search finds headphones" "headphones" "$BODY"

# --------------------------------------------------
section "7. Fuzzy Search (Typo Tolerance)"
# --------------------------------------------------

# Misspelled: "lether" instead of "leather"
request GET "$PRODUCTSERVICE/search/products?query=lether+walet&page=0&size=10"
assert_status "Fuzzy search: 'lether walet'" "200" "$STATUS"
assert_body_contains "Fuzzy search finds wallet despite typos" "wallet" "$BODY"

# Misspelled: "headhpones" instead of "headphones"
request GET "$PRODUCTSERVICE/search/products?query=headhpones&page=0&size=10"
assert_status "Fuzzy search: 'headhpones'" "200" "$STATUS"
assert_body_contains "Fuzzy search finds headphones despite typo" "headphones" "$BODY"

# --------------------------------------------------
section "8. Filtered Search"
# --------------------------------------------------

# Price range filter
request GET "$PRODUCTSERVICE/search/products?query=&minPrice=1000&maxPrice=5000&page=0&size=10"
assert_status "Search: price range 1000-5000" "200" "$STATUS"

# Currency filter
request GET "$PRODUCTSERVICE/search/products?query=&currency=INR&page=0&size=10"
assert_status "Search: currency=INR" "200" "$STATUS"

# Category filter
request GET "$PRODUCTSERVICE/search/products?query=&categoryName=Electronics&page=0&size=10"
assert_status "Search: categoryName=Electronics" "200" "$STATUS"
assert_body_contains "Electronics filter returns electronics" "Electronics" "$BODY"

# Combined: query + category + price
request GET "$PRODUCTSERVICE/search/products?query=wireless&categoryName=Electronics&minPrice=2000&page=0&size=10"
assert_status "Search: query + category + price combined" "200" "$STATUS"

# --------------------------------------------------
section "9. Suggestions (Typeahead)"
# --------------------------------------------------

request GET "$PRODUCTSERVICE/search/products/suggest?prefix=Prem&limit=5"
assert_status "Suggest: prefix='Prem'" "200" "$STATUS"
assert_body_contains "Suggestions contain Premium product" "Premium" "$BODY"

request GET "$PRODUCTSERVICE/search/products/suggest?prefix=Wire&limit=5"
assert_status "Suggest: prefix='Wire'" "200" "$STATUS"
assert_body_contains "Suggestions contain Wireless product" "Wireless" "$BODY"

# --------------------------------------------------
section "10. Real-Time Sync (Event-Driven Indexing)"
# --------------------------------------------------

# Create a new product and verify it appears in search
SYNC_PRODUCT_NAME="SyncTest-Laptop-${TIMESTAMP}"
request POST "$PRODUCTSERVICE/products" "$AUTH_HEADERS" \
    "{\"name\":\"${SYNC_PRODUCT_NAME}\",\"description\":\"Real-time sync test product\",\"price\":49999.00,\"currency\":\"INR\",\"categoryName\":\"Electronics\"}"
assert_status "POST /products (sync test product)" "200" "$STATUS"
SYNC_PRODUCT_ID=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

# Wait for async indexing + ES refresh
sleep 3

# Search for the newly created product
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

# Update the product and verify search reflects the change
if [ -n "$SYNC_PRODUCT_ID" ]; then
    UPDATED_NAME="SyncTest-Updated-Laptop-${TIMESTAMP}"
    request PATCH "$PRODUCTSERVICE/products/$SYNC_PRODUCT_ID" "$AUTH_HEADERS" \
        "{\"name\":\"${UPDATED_NAME}\"}"
    assert_status "PATCH /products (update sync test product)" "200" "$STATUS"

    sleep 3

    # Old name should not return results
    request GET "$PRODUCTSERVICE/search/products?query=${SYNC_PRODUCT_NAME}&page=0&size=10"
    if ! echo "$BODY" | grep -q "$SYNC_PRODUCT_NAME"; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: old name no longer found in search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: old name still found in search"
        FAIL=$((FAIL + 1))
    fi

    # New name should return results
    request GET "$PRODUCTSERVICE/search/products?query=${UPDATED_NAME}&page=0&size=10"
    NEW_RESULTS=$(echo "$BODY" | python3 -c "import sys,json; print(json.load(sys.stdin).get('totalElements', 0))" 2>/dev/null || echo "0")

    if [ "$NEW_RESULTS" -gt 0 ] 2>/dev/null; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: updated name found in search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: updated name NOT found in search"
        FAIL=$((FAIL + 1))
    fi

    # Delete the product and verify it disappears from search
    request DELETE "$PRODUCTSERVICE/products/$SYNC_PRODUCT_ID" "Authorization: Bearer $TOKEN"
    assert_status "DELETE /products (delete sync test product)" "200" "$STATUS"

    # Wait for async indexing + ES refresh, then verify deleted product is gone
    sleep 3

    request GET "$PRODUCTSERVICE/search/products?query=${UPDATED_NAME}&page=0&size=10"
    # Check that the specific deleted product name does NOT appear in results
    if ! echo "$BODY" | grep -q "$UPDATED_NAME"; then
        echo -e "  ${GREEN}PASS${NC} Real-time indexing: deleted product removed from search"
        PASS=$((PASS + 1))
    else
        echo -e "  ${RED}FAIL${NC} Real-time indexing: deleted product still in search"
        FAIL=$((FAIL + 1))
    fi
else
    SKIP=$((SKIP + 3))
    echo -e "  ${YELLOW}SKIP${NC} Update/delete sync tests (no product ID)"
fi

# --------------------------------------------------
section "11. Pagination & Sorting"
# --------------------------------------------------

request GET "$PRODUCTSERVICE/search/products?query=&page=0&size=2&sortBy=price&sortDir=asc"
assert_status "Search: page=0, size=2, sort=price asc" "200" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=&page=0&size=5&sortBy=name&sortDir=desc"
assert_status "Search: sort=name desc" "200" "$STATUS"

request GET "$PRODUCTSERVICE/search/products?query=&page=0&size=5&sortBy=createdAt&sortDir=desc"
assert_status "Search: sort=createdAt desc" "200" "$STATUS"

# --------------------------------------------------
section "12. Edge Cases"
# --------------------------------------------------

# Empty query (should return all non-deleted products)
request GET "$PRODUCTSERVICE/search/products?query=&page=0&size=10"
assert_status "Search: empty query (returns all)" "200" "$STATUS"

# Query with no results
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

# Invalid sort field
request GET "$PRODUCTSERVICE/search/products?query=test&sortBy=invalidField&page=0&size=10"
assert_status "Search: invalid sort field returns 400" "400" "$STATUS"

# Page size exceeds max
request GET "$PRODUCTSERVICE/search/products?query=test&page=0&size=200"
assert_status "Search: page size > 100 returns 400" "400" "$STATUS"

# --------------------------------------------------
echo ""
echo "=============================================="
printf "  Results: ${GREEN}%d passed${NC}, ${RED}%d failed${NC}, ${YELLOW}%d skipped${NC}\n" "$PASS" "$FAIL" "$SKIP"
echo "=============================================="

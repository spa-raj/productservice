# Kubernetes API Testing Guide

Guide for testing productservice APIs in a Kubernetes (minikube) environment using terminal-based OAuth2 authentication against the userservice authorization server.

## Prerequisites

- Both `userservice` and `productservice` deployed in the `vibevault` namespace
- Port-forwards active:
  ```bash
  kubectl port-forward -n vibevault service/userservice 8081:80 &
  kubectl port-forward -n vibevault service/productservice 8080:80 &
  ```

## Obtaining an OAuth2 Token from Terminal

The userservice uses Spring Authorization Server with form-based login. Since there's no browser, the OAuth2 Authorization Code flow must be driven manually with `curl`.

### Step 1: Get the Login Page CSRF Token

```bash
curl -s -c /tmp/cookies.txt http://localhost:8081/login > /tmp/login.html
CSRF=$(grep -o 'name="_csrf"[^>]*value="[^"]*"' /tmp/login.html | grep -o 'value="[^"]*"' | cut -d'"' -f2)
```

### Step 2: Submit Login Credentials

```bash
curl -s -c /tmp/cookies.txt -b /tmp/cookies.txt \
  -X POST http://localhost:8081/login \
  -d "username=admin@gmail.com&password=abcd@1234&_csrf=$CSRF" > /dev/null
```

This authenticates the session. The session cookie is stored in `/tmp/cookies.txt`.

### Step 3: Initiate the Authorization Code Request

```bash
REDIRECT=$(curl -s -v -c /tmp/cookies.txt -b /tmp/cookies.txt \
  "http://localhost:8081/oauth2/authorize?client_id=vibevault-client&response_type=code&scope=openid%20profile%20email%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback&state=test123" \
  2>&1 | grep -i "^< location:" | sed 's/< location: //' | tr -d '\r')
```

**Two possible outcomes:**

- **Redirect contains `code=`** — consent was previously granted, skip to Step 5
- **Redirect to login or consent page** — proceed to Step 4

### Step 4: Submit Consent (First Time Only)

Extract the state parameter from the consent page and submit:

```bash
CONSENT_PAGE=$(curl -s -c /tmp/cookies.txt -b /tmp/cookies.txt \
  "http://localhost:8081/oauth2/authorize?client_id=vibevault-client&response_type=code&scope=openid%20profile%20email%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback&state=test123")

STATE=$(echo "$CONSENT_PAGE" | grep -o 'name="state"[^>]*value="[^"]*"' | grep -o 'value="[^"]*"' | cut -d'"' -f2)

REDIRECT=$(curl -s -v -c /tmp/cookies.txt -b /tmp/cookies.txt \
  -X POST http://localhost:8081/oauth2/authorize \
  -d "client_id=vibevault-client&state=$STATE&scope=read&scope=profile&scope=write&scope=email" \
  2>&1 | grep -i "^< location:" | sed 's/< location: //' | tr -d '\r')
```

### Step 5: Extract the Authorization Code

```bash
CODE=$(echo "$REDIRECT" | grep -o 'code=[^&]*' | cut -d= -f2)
```

### Step 6: Exchange Code for Access Token

```bash
TOKEN_RESPONSE=$(curl -s -X POST http://localhost:8081/oauth2/token \
  -u "vibevault-client:abc@12345" \
  -d "grant_type=authorization_code&code=$CODE&redirect_uri=https://oauth.pstmn.io/v1/callback")

ADMIN_TOKEN=$(echo "$TOKEN_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
```

### Verify Token Claims

```bash
echo "$ADMIN_TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null | python3 -m json.tool
```

Expected output includes `"roles": ["ADMIN"]`.

## Quick Token Script (All-in-One)

```bash
#!/bin/bash
# get-admin-token.sh — Obtain an ADMIN OAuth2 token from userservice

curl -s -c /tmp/cookies.txt http://localhost:8081/login > /tmp/login.html
CSRF=$(grep -o 'name="_csrf"[^>]*value="[^"]*"' /tmp/login.html | grep -o 'value="[^"]*"' | cut -d'"' -f2)

curl -s -c /tmp/cookies.txt -b /tmp/cookies.txt -X POST http://localhost:8081/login \
  -d "username=admin@gmail.com&password=abcd@1234&_csrf=$CSRF" > /dev/null

REDIRECT=$(curl -s -v -c /tmp/cookies.txt -b /tmp/cookies.txt \
  "http://localhost:8081/oauth2/authorize?client_id=vibevault-client&response_type=code&scope=openid%20profile%20email%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback&state=test" \
  2>&1 | grep -i "^< location:" | sed 's/< location: //' | tr -d '\r')

CODE=$(echo "$REDIRECT" | grep -o 'code=[^&]*' | cut -d= -f2)

if [ -z "$CODE" ]; then
  CONSENT_PAGE=$(curl -s -c /tmp/cookies.txt -b /tmp/cookies.txt \
    "http://localhost:8081/oauth2/authorize?client_id=vibevault-client&response_type=code&scope=openid%20profile%20email%20read%20write&redirect_uri=https://oauth.pstmn.io/v1/callback&state=test")
  STATE=$(echo "$CONSENT_PAGE" | grep -o 'name="state"[^>]*value="[^"]*"' | grep -o 'value="[^"]*"' | cut -d'"' -f2)
  REDIRECT=$(curl -s -v -c /tmp/cookies.txt -b /tmp/cookies.txt \
    -X POST http://localhost:8081/oauth2/authorize \
    -d "client_id=vibevault-client&state=$STATE&scope=read&scope=profile&scope=write&scope=email" \
    2>&1 | grep -i "^< location:" | sed 's/< location: //' | tr -d '\r')
  CODE=$(echo "$REDIRECT" | grep -o 'code=[^&]*' | cut -d= -f2)
fi

ADMIN_TOKEN=$(curl -s -X POST http://localhost:8081/oauth2/token \
  -u "vibevault-client:abc@12345" \
  -d "grant_type=authorization_code&code=$CODE&redirect_uri=https://oauth.pstmn.io/v1/callback" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

echo "$ADMIN_TOKEN"
```

Usage:
```bash
export ADMIN_TOKEN=$(bash get-admin-token.sh)
```

## Client Credentials Token (No Roles)

For testing public endpoints or verifying 403 responses:

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/oauth2/token \
  -u "vibevault-client:abc@12345" \
  -d "grant_type=client_credentials&scope=openid read write" \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
```

This token has `"roles": []` — authenticated but no ADMIN/SELLER role.

## Testing the APIs

### Public Endpoints (No Token Required)

```bash
# Health check
curl -s http://localhost:8080/actuator/health

# List products
curl -s http://localhost:8080/products

# List categories
curl -s http://localhost:8080/categories

# Get product by ID
curl -s http://localhost:8080/products/{id}

# Get category by name
curl -s http://localhost:8080/categories/name/Electronics

# Get products in a category
curl -s http://localhost:8080/categories/products/Electronics

# Search products
curl -s "http://localhost:8080/search/products?query=iphone&minPrice=100&maxPrice=1000&sortBy=price&sortDir=asc"

# Autocomplete suggestions
curl -s "http://localhost:8080/search/products/suggest?prefix=iPh"
```

### Authenticated Endpoints (ADMIN/SELLER Token Required)

```bash
# Create category
curl -s -X POST http://localhost:8080/categories \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Devices and gadgets"}'

# Create product
curl -s -X POST http://localhost:8080/products \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 14","description":"Apple smartphone","imageUrl":"https://example.com/iphone.jpg","price":699.99,"currency":"USD","categoryName":"Electronics"}'

# Update product (PATCH)
curl -s -X PATCH http://localhost:8080/products/{id} \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 14 Pro","price":999.99,"currency":"USD","categoryName":"Electronics"}'

# Replace product (PUT)
curl -s -X PUT http://localhost:8080/products/{id} \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"iPhone 15","description":"New model","imageUrl":"https://example.com/iphone15.jpg","price":1099.99,"currency":"USD","categoryName":"Electronics"}'

# Delete product
curl -s -X DELETE http://localhost:8080/products/{id} \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

## Known Issues

### RSA Key Mismatch with Multiple Userservice Replicas

Each userservice pod generates its own in-memory RSA keypair on startup. With multiple replicas, a token signed by one pod may fail validation when the JWKS is fetched from another pod with a different key.

**Workaround:** Scale userservice to 1 replica for testing:
```bash
kubectl scale deployment/userservice -n vibevault --replicas=1
```

**Production fix:** Persist RSA keys externally (database, K8s Secret, or vault). Tracked in [userservice#25](https://github.com/spa-raj/userservice/issues/25).

### Issuer URI Must Match Token Claims

The productservice `ISSUER_URI` config must exactly match the `iss` claim in tokens issued by userservice. The userservice sets its issuer to `http://userservice:8081`, so:
- The userservice K8s Service must expose port 8081 (in addition to 80)
- The productservice configmap must use `ISSUER_URI: "http://userservice:8081"`

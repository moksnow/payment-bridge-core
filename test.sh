#!/bin/bash

# ============================================================
#  Payment Bridge — Full Flow Test Script
#  Author: Moh Khandan
# ============================================================

BASE_URL="http://localhost:8080/api"
SENDER_EMAIL="sender@test.com"
SENDER_PASS="Password123!"
RECEIVER_EMAIL="receiver@test.com"
RECEIVER_PASS="Password123!"

GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

print_step() {
  echo ""
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
  echo -e "${CYAN}  STEP $1: $2${NC}"
  echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

print_ok()   { echo -e "${GREEN}  ✅ $1${NC}"; }
print_fail() { echo -e "${RED}  ❌ $1${NC}"; }
print_info() { echo -e "${YELLOW}  ℹ  $1${NC}"; }

check_status() {
  local status=$1
  local expected=$2
  local label=$3
  if [ "$status" -eq "$expected" ]; then
    print_ok "$label (HTTP $status)"
  else
    print_fail "$label — expected HTTP $expected, got HTTP $status"
  fi
}

json_field() {
  echo "$1" | grep -o "\"$2\":\"[^\"]*\"" | head -1 | cut -d'"' -f4
}

# ============================================================
#  STEP 1: Register Sender
# ============================================================
print_step "1" "Register Sender User"

BODY=$(curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$SENDER_EMAIL\", \"password\": \"$SENDER_PASS\"}")

if echo "$BODY" | grep -q "EMAIL_TAKEN"; then
  print_info "Sender already exists, logging in..."
  BODY=$(curl -s -X POST "$BASE_URL/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$SENDER_EMAIL\", \"password\": \"$SENDER_PASS\"}")
fi

SENDER_TOKEN=$(json_field "$BODY" "token")
SENDER_USER_ID=$(json_field "$BODY" "userId")

if [ -z "$SENDER_TOKEN" ]; then
  print_fail "Could not get sender token"
  echo "Response: $BODY"
  exit 1
fi

print_ok "Sender registered/logged in"
print_info "userId: $SENDER_USER_ID"

# ============================================================
#  STEP 2: Register Receiver
# ============================================================
print_step "2" "Register Receiver User"

BODY=$(curl -s -X POST "$BASE_URL/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\": \"$RECEIVER_EMAIL\", \"password\": \"$RECEIVER_PASS\"}")

if echo "$BODY" | grep -q "EMAIL_TAKEN"; then
  print_info "Receiver already exists, logging in..."
  BODY=$(curl -s -X POST "$BASE_URL/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\": \"$RECEIVER_EMAIL\", \"password\": \"$RECEIVER_PASS\"}")
fi

RECEIVER_TOKEN=$(json_field "$BODY" "token")
RECEIVER_USER_ID=$(json_field "$BODY" "userId")

if [ -z "$RECEIVER_TOKEN" ]; then
  print_fail "Could not get receiver token"
  echo "Response: $BODY"
  exit 1
fi

print_ok "Receiver registered/logged in"
print_info "userId: $RECEIVER_USER_ID"

# ============================================================
#  STEP 3: Create Sender Wallet (USD)
# ============================================================
print_step "3" "Create Sender Wallet (USD)"

BODY=$(curl -s -X POST "$BASE_URL/v1/wallets?currency=USD" \
  -H "Authorization: Bearer $SENDER_TOKEN")

if echo "$BODY" | grep -q "WALLET_EXISTS"; then
  print_info "Sender wallet already exists"
elif echo "$BODY" | grep -q '"id"'; then
    print_ok "Sender wallet created"
  else
    print_fail "Wallet creation failed"
    echo "Response: $BODY"
  fi

SENDER_ACCOUNT="WALLET-${SENDER_USER_ID}-USD"
print_info "Account code: $SENDER_ACCOUNT"

# ============================================================
#  STEP 4: Create Receiver Wallets (USD + EUR)
# ============================================================
print_step "4" "Create Receiver Wallets (USD + EUR)"

BODY=$(curl -s -X POST "$BASE_URL/v1/wallets?currency=USD" \
  -H "Authorization: Bearer $RECEIVER_TOKEN")
if echo "$BODY" | grep -q "WALLET_EXISTS"; then
  print_info "Receiver USD wallet already exists"
else
  print_ok "Receiver USD wallet created"
fi

BODY=$(curl -s -X POST "$BASE_URL/v1/wallets?currency=EUR" \
  -H "Authorization: Bearer $RECEIVER_TOKEN")
if echo "$BODY" | grep -q "WALLET_EXISTS"; then
  print_info "Receiver EUR wallet already exists"
else
  print_ok "Receiver EUR wallet created"
fi

RECEIVER_USD_ACCOUNT="WALLET-${RECEIVER_USER_ID}-USD"
RECEIVER_EUR_ACCOUNT="WALLET-${RECEIVER_USER_ID}-EUR"
print_info "Receiver USD: $RECEIVER_USD_ACCOUNT"
print_info "Receiver EUR: $RECEIVER_EUR_ACCOUNT"

# ============================================================
#  STEP 5: Check KYC Level
# ============================================================
print_step "5" "Check Sender KYC Level"

BODY=$(curl -s -X GET "$BASE_URL/v1/compliance/kyc/me" \
  -H "Authorization: Bearer $SENDER_TOKEN")

KYC_LEVEL=$(echo "$BODY" | grep -o '"kycLevel":"[^"]*"' | cut -d'"' -f4)
MAX_TX=$(echo "$BODY" | grep -o '"maxTransactionAmount":[0-9.]*' | cut -d: -f2)
print_info "KYC Level: $KYC_LEVEL | Max transaction: $MAX_TX USD"

# ============================================================
#  STEP 6: Upgrade KYC to BASIC
# ============================================================
print_step "6" "Upgrade Sender KYC to BASIC"

BODY=$(curl -s -w "\n%{http_code}" -X PUT \
  "$BASE_URL/v1/compliance/kyc/$SENDER_USER_ID" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"kycLevel": "BASIC", "notes": "verified manually"}')

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
check_status "$HTTP_STATUS" 200 "KYC upgrade"
KYC_LEVEL=$(echo "$BODY" | grep -o '"kycLevel":"[^"]*"' | cut -d'"' -f4)
print_info "New KYC Level: $KYC_LEVEL"

# ============================================================
#  STEP 7: Deposit Funds to Sender
# ============================================================
print_step "7" "Deposit 1000 USD to Sender"

BODY=$(curl -s -X POST "$BASE_URL/v1/wallets/deposit" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 1000.00, "currency": "USD"}')

BALANCE=$(echo "$BODY" | grep -o '"balance":[0-9.]*' | head -1 | cut -d: -f2)
print_ok "Deposit done — balance: $BALANCE USD"

# ============================================================
#  STEP 8: Payment (USD → USD)
# ============================================================
print_step "8" "Payment — 100 USD to Receiver (MOCK)"

IDEM_KEY="pay-$(date +%s)-001"
BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_USD_ACCOUNT\",
    \"amount\": 100.00,
    \"currency\": \"USD\",
    \"railType\": \"MOCK\",
    \"description\": \"test payment\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
check_status "$HTTP_STATUS" 201 "USD payment"

PAYMENT_ID=$(json_field "$BODY" "id")
PAY_STATUS=$(json_field "$BODY" "status")
EXTERNAL_REF=$(json_field "$BODY" "externalRef")
print_info "Payment ID: $PAYMENT_ID"
print_info "Status: $PAY_STATUS | Ref: $EXTERNAL_REF"

# ============================================================
#  STEP 9: Payment with FX (USD → EUR)
# ============================================================
print_step "9" "Payment with FX — 100 USD → EUR (MOCK)"

IDEM_KEY_FX="pay-$(date +%s)-002"
BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY_FX" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_EUR_ACCOUNT\",
    \"amount\": 100.00,
    \"currency\": \"USD\",
    \"receiveCurrency\": \"EUR\",
    \"railType\": \"MOCK\",
    \"description\": \"fx payment\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
check_status "$HTTP_STATUS" 201 "FX payment"

FX_PAYMENT_ID=$(json_field "$BODY" "id")
FX_RATE=$(echo "$BODY" | grep -o '"fxRate":[0-9.]*' | cut -d: -f2)
RECEIVE_AMT=$(echo "$BODY" | grep -o '"receiveAmount":[0-9.]*' | cut -d: -f2)
print_info "FX Rate: $FX_RATE | Receiver gets: $RECEIVE_AMT EUR"
print_info "Payment ID: $FX_PAYMENT_ID"

# ============================================================
#  STEP 10: Check Sender Balance
# ============================================================
print_step "10" "Check Sender Balance"

BODY=$(curl -s -X GET "$BASE_URL/v1/wallets" \
  -H "Authorization: Bearer $SENDER_TOKEN")

BALANCE=$(echo "$BODY" | grep -o '"balance":[0-9.]*' | head -1 | cut -d: -f2)
print_ok "Sender USD balance: $BALANCE"
echo "$BODY"

# ============================================================
#  STEP 11: View Ledger for USD Payment
# ============================================================
print_step "11" "View Ledger — USD Payment ($PAYMENT_ID)"

BODY=$(curl -s -X GET "$BASE_URL/v1/ledger/payments/$PAYMENT_ID" \
  -H "Authorization: Bearer $SENDER_TOKEN")

ENTRY_COUNT=$(echo "$BODY" | grep -o '"entryType"' | wc -l)
print_ok "Ledger entries found: $ENTRY_COUNT (expected: 2)"
echo "$BODY"

# ============================================================
#  STEP 12: View Account History
# ============================================================
print_step "12" "View Account History — $SENDER_ACCOUNT"

BODY=$(curl -s -X GET "$BASE_URL/v1/ledger/accounts/$SENDER_ACCOUNT" \
  -H "Authorization: Bearer $SENDER_TOKEN")

ENTRY_COUNT=$(echo "$BODY" | grep -o '"entryType"' | wc -l)
print_ok "Total ledger entries for account: $ENTRY_COUNT"
echo "$BODY"

# ============================================================
#  STEP 13: Simulate Failed Payment
# ============================================================
print_step "13" "Simulate Failed Payment (description contains 'fail')"

IDEM_KEY_FAIL="pay-$(date +%s)-003"
BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY_FAIL" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_USD_ACCOUNT\",
    \"amount\": 50.00,
    \"currency\": \"USD\",
    \"railType\": \"MOCK\",
    \"description\": \"fail this payment\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
FAIL_STATUS=$(json_field "$BODY" "status")
FAIL_REASON=$(json_field "$BODY" "failureReason")
print_info "Status: $FAIL_STATUS | Reason: $FAIL_REASON"

if [ "$FAIL_STATUS" = "FAILED" ]; then
  print_ok "Payment correctly failed (HTTP $HTTP_STATUS)"
else
  print_fail "Expected FAILED status, got $FAIL_STATUS"
fi

# ============================================================
#  STEP 14: Test Idempotency
# ============================================================
print_step "14" "Test Duplicate Idempotency Key"

BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_USD_ACCOUNT\",
    \"amount\": 100.00,
    \"currency\": \"USD\",
    \"railType\": \"MOCK\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
check_status "$HTTP_STATUS" 409 "Duplicate request blocked"

# ============================================================
#  STEP 15: View AML Flags
# ============================================================
print_step "15" "View AML Flags"

BODY=$(curl -s -X GET "$BASE_URL/v1/compliance/aml/me" \
  -H "Authorization: Bearer $SENDER_TOKEN")

FLAG_COUNT=$(echo "$BODY" | grep -o '"status"' | wc -l)
print_ok "AML entries recorded: $FLAG_COUNT"
echo "$BODY"


# ============================================================
#  STEP 16: CBDC Payment (USDC)
# ============================================================
print_step "16" "CBDC Payment — 10 USDC (CBDC_SANDBOX)"

# ابتدا wallet USDC برای sender و receiver بساز
curl -s -X POST "$BASE_URL/v1/wallets?currency=USDC" \
  -H "Authorization: Bearer $SENDER_TOKEN" > /dev/null

curl -s -X POST "$BASE_URL/v1/wallets?currency=USDC" \
  -H "Authorization: Bearer $RECEIVER_TOKEN" > /dev/null

# deposit USDC به sender
curl -s -X POST "$BASE_URL/v1/wallets/deposit" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount": 100.00, "currency": "USDC"}' > /dev/null

RECEIVER_USDC_ACCOUNT="WALLET-${RECEIVER_USER_ID}-USDC"
IDEM_KEY_CBDC="pay-$(date +%s)-cbdc"

BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY_CBDC" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_USDC_ACCOUNT\",
    \"amount\": 10.00,
    \"currency\": \"USDC\",
    \"railType\": \"CBDC_SANDBOX\",
    \"description\": \"cbdc test payment\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
check_status "$HTTP_STATUS" 201 "CBDC payment"

CBDC_PAYMENT_ID=$(json_field "$BODY" "id")
CBDC_STATUS=$(json_field "$BODY" "status")
CBDC_REF=$(json_field "$BODY" "externalRef")
print_info "CBDC Payment ID: $CBDC_PAYMENT_ID"
print_info "Status: $CBDC_STATUS | Network Ref: $CBDC_REF"


# ============================================================
#  STEP 17: CBDC Mint (USD → USDC)
# ============================================================
print_step "17" "CBDC Mint — 50 USD → USDC (Fiat to Digital)"

curl -s -X PUT "$BASE_URL/v1/compliance/kyc/$SENDER_USER_ID" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"kycLevel": "FULL", "notes": "upgraded for CBDC mint test"}' > /dev/null

curl -s -X POST "$BASE_URL/v1/wallets?currency=USDC" \
  -H "Authorization: Bearer $RECEIVER_TOKEN" > /dev/null

IDEM_KEY_MINT="pay-$(date +%s)-mint"
BODY=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/v1/payments" \
  -H "Authorization: Bearer $SENDER_TOKEN" \
  -H "Content-Type: application/json" \
  -H "X-Idempotency-Key: $IDEM_KEY_MINT" \
  -d "{
    \"receiverWalletAccountCode\": \"$RECEIVER_USDC_ACCOUNT\",
    \"amount\": 50.00,
    \"currency\": \"USD\",
    \"receiveCurrency\": \"USDC\",
    \"railType\": \"CBDC_SANDBOX\",
    \"description\": \"mint usdc from usd\"
  }")

HTTP_STATUS=$(echo "$BODY" | tail -1)
BODY=$(echo "$BODY" | sed '$d')
check_status "$HTTP_STATUS" 201 "CBDC MINT (USD→USDC)"

MINT_STATUS=$(json_field "$BODY" "status")
MINT_REF=$(json_field "$BODY" "externalRef")
print_info "Mint Status: $MINT_STATUS | Network Ref: $MINT_REF"

# ============================================================
#  STEP 18: Query CBDC Networks
# ============================================================
print_step "18" "Query CBDC Networks"

BODY=$(curl -s -X GET "$BASE_URL/cbdc-sandbox/networks")
print_ok "CBDC networks available"
echo "$BODY"

# ============================================================
#  Summary
# ============================================================
echo ""
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  ✅ Test flow complete${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "  Sender ID     : $SENDER_USER_ID"
echo -e "  Receiver ID   : $RECEIVER_USER_ID"
echo -e "  USD Payment   : $PAYMENT_ID"
echo -e "  FX  Payment   : $FX_PAYMENT_ID
  CBDC Payment  : $CBDC_PAYMENT_ID"
echo ""

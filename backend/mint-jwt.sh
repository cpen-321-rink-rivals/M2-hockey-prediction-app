#!/usr/bin/env bash
# mint-jwt.sh — create an HS256 JWT from the shell
# deps: openssl (required), jq (optional)

set -euo pipefail

# Defaults (override via flags or env)
SECRET="${SECRET:-${JWT_SECRET:-}}"
ID="${ID:-670fd4e402b8b6a8f5b9a0c1}"
EMAIL="${EMAIL:-dev@example.com}"
AUD="${AUD:-your-api}"
ISS="${ISS:-local-auth}"
TTL="${TTL:-3600}"   # seconds

usage() {
  cat >&2 <<EOF
Usage: JWT_SECRET=... $0 [options]

Options:
  -S, --secret  <str>   JWT secret (or env JWT_SECRET)
  -i, --id      <str>   "id" claim (default: $ID)
  -e, --email   <str>   "email" claim (default: $EMAIL)
  -a, --aud     <str>   "aud" claim (default: $AUD)
  -s, --iss     <str>   "iss" claim (default: $ISS)
  -t, --ttl     <int>   TTL in seconds (default: $TTL)
  -h, --help            Show this help
EOF
}

# Parse flags
while [[ $# -gt 0 ]]; do
  case "$1" in
    -S|--secret) SECRET="${2:-}"; shift 2;;
    -i|--id)     ID="${2:-}"; shift 2;;
    -e|--email)  EMAIL="${2:-}"; shift 2;;
    -a|--aud)    AUD="${2:-}"; shift 2;;
    -s|--iss)    ISS="${2:-}"; shift 2;;
    -t|--ttl)    TTL="${2:-}"; shift 2;;
    -h|--help)   usage; exit 0;;
    *) echo "Unknown option: $1" >&2; usage; exit 1;;
  esac
done

if [[ -z "${SECRET}" ]]; then
  echo "JWT secret is empty. Set \$JWT_SECRET or pass --secret." >&2
  exit 1
fi

# Base64URL (RFC 7515) without padding
b64url() {
  # -A = single line, portable via openssl
  openssl base64 -A | tr '+/' '-_' | tr -d '='
}

now=$(date +%s)
exp=$(( now + TTL ))

header='{"alg":"HS256","typ":"JWT"}'

# Build payload (use jq if present; otherwise fall back to printf)
if command -v jq >/dev/null 2>&1; then
  payload=$(jq -c -n \
    --arg id "$ID" \
    --arg email "$EMAIL" \
    --arg aud "$AUD" \
    --arg iss "$ISS" \
    --argjson iat "$now" \
    --argjson exp "$exp" \
    '{id:$id,email:$email,aud:$aud,iss:$iss,iat:$iat,exp:$exp}')
else
  # Minimal JSON (no escaping for odd chars — prefer jq if inputs may contain quotes)
  payload=$(printf '{"id":"%s","email":"%s","aud":"%s","iss":"%s","iat":%d,"exp":%d}' \
                  "$ID" "$EMAIL" "$AUD" "$ISS" "$now" "$exp")
fi

seg1=$(printf '%s' "$header"  | b64url)
seg2=$(printf '%s' "$payload" | b64url)
to_sign="$seg1.$seg2"

# HMAC-SHA256 (binary) then base64url
# Use -mac HMAC with key:SECRET; quoted to preserve special chars.
sig=$(printf '%s' "$to_sign" \
  | openssl dgst -binary -sha256 -mac HMAC -macopt key:"$SECRET" \
  | b64url)

printf '%s.%s\n' "$to_sign" "$sig"

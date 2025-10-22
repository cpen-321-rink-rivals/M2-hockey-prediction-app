param(
  [string]$Secret = $env:JWT_SECRET,           # or pass -Secret '...'
  [string]$Id     = "670fd4e402b8b6a8f5b9a0c1",
  [string]$Email  = "dev@example.com",
  [string]$Aud    = "your-api",
  [string]$Iss    = "local-auth",
  [int]$TTLSeconds = 3600                       # 1h
)

function B64Url([byte[]]$b) {
  [Convert]::ToBase64String($b).TrimEnd('=').Replace('+','-').Replace('/','_')
}

if ([string]::IsNullOrWhiteSpace($Secret)) {
  throw "JWT secret is empty. Set `$env:JWT_SECRET or pass -Secret '...'."
}

$now = [int][double]::Parse((Get-Date -UFormat %s))
$exp = $now + $TTLSeconds

$headerJson = '{"alg":"HS256","typ":"JWT"}'
$payloadObj = @{
  id    = $Id
  email = $Email
  aud   = $Aud
  iss   = $Iss
  iat   = $now
  exp   = $exp
}
$payloadJson = ($payloadObj | ConvertTo-Json -Compress)

$seg1 = B64Url([Text.Encoding]::UTF8.GetBytes($headerJson))
$seg2 = B64Url([Text.Encoding]::UTF8.GetBytes($payloadJson))
$toSign = "$seg1.$seg2"

# HMAC-SHA256(signing)
# HMAC-SHA256(signing) — avoid ctor overload; set Key explicitly
[byte[]]$keyBytes = [Text.Encoding]::UTF8.GetBytes($Secret)
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = $keyBytes
try {
  $sigBytes = $hmac.ComputeHash([Text.Encoding]::UTF8.GetBytes($toSign))
} finally {
  $hmac.Dispose()
}
$seg3 = B64Url($sigBytes)
$token = "$toSign.$seg3"
$token


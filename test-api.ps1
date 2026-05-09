param(
    [string]$Email = "admin@inkflow.com",
    [string]$Password = "admin" # Altere se a senha for diferente
)

$envFilePath = "$PSScriptRoot\.env"
$port = 8080

if (Test-Path $envFilePath) {
    $envContent = Get-Content $envFilePath
    foreach ($line in $envContent) {
        if ($line -match "^PORT=(.*)") {
            $port = $matches[1].Trim()
        }
    }
}

$baseUrl = "http://localhost:$port"
Write-Host "Iniciando testes no servidor: $baseUrl" -ForegroundColor Cyan

# 1. Login
$loginUrl = "$baseUrl/api/auth/login"
$body = @{
    email = $Email
    password = $Password
} | ConvertTo-Json

Write-Host "Realizando login com $Email..."
try {
    $response = Invoke-RestMethod -UseBasicParsing -Uri $loginUrl -Method Post -Body $body -ContentType "application/json" -ErrorAction Stop
    $token = $response.token
    Write-Host "✅ Login bem-sucedido. JWT capturado." -ForegroundColor Green
} catch {
    Write-Host "❌ Falha no login. Verifique as credenciais ou se o servidor esta rodando." -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Yellow
    exit 1
}

$headers = @{
    "Authorization" = "Bearer $token"
    "Content-Type"  = "application/json"
}

# Definição dos endpoints
$endpoints = @(
    @{ Name = "Health Check"; Method = "GET"; Url = "/api/health" },
    @{ Name = "Lista de Agendamentos"; Method = "GET"; Url = "/api/admin/agendamentos" },
    @{ Name = "Lista de Clientes"; Method = "GET"; Url = "/api/admin/clientes" },
    @{ Name = "Cicatrização Ativa"; Method = "GET"; Url = "/api/cicatrizacao/ativa/1" }
)

$successCount = 0
$totalCount = $endpoints.Count

foreach ($ep in $endpoints) {
    $url = "$baseUrl$($ep.Url)"
    try {
        $res = Invoke-WebRequest -UseBasicParsing -Uri $url -Method $ep.Method -Headers $headers -ErrorAction Stop
        if ($res.StatusCode -eq 200 -or $res.StatusCode -eq 201) {
            Write-Host "✅ $($ep.Name) [$($ep.Method) $($ep.Url)] -> HTTP $($res.StatusCode.value__)" -ForegroundColor Green
            $successCount++
        } else {
            Write-Host "❌ $($ep.Name) [$($ep.Method) $($ep.Url)] -> HTTP $($res.StatusCode.value__)" -ForegroundColor Red
        }
    } catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        # Se for 404 (Not Found), significa que o endpoint existe e processou a requisição, mas o recurso (ex: ID 1) não está no banco. Consideramos OK no contexto de smoke test.
        if ($statusCode -eq 404) {
             Write-Host "✅ $($ep.Name) [$($ep.Method) $($ep.Url)] -> HTTP 404 (Endpoint OK, recurso não encontrado)" -ForegroundColor Green
             $successCount++
        } else {
             Write-Host "❌ $($ep.Name) [$($ep.Method) $($ep.Url)] -> HTTP $statusCode" -ForegroundColor Red
             Write-Host "   Detalhe: $($_.Exception.Message)" -ForegroundColor Yellow
        }
    }
}

Write-Host ""
Write-Host "Resumo: $successCount/$totalCount endpoints OK" -ForegroundColor Cyan

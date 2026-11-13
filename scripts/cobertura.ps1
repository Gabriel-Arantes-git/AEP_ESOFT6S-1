# Gera os relatórios de cobertura do backend (JaCoCo) e do frontend (Vitest).
# Uso: ./scripts/cobertura.ps1 [-Alvo backend|frontend|todos]
param([ValidateSet('backend', 'frontend', 'todos')][string]$Alvo = 'todos')

$ErrorActionPreference = 'Stop'
$raiz = Split-Path -Parent $PSScriptRoot

function Rodar-Backend {
    Write-Host '>> Backend: mvnw verify'
    Push-Location $raiz
    try { & ./mvnw.cmd -B verify; if ($LASTEXITCODE) { throw 'Backend falhou' } } finally { Pop-Location }
    Write-Host "Relatório: $raiz\target\site\jacoco\index.html"
}

function Rodar-Frontend {
    Write-Host '>> Frontend: npm run test:coverage'
    Push-Location "$raiz\frontend"
    try {
        npm ci; if ($LASTEXITCODE) { throw 'npm ci falhou' }
        npm run test:coverage; if ($LASTEXITCODE) { throw 'Frontend falhou' }
    } finally { Pop-Location }
    Write-Host "Relatório: $raiz\frontend\coverage\aep-denuncias\index.html"
}

switch ($Alvo) {
    'backend'  { Rodar-Backend }
    'frontend' { Rodar-Frontend }
    'todos'    { Rodar-Backend; Rodar-Frontend }
}

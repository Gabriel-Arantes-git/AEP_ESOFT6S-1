#!/usr/bin/env bash
# Gera os relatórios de cobertura do backend (JaCoCo) e do frontend (Vitest).
# Uso: ./scripts/cobertura.sh [backend|frontend]   (sem argumento roda os dois)
set -euo pipefail

raiz="$(cd "$(dirname "$0")/.." && pwd)"
alvo="${1:-todos}"

backend() {
  echo ">> Backend: ./mvnw verify"
  (cd "$raiz" && ./mvnw -B verify)
  echo "Relatório: $raiz/target/site/jacoco/index.html"
}

frontend() {
  echo ">> Frontend: npm run test:coverage"
  (cd "$raiz/frontend" && npm ci && npm run test:coverage)
  echo "Relatório: $raiz/frontend/coverage/aep-denuncias/index.html"
}

case "$alvo" in
  backend)  backend ;;
  frontend) frontend ;;
  todos)    backend; frontend ;;
  *) echo "Uso: $0 [backend|frontend]" >&2; exit 1 ;;
esac

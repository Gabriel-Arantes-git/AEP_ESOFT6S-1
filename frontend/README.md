# Frontend — Denúncias Ambientais (AEP)

Aplicação Angular 21 (componentes standalone, sem SSR) que consome a API do backend Spring Boot deste repositório.

## Pré-requisitos

- Node.js 20+ e npm
- Backend rodando em `http://localhost:8080/api` (veja o README da raiz)

## Como rodar

```bash
cd frontend
npm install
npm start
```

A aplicação sobe em `http://localhost:4200`. Em desenvolvimento, as chamadas para `/api` são encaminhadas ao backend pelo proxy definido em `proxy.conf.json` (`/api` → `http://localhost:8080`). O backend já libera CORS para `http://localhost:4200`.

| Ambiente | `apiUrl` | Arquivo |
|---|---|---|
| Desenvolvimento (`npm start`) | `/api` (via proxy) | `src/environments/environment.development.ts` |
| Produção (`npm run build`) | `http://localhost:8080/api` | `src/environments/environment.ts` |

## Rotas disponíveis

| Rota | Tela | Observação |
|---|---|---|
| `/` | — | Redireciona para `/home` |
| `/login` | Login | E-mail e senha; sucesso leva a `/home` |
| `/cadastro` | Cadastro | Cria conta de cidadão (perfil `CIDADAO`) |
| `/home` | Página inicial | Contadores e atalhos; funciona sem login |
| qualquer outra | — | Redireciona para `/login` |

Os atalhos "Nova Solicitação", "Acompanhamento", "Mapa" e "Logs" apontam para telas das próximas entregas (`/solicitacao/nova`, `/acompanhamento`, `/mapa`, `/logs`). Até lá, clicar neles cai no redirecionamento para `/login`.

## Estrutura

```
src/app/
├── core/
│   ├── constants/     Status, prioridades, cores e formatação de prazo
│   ├── guards/        authGuard — protege rotas autenticadas
│   ├── interceptors/  authInterceptor — anexa o token JWT
│   ├── models/        Tipos da API (ids string, como no MongoDB)
│   ├── services/      Auth, categorias, departamentos, enums, logs, SLA, solicitações, geocoding, rascunho
│   └── utils/         Validators de formulário (CPF, senha) e helpers do Leaflet
├── features/
│   ├── auth/login/    Tela de login
│   ├── auth/cadastro/ Tela de cadastro (máscaras de CPF e telefone, senha forte)
│   └── home/          Página inicial com contadores e atalhos
├── shared/components/
│   └── bottom-nav/    Navegação principal (Sair/Entrar, Logs para GESTOR)
├── app.config.ts      Router + HttpClient com o interceptor
└── app.routes.ts      Rotas com carregamento sob demanda
```

## Testes e cobertura

Stack: Vitest (via `@angular/build:unit-test`) + jsdom.

```bash
npm test                  # modo watch
npm run test:coverage     # execução única com cobertura
```

- Relatório HTML: `frontend/coverage/aep-denuncias/index.html` (também `lcov.info`)
- Cobertura mínima: **70%** em statements, branches, functions e lines. Abaixo disso o comando termina com erro, por exemplo: `Coverage for branches (97.08%) does not meet global threshold (99%)`.
- A medição cobre todo `src/app/**/*.ts`; só os arquivos `*.spec.ts` ficam de fora.
- Medição atual: 99,65% de statements, 96,28% de branches, 100% de functions e 99,52% de lines (118 testes).

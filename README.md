# Sistema de Denúncias Ambientais — AEP

Sistema para registro e acompanhamento de denúncias de problemas urbanos e ambientais — descarte irregular de lixo, queimadas, poluição, falhas de manutenção — com triagem, encaminhamento ao departamento responsável e cálculo automático de prazo (SLA). Evolução da versão `aep_2026` (Java + H2 + CLI), agora em Spring Boot com API REST e persistência em MongoDB. A mesma lógica de negócio é exposta por três caminhos: endpoints REST (documentados via Swagger), um frontend Angular e um modo de interação por terminal (perfil `cli`).

## Documentação

| Documento | Conteúdo |
|---|---|
| [`docs/arquitetura.md`](docs/arquitetura.md) | Camadas do backend e do frontend, autenticação, permissões por endpoint, fluxo de status e SLA |
| [`docs/modelo-de-colecoes.md`](docs/modelo-de-colecoes.md) | Diagrama das coleções, relacionamentos e subdocumentos (`localizacao`, `historico`) |
| [`frontend/README.md`](frontend/README.md) | Como rodar e testar o frontend |

## Estrutura do repositório

```
.
├── src/            Backend Spring Boot (API REST + CLI)
├── frontend/       Frontend Angular 21
├── docs/           Arquitetura e modelo de coleções
├── pom.xml         Build Maven (wrapper: mvnw / mvnw.cmd)
└── openspec/       Especificações e histórico de mudanças
```

---

## Funcionalidades

**Cidadão**
- Abertura de solicitação identificada ou anônima
- Consulta por protocolo com histórico completo de movimentações
- Cadastro de conta

**Atendente**
- Visualização da fila por status
- Triagem com definição de prioridade e encaminhamento para departamento destino (calcula prazo automaticamente via SLA)
- Atualização de status com comentário obrigatório

**Gestor**
- Painel geral com todas as solicitações
- Filtro por status
- Atualização de status e encerramento de solicitações
- Consulta à trilha de auditoria (logs) de cada solicitação

---

## Fluxo de Status

```
ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO
                ↘                            ↗
                         ENCERRADO
                   (gestor pode encerrar direto da triagem)
```

Transições fora desse fluxo são rejeitadas (`StatusSolicitacao.podeMoverPara`). Somente o perfil GESTOR pode mover uma solicitação para ENCERRADO.

---

## SLA (Prazo por Prioridade)

| Prioridade | Prazo   | Uso                              |
|------------|---------|-----------------------------------|
| BAIXA      | 7 dias  | Impacto local e baixo risco      |
| MEDIA      | 3 dias  | Impacto moderado                 |
| ALTA       | 24h     | Risco à saúde ou segurança        |
| CRITICA    | 4h      | Risco imediato — emergência       |

O prazo é calculado automaticamente no momento da triagem e armazenado em `prazoAlvo` na solicitação.

---

## Protocolo

Formato: `DEN-YYYY-NNNNN` (ex.: `DEN-2026-00001`)

Gerado automaticamente na abertura. Para denúncias anônimas, é o único meio de acompanhamento.

---

## Arquitetura

Visão completa (backend, frontend, autenticação, permissões e fluxo de status/SLA) em [`docs/arquitetura.md`](docs/arquitetura.md). Estrutura do backend:

```
src/main/java/com/aep/backend/
│
├── BackendApplication.java            Ponto de entrada (modo web e modo cli)
│
├── auth/
│   ├── AuthController.java            Login — emissão de token JWT
│   └── dto/                           LoginRequest, AuthResponse
│
├── cli/
│   ├── TerminalRunner.java            Entrada do modo terminal (perfil "cli")
│   ├── CidadaoMenu.java               Abrir solicitação, consultar protocolo, cadastro
│   ├── AtendenteMenu.java             Fila, triagem, atualização de status
│   ├── GestorMenu.java                Painel geral, status, auditoria
│   └── SolicitacaoPrinter.java        Formatação de saída compartilhada
│
├── domain/
│   ├── abstraction/
│   │   ├── DefaultEntity              Base de todas as entidades (id, dataCadastro)
│   │   ├── Ativavel                   Contrato de exclusão lógica (setAtivo)
│   │   ├── DefaultCrudRepository<E>   MongoRepository base
│   │   ├── DefaultCrudService<R,E>    salvar/buscarPorId/listarTodos/atualizar/deletar
│   │   └── DefaultCrudController<S,R,E>  Endpoints CRUD genéricos
│   │
│   ├── enums/
│   │   ├── PerfilUsuario              CIDADAO · ATENDENTE · GESTOR
│   │   ├── Prioridade                 BAIXA · MEDIA · ALTA · CRITICA
│   │   └── StatusSolicitacao          ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO
│   │
│   ├── usuario/          entity · repository · service · controller · dto
│   ├── categoria/        entity · repository · service · controller
│   ├── departamento/     entity · repository · service · controller
│   ├── sla/               entity · repository · service · controller
│   ├── solicitacao/      entity (Solicitacao, Movimentacao, Localizacao) · repository · service · controller · dto
│   └── log/               entity (LogAcao) · repository · service · controller
│
└── infra/
    ├── config/            SecurityConfig, MongoConfig, OpenApiConfig, DataInitializer
    ├── security/          JwtTokenProvider, JwtAuthFilter, UserDetailsServiceImpl
    └── exception/         GlobalExceptionHandler
```

### Padrão de serviços

Todo service estende `DefaultCrudService<R, E>` e implementa `getRepository()`. Os métodos `salvar`, `buscarPorId`, `listarTodos`, `atualizar` e `deletar` são herdados. Regras de negócio específicas (ex.: `criar`/`moverStatus` em `SolicitacaoService`, `cadastrar` em `UsuarioService`) são adicionadas nos services concretos.

`deletar()` genérico faz exclusão lógica (`setAtivo(false)`) para entidades que implementam `Ativavel` (`Categoria`, `DepartamentoDestino`) e exclusão física para as demais — exceto `Usuario`, que sobrescreve `deletar()` para sempre inativar, mesmo sem implementar `Ativavel`, preservando o histórico referenciado em `Movimentacao`, `LogAcao` e `Solicitacao`.

---

## Banco de Dados

- **MongoDB**, um documento por coleção via `@Document`
- Índices únicos: `usuario.email`, `usuario.cpf`, `solicitacao.protocolo`, `categoria.nome`, `departamento_destino.nome`, `sla_config.prioridade`
- Dados iniciais (usuários, categorias, departamentos, SLA) semeados por `DataInitializer` na subida da aplicação — idempotente, seguro rodar múltiplas vezes

Diagrama, relacionamentos e subdocumentos (`localizacao`, `historico` e os resumos de referência): [`docs/modelo-de-colecoes.md`](docs/modelo-de-colecoes.md).

**Coleções:**

| Coleção                | Responsabilidade                              |
|-------------------------|-----------------------------------------------|
| `usuario`               | Cidadãos, atendentes e gestores               |
| `categoria`             | Tipos de problema ambiental                   |
| `sla_config`            | Prazo-alvo por prioridade                     |
| `solicitacao`           | Denúncias — entidade central                  |
| `movimentacao`          | Histórico de mudanças de status               |
| `departamento_destino`  | Órgãos/departamentos para encaminhamento      |
| `log_acao`              | Auditoria de operações críticas               |

---

## Departamentos Destino (padrão)

| Departamento         | Responsabilidade                        |
|----------------------|-----------------------------------------|
| Prefeitura Municipal | Infraestrutura urbana geral             |
| COPEL                | Energia elétrica                        |
| SANEPAR              | Saneamento básico                       |
| COMPAGAS              | Distribuição de gás                     |
| SESP                 | Segurança pública                       |
| SEMA                 | Meio ambiente                           |

---

## Regras de Negócio Relevantes

- Descrição é obrigatória em toda solicitação; denúncias anônimas exigem, além disso, no mínimo 50 caracteres
- Transições de status seguem fluxo fixo — voltar status não é permitido
- Somente o perfil GESTOR pode encerrar uma solicitação
- Solicitações nunca são deletadas — apenas encerradas
- Usuário excluído é inativado (`ativo=false`), nunca apagado — perde acesso de login, mas seu histórico permanece íntegro
- Categorias e departamentos excluídos também são inativados, não removidos

---

## Tecnologias

| Item                | Detalhe                          |
|---------------------|-----------------------------------|
| Linguagem           | Java 21                           |
| Framework           | Spring Boot 4.0.6                 |
| Build               | Maven via wrapper (`mvnw`)        |
| Banco               | MongoDB (replica set)             |
| Autenticação        | Spring Security + JWT (jjwt 0.12.6) |
| Documentação da API | springdoc-openapi 3.1.0 (Swagger)  |
| Boilerplate         | Lombok                             |
| Interface           | REST (Swagger) + CLI (terminal) + frontend web |
| Frontend            | Angular 21 (standalone), Leaflet, Vitest |

---

## Stack de Testes

A suíte de testes deste projeto usa a stack abaixo para validar regras de negócio, controllers e autenticação:

- JUnit 5 — execução dos testes
- Mockito — mocks e stubs dos serviços
- Spring Test / MockMvc — testes de endpoints HTTP e serialização JSON
- Spring Security Test — autenticação mockada (`@WithMockUser`, `authentication(...)`)
- AssertJ / Spring Assertions — validações de resultado

Os testes cobrem cenários importantes de fluxo de negócio, como:

- login e autenticação JWT
- cadastro e validação de usuários
- abertura e consulta de solicitações
- permissões por perfil (CIDADAO, ATENDENTE, GESTOR)
- transições de status e regras de negócio

### Como testar

> Importante: neste projeto o uso do wrapper Maven (`mvnw`) é obrigatório em ambientes sem `mvn` instalado ou sem configuração global correta. O comando `mvn` puro pode falhar fora do ambiente local do projeto.

A partir da raiz do repositório:

#### Windows (PowerShell / CMD)

```powershell
./mvnw.cmd test
```

ou para uma classe específica:

```powershell
./mvnw.cmd -Dtest=UsuarioControllerTest,SolicitacaoControllerTest test
```

#### Linux / macOS

```bash
./mvnw test
```

ou específico:

```bash
./mvnw -Dtest=UsuarioControllerTest,SolicitacaoControllerTest test
```

#### Executando somente para ver os testes correrem sem muito log

```bash
./mvnw -q test
```

### Cobertura de testes (JaCoCo)

A cobertura mínima exigida é de **70% das linhas** do projeto. O gate roda junto com o `verify`: abaixo disso o build falha na etapa `jacoco:check`.

```bash
./mvnw verify          # Linux / macOS
./mvnw.cmd verify      # Windows
```

- Relatório HTML: `target/site/jacoco/index.html` (também gera `jacoco.xml` e `jacoco.csv` na mesma pasta)
- Código gerado pelo Lombok fica fora da medição (`lombok.config`); nenhuma classe escrita à mão é excluída
- Medição atual: 97,1% das linhas (738 de 760)

### Validação atual da suíte

A suíte foi validada com sucesso no ambiente atual com o comando:

```bash
./mvnw -q test
```

Esse comando retorna sucesso após rodar toda a suíte de testes automatizados do projeto.

---

## Como Rodar

### 1. Banco de dados

A aplicação usa transações (`@Transactional`), que no MongoDB exigem replica set — mesmo com um único nó. Subindo via Docker:

```bash
docker run -d --name aep-mongo -p 27017:27017 mongo:7 --replSet rs0
docker exec aep-mongo mongosh --eval "rs.initiate()"
```

A URI padrão (`application.properties`) já aponta para `mongodb://localhost:27017/aepdb?replicaSet=rs0`. Para outro host/porta, defina a variável de ambiente `MONGODB_URI`.

### 2. Modo Web (API REST)

```bash
./mvnw spring-boot:run
```

- Servidor em `http://localhost:8080/api`
- Swagger UI em `http://localhost:8080/api/swagger-ui.html`
- Login: `POST /api/auth/login` — o token retornado vai no header `Authorization: Bearer <token>`

### 3. Frontend (Angular)

Com o backend no ar, em outro terminal (Node.js 20+):

```bash
cd frontend
npm install
npm start
```

- Aplicação em `http://localhost:4200`
- Em desenvolvimento, `/api` é encaminhado ao backend pelo `proxy.conf.json`; o CORS do backend já libera `http://localhost:4200`
- Testes: `npm test` (watch) ou `npm run test:coverage` (cobertura mínima de 70%)

### 4. Modo CLI (terminal)

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=cli
```

Sobe o mesmo contexto Spring (Mongo, Security, seed de dados), sem abrir porta HTTP, e apresenta o menu de terminal (`TerminalRunner`) chamando diretamente os mesmos services usados pelos endpoints REST. O MongoDB do passo 1 precisa estar rodando.

### Variáveis de ambiente

| Variável | Padrão | Uso |
|---|---|---|
| `MONGODB_URI` | `mongodb://localhost:27017/aepdb?replicaSet=rs0` | Conexão com o MongoDB |
| `JWT_SECRET` | valor de desenvolvimento no `application.properties` | Segredo (Base64) de assinatura do JWT. Defina um próprio fora do ambiente local |

---

## Perfis e usuários padrão

| Perfil | O que faz |
|---|---|
| `CIDADAO` | Abre solicitações (identificadas ou anônimas), acompanha as suas e consulta qualquer protocolo |
| `ATENDENTE` | Vê a fila, faz a triagem (prioridade + departamento) e move o status até `RESOLVIDO` |
| `GESTOR` | Tudo do atendente, mais encerrar solicitações, gerenciar usuários e consultar a auditoria (logs) |

Usuários semeados por `DataInitializer` (apenas desenvolvimento — troque as senhas fora do ambiente local):

| E-mail                | Senha   | Perfil    |
|------------------------|---------|-----------|
| admin@gov              | admin   | GESTOR    |
| atendente@gov          | 123     | ATENDENTE |
| teste123@gmail.com     | teste   | CIDADAO   |

A matriz completa de permissões por endpoint está em [`docs/arquitetura.md`](docs/arquitetura.md#autenticação-e-autorização).

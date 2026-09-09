Resumo do projeto e plano de testes (Gerado por assistente)

1) Objetivo
- Mapear classes existentes e fornecer um fluxo passo-a-passo para criar testes JUnit (JUnit 5) que busquem cobertura de 100% de TODAS as classes.
- Não modificar código; apenas orientar e gerar este arquivo local que o usuário pode apagar antes do commit.
- Ignorar variáveis de ambiente (não salvar).

2) Lista de classes encontradas (src/main/java)
- com.aep.backend.BackendApplication
- com.aep.backend.auth.dto.AuthResponse
- com.aep.backend.auth.dto.LoginRequest
- com.aep.backend.domain.abstraction.Ativavel
- com.aep.backend.domain.abstraction.DefaultCrudController
- com.aep.backend.domain.abstraction.DefaultCrudRepository
- com.aep.backend.domain.abstraction.DefaultCrudService
- com.aep.backend.domain.abstraction.DefaultEntity
- com.aep.backend.domain.categoria.entity.Categoria
- com.aep.backend.domain.categoria.entity.CategoriaResumo
- com.aep.backend.domain.categoria.repository.CategoriaRepository
- com.aep.backend.domain.departamento.entity.DepartamentoDestino
- com.aep.backend.domain.departamento.entity.DepartamentoResumo
- com.aep.backend.domain.departamento.repository.DepartamentoRepository
- com.aep.backend.domain.enums.PerfilUsuario
- com.aep.backend.domain.enums.Prioridade
- com.aep.backend.domain.enums.StatusSolicitacao
- com.aep.backend.domain.log.entity.LogAcao
- com.aep.backend.domain.log.repository.LogRepository
- com.aep.backend.domain.sla.entity.SlaConfig
- com.aep.backend.domain.sla.repository.SlaRepository
- com.aep.backend.domain.solicitacao.dto.LogAcaoResponse
- com.aep.backend.domain.solicitacao.dto.MoverStatusRequest
- com.aep.backend.domain.solicitacao.dto.MovimentacaoResponse
- com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest
- com.aep.backend.domain.solicitacao.dto.SolicitacaoResponse
- com.aep.backend.domain.solicitacao.entity.Movimentacao
- com.aep.backend.domain.solicitacao.entity.Solicitacao
- com.aep.backend.domain.solicitacao.repository.MovimentacaoRepository
- com.aep.backend.domain.solicitacao.repository.SolicitacaoRepository
- com.aep.backend.domain.usuario.dto.UsuarioRequest
- com.aep.backend.domain.usuario.dto.UsuarioResponse
- com.aep.backend.domain.usuario.entity.Usuario
- com.aep.backend.domain.usuario.entity.UsuarioResumo
- com.aep.backend.domain.usuario.repository.UsuarioRepository
- com.aep.backend.infra.config.DataInitializer
- com.aep.backend.infra.config.MongoConfig
- com.aep.backend.infra.config.OpenApiConfig

3) Arquivos de teste existentes
- src/test/java/com/aep/backend/BackendApplicationTests.java

4) Detectar build system
- Verifique se o projeto usa Maven (pom.xml) ou Gradle (build.gradle). Adicione dependências de teste/coverage conforme abaixo (não aplicadas automaticamente).

Maven (exemplo) -> adicionar em pom.xml:
- junit-jupiter, mockito, spring-boot-starter-test (já inclui JUnit/Mockito), plugin jacoco-maven-plugin

Gradle (exemplo) -> adicionar em build.gradle:
- testImplementation('org.springframework.boot:spring-boot-starter-test')
- aplicar plugin 'jacoco'

5) Estratégia geral para 100% de cobertura
- DTOs e entidades: testes unitários simples que instanciam objetos, chamam getters/setters, equals/hashCode/toString. Ex.: assert getters, serialização (opcional)
- Enums: testar valores e comportamentos (valueOf, listas)
- Classes abstratas (DefaultCrudService, DefaultEntity, DefaultCrudController): criar classes de teste/implementações anônimas mínimas para cobrir métodos concretos e lógica comum.
- Repositories (interfaces Spring Data): escrever testes que validem que a interface existe; cobertura efetiva de métodos gerados é limitada — para cobrí-los, usar @DataMongoTest ou testes de integração com um Mongo embutido (ex.: Flapdoodle) para executar operações CRUD.
- Config classes (MongoConfig, OpenApiConfig): testes de contexto que iniciam ApplicationContext parcial e verificam beans criados (use @SpringBootTest / @ContextConfiguration / @Import).
- DataInitializer: teste unitário/integration que chama o método init e verifica comportamento (mockar repositórios ou usar @SpringBootTest com profile de teste).
- Controllers (se existirem): usar MockMvc via @WebMvcTest para cobrir endpoints. Aqui há DefaultCrudController — criar um teste de controller para uma implementação concreta.

6) Biblioteca recomendada e padrões
- JUnit 5 (jupiter)
- Mockito + MockitoExtension (@ExtendWith(MockitoExtension.class)) para unit tests
- Spring Boot Test (spring-boot-starter-test) para testes que iniciam partes do contexto
- MockMvc para controllers
- @DataMongoTest / Flapdoodle embedded Mongo para repositories que precisam de persistência real
- JaCoCo para medir cobertura

7) Passo-a-passo prático (exemplo mínimo e repetível)
A) Preparar dependências: adicionar spring-boot-starter-test e jacoco (manual no build)
B) Criar estrutura de testes: src/test/java/com/aep/backend/... com classes de teste nomeadas ClassNameTest.java
C) Para cada DTO/Entity: criar teste que:
   - instancia usando construtor
   - chama todos os getters/setters
   - chama equals/hashCode/toString (quando presentes)
   - Esperado: 100% das linhas do POJO executadas
D) Para enums: iterar values() e assertEquals para cada constante
E) Para classes abstratas: definir subclasse de teste local (static class TestService extends DefaultCrudService<...>) e invocar métodos configurados
F) Para config classes: usar @SpringBootTest(classes = {MongoConfig.class, OpenApiConfig.class}) e assertNotNull(applicationContext.getBean(MongoTemplate.class)) — ajustar conforme beans reais
G) Para repositories: iniciar teste com @DataMongoTest e usar repository.save/findById/delete para cobrir métodos
H) Para DataInitializer: mockar repositórios e chamar método init, verificar chamadas (Mockito.verify)
I) Para cobertura final: executar mvn test + mvn jacoco:report (Maven) ou gradle test jacocoTestReport (Gradle) e ajustar testes faltantes até 100%

8) Templates rápidos (exemplos para copiar)
- DTO test: instanciar e assertEquals entre getters/setters
- Service unit test: @ExtendWith(MockitoExtension.class), @Mock dependencies, @InjectMocks service, when/verify
- Controller: @WebMvcTest(YourController.class) + MockMvc.perform(...) and assert status/body
- Repository: @DataMongoTest + @AutoConfigureDataMongo(embedded) -> salvar e buscar

9) Observações finais
- 100% cobertura é possível mas trabalhoso; focar em testes determinísticos e sem dependências externas (use mocks ou embedded DB)
- Antes do commit, apagar a pasta copilot-mapping se não quiser vestígios
- Não salvar variáveis ambiente — nenhuma será gravada neste arquivo

10) Próximo passo que posso executar por você (escolher uma):
- Gerar templates de testes (arquivos .java) para todas as classes detectadas (criação automática de testes básicos cobrindo getters/setters e enums)
- Gerar snippets para pom.xml / build.gradle que adicionam dependências e JaCoCo
- Gerar exemplos concretos para as classes abstratas e DataInitializer

Escolha uma opção e eu gero os arquivos de teste ou snippets. Se preferir, eu gero apenas os templates de teste (recomendado).
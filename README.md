# Tech Challenge Oficina Mecânica

API REST desenvolvida para o Tech Challenge da FIAP. O sistema gerencia clientes, veículos, serviços, peças, estoque e todo o ciclo de vida de uma Ordem de Serviço (OS), do recebimento do veículo até sua entrega.

## Funcionalidades

- Cadastro e manutenção de clientes e veículos.
- Catálogo de serviços e peças.
- Controle de estoque com entrada, saída, baixa automática e devolução.
- Criação e consulta de ordens de serviço.
- Cálculo automático do orçamento.
- Aprovação ou recusa do orçamento.
- Controle automático das transições de status.
- Consulta pública do andamento da OS.
- Indicadores de duração e tempo médio de execução.
- Autenticação administrativa com JWT.
- Documentação interativa com Swagger/OpenAPI.

O fluxo principal da OS é:

```text
RECEBIDA -> EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO
         -> EM_EXECUCAO -> FINALIZADA -> ENTREGUE
```

Uma OS também pode assumir o status `CANCELADA` quando as regras do fluxo permitirem.

## Arquitetura

O projeto utiliza arquitetura em camadas orientada por conceitos de DDD:

```text
interfaces/controller
        |
application/service, dto, mapper, validation
        |
domain
        |
infrastructure/repository, security, config
        |
PostgreSQL
```

- `domain`: entidades, enums e regras centrais do negócio.
- `application`: casos de uso, serviços, DTOs, mapeamentos e validações.
- `infrastructure`: persistência JPA, segurança JWT e configurações técnicas.
- `interfaces`: controllers REST e tratamento padronizado de erros.

`OrdemServico` é o principal agregado do fluxo operacional. Suas coleções de itens usam `Set`/`LinkedHashSet` para manter consistência com o carregamento do Hibernate.

## Tecnologias

- Java 21
- Spring Boot 3.5
- Maven
- Spring Web
- Spring Data JPA / Hibernate
- Spring Security e JWT
- PostgreSQL 16
- Bean Validation
- Swagger / OpenAPI
- Lombok
- JUnit 5, Mockito e MockMvc
- JaCoCo
- Docker e Docker Compose

## Pré-requisitos

Para execução local:

- JDK 21
- Maven 3.9 ou superior
- PostgreSQL 16

Para execução em containers:

- Docker Desktop com Docker Compose

## Configuração

A aplicação aceita as seguintes variáveis de ambiente:

| Variável | Padrão local | Descrição |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/oficina` | URL JDBC do PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuário do banco |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Senha do banco |
| `JPA_DDL_AUTO` | `update` | Estratégia de schema do Hibernate |
| `JPA_SHOW_SQL` | `false` | Exibição de SQL para diagnóstico local |
| `ADMIN_EMAIL` | `admin@oficina.com` | E-mail do administrador inicial |
| `ADMIN_PASSWORD` | `admin123` | Senha do administrador inicial |
| `JWT_SECRET` | chave local de desenvolvimento | Chave Base64 de pelo menos 256 bits |
| `JWT_EXPIRATION_MINUTES` | `60` | Validade do token em minutos |
| `SWAGGER_ENABLED` | `true` | Habilita a documentação interativa |

Os valores padrão são destinados apenas ao desenvolvimento. Troque senhas e a chave JWT antes de publicar a aplicação.

No Docker Compose, `POSTGRES_PASSWORD`, `ADMIN_PASSWORD` e `JWT_SECRET` são obrigatórias. Gere uma chave JWT com pelo menos 256 bits, por exemplo com `openssl rand -base64 32`, e não versione o arquivo `.env`.

## Execução local

Crie previamente o banco `oficina` no PostgreSQL e ajuste as variáveis, se necessário.

```bash
mvn clean package
java -jar target/tech-challenge-oficina-1.0.0-SNAPSHOT.jar
```

A API ficará disponível em `http://localhost:8080`.

## Execução com Docker Compose

Copie o arquivo de exemplo e altere as credenciais:

```powershell
Copy-Item .env.example .env
docker compose config
docker compose up --build
```

No Linux ou macOS:

```bash
cp .env.example .env
```

O Compose cria:

- `oficina-app`: aplicação Spring Boot na porta configurada por `APP_PORT`.
- `oficina-postgres`: PostgreSQL na porta configurada por `POSTGRES_PORT`.
- `oficina-postgres-data`: volume persistente do banco.
- `oficina-network`: rede interna entre aplicação e banco.

Para interromper os containers sem apagar dados:

```bash
docker compose stop
```

## Swagger e OpenAPI

Com a aplicação em execução:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Especificação OpenAPI: `http://localhost:8080/v3/api-docs`

O login, a documentação e o acompanhamento público são liberados sem autenticação. Os demais endpoints exigem um JWT com perfil administrativo.

## Autenticação JWT

Faça login em:

```http
POST /api/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@oficina.com",
  "senha": "admin123"
}
```

Copie o token retornado e envie nas operações administrativas:

```http
Authorization: Bearer SEU_TOKEN
```

No Swagger, selecione **Authorize** e informe o token. A consulta pública não exige autenticação:

```http
GET /api/publico/ordens-servico/{numeroOs}?placa={placa}
```

## Recursos da API

| Recurso | Caminho base | Acesso |
|---|---|---|
| Autenticação | `/api/auth` | Público |
| Acompanhamento da OS | `/api/publico/ordens-servico` | Público |
| Clientes | `/api/clientes` | Administrador |
| Veículos | `/api/veiculos` | Administrador |
| Serviços | `/api/servicos` | Administrador |
| Peças e estoque | `/api/pecas` | Administrador |
| Ordens de serviço | `/api/ordens-servico` | Administrador |

Os contratos completos, parâmetros e respostas estão disponíveis no Swagger.

Consulte também a [documentação detalhada dos endpoints](docs/ENDPOINTS.md), com exemplos de requests e o fluxo completo da OS.

O vocabulário oficial do domínio está definido na [Linguagem Ubíqua](docs/LINGUAGEM-UBIQUA.md).

O fluxo de comandos, eventos, políticas e agregados está no [Event Storming da Ordem de Serviço](docs/EVENT-STORMING-ORDEM-SERVICO.md).

O ciclo de cadastro, uso, baixa e devolução está no [Event Storming de Peças e Insumos](docs/EVENT-STORMING-PECAS-INSUMOS.md).

As entidades, agregados, contexts, repositories, services e camadas estão nos [Diagramas DDD e de Arquitetura](docs/DIAGRAMAS-DDD.md).

O resultado consolidado das verificações de entrega está na [Revisão Final](docs/REVISAO-FINAL.md).

O scan, as correções aplicadas e o resultado final estão no [Relatório de Vulnerabilidades](docs/RELATORIO-VULNERABILIDADES.md).

## Testes e cobertura

Execute a suíte completa com:

```bash
mvn clean test
```

O JaCoCo gera o relatório em `target/site/jacoco/index.html`.

O build exige cobertura mínima de 80% por instruções na camada de domínio. A suíte inclui testes unitários de domínio e serviços, testes de segurança e testes de integração dos controllers com MockMvc.

## Estrutura do projeto

```text
src/
|-- main/
|   |-- java/br/com/fiap/oficina/
|   |   |-- application/
|   |   |   |-- dto/
|   |   |   |-- exception/
|   |   |   |-- mapper/
|   |   |   |-- service/
|   |   |   `-- validation/
|   |   |-- domain/
|   |   |   |-- cliente/
|   |   |   |-- ordemservico/
|   |   |   |-- peca/
|   |   |   |-- servico/
|   |   |   |-- usuario/
|   |   |   `-- veiculo/
|   |   |-- infrastructure/
|   |   |   |-- config/
|   |   |   |-- repository/
|   |   |   `-- security/
|   |   `-- interfaces/controller/
|   `-- resources/application.yml
`-- test/java/br/com/fiap/oficina/
```

Arquivos de infraestrutura na raiz:

- `Dockerfile`: build multi-stage e runtime Java 21 não-root.
- `docker-compose.yml`: aplicação, PostgreSQL, rede e volume.
- `.env.example`: referência das variáveis do Compose.
- `.dockerignore`: exclusões do contexto de build.
- `pom.xml`: dependências, empacotamento, testes e JaCoCo.

## Tratamento de erros

A API utiliza respostas JSON padronizadas e diferencia erros de validação, recurso não encontrado, conflito, regra de negócio e falha interna. Stack traces não são expostas aos clientes.

## Observações de segurança

- Não versione o arquivo `.env` com credenciais reais.
- Use uma chave JWT Base64 forte e exclusiva por ambiente.
- Troque a senha do administrador inicial.
- Em produção, prefira migrations versionadas para o banco em vez de `ddl-auto=update`.
- Em produção, defina `SWAGGER_ENABLED=false` e mantenha `JPA_SHOW_SQL=false`.

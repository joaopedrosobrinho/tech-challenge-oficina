# Tech Challenge Oficina Mecânica - Fase 2

API REST para gerenciar clientes, veículos, serviços, peças e o ciclo de vida de ordens de serviço. A segunda fase acrescenta arquitetura hexagonal, aprovação externa de orçamento, notificações de status e infraestrutura reproduzível com Kubernetes, Terraform e GitHub Actions.

## Arquitetura

```mermaid
flowchart LR
    API[REST / integração externa] --> UC[Casos de uso]
    UC --> D[Domínio]
    UC --> P[Portas]
    A[Adaptadores JPA, JDBC e JWT] -. implementam .-> P
    A --> DB[(PostgreSQL)]
    DB --> W[Worker de e-mail]
    W --> SMTP[Mailpit / SMTP]
```

O domínio não depende de Spring ou JPA. O mapeamento de persistência fica em `META-INF/orm.xml`; as regras de estoque, orçamento e transição de status permanecem no domínio. A aplicação usa portas para persistência, consulta entre contextos, tokens e notificações. As mensagens de status são registradas na mesma transação da OS e enviadas posteriormente.

A infraestrutura usa Kind com dois nós, PostgreSQL com volume persistente, duas a quatro réplicas da aplicação, Services, ConfigMaps, Secrets, probes, HPA e Metrics Server. O banco local tem uma réplica; o ambiente serve à demonstração acadêmica e não representa uma implantação de produção com banco redundante.

Os desenhos de componentes, infraestrutura e fluxo de deploy, decisões e instruções detalhadas estão em [Arquitetura e operação da fase 2](docs/FASE-2.md).

## Tecnologias

Java 21, Spring Boot 3.5, Maven, PostgreSQL 16, Flyway, Spring Security/JWT, JUnit, Mockito, ArchUnit, JaCoCo, Docker Distroless não-root, Kubernetes, Terraform, GitHub Actions, SMTP/Mailpit e k6.

## Execução local com Compose

Pré-requisitos: Docker em execução e Python 3 para gerar a configuração local.

```powershell
python scripts/configure-local.py
docker compose config --quiet
docker compose up --build -d
```

O gerador cria `.env` com credenciais aleatórias e preserva arquivos existentes. Há também um [.env.example](.env.example) para configuração manual. Em arquivos antigos, configure `INTEGRATION_TOKEN` e use `JPA_DDL_AUTO=validate`.

- API: `http://localhost:8080`.
- Swagger: [documentação interativa](http://localhost:8080/swagger-ui.html).
- E-mails capturados: [Mailpit](http://localhost:8025).
- Prontidão: `http://localhost:8080/actuator/health/readiness`.

As credenciais administrativas estão no `.env`. Os e-mails do Mailpit ficam no laboratório; não são enviados aos clientes reais. O Compose da fase 2 utiliza um volume novo e preserva o volume da primeira fase.

```powershell
python scripts/smoke-test.py
docker compose stop
```

O smoke test cria dados de demonstração, abre uma OS, envia aprovações simultâneas e repete o mesmo evento, confere o estoque, finaliza, entrega e consulta o histórico.

## Execução sem container da aplicação

Com Java 21 e Maven 3.9+, configure as variáveis de banco e credenciais no ambiente do processo. O Spring Boot não lê `.env` automaticamente; o Compose e os scripts Python fazem essa leitura.

```powershell
mvn -B -ntp verify
java -jar target/tech-challenge-oficina-1.0.0-SNAPSHOT.jar
```

Por padrão, a conexão aponta para PostgreSQL em `localhost:5432/oficina`. Flyway cria e evolui o schema. Para notificações, configure `NOTIFICACOES_ENABLED=true`, `SMTP_HOST`, `SMTP_PORT` e, se necessário, `SMTP_USERNAME`, `SMTP_PASSWORD`, `SMTP_AUTH` e `SMTP_STARTTLS`.

## APIs

O [contrato OpenAPI completo](docs/openapi.json) pode ser importado no Postman. Com a aplicação iniciada, o mesmo contrato está em `/v3/api-docs`.

| Recurso | Endpoint | Acesso |
|---|---|---|
| Login | `POST /api/auth/login` | Público |
| Clientes | `/api/clientes` | JWT administrativo |
| Veículos | `/api/veiculos` | JWT administrativo |
| Serviços | `/api/servicos` | JWT administrativo |
| Peças e estoque | `/api/pecas` | JWT administrativo |
| Abertura de OS | `POST /api/ordens-servico` | JWT administrativo |
| Fila operacional | `GET /api/ordens-servico` | JWT administrativo |
| Consulta individual | `GET /api/ordens-servico/{id}` | JWT administrativo |
| Diagnóstico | `POST /api/ordens-servico/{id}/diagnostico` | JWT administrativo |
| Envio do orçamento | `POST /api/ordens-servico/{id}/orcamento/enviar-aprovacao` | JWT administrativo |
| Decisão externa | `POST /api/integracoes/ordens-servico/{id}/aprovacao` | `X-Integration-Key` |
| Acompanhamento | `GET /api/publico/ordens-servico/{numeroOs}?placa=...` | Público, com número e placa |

A fila segue `EM_EXECUCAO > AGUARDANDO_APROVACAO > EM_DIAGNOSTICO > RECEBIDA`, com as mais antigas primeiro. OS finalizadas, entregues e canceladas ficam fora da fila, mas continuam no histórico.

A integração externa recebe `eventoId`, `versaoOrcamento`, `aprovada` e `observacao`. Repetir o mesmo evento não baixa estoque novamente. Cada reenvio de orçamento gera uma nova versão, retornada pela API. O [guia da fase 2](docs/FASE-2.md) explica os contratos, códigos HTTP e garantias de entrega.

## Kubernetes e Terraform

Instale Terraform, Kind e kubectl e mantenha Docker em execução. O [guia de operação](docs/FASE-2.md#provisionamento-local) detalha versões, recursos e acesso.

```powershell
terraform -chdir=infra/cluster init
terraform -chdir=infra/cluster apply
terraform -chdir=infra/database init
terraform -chdir=infra/database apply
docker build -t tech-challenge-oficina:local .
python scripts/deploy-local.py
kubectl --kubeconfig infra/cluster/kubeconfig -n oficina port-forward service/oficina 8080:8080
```

Os manifestos estão em [k8s](k8s) e os módulos em [infra](infra). Para observar escala automática, execute `python scripts/check-hpa.py`; o script gera carga com k6 e registra métricas e réplicas em `target/cluster`.

## Testes e pipeline

```powershell
mvn -B -ntp verify
```

A suíte cobre domínio, casos de uso, autenticação, integração externa, fila operacional, persistência, notificações e fronteiras arquiteturais. Os testes locais de banco usam H2 em modo PostgreSQL. No pipeline, `TEST_DATABASE_URL`, `TEST_DATABASE_DRIVER`, `TEST_DATABASE_USER` e `TEST_DATABASE_PASSWORD` direcionam os mesmos testes para PostgreSQL real.

O JaCoCo gera `target/site/jacoco/index.html` e exige pelo menos 80% de cobertura por instruções no domínio. Esse limite não é uma medida de cobertura global.

O [workflow CI/CD](.github/workflows/ci-cd.yml) executa build, testes em PostgreSQL, build da imagem, Terraform, deploy de banco/aplicação, smoke test e carga com HPA. O cluster é descartável e fica no executor do GitHub Actions. Relatórios são publicados como artefatos; não há deploy cloud permanente.

## Entrega

- [Arquitetura e operação](docs/FASE-2.md).
- [Contrato das APIs](docs/openapi.json).
- Vídeo: pendente de gravação e publicação no YouTube ou Vimeo.

Para gerar o PDF, instale `reportlab` e execute:

```powershell
python scripts/gerar-entrega.py --video-url URL_DO_VIDEO --output docs/entrega-fase-2.pdf
```

O PDF do portal precisa conter o link deste repositório, a arquitetura e o link real do vídeo. O acesso do usuário `soat-architecture` deve ser conferido antes do envio.

O [modelo de domínio](docs/modelo-dominio.md) e os documentos [Linguagem Ubíqua](docs/LINGUAGEM-UBIQUA.md), [Event Storming de OS](docs/EVENT-STORMING-ORDEM-SERVICO.md) e [Event Storming de Peças](docs/EVENT-STORMING-PECAS-INSUMOS.md) registram a modelagem inicial.

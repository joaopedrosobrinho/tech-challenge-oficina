# Revisão Final da Entrega

Revisão executada em 19/08/2026 para o marco de 31/08 do cronograma.

## Resultado executivo

| Área | Resultado | Evidência |
|---|---|---|
| Código e arquitetura | Aprovado | 78 classes de produção compiladas com Java 21 |
| Testes | Aprovado | 157 testes, sem falhas ou erros |
| Cobertura | Aprovado | 81,31% total; JaCoCo exige no mínimo 80% por instruções no domínio |
| Swagger/OpenAPI | Aprovado | `/swagger-ui.html` e `/v3/api-docs` |
| Autenticação | Aprovado | Login público, JWT Bearer e APIs administrativas protegidas |
| Dockerfile | Aprovado | Build multi-stage e execução com usuário não-root |
| Docker Compose | Aprovado | Aplicação, PostgreSQL, healthcheck, rede e volume persistente |
| Documentação DDD | Aprovado | Linguagem Ubíqua e diagramas de agregados, contexts e camadas |
| Event Storming | Aprovado | Fluxos de Ordem de Serviço e de peças/insumos |
| Vulnerabilidades | Aprovado | Docker Scout: redução de 19 para 0 achados na imagem final distroless |
| Git | Requer ação do responsável | Alterações do cronograma ainda precisam ser revisadas e commitadas |

## Verificações realizadas

- O projeto usa Java 21 e Spring Boot 3.5.15.
- Os controllers atuais estão documentados em `ENDPOINTS.md`.
- O fluxo da OS contempla `RECEBIDA`, `EM_DIAGNOSTICO`, `AGUARDANDO_APROVACAO`, `EM_EXECUCAO`, `FINALIZADA`, `ENTREGUE` e `CANCELADA`.
- As coleções `ItemServico` e `ItemPeca` permanecem como `Set`/`LinkedHashSet`.
- O Compose foi validado com segredos temporários, sem criar ou alterar banco.
- Os containers `oficina-app` e `oficina-postgres` estavam ativos; o PostgreSQL estava saudável.
- `POSTGRES_PASSWORD`, `ADMIN_PASSWORD` e `JWT_SECRET` são obrigatórios no Compose.
- `.env` e variantes são ignorados pelo Git, preservando apenas `.env.example`.
- SQL fica oculto por padrão e Swagger pode ser desativado com `SWAGGER_ENABLED=false`.
- Os links relativos do README apontam para arquivos existentes no repositório.
- `git diff --check` não encontrou erros de whitespace; apenas avisos de conversão LF/CRLF no Windows.

## Artefatos conferidos

- `README.md`
- `docs/ENDPOINTS.md`
- `docs/LINGUAGEM-UBIQUA.md`
- `docs/EVENT-STORMING-ORDEM-SERVICO.md`
- `docs/EVENT-STORMING-PECAS-INSUMOS.md`
- `docs/DIAGRAMAS-DDD.md`
- `docs/arquitetura.md`
- `docs/modelo-dominio.md`
- `Dockerfile`
- `docker-compose.yml`
- `.env.example`

## Pendências antes da entrega

1. Revisar a alteração local de `.idea/misc.xml`; esse arquivo não foi modificado nem descartado durante esta revisão.
2. Revisar o conjunto de alterações no Git, criar commits coerentes e enviar à branch principal conforme a política do grupo.

## Comandos de validação

```bash
mvn clean compile
mvn test
docker compose config
```

O `docker compose config` exige um `.env` preenchido a partir de `.env.example`. Nenhum comando destrutivo de Docker, banco ou Git faz parte desta validação.

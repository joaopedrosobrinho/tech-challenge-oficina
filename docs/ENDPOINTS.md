# Endpoints e exemplos da API

Base local: `http://localhost:8080`

Documentação interativa: `http://localhost:8080/swagger-ui.html`

## Autenticação

Somente o login, o Swagger/OpenAPI e o acompanhamento público são liberados sem token. Nas demais operações, envie:

```http
Authorization: Bearer SEU_TOKEN
```

### Login administrativo

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

Resposta `200 OK`:

```json
{
  "email": "admin@oficina.com",
  "perfil": "ADMIN",
  "tipo": "Bearer",
  "token": "eyJ...",
  "expiraEm": "2026-08-19T03:35:00Z"
}
```

Use as credenciais configuradas em `ADMIN_EMAIL` e `ADMIN_PASSWORD`.

## Códigos HTTP

| Código | Significado |
|---|---|
| `200` | Consulta ou alteração concluída |
| `201` | Recurso criado; o cabeçalho `Location` identifica o recurso |
| `204` | Exclusão concluída sem corpo |
| `400` | Parâmetro, JSON ou validação inválida |
| `401` | Token ausente, inválido ou expirado |
| `403` | Usuário autenticado sem permissão |
| `404` | Recurso não encontrado |
| `409` | Conflito de unicidade ou estado persistente |
| `422` | Regra de negócio não atendida |
| `500` | Falha interna não esperada |

## Clientes

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/clientes` | Cadastrar cliente |
| `GET` | `/api/clientes` | Listar clientes |
| `GET` | `/api/clientes/{id}` | Buscar por ID |
| `GET` | `/api/clientes/documento/{cpfCnpj}` | Buscar por CPF/CNPJ |
| `PUT` | `/api/clientes/{id}` | Atualizar cliente |
| `DELETE` | `/api/clientes/{id}` | Excluir cliente sem OS vinculada |

Exemplo de cadastro:

```http
POST /api/clientes
Content-Type: application/json
Authorization: Bearer SEU_TOKEN
```

```json
{
  "nome": "Maria da Silva",
  "cpfCnpj": "529.982.247-25",
  "telefone": "(11) 91234-5678",
  "email": "maria.silva@email.com"
}
```

Resposta `201 Created`:

```json
{
  "id": 1,
  "nome": "Maria da Silva",
  "cpfCnpj": "52998224725",
  "telefone": "11912345678",
  "email": "maria.silva@email.com"
}
```

## Veículos

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/veiculos` | Cadastrar veículo |
| `GET` | `/api/veiculos` | Listar veículos |
| `GET` | `/api/veiculos/{id}` | Buscar por ID |
| `GET` | `/api/veiculos/placa/{placa}` | Buscar pela placa |
| `GET` | `/api/veiculos/cliente/{clienteId}` | Listar por cliente |
| `PUT` | `/api/veiculos/{id}` | Atualizar veículo |
| `DELETE` | `/api/veiculos/{id}` | Excluir veículo sem OS vinculada |

Exemplo de cadastro:

```json
{
  "placa": "ABC1D23",
  "marca": "Honda",
  "modelo": "Civic",
  "ano": 2022,
  "clienteId": 1
}
```

Envie o JSON para `POST /api/veiculos`. São aceitas placas antigas e Mercosul; a API normaliza a placa.

## Serviços

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/servicos` | Cadastrar serviço |
| `GET` | `/api/servicos` | Listar todos |
| `GET` | `/api/servicos/ativos` | Listar ativos |
| `GET` | `/api/servicos/pesquisa?nome={nome}` | Pesquisar por nome |
| `GET` | `/api/servicos/{id}` | Buscar por ID |
| `PUT` | `/api/servicos/{id}` | Atualizar serviço |
| `PATCH` | `/api/servicos/{id}/ativar` | Ativar |
| `PATCH` | `/api/servicos/{id}/desativar` | Desativar |
| `DELETE` | `/api/servicos/{id}` | Excluir |

Exemplo de cadastro:

```json
{
  "nome": "Troca de óleo",
  "descricao": "Substituição do óleo e do filtro",
  "valor": 150.00,
  "tempoEstimadoMinutos": 60,
  "ativo": true
}
```

## Peças e estoque

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/pecas` | Cadastrar peça |
| `GET` | `/api/pecas` | Listar todas |
| `GET` | `/api/pecas/ativas` | Listar ativas |
| `GET` | `/api/pecas/pesquisa?nome={nome}` | Pesquisar por nome |
| `GET` | `/api/pecas/estoque-baixo?limite=5` | Consultar estoque baixo |
| `GET` | `/api/pecas/codigo/{codigo}` | Buscar por código |
| `GET` | `/api/pecas/{id}` | Buscar por ID |
| `PUT` | `/api/pecas/{id}` | Atualizar peça |
| `PATCH` | `/api/pecas/{id}/entrada-estoque` | Registrar entrada |
| `PATCH` | `/api/pecas/{id}/saida-estoque` | Registrar saída |
| `PATCH` | `/api/pecas/{id}/ativar` | Ativar |
| `PATCH` | `/api/pecas/{id}/desativar` | Desativar |
| `DELETE` | `/api/pecas/{id}` | Excluir |

Exemplo de cadastro:

```json
{
  "codigo": "FLT-001",
  "nome": "Filtro de óleo",
  "descricao": "Filtro compatível com o veículo",
  "valor": 45.90,
  "quantidadeEstoque": 20,
  "ativo": true
}
```

Entrada ou saída manual de estoque:

```http
PATCH /api/pecas/1/entrada-estoque
```

```json
{
  "quantidade": 5
}
```

A saída manual e a aprovação de uma OS impedem estoque negativo. Ao aprovar o orçamento, as peças da OS são baixadas uma única vez. O cancelamento devolve o estoque quando aplicável.

## Ordens de serviço

| Método | Endpoint | Descrição |
|---|---|---|
| `POST` | `/api/ordens-servico` | Criar OS com itens |
| `GET` | `/api/ordens-servico` | Listar todas |
| `GET` | `/api/ordens-servico/{id}` | Buscar por ID |
| `GET` | `/api/ordens-servico/numero/{numeroOs}` | Buscar pelo número |
| `GET` | `/api/ordens-servico/cliente/{clienteId}` | Listar por cliente |
| `GET` | `/api/ordens-servico/veiculo/{veiculoId}` | Listar por veículo |
| `GET` | `/api/ordens-servico/status/{status}` | Listar por status |
| `POST` | `/api/ordens-servico/{id}/servicos` | Adicionar ou incrementar serviço |
| `POST` | `/api/ordens-servico/{id}/pecas` | Adicionar ou incrementar peça |
| `DELETE` | `/api/ordens-servico/{id}/pecas/{pecaId}` | Remover peça antes da execução |
| `GET` | `/api/ordens-servico/{id}/orcamento` | Consultar orçamento |
| `POST` | `/api/ordens-servico/{id}/orcamento/recalcular` | Recalcular orçamento |
| `POST` | `/api/ordens-servico/{id}/orcamento/enviar-aprovacao` | Enviar para aprovação |
| `POST` | `/api/ordens-servico/{id}/orcamento/aprovar` | Aprovar e iniciar execução |
| `POST` | `/api/ordens-servico/{id}/orcamento/recusar` | Recusar e voltar ao diagnóstico |
| `POST` | `/api/ordens-servico/{id}/finalizar` | Finalizar execução |
| `POST` | `/api/ordens-servico/{id}/entregar` | Registrar entrega |
| `POST` | `/api/ordens-servico/{id}/cancelar` | Cancelar quando permitido |
| `GET` | `/api/ordens-servico/{id}/duracao-execucao` | Consultar duração |
| `GET` | `/api/ordens-servico/indicadores/tempo-medio-execucao` | Consultar tempo médio |

Status aceitos na consulta por status:

```text
RECEBIDA
EM_DIAGNOSTICO
AGUARDANDO_APROVACAO
EM_EXECUCAO
FINALIZADA
ENTREGUE
CANCELADA
```

### Criar uma OS

Cliente, veículo, serviço e peça devem existir previamente.

```http
POST /api/ordens-servico
Content-Type: application/json
Authorization: Bearer SEU_TOKEN
```

```json
{
  "clienteId": 1,
  "veiculoId": 1,
  "observacoes": "Cliente relata ruído ao frear",
  "servicos": [
    {
      "servicoId": 1,
      "quantidade": 1
    }
  ],
  "pecas": [
    {
      "pecaId": 1,
      "quantidade": 2
    }
  ]
}
```

A resposta `201 Created` contém o `id`, o `numeroOs`, o status, os itens e o total calculado.

### Alterar itens e recalcular

Adicionar serviço:

```http
POST /api/ordens-servico/1/servicos
```

```json
{
  "servicoId": 2,
  "quantidade": 1
}
```

Adicionar peça:

```http
POST /api/ordens-servico/1/pecas
```

```json
{
  "pecaId": 2,
  "quantidade": 2
}
```

Consultar ou recalcular:

```http
GET  /api/ordens-servico/1/orcamento
POST /api/ordens-servico/1/orcamento/recalcular
```

### Aprovar o orçamento

Primeiro envie o orçamento:

```http
POST /api/ordens-servico/1/orcamento/enviar-aprovacao
```

Depois aprove:

```http
POST /api/ordens-servico/1/orcamento/aprovar
Content-Type: application/json
```

```json
{
  "observacao": "Orçamento aprovado pelo cliente"
}
```

A aprovação altera a OS para `EM_EXECUCAO`, registra a data de início e baixa o estoque das peças.

### Recusar o orçamento

```http
POST /api/ordens-servico/1/orcamento/recusar
Content-Type: application/json
```

```json
{
  "observacao": "Cliente solicitou revisão dos valores"
}
```

A observação é obrigatória na recusa. A OS volta para `EM_DIAGNOSTICO`, permitindo revisar itens e reenviar o orçamento.

### Finalizar e entregar

```http
POST /api/ordens-servico/1/finalizar
POST /api/ordens-servico/1/entregar
```

Somente uma OS `EM_EXECUCAO` pode ser finalizada, e somente uma OS `FINALIZADA` pode ser entregue.

## Acompanhamento público

O cliente informa o número da OS e a placa vinculada:

```http
GET /api/publico/ordens-servico/OS-20260819023528-4CFC9F?placa=ABC1D23
```

Não envie token. A resposta contém somente dados operacionais necessários para acompanhamento, sem CPF/CNPJ, e-mail ou telefone.

Possível resposta:

```json
{
  "numeroOs": "OS-20260819023528-4CFC9F",
  "status": "EM_EXECUCAO",
  "etapaAtual": "Serviço em execução",
  "dataCriacao": "2026-08-19T02:35:28",
  "dataInicioExecucao": "2026-08-19T02:36:10",
  "dataFinalizacao": null,
  "dataEntrega": null,
  "dataCancelamento": null
}
```

## Roteiro no Swagger

1. Execute `POST /api/auth/login`.
2. Copie o campo `token`.
3. Selecione **Authorize** e informe o token.
4. Cadastre cliente, veículo, serviço e peça, anotando os IDs.
5. Crie a OS com esses IDs.
6. Consulte o orçamento.
7. Envie o orçamento para aprovação.
8. Aprove o orçamento e confirme `EM_EXECUCAO`.
9. Consulte a peça e confirme a baixa do estoque.
10. Finalize e entregue a OS.
11. Consulte o acompanhamento público usando número da OS e placa, sem autenticação.

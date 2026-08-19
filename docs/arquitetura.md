# Arquitetura do Sistema - Oficina

## Objetivo

Desenvolver o MVP do back-end de um Sistema Integrado de Atendimento e Execução de Serviços para uma oficina mecânica.

O sistema será responsável pelo gerenciamento de:

- Clientes
- Veículos
- Serviços
- Peças
- Ordens de Serviço

A solução utiliza arquitetura monolítica em camadas seguindo princípios de Domain-Driven Design (DDD).

---

# Arquitetura

O projeto utiliza a divisão:

```text
Controller
↓
Application Service
↓
Domain
↓
Repository
↓
PostgreSQL
```

---

# Camadas

## Interfaces

Responsável pela comunicação externa da aplicação.

Contém:

- Controllers REST
- Endpoints HTTP

Exemplo:

Cliente → ClienteController

---

## Application

Responsável pelos casos de uso.

Contém:

- Services
- DTOs
- Mappers

Exemplo:

Criar uma Ordem de Serviço.

---

## Domain

Camada principal do sistema.

Contém:

- Entidades
- Regras de negócio
- Enums

Domínios:

- Cliente
- Veículo
- Serviço
- Peça
- Ordem de Serviço

---

## Infrastructure

Responsável pela parte técnica.

Contém:

- Repositories
- Segurança JWT
- Configurações
- Banco de Dados

---

# Estrutura do Projeto


br.com.fiap.oficina

application
- dto
- mapper
- service

domain
- cliente
- veiculo
- servico
- peca
- ordemservico

infrastructure
- config
- repository
- security

interfaces
- controller


---

# Fluxo Ordem de Serviço


Cliente identificado

↓

Veículo cadastrado

↓

Ordem criada

↓

Serviços adicionados

↓

Peças adicionadas

↓

Orçamento gerado

↓

Cliente aprova

↓

Execução

↓

Finalização

↓

Entrega


---

# Status Ordem de Serviço


RECEBIDA

EM_DIAGNOSTICO

AGUARDANDO_APROVACAO

EM_EXECUCAO

FINALIZADA

ENTREGUE

CANCELADA


---

# Relacionamentos


Cliente 1:N Veículo

Veículo 1:N OrdemServico

OrdemServico 1:N Serviços

OrdemServico 1:N Peças


---

# Banco de Dados

Banco escolhido:

PostgreSQL

Justificativa:

Banco relacional open source, robusto, com suporte a transações ACID e excelente integração com Java Spring Boot através do Spring Data JPA.

---

# Segurança

É utilizada autenticação JWT com token Bearer e sessão stateless.

As APIs administrativas são protegidas com o perfil `ADMIN`.

Clientes podem consultar o andamento das Ordens de Serviço pelo número da OS e pela placa, sem exposição de dados pessoais.

---

# Qualidade

Foram implementados:

- Testes unitários
- Testes de integração
- Cobertura mínima de 80%

Ferramentas:

- JUnit
- Mockito

---

# Documentação API

É utilizado:

Swagger / OpenAPI


URL:

http://localhost:8080/swagger-ui.html


---

# Tecnologias

- Java 21
- Spring Boot 3
- Maven
- PostgreSQL
- Docker
- Docker Compose
- JWT
- Swagger
- JUnit
- Mockito

---

# Conclusão

A arquitetura definida atende aos requisitos do Tech Challenge, mantendo organização, separação de responsabilidades e permitindo evolução futura do sistema.

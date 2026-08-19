# Diagramas DDD e Arquitetura

Este documento apresenta uma leitura arquitetural do projeto `tech-challenge-oficina`. Os diagramas representam o código atual; elementos descritos como futuros não devem ser interpretados como já implementados.

## Visão estratégica

O sistema está implantado como um monólito modular em camadas. Os contextos abaixo são limites funcionais dentro da mesma aplicação e do mesmo banco de dados, não microserviços independentes.

```mermaid
flowchart LR
    C1[Cadastro de Clientes e Veículos]
    C2[Catálogo de Serviços]
    C3[Peças e Estoque]
    C4[Ordens de Serviço]
    C5[Identidade e Acesso]

    C1 -->|cliente e veículo| C4
    C2 -->|serviço ativo e preço| C4
    C3 <-->|peça, saldo, baixa e devolução| C4
    C5 -->|autenticação e autorização| C1
    C5 -->|autenticação e autorização| C2
    C5 -->|autenticação e autorização| C3
    C5 -->|autenticação e autorização| C4
```

### Contextos funcionais

| Contexto | Responsabilidade | Elementos principais |
|---|---|---|
| Clientes e Veículos | Identificar proprietários e seus veículos | Cliente, Veículo |
| Catálogo de Serviços | Manter serviços disponíveis e preços | Serviço |
| Peças e Estoque | Manter catálogo, saldo e movimentações | Peça |
| Ordens de Serviço | Orquestrar diagnóstico, orçamento, aprovação e execução | OrdemServico, ItemServico, ItemPeca |
| Identidade e Acesso | Autenticar administrador e proteger endpoints | Usuario, PerfilUsuario, JWT |

O contexto de Ordens de Serviço é o núcleo principal. Ele consome dados dos contextos de suporte e coordena a baixa ou devolução de peças.

## Modelo de domínio

```mermaid
classDiagram
    class Cliente {
        +Long id
        +String nome
        +String cpfCnpj
        +String telefone
        +String email
        +adicionarVeiculo(Veiculo)
        +removerVeiculo(Veiculo)
        +adicionarOrdemServico(OrdemServico)
    }

    class Veiculo {
        +Long id
        +String placa
        +String marca
        +String modelo
        +Integer ano
        +adicionarOrdemServico(OrdemServico)
    }

    class Servico {
        +Long id
        +String nome
        +String descricao
        +BigDecimal valor
        +Integer tempoEstimadoMinutos
        +Boolean ativo
    }

    class Peca {
        +Long id
        +Long versao
        +String codigo
        +String nome
        +BigDecimal valor
        +Integer quantidadeEstoque
        +Boolean ativo
        +possuiEstoque(Integer) boolean
        +baixarEstoque(Integer)
        +devolverEstoque(Integer)
    }

    class OrdemServico {
        +Long id
        +String numeroOs
        +StatusOrdemServico status
        +StatusAprovacaoOrcamento statusAprovacaoOrcamento
        +BigDecimal valorTotal
        +Boolean estoqueBaixado
        +Boolean estoqueDevolvido
        +adicionarServico(ItemServico)
        +adicionarPeca(ItemPeca)
        +recalcularValorTotal()
        +enviarParaAprovacao()
        +aprovarOrcamentoAutomaticamente(String)
        +recusarOrcamentoAutomaticamente(String)
        +finalizarExecucao()
        +entregarVeiculo()
        +cancelar()
    }

    class ItemServico {
        +Long id
        +Integer quantidade
        +BigDecimal valorUnitario
        +calcularSubtotal() BigDecimal
        +aumentarQuantidade(Integer)
    }

    class ItemPeca {
        +Long id
        +Integer quantidade
        +BigDecimal valorUnitario
        +calcularSubtotal() BigDecimal
        +aumentarQuantidade(Integer)
    }

    class Usuario {
        +Long id
        +String email
        +String senha
        +PerfilUsuario perfil
        +Boolean ativo
    }

    class StatusOrdemServico {
        <<enumeration>>
        RECEBIDA
        EM_DIAGNOSTICO
        AGUARDANDO_APROVACAO
        EM_EXECUCAO
        FINALIZADA
        ENTREGUE
        CANCELADA
    }

    class StatusAprovacaoOrcamento {
        <<enumeration>>
        PENDENTE
        APROVADO
        RECUSADO
    }

    class PerfilUsuario {
        <<enumeration>>
        ADMIN
    }

    Cliente "1" --> "0..*" Veiculo : possui
    Cliente "1" --> "0..*" OrdemServico : solicita
    Veiculo "1" --> "0..*" OrdemServico : recebe atendimento
    OrdemServico "1" *-- "0..*" ItemServico : compõe
    OrdemServico "1" *-- "0..*" ItemPeca : compõe
    ItemServico "0..*" --> "1" Servico : referencia
    ItemPeca "0..*" --> "1" Peca : referencia
    OrdemServico --> StatusOrdemServico
    OrdemServico --> StatusAprovacaoOrcamento
    Usuario --> PerfilUsuario
```

## Agregados

### Agregado Ordem de Serviço

```mermaid
flowchart TB
    AR[OrdemServico<br/>Aggregate Root]
    IS[ItemServico<br/>Entidade interna]
    IP[ItemPeca<br/>Entidade interna]
    C[Cliente<br/>referência externa]
    V[Veiculo<br/>referência externa]
    S[Servico<br/>referência externa]
    P[Peca<br/>agregado externo]

    AR --> IS
    AR --> IP
    AR -. referência .-> C
    AR -. referência .-> V
    IS -. referência .-> S
    IP -. referência .-> P
```

Responsabilidades da raiz:

- Proteger transições de status.
- Validar cliente e veículo.
- Controlar inclusão, incremento e remoção de itens.
- Calcular o orçamento.
- Controlar aprovação e recusa.
- Coordenar baixa e devolução de estoque.
- Registrar datas do ciclo de vida.

`ItemServico` e `ItemPeca` não são manipulados diretamente pela API fora da OS.

### Agregado Peça

```mermaid
flowchart TB
    P[Peca<br/>Aggregate Root]
    S[Saldo de estoque]
    V[Versão otimista]
    P --> S
    P --> V
```

Protege o saldo contra quantidades inválidas e estoque negativo. A versão otimista detecta atualizações concorrentes.

### Agregados de cadastro

- **Cliente**: raiz do cadastro do proprietário; mantém a associação com seus veículos.
- **Veículo**: possui identidade própria e participa da OS por referência.
- **Serviço**: item independente do catálogo.
- **Usuário**: identidade administrativa para autenticação.

Cliente e Veículo possuem relacionamento bidirecional no modelo JPA, mas a criação e manutenção são realizadas por serviços de aplicação próprios.

## Value Objects

O projeto atual não possui classes dedicadas de Value Object.

Conceitos com potencial para se tornarem Value Objects:

| Conceito atual | Possível Value Object | Benefício |
|---|---|---|
| `String cpfCnpj` | `Documento` | Centralizar normalização e validação |
| `String placa` | `Placa` | Garantir formato válido na construção |
| `String email` | `Email` | Normalização e invariantes |
| `BigDecimal valor` | `Dinheiro` | Escala, moeda e operações financeiras |
| `String numeroOs` | `NumeroOrdemServico` | Geração, formato e identidade pública |

Esses Value Objects são sugestões de evolução e não existem no código atual.

## Repositories

```mermaid
classDiagram
    class ClienteRepository {
        <<repository>>
        +findByCpfCnpj(String)
        +findByEmailIgnoreCase(String)
    }
    class VeiculoRepository {
        <<repository>>
        +findByPlacaIgnoreCase(String)
        +findAllByClienteId(Long)
    }
    class ServicoRepository {
        <<repository>>
        +findAllByAtivoTrueOrderByNomeAsc()
        +findByNomeContainingIgnoreCaseOrderByNomeAsc(String)
    }
    class PecaRepository {
        <<repository>>
        +findByCodigoIgnoreCase(String)
        +findAllByAtivoTrueOrderByNomeAsc()
        +findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(Integer)
    }
    class OrdemServicoRepository {
        <<repository>>
        +findByNumeroOs(String)
        +findAllByClienteIdOrderByDataCriacaoDesc(Long)
        +findAllByVeiculoIdOrderByDataCriacaoDesc(Long)
        +findAllByStatusOrderByDataCriacaoDesc(StatusOrdemServico)
    }
    class UsuarioRepository {
        <<repository>>
        +findByEmail(String)
    }

    ClienteRepository --> Cliente
    VeiculoRepository --> Veiculo
    ServicoRepository --> Servico
    PecaRepository --> Peca
    OrdemServicoRepository --> OrdemServico
    UsuarioRepository --> Usuario
```

As interfaces estendem Spring Data JPA e ficam na camada `infrastructure/repository` do projeto atual.

## Serviços de aplicação

```mermaid
flowchart LR
    CC[ClienteController] --> CS[ClienteService] --> CR[ClienteRepository]
    VC[VeiculoController] --> VS[VeiculoService] --> VR[VeiculoRepository]
    SC[ServicoController] --> SS[ServicoService] --> SR[ServicoRepository]
    PC[PecaController] --> PS[PecaService] --> PR[PecaRepository]
    OC[OrdemServicoController] --> OS[OrdemServicoService]
    OS --> OR[OrdemServicoRepository]
    OS --> CR
    OS --> VR
    OS --> SR
    OS --> PR
    AC[AutenticacaoController] --> AS[AutenticacaoService] --> UR[UsuarioRepository]
```

| Serviço | Papel |
|---|---|
| `ClienteService` | CRUD, normalização, unicidade e proteção de exclusão |
| `VeiculoService` | CRUD, normalização de placa e vínculo com cliente |
| `ServicoService` | CRUD e ativação do catálogo de serviços |
| `PecaService` | CRUD, ativação e movimentação manual de estoque |
| `OrdemServicoService` | Caso de uso central, transações, consultas e mapeamento do fluxo da OS |
| `AutenticacaoService` | Validação de credenciais e emissão de JWT |

Os serviços de aplicação orquestram repositories, mappers e agregados. Regras centrais de transição e cálculo permanecem em `OrdemServico` e `Peca`.

## Arquitetura em camadas

```mermaid
flowchart TB
    subgraph Interfaces
        REST[Controllers REST]
        ERR[GlobalExceptionHandler]
        API[Swagger / OpenAPI]
    end

    subgraph Application
        APP[Services / Casos de uso]
        DTO[DTOs]
        MAP[Mappers]
        VAL[Validadores]
    end

    subgraph Domain
        ENT[Entidades e Agregados]
        ENUM[Enums]
        RULE[Regras e Invariantes]
    end

    subgraph Infrastructure
        REP[Repositories JPA]
        SEC[JWT / Spring Security]
        CFG[Configurações]
    end

    DB[(PostgreSQL)]

    REST --> APP
    REST --> DTO
    ERR --> REST
    API --> REST
    APP --> MAP
    APP --> VAL
    APP --> ENT
    APP --> REP
    DTO --> MAP
    ENT --> ENUM
    ENT --> RULE
    REP --> ENT
    REP --> DB
    SEC --> REST
    CFG --> SEC
```

### Responsabilidades

| Camada | Pode conhecer | Não deve concentrar |
|---|---|---|
| Interfaces | Application e contratos HTTP | Regras de negócio e acesso direto ao banco |
| Application | Domain, repositories, DTOs e mappers | Detalhes HTTP |
| Domain | Seus próprios conceitos | Controllers, JSON, JWT ou queries de infraestrutura |
| Infrastructure | Domain e detalhes técnicos | Decisões do fluxo de negócio |

Observação: as entidades usam anotações JPA, portanto o domínio atual possui dependência técnica de persistência. É uma escolha pragmática deste projeto em camadas, não uma implementação de domínio completamente isolado.

## Fluxo de uma requisição administrativa

```mermaid
sequenceDiagram
    actor Admin as Administrador
    participant F as JwtAuthenticationFilter
    participant C as Controller
    participant S as Application Service
    participant A as Agregado
    participant R as Repository JPA
    participant DB as PostgreSQL

    Admin->>F: HTTP + Bearer token
    F->>F: validar JWT e montar contexto
    F->>C: requisição autorizada
    C->>S: DTO de entrada
    S->>R: carregar entidades
    R->>DB: query
    DB-->>R: dados
    R-->>S: entidades
    S->>A: executar comportamento do domínio
    A-->>S: novo estado ou exceção
    S->>R: persistir agregado
    R->>DB: transação
    S-->>C: DTO de resposta
    C-->>Admin: HTTP JSON
```

## Dependências externas e implantação

```mermaid
flowchart LR
    U[Cliente HTTP / Swagger] --> APP[Aplicação Spring Boot<br/>Java 21]
    APP --> PG[(PostgreSQL 16)]
    APP --> JWT[JWT local assinado]

    subgraph Docker Compose
        APP
        PG
    end
```

O sistema não depende atualmente de mensageria, cache distribuído, serviço externo de identidade ou API de terceiros.

## Decisões e limitações atuais

- Monólito modular com banco único.
- Contextos funcionais não são módulos Java independentes.
- Eventos de negócio são documentais, não mensagens publicadas.
- Não há Value Objects dedicados.
- Não há entidade Mecânico, Fornecedor ou Insumo.
- Repositories Spring Data ficam em `infrastructure`, embora sejam interfaces usadas pela aplicação.
- Entidades do domínio possuem anotações JPA.
- `OrdemServicoService` coordena múltiplos agregados em transações locais.

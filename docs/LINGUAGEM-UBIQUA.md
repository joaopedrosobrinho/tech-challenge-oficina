# Linguagem Ubíqua — Oficina Mecânica

Este glossário estabelece o vocabulário compartilhado entre negócio, código, API, documentação, testes e apresentação do Tech Challenge. Os termos devem conservar o mesmo significado em todas essas áreas.

## Atores

| Termo | Definição |
|---|---|
| **Cliente** | Pessoa física ou jurídica proprietária de um ou mais veículos e responsável por acompanhar e aprovar o orçamento. É identificada por CPF ou CNPJ válido. |
| **Administrador** | Usuário interno autenticado que opera os cadastros e o fluxo administrativo da oficina. Possui o perfil `ADMIN`. |
| **Oficina** | Organização responsável por diagnosticar o veículo, montar o orçamento, executar os serviços e controlar as peças utilizadas. |

## Conceitos principais

### Cliente

Cadastro que contém nome, CPF/CNPJ, telefone e e-mail. Um cliente pode possuir vários veículos e várias Ordens de Serviço.

Regras:

- CPF/CNPJ e e-mail são únicos.
- O documento deve ser válido e é normalizado antes da persistência.
- Um cliente com OS vinculada não deve ser excluído.

### Veículo

Bem pertencente a um cliente e recebido pela oficina para diagnóstico ou manutenção. É identificado de forma única pela placa.

Regras:

- Aceita placa brasileira antiga ou Mercosul.
- A placa é normalizada e não pode ser duplicada.
- Toda OS deve associar um veículo ao seu verdadeiro cliente.
- Um veículo com OS vinculada não deve ser excluído.

### Ordem de Serviço (OS)

Registro central do atendimento realizado pela oficina. Reúne cliente, veículo, serviços, peças, orçamento, aprovação, estoque, datas e estado atual.

`OrdemServico` é a raiz do agregado operacional: as alterações de itens e as transições do fluxo devem respeitar suas regras.

Cada OS recebe um **Número da OS**, identificador público gerado no formato `OS-data-hora-identificador`, usado também no acompanhamento pelo cliente.

### Diagnóstico

Etapa em que a oficina avalia o veículo e define os serviços e peças necessários. A inclusão do primeiro item em uma OS recebida conduz automaticamente ao diagnóstico.

Durante o diagnóstico, o orçamento ainda pode ser alterado.

### Serviço

Atividade executada pela oficina, como diagnóstico, alinhamento ou troca de óleo. Possui descrição, preço, tempo estimado e indicador de atividade.

Somente serviços ativos podem ser incluídos em uma OS.

### Item de Serviço

Participação de um Serviço em uma OS. Registra serviço, quantidade e valor unitário vigente no momento da inclusão.

O **Subtotal do Serviço** é `quantidade × valor unitário`.

### Peça

Componente físico mantido no estoque e potencialmente consumido durante a execução, como filtro, pastilha ou correia. Possui código único, preço, quantidade disponível e indicador de atividade.

Somente peças ativas podem ser incluídas em uma OS.

### Item de Peça

Participação de uma Peça em uma OS. Registra peça, quantidade e valor unitário vigente no momento da inclusão.

O **Subtotal da Peça** é `quantidade × valor unitário`.

### Estoque

Quantidade disponível de cada peça. Nunca pode ficar negativo.

Termos associados:

- **Entrada de estoque**: aumento manual da quantidade disponível.
- **Saída de estoque**: redução manual, permitida somente com saldo suficiente.
- **Baixa de estoque**: consumo automático das peças quando o orçamento é aprovado e a OS entra em execução.
- **Devolução de estoque**: reposição automática das peças quando uma OS com baixa realizada é cancelada.
- **Estoque insuficiente**: condição que impede a aprovação e o início da execução.
- **Estoque baixo**: consulta de peças cuja quantidade está abaixo de um limite informado.

A baixa e a devolução de uma mesma OS acontecem no máximo uma vez. A versão otimista da peça protege movimentações concorrentes.

### Orçamento

Composição financeira dos itens de serviço e de peça de uma OS.

- **Total de serviços**: soma dos subtotais dos serviços.
- **Total de peças**: soma dos subtotais das peças.
- **Valor total**: total de serviços mais total de peças.
- **Recalcular orçamento**: atualizar os totais usando os itens já registrados.
- **Enviar para aprovação**: congelar temporariamente as alterações e solicitar a decisão do cliente.

Um orçamento precisa possuir pelo menos um serviço e valor total maior que zero para ser enviado.

### Aprovação do Orçamento

Decisão do cliente sobre o orçamento enviado. Possui estado próprio:

| Estado | Significado |
|---|---|
| `PENDENTE` | Orçamento ainda não decidido ou reenviado após revisão. |
| `APROVADO` | Cliente aceitou o orçamento; a execução começou. |
| `RECUSADO` | Cliente recusou; a OS voltou ao diagnóstico. |

**Aprovar orçamento** inicia a execução e baixa o estoque. A observação é opcional.

**Recusar orçamento** exige uma observação e devolve a OS ao diagnóstico para revisão e possível reenvio.

### Execução

Etapa em que os serviços aprovados são realizados. Começa após a aprovação e registra `dataInicioExecucao`.

Durante a execução, itens não podem ser adicionados, incrementados ou removidos.

### Finalização

Conclusão técnica dos trabalhos da oficina. Somente uma OS em execução pode ser finalizada. Registra `dataFinalizacao`.

Finalização não significa que o veículo já foi retirado pelo cliente.

### Entrega

Registro da retirada ou devolução do veículo ao cliente. Somente uma OS finalizada pode ser entregue. Registra `dataEntrega`.

### Cancelamento

Interrupção definitiva da OS antes da finalização ou entrega. Registra `dataCancelamento`.

Se o estoque já tiver sido baixado, o cancelamento devolve as peças. Uma OS finalizada, entregue ou já cancelada não pode ser cancelada novamente.

### Duração da Execução

Intervalo entre o início da execução e a finalização. Não representa o tempo total desde o recebimento.

### Tempo Médio de Execução

Média das durações das OS que possuem início e finalização. Ordens ainda não finalizadas não participam do indicador.

### Acompanhamento Público

Consulta limitada feita com Número da OS e placa. Expõe somente o progresso operacional e suas datas, sem CPF/CNPJ, telefone, e-mail ou demais dados sensíveis.

## Ciclo de vida da OS

| Status | Significado | Próxima transição normal |
|---|---|---|
| `RECEBIDA` | Veículo e solicitação foram registrados. | `EM_DIAGNOSTICO` |
| `EM_DIAGNOSTICO` | Itens e orçamento estão sendo definidos. | `AGUARDANDO_APROVACAO` |
| `AGUARDANDO_APROVACAO` | Orçamento enviado e itens temporariamente bloqueados. | `EM_EXECUCAO` após aprovação ou `EM_DIAGNOSTICO` após recusa |
| `EM_EXECUCAO` | Serviços aprovados estão sendo realizados. | `FINALIZADA` |
| `FINALIZADA` | Trabalho técnico concluído; veículo aguarda retirada. | `ENTREGUE` |
| `ENTREGUE` | Veículo entregue ao cliente; fluxo concluído. | Estado terminal |
| `CANCELADA` | Atendimento interrompido de forma definitiva. | Estado terminal |

Fluxo principal:

```text
RECEBIDA
  -> EM_DIAGNOSTICO
  -> AGUARDANDO_APROVACAO
  -> EM_EXECUCAO
  -> FINALIZADA
  -> ENTREGUE
```

## Comandos do domínio

Comandos representam intenções de alteração:

- Cadastrar Cliente.
- Cadastrar Veículo.
- Cadastrar Serviço.
- Cadastrar Peça.
- Registrar Entrada de Estoque.
- Registrar Saída de Estoque.
- Criar Ordem de Serviço.
- Iniciar Diagnóstico.
- Adicionar Serviço à OS.
- Adicionar Peça à OS.
- Remover Peça da OS.
- Recalcular Orçamento.
- Enviar Orçamento para Aprovação.
- Aprovar Orçamento.
- Recusar Orçamento.
- Finalizar Execução.
- Entregar Veículo.
- Cancelar Ordem de Serviço.

## Eventos de negócio

Eventos descrevem fatos já ocorridos. Estes nomes são conceituais e serão usados nos diagramas de Event Storming; não implicam que exista mensageria no código atual.

- Cliente Cadastrado.
- Veículo Cadastrado.
- Ordem de Serviço Criada.
- Diagnóstico Iniciado.
- Serviço Adicionado à OS.
- Peça Adicionada à OS.
- Orçamento Recalculado.
- Orçamento Enviado para Aprovação.
- Orçamento Aprovado.
- Orçamento Recusado.
- Estoque Baixado.
- Estoque Devolvido.
- Execução Iniciada.
- Ordem de Serviço Finalizada.
- Veículo Entregue.
- Ordem de Serviço Cancelada.

## Invariantes do domínio

- A OS deve possuir cliente e veículo válidos.
- O veículo da OS deve pertencer ao cliente informado.
- O número da OS é obrigatório e único.
- O orçamento enviado deve possuir serviço e valor positivo.
- Itens não podem mudar enquanto o orçamento aguarda aprovação ou após o início da execução.
- A execução só começa com orçamento aprovado e estoque suficiente.
- Estoque nunca pode ficar negativo.
- Baixa e devolução de estoque não podem ocorrer em duplicidade.
- Finalização exige execução em andamento.
- Entrega exige finalização.
- Estados terminais não retornam ao fluxo normal.

## Termos preferidos e termos a evitar

| Usar | Evitar | Motivo |
|---|---|---|
| Ordem de Serviço ou OS | Pedido, chamado, ticket | O agregado representa atendimento mecânico completo. |
| Serviço | Produto, tarefa genérica | Serviço é a atividade executada pela oficina. |
| Peça | Produto ou material genérico | Peça é o componente controlado em estoque. |
| Item de Serviço | Serviço da lista | Distingue catálogo e participação na OS. |
| Item de Peça | Peça da lista | Distingue catálogo e participação na OS. |
| Baixa de estoque | Reserva | O modelo atual consome o estoque na aprovação; não há reserva separada. |
| Finalização | Entrega | São momentos diferentes do fluxo. |
| Recusa do orçamento | Cancelamento da OS | Recusa permite revisão; cancelamento encerra a OS. |
| Número da OS | ID da OS | Número é a referência pública; ID é a chave interna. |
| Administrador | Mecânico autenticado | O modelo atual possui apenas o perfil administrativo, sem perfil específico de mecânico. |

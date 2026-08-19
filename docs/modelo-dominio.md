# Modelo de Domínio

## Entidades

O sistema possui as seguintes entidades:

- Cliente
- Veículo
- Serviço
- Peça
- Ordem de Serviço
- Item de Serviço
- Item de Peça
- Usuário administrativo

## Relacionamentos

```text
Cliente
   |
   | 1:N
   |
Veículo
   |
   | 1:N
   |
Ordem de Serviço
   |
   |--------------------|
   |                    |
   | 1:N                | 1:N
   |                    |
Item de Serviço      Item de Peça
   |                    |
   | N:1                | N:1
   |                    |
Serviço               Peça
```

## Cliente e Veículo

Um cliente pode possuir vários veículos.

Um veículo pertence a somente um cliente.

## Cliente e Ordem de Serviço

Um cliente pode possuir várias ordens de serviço.

Cada ordem de serviço pertence a somente um cliente.

## Veículo e Ordem de Serviço

Um veículo pode possuir várias ordens de serviço ao longo do tempo.

Cada ordem de serviço está relacionada a somente um veículo.

## Ordem de Serviço e Serviço

Uma ordem de serviço pode possuir vários serviços.

O relacionamento é representado pela entidade ItemServico, que armazena:

- Serviço
- Quantidade
- Valor unitário
- Subtotal

O valor unitário é armazenado no item para manter o histórico do preço aplicado no momento da criação do orçamento.

## Ordem de Serviço e Peça

Uma ordem de serviço pode possuir várias peças e insumos.

O relacionamento é representado pela entidade ItemPeca, que armazena:

- Peça
- Quantidade
- Valor unitário
- Subtotal

O valor unitário é armazenado no item para manter o histórico do preço aplicado no momento da criação do orçamento.

## Regras do Modelo

- Um veículo deve pertencer a um cliente.
- Uma ordem de serviço deve possuir um cliente.
- Uma ordem de serviço deve possuir um veículo.
- O veículo da ordem de serviço deve pertencer ao cliente informado.
- Uma peça não pode ser utilizada em quantidade maior que o estoque disponível.
- O valor total da ordem de serviço é calculado pela soma dos serviços e das peças.
- A exclusão de um item da ordem de serviço recalcula automaticamente o orçamento.
- A exclusão de uma ordem de serviço remove seus itens associados.
- Um cliente ou veículo com ordem de serviço vinculada não pode ser excluído.
- A aprovação do orçamento baixa o estoque uma única vez.
- O cancelamento devolve o estoque baixado quando aplicável e impede devolução duplicada.
- As coleções de itens da OS usam `Set`/`LinkedHashSet`, evitando carregamento simultâneo de múltiplas bags pelo Hibernate.

## Agregados

### Agregado Cliente

Raiz:

```text
Cliente
```

Entidades associadas:

```text
Veículo
```

### Agregado Ordem de Serviço

Raiz:

```text
OrdemServico
```

Entidades associadas:

```text
ItemServico
ItemPeca
```

### Agregado Serviço

Raiz:

```text
Servico
```

### Agregado Peça

Raiz:

```text
Peca
```

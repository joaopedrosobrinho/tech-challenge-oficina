# Relatório de Análise de Vulnerabilidades

## Identificação

- Projeto: Tech Challenge - Sistema de Oficina Mecânica
- Grupo: Grupo 111
- Responsável: João Pedro de Souza Sobrinho - RM376027
- Data da análise: 19/08/2026
- Ferramenta principal: Docker Scout 1.24.0
- Imagem final analisada: `tech-challenge-oficina:scan-zero`
- ID da imagem: `sha256:4a711121aae1494435c2ff69a5c5c1159044b0a77024198eaae14aa74ee1ec8f`

## Escopo e metodologia

O Docker Scout analisou os pacotes do sistema operacional e as dependências Java empacotadas na imagem executável. O procedimento foi:

1. Construir a imagem da aplicação.
2. Executar o scan inicial em formato SARIF.
3. Classificar os achados por severidade, pacote e versão corrigida.
4. Atualizar dependências Java e a imagem-base.
5. Reconstruir a imagem sem reiniciar os containers em uso.
6. Executar novo scan e confirmar a ausência de vulnerabilidades conhecidas.
7. Executar compilação, testes e verificação JaCoCo.

Comando utilizado:

```powershell
docker scout cves tech-challenge-oficina:scan-zero `
  --format sarif `
  --output target/docker-scout-zero.sarif
```

## Resultado inicial

O primeiro scan indexou 292 pacotes e encontrou 19 vulnerabilidades em 16 pacotes:

| Severidade | Quantidade |
|---|---:|
| Crítica | 0 |
| Alta | 1 |
| Média | 10 |
| Baixa | 8 |
| **Total** | **19** |

### Dependências Java corrigíveis

| Achado | Severidade | Pacote anterior | Versão corrigida aplicada |
|---|---|---|---|
| CVE-2026-54291 | Alta - 8,2 | PostgreSQL JDBC 42.7.11 | 42.7.12 |
| CVE-2025-48924 | Média - 6,5 | Commons Lang 3.17.0 | 3.18.0 |
| CVE-2026-49844 | Média - 6,3 | Log4j API 2.24.3 | 2.25.5 |
| CVE-2026-54515 | Média - 5,3 | Jackson Databind 2.21.4 | 2.21.5 |
| CVE-2026-59889 | Média - 6,5 | Jackson Databind 2.21.4 | 2.21.5 |
| GHSA-mhm7-754m-9p8w | Média - 6,5 | Jackson Databind 2.21.4 | 2.21.5 |

### Achados da imagem Ubuntu substituída

A imagem inicial baseada em Ubuntu Jammy continha 13 vulnerabilidades em pacotes do sistema: `wget`, `expat`, `p11-kit`, `util-linux`, `libpng`, `gcc-12`, `pcre2`, `libzstd`, `shadow`, `ncurses`, `libgcrypt` e `systemd`. O scanner não indicava correção para esses pacotes na imagem utilizada.

Como correção final, a camada de execução foi substituída por `gcr.io/distroless/java21-debian12:nonroot`, preservando Java 21 e reduzindo a superfície da imagem ao mínimo necessário para executar a aplicação.

## Correções realizadas

- PostgreSQL JDBC atualizado de 42.7.11 para 42.7.12.
- Apache Commons Lang atualizado de 3.17.0 para 3.18.0.
- Log4j atualizado de 2.24.3 para 2.25.5.
- Jackson atualizado de 2.21.4 para 2.21.5.
- Runtime Docker migrado de Ubuntu Jammy para uma imagem distroless Java 21.
- Gerenciador de pacotes, shell e utilitários desnecessários removidos da camada de execução.
- Usuário `nonroot` utilizado na imagem final.
- Segredos obrigatórios no Docker Compose, `.env` ignorado pelo Git, SQL oculto por padrão e Swagger desativável em produção.

## Resultado final

O scan final indexou 120 pacotes e não encontrou vulnerabilidades:

| Severidade | Quantidade |
|---|---:|
| Crítica | 0 |
| Alta | 0 |
| Média | 0 |
| Baixa | 0 |
| **Total** | **0** |

Houve redução de 19 para zero achados. O resultado é uma fotografia das bases de vulnerabilidades na data da análise e deve ser revalidado periodicamente.

## Validação após as correções

- `mvn clean compile`: `BUILD SUCCESS`.
- `mvn test`: 157 testes, sem falhas ou erros.
- JaCoCo: regra mínima de 80% no domínio atendida.
- Build da imagem distroless: concluído com sucesso.
- Containers existentes não foram reiniciados e o volume PostgreSQL não foi alterado.

## Evidências e reprodutibilidade

- Relatório SARIF final: `target/docker-scout-zero.sarif`.
- SHA-256 do SARIF: `D5792A5052FB157AAF686DEC92FD7A5A2D83F524C846C9C4032FC13E820A44DF`.
- Tamanho da imagem final: 120.423.210 bytes.
- Resultado final do scanner: 120 pacotes e nenhuma vulnerabilidade detectada.

O diretório `target` é regenerável e não é versionado. Para atualizar a evidência, reconstrua a imagem e execute novamente o comando de scan.

## Limitações

Uma tentativa anterior com OWASP Dependency-Check 12.2.2 não foi usada como resultado, pois a sincronização inicial do NVD/CISA terminou após 9h34 com falhas de API e DNS. O relatório deste documento se baseia exclusivamente no scan concluído com sucesso pelo Docker Scout.

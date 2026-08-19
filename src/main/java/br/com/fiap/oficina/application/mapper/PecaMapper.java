package br.com.fiap.oficina.application.mapper;

import br.com.fiap.oficina.application.dto.peca.PecaRequest;
import br.com.fiap.oficina.application.dto.peca.PecaResponse;
import br.com.fiap.oficina.domain.peca.Peca;

public final class PecaMapper {

    private PecaMapper() {
    }

    public static Peca paraEntidade(PecaRequest request) {
        if (request == null) {
            return null;
        }

        return Peca.builder()
                .codigo(request.codigo().trim().toUpperCase())
                .nome(request.nome().trim())
                .descricao(normalizarTextoOpcional(request.descricao()))
                .valor(request.valor())
                .quantidadeEstoque(request.quantidadeEstoque())
                .ativo(request.ativo() == null || request.ativo())
                .build();
    }

    public static PecaResponse paraResponse(Peca peca) {
        if (peca == null) {
            return null;
        }

        return new PecaResponse(
                peca.getId(),
                peca.getCodigo(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getValor(),
                peca.getQuantidadeEstoque(),
                peca.getAtivo()
        );
    }

    public static void atualizarEntidade(
            Peca peca,
            PecaRequest request
    ) {
        peca.setCodigo(request.codigo().trim().toUpperCase());
        peca.setNome(request.nome().trim());
        peca.setDescricao(normalizarTextoOpcional(request.descricao()));
        peca.setValor(request.valor());
        peca.setQuantidadeEstoque(request.quantidadeEstoque());

        if (request.ativo() != null) {
            peca.setAtivo(request.ativo());
        }
    }

    private static String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
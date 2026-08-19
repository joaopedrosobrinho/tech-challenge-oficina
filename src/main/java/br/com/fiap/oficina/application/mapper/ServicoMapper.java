package br.com.fiap.oficina.application.mapper;

import br.com.fiap.oficina.application.dto.servico.ServicoRequest;
import br.com.fiap.oficina.application.dto.servico.ServicoResponse;
import br.com.fiap.oficina.domain.servico.Servico;

public final class ServicoMapper {

    private ServicoMapper() {
    }

    public static Servico paraEntidade(ServicoRequest request) {
        if (request == null) {
            return null;
        }

        return Servico.builder()
                .nome(request.nome().trim())
                .descricao(normalizarTextoOpcional(request.descricao()))
                .valor(request.valor())
                .tempoEstimadoMinutos(request.tempoEstimadoMinutos())
                .ativo(request.ativo() == null || request.ativo())
                .build();
    }

    public static ServicoResponse paraResponse(Servico servico) {
        if (servico == null) {
            return null;
        }

        return new ServicoResponse(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getValor(),
                servico.getTempoEstimadoMinutos(),
                servico.getAtivo()
        );
    }

    public static void atualizarEntidade(
            Servico servico,
            ServicoRequest request
    ) {
        servico.setNome(request.nome().trim());
        servico.setDescricao(normalizarTextoOpcional(request.descricao()));
        servico.setValor(request.valor());
        servico.setTempoEstimadoMinutos(request.tempoEstimadoMinutos());

        if (request.ativo() != null) {
            servico.setAtivo(request.ativo());
        }
    }

    private static String normalizarTextoOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
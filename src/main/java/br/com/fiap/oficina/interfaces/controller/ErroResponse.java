package br.com.fiap.oficina.interfaces.controller;

import java.time.LocalDateTime;
import java.util.Map;

public record ErroResponse(
        LocalDateTime dataHora,
        Integer status,
        String erro,
        String mensagem,
        String caminho,
        Map<String, String> campos
) {
}
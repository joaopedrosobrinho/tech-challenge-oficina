package br.com.fiap.oficina.domain.common;

public class ExcecaoDominio extends RuntimeException {

    public ExcecaoDominio(String mensagem) {
        super(mensagem);
    }
}
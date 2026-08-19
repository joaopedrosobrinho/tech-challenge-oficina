package br.com.fiap.oficina.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator
        implements ConstraintValidator<CpfCnpjValido, String> {

    @Override
    public boolean isValid(
            String documento,
            ConstraintValidatorContext context
    ) {
        return documento == null || documento.isBlank()
                || documentoValido(documento);
    }

    public static boolean documentoValido(String documento) {
        if (documento == null || documento.isBlank()
                || !documento.matches("[0-9.\\-/\\s]+")) {
            return false;
        }

        String numeros = documento.replaceAll("\\D", "");

        if (todosDigitosIguais(numeros)) {
            return false;
        }

        return numeros.length() == 11
                ? cpfValido(numeros)
                : numeros.length() == 14 && cnpjValido(numeros);
    }

    private static boolean cpfValido(String cpf) {
        int primeiroDigito = calcularDigitoCpf(cpf, 9, 10);

        if (primeiroDigito != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        int segundoDigito = calcularDigitoCpf(cpf, 10, 11);
        return segundoDigito == Character.getNumericValue(cpf.charAt(10));
    }

    private static int calcularDigitoCpf(
            String cpf,
            int quantidade,
            int pesoInicial
    ) {
        int soma = 0;

        for (int indice = 0; indice < quantidade; indice++) {
            soma += Character.getNumericValue(cpf.charAt(indice))
                    * (pesoInicial - indice);
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean cnpjValido(String cnpj) {
        int[] pesosPrimeiro = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundo = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int primeiroDigito = calcularDigitoCnpj(cnpj, pesosPrimeiro);

        if (primeiroDigito != Character.getNumericValue(cnpj.charAt(12))) {
            return false;
        }

        int segundoDigito = calcularDigitoCnpj(cnpj, pesosSegundo);
        return segundoDigito == Character.getNumericValue(cnpj.charAt(13));
    }

    private static int calcularDigitoCnpj(String cnpj, int[] pesos) {
        int soma = 0;

        for (int indice = 0; indice < pesos.length; indice++) {
            soma += Character.getNumericValue(cnpj.charAt(indice))
                    * pesos[indice];
        }

        int resto = soma % 11;
        return resto < 2 ? 0 : 11 - resto;
    }

    private static boolean todosDigitosIguais(String numeros) {
        return !numeros.isEmpty()
                && numeros.chars().allMatch(digito -> digito == numeros.charAt(0));
    }
}

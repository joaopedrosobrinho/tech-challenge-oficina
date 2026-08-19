package br.com.fiap.oficina.application.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class PlacaValidator
        implements ConstraintValidator<PlacaValida, String> {

    private static final String FORMATO_ANTIGO = "[A-Z]{3}[0-9]{4}";
    private static final String FORMATO_MERCOSUL = "[A-Z]{3}[0-9][A-Z][0-9]{2}";

    @Override
    public boolean isValid(
            String placa,
            ConstraintValidatorContext context
    ) {
        return placa == null || placa.isBlank() || placaValida(placa);
    }

    public static boolean placaValida(String placa) {
        if (placa == null || placa.isBlank()
                || !placa.matches("[A-Za-z0-9\\-\\s]+")) {
            return false;
        }

        String placaNormalizada = normalizar(placa);
        return placaNormalizada.matches(FORMATO_ANTIGO)
                || placaNormalizada.matches(FORMATO_MERCOSUL);
    }

    public static String normalizar(String placa) {
        if (placa == null) {
            return null;
        }

        return placa.replaceAll("[-\\s]", "")
                .toUpperCase(Locale.ROOT);
    }
}

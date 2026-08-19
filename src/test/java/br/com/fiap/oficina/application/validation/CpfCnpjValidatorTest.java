package br.com.fiap.oficina.application.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfCnpjValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "52998224725",
            "529.982.247-25",
            "11222333000181",
            "11.222.333/0001-81"
    })
    void deveAceitarCpfECnpjValidos(String documento) {
        assertTrue(CpfCnpjValidator.documentoValido(documento));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11111111111",
            "00000000000000",
            "52998224724",
            "11222333000180",
            "123",
            "abc52998224725"
    })
    void deveRejeitarCpfECnpjInvalidos(String documento) {
        assertFalse(CpfCnpjValidator.documentoValido(documento));
    }
}

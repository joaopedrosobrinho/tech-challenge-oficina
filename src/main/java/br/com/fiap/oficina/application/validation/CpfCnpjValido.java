package br.com.fiap.oficina.application.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = CpfCnpjValidator.class)
@Target({
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.ANNOTATION_TYPE,
        ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfCnpjValido {

    String message() default "O CPF/CNPJ informado é inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}

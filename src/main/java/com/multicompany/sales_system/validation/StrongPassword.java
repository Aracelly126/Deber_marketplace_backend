package com.multicompany.sales_system.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default
            "La contraseña debe tener mínimo 8 caracteres e incluir mayúscula, minúscula, número y símbolo";
    int minLength() default 8;      // puedes subir a 10/12 si lo deseas
    boolean requireUppercase() default true;
    boolean requireLowercase() default true;
    boolean requireDigit() default true;
    boolean requireSymbol() default true; // símbolos como !@#$%^&*()-_+= etc.
    boolean forbidSpaces() default true;

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

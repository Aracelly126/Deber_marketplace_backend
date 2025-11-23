package com.multicompany.sales_system.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    private int minLength;
    private boolean requireUppercase;
    private boolean requireLowercase;
    private boolean requireDigit;
    private boolean requireSymbol;
    private boolean forbidSpaces;

    @Override
    public void initialize(StrongPassword cfg) {
        this.minLength = cfg.minLength();
        this.requireUppercase = cfg.requireUppercase();
        this.requireLowercase = cfg.requireLowercase();
        this.requireDigit = cfg.requireDigit();
        this.requireSymbol = cfg.requireSymbol();
        this.forbidSpaces = cfg.forbidSpaces();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext ctx) {
        if (password == null) return false;

        // Longitud mínima
        if (password.length() < minLength) return false;

        // Flags
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSymbol = false;
        boolean hasSpace = false;

        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            else if (Character.isLowerCase(ch)) hasLower = true;
            else if (Character.isDigit(ch)) hasDigit = true;
            else if (Character.isWhitespace(ch)) hasSpace = true;
            else hasSymbol = true; // cualquier otro char lo consideramos símbolo
        }

        if (forbidSpaces && hasSpace) return false;
        if (requireUppercase && !hasUpper) return false;
        if (requireLowercase && !hasLower) return false;
        if (requireDigit && !hasDigit) return false;
        if (requireSymbol && !hasSymbol) return false;

        return true;
    }
}

package com.multicompany.sales_system.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CedulaEcuatorianaValidator implements ConstraintValidator<CedulaEcuatoriana, String> {

    @Override
    public boolean isValid(String cedula, ConstraintValidatorContext context) {
        // 🔹 Validar que no sea nulo y que tenga exactamente 10 dígitos
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }

        // 🔹 Extraer el código de provincia (los dos primeros dígitos)
        int codigoProvincia = Integer.parseInt(cedula.substring(0, 2));
        if (codigoProvincia < 1 || codigoProvincia > 24) {
            return false; // provincias válidas: 01 - 24
        }

        // 🔹 Validar tercer dígito (solo 0-5 son válidos para personas naturales)
        int tercerDigito = Character.getNumericValue(cedula.charAt(2));
        if (tercerDigito >= 6) {
            return false;
        }

        // 🔹 Algoritmo de validación módulo 10
        int[] coeficientes = {2, 1, 2, 1, 2, 1, 2, 1, 2};
        int suma = 0;

        // Recorremos los 9 primeros dígitos
        for (int i = 0; i < 9; i++) {
            int digito = Character.getNumericValue(cedula.charAt(i));
            int producto = digito * coeficientes[i];

            // Si el resultado es mayor o igual a 10, restamos 9
            if (producto >= 10) {
                producto -= 9;
            }
            suma += producto;
        }

        // 🔹 Obtener el dígito verificador (último número de la cédula)
        int digitoVerificador = Character.getNumericValue(cedula.charAt(9));

        // 🔹 Calcular el dígito esperado con módulo 10
        int digitoEsperado = (suma % 10 == 0) ? 0 : (10 - (suma % 10));

        // 🔹 Comparar el dígito verificador con el esperado
        return digitoVerificador == digitoEsperado;
    }
}

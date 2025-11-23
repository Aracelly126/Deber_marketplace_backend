package com.multicompany.sales_system.dto.user;

import com.multicompany.sales_system.validation.CedulaEcuatoriana;
import com.multicompany.sales_system.validation.StrongPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String nombre;

    @NotBlank
    private String apellido;

    @CedulaEcuatoriana
    private String cedula;

    @NotBlank
    @Email
    private String correo;

    @StrongPassword(
        minLength = 8,
        message = "La contraseña debe tener mínimo 8 caracteres, incluir mayúscula, minúscula, número y símbolo"
    )
    @NotBlank
    private String contrasena;

    // Campos opcionales
    private String telefono;
    private String direccion;
    private String genero;
}

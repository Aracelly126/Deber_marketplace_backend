package com.multicompany.sales_system.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank @Email
    private String correo;

    @NotBlank
    private String contrasena;

    private boolean rememberMe = false;
}

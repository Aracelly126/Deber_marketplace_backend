package com.multicompany.sales_system.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private Long id;
    private String correo;
    private String nombre;
    private String apellido;
    private String rol;
    private String estado;
    private boolean emailVerificado;
    private String message;
    private String token;
}

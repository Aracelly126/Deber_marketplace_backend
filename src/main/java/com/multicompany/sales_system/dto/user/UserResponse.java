package com.multicompany.sales_system.dto.user;

import com.multicompany.sales_system.model.enums.UsuarioRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long idUsuario;
    private String cedula;
    private String nombre;
    private String apellido;
    private String correo;
    private String telefono;
    private String direccion;
    private String genero;
    private UsuarioRole rol;
    private String estado;
    private boolean emailVerificado;
    private LocalDateTime fechaRegistro;
}

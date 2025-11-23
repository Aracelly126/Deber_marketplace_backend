package com.multicompany.sales_system.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminCreateModeratorRequest {

    @NotBlank @Size(max = 255)
    private String nombre;

    @NotBlank @Size(max = 255)
    private String apellido;

    @NotBlank @Size(min = 10, max = 10, message = "La cédula debe tener 10 dígitos")
    private String cedula;

    @NotBlank @Email @Size(max = 255)
    private String correo;

    @NotBlank @Size(min = 8, max = 255)  // tu regla de password
    private String contrasena;

    @Size(max = 25)  private String telefono;
    @Size(max = 255) private String direccion;
    @Size(max = 2)   private String genero;
}

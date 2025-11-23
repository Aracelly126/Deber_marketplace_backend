package com.multicompany.sales_system.dto.user;

import com.multicompany.sales_system.model.Usuario.EstadoUsuario;
import com.multicompany.sales_system.model.enums.UsuarioRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 255, message = "El apellido no puede exceder 255 caracteres")
    private String apellido;

    @NotBlank(message = "La cédula es obligatoria")
    @Size(max = 25, message = "La cédula no puede exceder 25 caracteres")
    private String cedula;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 255, message = "El correo no puede exceder 255 caracteres")
    private String correo;

    @Size(max = 25, message = "El teléfono no puede exceder 25 caracteres")
    private String telefono;

    @Size(max = 255, message = "La dirección no puede exceder 255 caracteres")
    private String direccion;

    @Size(max = 10, message = "El género no puede exceder 10 caracteres")
    private String genero;

    private UsuarioRole rol;

    private EstadoUsuario estado;
}

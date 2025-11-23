package com.multicompany.sales_system.dto.gestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

/**
 * DTO para solicitudes de interés en productos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InteresRequestDTO {

    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;
}
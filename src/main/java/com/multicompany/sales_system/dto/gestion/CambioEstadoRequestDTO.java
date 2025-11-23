package com.multicompany.sales_system.dto.gestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitudes de cambio de estado de producto
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CambioEstadoRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotBlank(message = "El nuevo estado es obligatorio")
    private String nuevoEstado;

    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    private Long usuarioId; // ID del usuario que solicita el cambio (opcional para algunas operaciones)
}
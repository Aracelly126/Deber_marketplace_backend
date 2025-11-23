package com.multicompany.sales_system.dto.gestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para solicitudes de apelación de productos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApelacionRequestDTO {

    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @NotNull(message = "El ID del vendedor es obligatorio")
    private Long vendedorId;

    @NotBlank(message = "La justificación es obligatoria")
    private String justificacion;

    private String comentarios; // Comentarios adicionales del vendedor
}
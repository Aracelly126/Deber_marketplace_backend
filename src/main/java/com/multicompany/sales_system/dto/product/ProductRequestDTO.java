package com.multicompany.sales_system.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {

    /**
     * El código ya NO debe enviarse desde el frontend al crear un producto.
     * Se genera automáticamente en el backend como UUID. Se deja el campo
     * aquí opcional para permitir actualizaciones si es necesario.
     */
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private Double precio;

    private String ubicacion;

    private Boolean disponibilidad = true;

    @NotNull(message = "El tipo es obligatorio")
    private String tipo;

    /**
     * Horario (opcional) utilizado cuando el tipo es SERVICIO.
     */
    private String horario;

    @NotNull(message = "El ID del vendedor es obligatorio")
    private Long idVendedor;

    @NotNull(message = "El ID de la categoría es obligatorio")
    private Long idCategoria;
}
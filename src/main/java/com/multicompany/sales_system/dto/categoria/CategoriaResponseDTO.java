package com.multicompany.sales_system.dto.categoria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaResponseDTO {

    private Long idCategoria;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private Integer cantidadProductos; // Cantidad de productos en esta categoría

    // Constructor sin cantidad de productos
    public CategoriaResponseDTO(Long idCategoria, String nombre, String descripcion, Boolean activo,
            LocalDateTime fechaCreacion) {
        this.idCategoria = idCategoria;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
        this.cantidadProductos = 0;
    }
}

package com.multicompany.sales_system.dto.gestion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * DTO para respuestas de operaciones de gestión
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GestionResponseDTO {

    private boolean exitoso;
    private String mensaje;
    private Long productoId;
    private String estadoAnterior;
    private String estadoNuevo;
    private LocalDateTime timestamp;

    // Constructor para respuestas simples
    public GestionResponseDTO(boolean exitoso, String mensaje) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    // Constructor para cambios de estado
    public GestionResponseDTO(boolean exitoso, String mensaje, Long productoId, String estadoAnterior,
            String estadoNuevo) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.productoId = productoId;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.timestamp = LocalDateTime.now();
    }
}
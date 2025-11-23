package com.multicompany.sales_system.dto.incident;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidenciaResponseDTO {
    private Long idIncidencia;
    private Long idProducto;
    private String nombreProducto;
    private Long idUsuarioReporta;
    private String nombreUsuarioReporta;
    private String motivo;
    private String descripcion;
    private String estado;
    private LocalDateTime fechaRegistro;
}
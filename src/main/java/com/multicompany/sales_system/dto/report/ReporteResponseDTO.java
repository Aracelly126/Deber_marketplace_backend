package com.multicompany.sales_system.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteResponseDTO {
    private Long idReporte;
    private Long idIncidencia;
    private String motivoIncidencia;
    private Long idModerador;
    private String nombreModerador;
    private String accionTomada;
    private String comentario;
    private LocalDateTime fechaAccion;
}
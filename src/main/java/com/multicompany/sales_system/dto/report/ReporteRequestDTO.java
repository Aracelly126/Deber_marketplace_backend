package com.multicompany.sales_system.dto.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReporteRequestDTO {

    @NotNull(message = "El ID de la incidencia es obligatorio")
    private Long idIncidencia;

    @NotNull(message = "El ID del moderador es obligatorio")
    private Long idModerador;

    @NotBlank(message = "La acción tomada es obligatoria")
    @Size(max = 500, message = "La acción tomada no puede exceder los 500 caracteres")
    private String accionTomada;

    @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres")
    private String comentario;
}
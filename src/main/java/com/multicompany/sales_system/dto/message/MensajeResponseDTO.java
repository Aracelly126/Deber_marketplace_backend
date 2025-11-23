package com.multicompany.sales_system.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeResponseDTO {
    private Long idMensaje;
    private Long idChat;
    private Long idEmisor;
    private String nombreEmisor;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private Boolean leido;
}
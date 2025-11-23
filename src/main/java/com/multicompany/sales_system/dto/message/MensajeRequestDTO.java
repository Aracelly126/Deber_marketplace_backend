package com.multicompany.sales_system.dto.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MensajeRequestDTO {

    @NotNull(message = "El ID del chat es obligatorio")
    private Long idChat;

    @NotNull(message = "El ID del emisor es obligatorio")
    private Long idEmisor;

    @NotBlank(message = "El contenido no puede estar vacío")
    private String contenido;
}
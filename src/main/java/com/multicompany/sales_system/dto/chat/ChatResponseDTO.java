package com.multicompany.sales_system.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import com.multicompany.sales_system.dto.message.MensajeResponseDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {
    private Long idChat;
    private Long idUsuario1;
    private String nombreUsuario1;
    private Long idUsuario2;
    private String nombreUsuario2;
    private List<MensajeResponseDTO> mensajes;
}
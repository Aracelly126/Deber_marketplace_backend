package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.chat.ChatResponseDTO;
import com.multicompany.sales_system.dto.message.MensajeRequestDTO;
import com.multicompany.sales_system.dto.message.MensajeResponseDTO;

import java.util.List;

public interface ChatService {
    ChatResponseDTO crearObtenerChat(Long idUsuario1, Long idUsuario2);

    List<ChatResponseDTO> obtenerChatsPorUsuario(Long idUsuario);

    MensajeResponseDTO enviarMensaje(MensajeRequestDTO mensajeRequest);

    List<MensajeResponseDTO> obtenerMensajesPorChat(Long idChat);

    void marcarMensajesComoLeidos(Long idChat, Long idUsuario);

    Long obtenerIdReceptor(Long idChat, Long idEmisor);

    Integer contarMensajesNoLeidos(Long idUsuario);
}
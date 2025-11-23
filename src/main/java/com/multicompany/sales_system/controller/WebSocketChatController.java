package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.message.MensajeRequestDTO;
import com.multicompany.sales_system.dto.message.MensajeResponseDTO;
import com.multicompany.sales_system.service.ChatService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.enviar")
    public void enviarMensaje(MensajeRequestDTO mensajeRequest) {
        try {
            // Guardar el mensaje en la base de datos
            MensajeResponseDTO mensajeGuardado = chatService.enviarMensaje(mensajeRequest);

            // Enviar a ambos usuarios del chat
            String destino = "/topic/chat." + mensajeRequest.getIdChat();
            messagingTemplate.convertAndSend(destino, mensajeGuardado);

            // También enviar notificación individual al receptor
            String destinoUsuario = "/queue/chat." + obtenerIdReceptor(mensajeRequest);
            messagingTemplate.convertAndSend(destinoUsuario, mensajeGuardado);

        } catch (Exception e) {
            // Enviar error al usuario emisor
            String destinoError = "/user/queue/errors";
            messagingTemplate.convertAndSendToUser(
                    mensajeRequest.getIdEmisor().toString(),
                    destinoError,
                    "Error al enviar mensaje: " + e.getMessage());
        }
    }

    private Long obtenerIdReceptor(MensajeRequestDTO mensajeRequest) {
        // Este método debería obtener el ID del receptor basado en el chat
        // Lo implementaremos en el servicio
        return chatService.obtenerIdReceptor(mensajeRequest.getIdChat(), mensajeRequest.getIdEmisor());
    }

    @MessageMapping("/chat.marcar-leido")
    public void marcarMensajesLeidos(@Payload Map<String, Object> payload) {
        Long idChat = Long.valueOf(payload.get("idChat").toString());
        Long idUsuario = Long.valueOf(payload.get("idUsuario").toString());

        try {
            chatService.marcarMensajesComoLeidos(idChat, idUsuario);

            // Notificar al otro usuario que sus mensajes fueron leídos
            Long idOtroUsuario = chatService.obtenerIdReceptor(idChat, idUsuario);
            String destino = "/queue/mensajes-leidos." + idOtroUsuario;

            Map<String, Object> notificacion = new HashMap<>();
            notificacion.put("idChat", idChat);
            notificacion.put("timestamp", LocalDateTime.now());
            notificacion.put("mensaje", "Tus mensajes han sido leídos");

            messagingTemplate.convertAndSend(destino, notificacion);

        } catch (Exception e) {
            // Enviar error
            String destinoError = "/user/queue/errors";
            messagingTemplate.convertAndSendToUser(
                    idUsuario.toString(),
                    destinoError,
                    "Error al marcar mensajes como leídos: " + e.getMessage());
        }
    }
}
package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.chat.ChatResponseDTO;
import com.multicompany.sales_system.dto.message.MensajeResponseDTO;
import com.multicompany.sales_system.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<ChatResponseDTO>> obtenerChatsPorUsuario(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(chatService.obtenerChatsPorUsuario(idUsuario));
    }

    @GetMapping("/{idUsuario1}/{idUsuario2}")
    public ResponseEntity<ChatResponseDTO> crearObtenerChat(@PathVariable Long idUsuario1,
            @PathVariable Long idUsuario2) {
        return ResponseEntity.ok(chatService.crearObtenerChat(idUsuario1, idUsuario2));
    }

    @GetMapping("/{idChat}/mensajes")
    public ResponseEntity<List<MensajeResponseDTO>> obtenerMensajesPorChat(@PathVariable Long idChat) {
        return ResponseEntity.ok(chatService.obtenerMensajesPorChat(idChat));
    }

    @GetMapping("/usuario/{idUsuario}/mensajes-no-leidos")
    public ResponseEntity<Integer> contarMensajesNoLeidos(@PathVariable Long idUsuario) {
        return ResponseEntity.ok(chatService.contarMensajesNoLeidos(idUsuario));
    }

    @PutMapping("/{idChat}/marcar-leido")
    public ResponseEntity<Void> marcarMensajesComoLeidos(
            @PathVariable Long idChat,
            @RequestParam Long usuarioId) {
        chatService.marcarMensajesComoLeidos(idChat, usuarioId);
        return ResponseEntity.ok().build();
    }
}
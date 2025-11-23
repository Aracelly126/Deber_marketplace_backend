package com.multicompany.sales_system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.dto.chat.ChatResponseDTO;
import com.multicompany.sales_system.dto.message.MensajeRequestDTO;
import com.multicompany.sales_system.dto.message.MensajeResponseDTO;
import com.multicompany.sales_system.model.Chat;
import com.multicompany.sales_system.model.Mensaje;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.ChatRepository;
import com.multicompany.sales_system.repository.MensajeRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.ChatService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final MensajeRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;

    public ChatServiceImpl(ChatRepository chatRepository,
            MensajeRepository mensajeRepository,
            UsuarioRepository usuarioRepository) {
        this.chatRepository = chatRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public ChatResponseDTO crearObtenerChat(Long idUsuario1, Long idUsuario2) {
        // Buscar chat existente entre los dos usuarios
        Chat chat = chatRepository.findByUsuarios(idUsuario1, idUsuario2)
                .orElseGet(() -> {
                    // Crear nuevo chat si no existe
                    Usuario usuario1 = usuarioRepository.findById(idUsuario1)
                            .orElseThrow(() -> new RuntimeException("Usuario 1 no encontrado"));
                    Usuario usuario2 = usuarioRepository.findById(idUsuario2)
                            .orElseThrow(() -> new RuntimeException("Usuario 2 no encontrado"));

                    Chat nuevoChat = new Chat();
                    nuevoChat.setUsuario1(usuario1);
                    nuevoChat.setUsuario2(usuario2);
                    return chatRepository.save(nuevoChat);
                });

        return toChatResponseDTO(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatResponseDTO> obtenerChatsPorUsuario(Long idUsuario) {
        return chatRepository.findByUsuarioId(idUsuario)
                .stream()
                .map(this::toChatResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MensajeResponseDTO enviarMensaje(MensajeRequestDTO mensajeRequest) {
        Chat chat = chatRepository.findById(mensajeRequest.getIdChat())
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        Usuario emisor = usuarioRepository.findById(mensajeRequest.getIdEmisor())
                .orElseThrow(() -> new RuntimeException("Emisor no encontrado"));

        // Validar que el emisor sea parte del chat
        if (!chat.getUsuario1().getIdUsuario().equals(emisor.getIdUsuario()) &&
                !chat.getUsuario2().getIdUsuario().equals(emisor.getIdUsuario())) {
            throw new RuntimeException("El usuario no pertenece a este chat");
        }

        Mensaje mensaje = new Mensaje();
        mensaje.setChat(chat);
        mensaje.setEmisor(emisor);
        mensaje.setContenido(mensajeRequest.getContenido());
        mensaje.setLeido(false);

        Mensaje mensajeGuardado = mensajeRepository.save(mensaje);
        return toMensajeResponseDTO(mensajeGuardado);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MensajeResponseDTO> obtenerMensajesPorChat(Long idChat) {
        return mensajeRepository.findByChatIdChatOrderByFechaEnvioAsc(idChat)
                .stream()
                .map(this::toMensajeResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void marcarMensajesComoLeidos(Long idChat, Long idUsuario) {
        List<Mensaje> mensajesNoLeidos = mensajeRepository.findByChatIdChatAndLeidoFalseAndEmisorIdUsuarioNot(idChat,
                idUsuario);
        mensajesNoLeidos.forEach(mensaje -> mensaje.setLeido(true));
        mensajeRepository.saveAll(mensajesNoLeidos);
    }

    @Override
    public Long obtenerIdReceptor(Long idChat, Long idEmisor) {
        Chat chat = chatRepository.findById(idChat)
                .orElseThrow(() -> new RuntimeException("Chat no encontrado"));

        if (chat.getUsuario1().getIdUsuario().equals(idEmisor)) {
            return chat.getUsuario2().getIdUsuario();
        } else {
            return chat.getUsuario1().getIdUsuario();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Integer contarMensajesNoLeidos(Long idUsuario) {
        return mensajeRepository.countMensajesNoLeidosPorUsuario(idUsuario);
    }

    // Métodos helper para conversión DTO
    private ChatResponseDTO toChatResponseDTO(Chat chat) {
        ChatResponseDTO dto = new ChatResponseDTO();
        dto.setIdChat(chat.getIdChat());
        dto.setIdUsuario1(chat.getUsuario1().getIdUsuario());
        dto.setNombreUsuario1(chat.getUsuario1().getNombre());
        dto.setIdUsuario2(chat.getUsuario2().getIdUsuario());
        dto.setNombreUsuario2(chat.getUsuario2().getNombre());

        // Cargar mensajes si es necesario
        if (chat.getMensajes() != null) {
            dto.setMensajes(chat.getMensajes().stream()
                    .map(this::toMensajeResponseDTO)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private MensajeResponseDTO toMensajeResponseDTO(Mensaje mensaje) {
        MensajeResponseDTO dto = new MensajeResponseDTO();
        dto.setIdMensaje(mensaje.getIdMensaje());
        dto.setIdChat(mensaje.getChat().getIdChat());
        dto.setIdEmisor(mensaje.getEmisor().getIdUsuario());
        dto.setNombreEmisor(mensaje.getEmisor().getNombre());
        dto.setContenido(mensaje.getContenido());
        dto.setFechaEnvio(mensaje.getFechaEnvio());
        dto.setLeido(mensaje.getLeido());
        return dto;
    }
}
package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mensaje")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    @ManyToOne
    @JoinColumn(name = "id_chat")
    private Chat chat;

    @ManyToOne
    @JoinColumn(name = "id_emisor")
    private Usuario emisor;

    private String contenido;
    private LocalDateTime fechaEnvio = LocalDateTime.now();

    private Boolean leido = false;
}

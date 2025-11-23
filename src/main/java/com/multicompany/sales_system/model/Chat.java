package com.multicompany.sales_system.model;


import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "chat")
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idChat;

    @ManyToOne
    @JoinColumn(name = "id_usuario1")
    private Usuario usuario1;

    @ManyToOne
    @JoinColumn(name = "id_usuario2")
    private Usuario usuario2;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mensaje> mensajes;
}

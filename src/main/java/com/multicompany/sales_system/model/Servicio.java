package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    private Long id; // Compartido con Producto (MapsId)

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_producto")
    private Producto producto;

    /**
     * Horario del servicio. Puede ser una representación simple (texto/JSON) por ahora.
     */
    private String horario;
}

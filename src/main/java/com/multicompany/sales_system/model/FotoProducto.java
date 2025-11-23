package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "foto_producto")
public class FotoProducto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idFoto;

    private String url;

    @ManyToOne
    @JoinColumn(name = "id_producto")
    private Producto producto;
}

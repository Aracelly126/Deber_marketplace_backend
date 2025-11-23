package com.multicompany.sales_system.model;

import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProducto;

    @Column(unique = true, nullable = false)
    private String codigo;

    private String nombre;
    private String descripcion;
    private Double precio;
    private String ubicacion;
    private Boolean disponibilidad = true;

    @Enumerated(EnumType.STRING)
    private TipoProducto tipo;

    @Enumerated(EnumType.STRING)
    private EstadoProducto estado = EstadoProducto.ACTIVO;

    private LocalDateTime fechaPublicacion = LocalDateTime.now();

    private LocalDateTime fechaExpiracion; // Fecha de expiración de la publicación

    @ManyToOne
    @JoinColumn(name = "id_vendedor")
    private Usuario vendedor;

    @ManyToOne
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<FotoProducto> fotos;
}

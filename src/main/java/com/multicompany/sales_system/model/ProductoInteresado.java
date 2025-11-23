package com.multicompany.sales_system.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad que representa la relación de interés entre un usuario y un producto
 * Implementa la funcionalidad "me interesa" donde los usuarios pueden guardar
 * productos
 */
@Data
@Entity
@Table(name = "producto_interesado", uniqueConstraints = @UniqueConstraint(name = "uk_producto_usuario_interes", columnNames = {
        "id_usuario", "id_producto" }))
public class ProductoInteresado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idProductoInteresado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "fecha_interes", nullable = false)
    private LocalDateTime fechaInteres = LocalDateTime.now();

    /**
     * Constructor por defecto
     */
    public ProductoInteresado() {
    }

    /**
     * Constructor con parámetros
     */
    public ProductoInteresado(Usuario usuario, Producto producto) {
        this.usuario = usuario;
        this.producto = producto;
        this.fechaInteres = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        if (fechaInteres == null) {
            fechaInteres = LocalDateTime.now();
        }
    }
}

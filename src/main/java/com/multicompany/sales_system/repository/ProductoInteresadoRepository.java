package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.ProductoInteresado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para gestionar la funcionalidad "me interesa" entre usuarios y
 * productos
 */
@Repository
public interface ProductoInteresadoRepository extends JpaRepository<ProductoInteresado, Long> {
        /**
         * Contar el total de intereses recibidos por todos los productos de un vendedor
         */
        @Query("SELECT COUNT(pi) FROM ProductoInteresado pi WHERE pi.producto.vendedor.idUsuario = :vendedorId")
        Long countInteresesByVendedor(@Param("vendedorId") Long vendedorId);

    /**
     * Buscar si un usuario ya tiene marcado como "me interesa" un producto
     * específico
     */
    Optional<ProductoInteresado> findByUsuarioIdUsuarioAndProductoIdProducto(Long usuarioId, Long productoId);

    /**
     * Verificar si existe la relación de interés entre usuario y producto
     */
    boolean existsByUsuarioIdUsuarioAndProductoIdProducto(Long usuarioId, Long productoId);

    /**
     * Obtener todos los productos marcados como "me interesa" por un usuario con
     * paginación
     */
    Page<ProductoInteresado> findByUsuarioIdUsuarioOrderByFechaInteresDesc(Long usuarioId, Pageable pageable);

    /**
     * Obtener todos los productos marcados como "me interesa" por un usuario (sin
     * paginación)
     */
    List<ProductoInteresado> findByUsuarioIdUsuarioOrderByFechaInteresDesc(Long usuarioId);

    /**
     * Contar cuántos usuarios han marcado como "me interesa" un producto específico
     */
    @Query("SELECT COUNT(pi) FROM ProductoInteresado pi WHERE pi.producto.idProducto = :productoId")
    Long countInteresesByProducto(@Param("productoId") Long productoId);

    /**
     * Obtener los usuarios que han marcado como "me interesa" un producto
     * específico
     */
    List<ProductoInteresado> findByProductoIdProductoOrderByFechaInteresDesc(Long productoId);

    /**
     * Eliminar la relación de interés entre un usuario y un producto
     */
    void deleteByUsuarioIdUsuarioAndProductoIdProducto(Long usuarioId, Long productoId);

    /**
     * Obtener productos más populares (con más "me interesa")
     */
    @Query("SELECT pi.producto.idProducto, COUNT(pi) as total " +
            "FROM ProductoInteresado pi " +
            "WHERE pi.producto.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
            "GROUP BY pi.producto.idProducto " +
            "ORDER BY total DESC")
    Page<Object[]> findProductosMasPopulares(Pageable pageable);

    /**
     * Buscar productos de interés de un usuario filtrados por tipo de producto
     */
    @Query("SELECT pi FROM ProductoInteresado pi " +
            "WHERE pi.usuario.idUsuario = :usuarioId " +
            "AND pi.producto.tipo = :tipo " +
            "AND pi.producto.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
            "ORDER BY pi.fechaInteres DESC")
    Page<ProductoInteresado> findByUsuarioAndTipoProducto(
            @Param("usuarioId") Long usuarioId,
            @Param("tipo") com.multicompany.sales_system.model.enums.TipoProducto tipo,
            Pageable pageable);

    /**
     * Buscar productos de interés de un usuario por rango de precio
     */
    @Query("SELECT pi FROM ProductoInteresado pi " +
            "WHERE pi.usuario.idUsuario = :usuarioId " +
            "AND pi.producto.precio BETWEEN :minPrice AND :maxPrice " +
            "AND pi.producto.estado = com.multicompany.sales_system.model.enums.EstadoProducto.ACTIVO " +
            "ORDER BY pi.fechaInteres DESC")
    Page<ProductoInteresado> findByUsuarioAndRangoPrecio(
            @Param("usuarioId") Long usuarioId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);
}

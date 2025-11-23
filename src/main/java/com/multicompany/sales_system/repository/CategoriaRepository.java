package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /**
     * Buscar categorías activas
     */
    List<Categoria> findByActivo(Boolean activo);

    /**
     * Buscar categorías activas con paginación
     */
    Page<Categoria> findByActivo(Boolean activo, Pageable pageable);

    /**
     * Buscar categoría por nombre exacto (case insensitive)
     */
    Optional<Categoria> findByNombreIgnoreCase(String nombre);

    /**
     * Buscar categorías que contengan el texto en el nombre
     */
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);

    /**
     * Buscar categorías que contengan el texto en el nombre con paginación
     */
    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    /**
     * Verificar si existe una categoría con ese nombre (útil para validación)
     */
    boolean existsByNombreIgnoreCase(String nombre);

    /**
     * Contar productos en una categoría
     */
    @Query("SELECT COUNT(p) FROM Producto p WHERE p.categoria.idCategoria = :categoriaId")
    Long countProductosByCategoriaId(@Param("categoriaId") Long categoriaId);

    /**
     * Verificar si una categoría tiene productos asociados
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Producto p WHERE p.categoria.idCategoria = :categoriaId")
    boolean categoriaHasProductos(@Param("categoriaId") Long categoriaId);

    /**
     * Obtener categorías ordenadas por cantidad de productos
     */
    @Query("SELECT c FROM Categoria c LEFT JOIN c.productos p GROUP BY c ORDER BY COUNT(p) DESC")
    List<Categoria> findAllOrderByProductCount();

    /**
     * Buscar categorías activas ordenadas por nombre
     */
    List<Categoria> findByActivoOrderByNombreAsc(Boolean activo);
}

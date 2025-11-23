package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.categoria.CategoriaRequestDTO;
import com.multicompany.sales_system.dto.categoria.CategoriaResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoriaService {

    /**
     * Crear una nueva categoría
     */
    CategoriaResponseDTO createCategoria(CategoriaRequestDTO requestDTO);

    /**
     * Actualizar una categoría existente
     */
    CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO requestDTO);

    /**
     * Obtener una categoría por ID
     */
    CategoriaResponseDTO getCategoriaById(Long id);

    /**
     * Obtener todas las categorías
     */
    List<CategoriaResponseDTO> getAllCategorias();

    /**
     * Obtener todas las categorías con paginación
     */
    Page<CategoriaResponseDTO> getAllCategoriasWithPagination(Pageable pageable);

    /**
     * Obtener solo categorías activas
     */
    List<CategoriaResponseDTO> getCategoriasActivas();

    /**
     * Obtener categorías activas con paginación
     */
    Page<CategoriaResponseDTO> getCategoriasActivasWithPagination(Pageable pageable);

    /**
     * Buscar categorías por nombre
     */
    List<CategoriaResponseDTO> searchCategoriasByNombre(String nombre);

    /**
     * Buscar categorías por nombre con paginación
     */
    Page<CategoriaResponseDTO> searchCategoriasByNombreWithPagination(String nombre, Pageable pageable);

    /**
     * Eliminar una categoría (solo si no tiene productos asociados)
     */
    void deleteCategoria(Long id);

    /**
     * Activar/Desactivar una categoría
     */
    CategoriaResponseDTO toggleActivoCategoria(Long id);

    /**
     * Verificar si una categoría tiene productos asociados
     */
    boolean categoriaHasProductos(Long id);

    /**
     * Obtener cantidad de productos de una categoría
     */
    Long countProductosInCategoria(Long id);
}

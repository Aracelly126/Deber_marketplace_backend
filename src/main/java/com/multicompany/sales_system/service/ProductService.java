package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.enums.TipoProducto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ProductService {

    // Métodos CRUD básicos
    ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO);

    ProductResponseDTO getProductById(Long id);

    Page<ProductResponseDTO> getAllProducts(Pageable pageable);

    List<ProductResponseDTO> getProductsByVendedor(Long vendedorId);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO);

    void deleteProduct(Long id);

    /**
     * Elimina un producto de forma lógica (cambia estado a ELIMINADO)
     * 
     * @param id ID del producto
     */
    void deleteProductLogico(Long id);

    List<ProductResponseDTO> searchProducts(String searchTerm);

    // NUEVOS MÉTODOS CON FILTROS

    // Filtros por precio
    Page<ProductResponseDTO> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable);

    Page<ProductResponseDTO> getProductsByMinPrice(Double minPrice, Pageable pageable);

    Page<ProductResponseDTO> getProductsByMaxPrice(Double maxPrice, Pageable pageable);

    // Filtros por tipo
    Page<ProductResponseDTO> getProductsByTipo(TipoProducto tipo, Pageable pageable);

    // Filtros por ubicación
    Page<ProductResponseDTO> getProductsByUbicacion(String ubicacion, Pageable pageable);

    // Búsqueda con paginación
    Page<ProductResponseDTO> searchProductsWithPagination(String searchTerm, Pageable pageable);

    // Filtro combinado (el más potente)
    Page<ProductResponseDTO> getProductsWithFilters(Double minPrice,
            Double maxPrice,
            TipoProducto tipo,
            String searchTerm,
            String ubicacion,
            Boolean disponibilidad,
            Pageable pageable);

    // --- Servicios ---
    /**
     * Obtener páginas de servicios (entidades Servicio con horario)
     */
    Page<ProductResponseDTO> getAllServices(Pageable pageable);

    /**
     * Obtener una vista combinada (productos y servicios) paginada.
     */
    Page<ProductResponseDTO> getAllProductsAndServices(Pageable pageable);
}

package com.multicompany.sales_system.service;

import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.ProductoInteresado;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.ProductoInteresadoRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la funcionalidad "me interesa" entre usuarios y
 * productos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoInteresadoService {

    private final ProductoInteresadoRepository productoInteresadoRepository;
    private final ProductRepository productRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Agregar un producto a la lista de "me interesa" del usuario
     */
    public Long getTotalInteresesByVendedor(Long vendedorId) {
        return productoInteresadoRepository.countInteresesByVendedor(vendedorId);
    }
    @Transactional
    public boolean agregarInteres(Long usuarioId, Long productoId) {
        // Verificar si ya existe la relación
        if (productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(usuarioId, productoId)) {
            log.warn("El usuario {} ya tiene marcado como 'me interesa' el producto {}", usuarioId, productoId);
            return false;
        }

        // Buscar usuario y producto
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + usuarioId));

        Producto producto = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        // Verificar que el producto esté activo
        if (!producto.getEstado().isVisible()) {
            throw new RuntimeException("No se puede marcar como 'me interesa' un producto que no está visible");
        }

        // Crear la relación
        ProductoInteresado productoInteresado = new ProductoInteresado(usuario, producto);
        productoInteresadoRepository.save(productoInteresado);

        log.info("Usuario {} agregó producto {} a 'me interesa'", usuarioId, productoId);
        return true;
    }

    /**
     * Quitar un producto de la lista de "me interesa" del usuario
     */
    @Transactional
    public boolean quitarInteres(Long usuarioId, Long productoId) {
        if (!productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(usuarioId, productoId)) {
            log.warn("El usuario {} no tiene marcado como 'me interesa' el producto {}", usuarioId, productoId);
            return false;
        }

        productoInteresadoRepository.deleteByUsuarioIdUsuarioAndProductoIdProducto(usuarioId, productoId);
        log.info("Usuario {} quitó producto {} de 'me interesa'", usuarioId, productoId);
        return true;
    }

    /**
     * Verificar si un usuario tiene marcado como "me interesa" un producto
     */
    @Transactional(readOnly = true)
    public boolean tieneInteres(Long usuarioId, Long productoId) {
        return productoInteresadoRepository.existsByUsuarioIdUsuarioAndProductoIdProducto(usuarioId, productoId);
    }

    /**
     * Obtener productos marcados como "me interesa" por un usuario (con paginación)
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> obtenerProductosInteres(Long usuarioId, Pageable pageable) {
        Page<ProductoInteresado> productosInteres = productoInteresadoRepository
                .findByUsuarioIdUsuarioOrderByFechaInteresDesc(usuarioId, pageable);

        return productosInteres.map(pi -> convertirAProductResponseDTO(pi.getProducto()));
    }

    /**
     * Obtener productos marcados como "me interesa" por un usuario filtrados por
     * tipo
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> obtenerProductosInteresPorTipo(Long usuarioId, TipoProducto tipo,
            Pageable pageable) {
        Page<ProductoInteresado> productosInteres = productoInteresadoRepository
                .findByUsuarioAndTipoProducto(usuarioId, tipo, pageable);

        return productosInteres.map(pi -> convertirAProductResponseDTO(pi.getProducto()));
    }

    /**
     * Obtener productos marcados como "me interesa" por un usuario filtrados por
     * rango de precio
     */
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> obtenerProductosInteresPorPrecio(Long usuarioId, Double minPrice, Double maxPrice,
            Pageable pageable) {
        Page<ProductoInteresado> productosInteres = productoInteresadoRepository
                .findByUsuarioAndRangoPrecio(usuarioId, minPrice, maxPrice, pageable);

        return productosInteres.map(pi -> convertirAProductResponseDTO(pi.getProducto()));
    }

    /**
     * Contar cuántos usuarios han marcado como "me interesa" un producto
     */
    @Transactional(readOnly = true)
    public Long contarInteresesPorProducto(Long productoId) {
        return productoInteresadoRepository.countInteresesByProducto(productoId);
    }

    /**
     * Obtener productos más populares (con más "me interesa")
     */
    @Transactional(readOnly = true)
    public Page<Object[]> obtenerProductosMasPopulares(Pageable pageable) {
        return productoInteresadoRepository.findProductosMasPopulares(pageable);
    }

    /**
     * Obtener usuarios que han marcado como "me interesa" un producto específico
     */
    @Transactional(readOnly = true)
    public List<Usuario> obtenerUsuariosInteresados(Long productoId) {
        List<ProductoInteresado> intereses = productoInteresadoRepository
                .findByProductoIdProductoOrderByFechaInteresDesc(productoId);

        return intereses.stream()
                .map(ProductoInteresado::getUsuario)
                .collect(Collectors.toList());
    }

    /**
     * Limpiar productos de interés expirados o eliminados
     */
    @Transactional
    public int limpiarProductosObsoletos() {
        // Esta funcionalidad requeriría una consulta personalizada
        // Por ahora, devuelvo 0 como placeholder
        log.info("Iniciando limpieza de productos obsoletos en 'me interesa'");
        return 0;
    }

    /**
     * Convertir Producto a ProductResponseDTO
     */
    private ProductResponseDTO convertirAProductResponseDTO(Producto producto) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setIdProducto(producto.getIdProducto());
        dto.setCodigo(producto.getCodigo());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setUbicacion(producto.getUbicacion());
        dto.setDisponibilidad(producto.getDisponibilidad());
        dto.setTipo(producto.getTipo().name());
        dto.setEstado(producto.getEstado().name());
        dto.setFechaPublicacion(producto.getFechaPublicacion());

        if (producto.getVendedor() != null) {
            dto.setIdVendedor(producto.getVendedor().getIdUsuario());
            dto.setNombreVendedor(producto.getVendedor().getNombre());
        }

        // No incluimos fotos por performance, se pueden cargar por separado si es
        // necesario
        return dto;
    }
}

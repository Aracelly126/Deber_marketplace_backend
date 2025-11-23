package com.multicompany.sales_system.service;

import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para gestionar restricciones de publicación de productos
 * Incluye detección de contenido prohibido, caducidad y gestión de estados
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoRestriccionService {

    private final ProductRepository productRepository;
    private final DetectorService detectorService;
    private final IncidenciaService incidenciaService;

    /**
     * Verificar si un producto contiene contenido prohibido y marcarlo en consecuencia
     */
    @Transactional
    public boolean verificarYMarcarProhibidos(Producto producto) {
        if (producto == null) {
            return false;
        }

        String textoCompleto = construirTextoCompleto(producto);
        boolean contieneProhibidas = detectorService.containsProhibited(textoCompleto);

        if (contieneProhibidas) {
            log.warn("Producto {} contiene contenido prohibido: {}", 
                     producto.getIdProducto(), 
                     detectorService.findMatchedWords(textoCompleto));
            
            producto.setEstado(EstadoProducto.PROHIBIDO);
            productRepository.save(producto);
            return true;
        }

        return false;
    }

    /**
     * Verificar si un producto ha expirado y marcarlo como expirado
     */
    @Transactional
    public boolean verificarYMarcarExpirados(Producto producto) {
        if (producto == null || producto.getFechaExpiracion() == null) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isAfter(producto.getFechaExpiracion()) && 
            producto.getEstado() == EstadoProducto.ACTIVO) {
            
            log.info("Producto {} ha expirado. Fecha de expiración: {}", 
                     producto.getIdProducto(), 
                     producto.getFechaExpiracion());
            
            producto.setEstado(EstadoProducto.ELIMINADO);
            productRepository.save(producto);
            return true;
        }

        return false;
    }

    /**
     * Procesar productos expirados en lote
     */
    @Transactional
    public int procesarProductosExpirados() {
        LocalDateTime ahora = LocalDateTime.now();
        List<Producto> productosExpirados = productRepository.findProductosExpirados(ahora);
        
        int procesados = 0;
        for (Producto producto : productosExpirados) {
            if (verificarYMarcarExpirados(producto)) {
                procesados++;
            }
        }
        
        log.info("Procesados {} productos expirados", procesados);
        return procesados;
    }

    /**
     * Crear incidencia automática para producto prohibido
     */
    @Transactional
    public void crearIncidenciaAutomatica(Producto producto, String motivo, String descripcion) {
        try {
            // Crear incidencia usando un usuario del sistema (administrador)
            // En un caso real, podrías tener un usuario "SISTEMA" para estas operaciones
            Long usuarioSistema = 1L; // Deberías tener un usuario administrador específico
            
            incidenciaService.crearPorDeteccion(
                producto.getIdProducto(),
                usuarioSistema,
                motivo,
                descripcion
            );
            
            log.info("Incidencia automática creada para producto {}: {}", 
                     producto.getIdProducto(), motivo);
                     
        } catch (Exception e) {
            log.error("Error al crear incidencia automática para producto {}: {}", 
                      producto.getIdProducto(), e.getMessage());
        }
    }

    /**
     * Verificar si un estado puede transicionar a otro
     */
    public boolean puedeTransicionarEstado(EstadoProducto estadoActual, EstadoProducto nuevoEstado) {
        if (estadoActual == null || nuevoEstado == null) {
            return false;
        }

        switch (estadoActual) {
            case ACTIVO:
                return Arrays.asList(EstadoProducto.OCULTO, EstadoProducto.PROHIBIDO, EstadoProducto.ELIMINADO)
                             .contains(nuevoEstado);
            case OCULTO:
                return Arrays.asList(EstadoProducto.ACTIVO, EstadoProducto.PROHIBIDO, EstadoProducto.ELIMINADO)
                             .contains(nuevoEstado);
            case PROHIBIDO:
                return Arrays.asList(EstadoProducto.APELADO, EstadoProducto.ELIMINADO)
                             .contains(nuevoEstado);
            case APELADO:
                return Arrays.asList(EstadoProducto.ACTIVO, EstadoProducto.PROHIBIDO, EstadoProducto.ELIMINADO)
                             .contains(nuevoEstado);
            case ELIMINADO:
                return false; // Los productos eliminados no pueden cambiar de estado
            default:
                return false;
        }
    }

    /**
     * Cambiar el estado de un producto con validaciones
     */
    @Transactional
    public boolean cambiarEstadoProducto(Long productoId, EstadoProducto nuevoEstado, String motivo) {
        Producto producto = productRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        EstadoProducto estadoActual = producto.getEstado();

        if (!puedeTransicionarEstado(estadoActual, nuevoEstado)) {
            log.warn("Transición de estado no permitida de {} a {} para producto {}", 
                     estadoActual, nuevoEstado, productoId);
            return false;
        }

        producto.setEstado(nuevoEstado);
        productRepository.save(producto);

        log.info("Estado del producto {} cambiado de {} a {}. Motivo: {}", 
                 productoId, estadoActual, nuevoEstado, motivo);

        return true;
    }

    /**
     * Construir texto completo del producto para análisis
     */
    private String construirTextoCompleto(Producto producto) {
        StringBuilder sb = new StringBuilder();
        
        if (producto.getNombre() != null) {
            sb.append(producto.getNombre()).append(" ");
        }
        if (producto.getDescripcion() != null) {
            sb.append(producto.getDescripcion()).append(" ");
        }
        if (producto.getUbicacion() != null) {
            sb.append(producto.getUbicacion()).append(" ");
        }
        
        return sb.toString().trim();
    }

    /**
     * Obtener productos que requieren revisión manual
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosParaRevision() {
        return productRepository.findByEstado(EstadoProducto.APELADO);
    }

    /**
     * Obtener productos prohibidos
     */
    @Transactional(readOnly = true)
    public List<Producto> obtenerProductosProhibidos() {
        return productRepository.findByEstado(EstadoProducto.PROHIBIDO);
    }
}

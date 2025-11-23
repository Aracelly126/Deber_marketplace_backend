package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.gestion.ApelacionRequestDTO;
import com.multicompany.sales_system.dto.gestion.CambioEstadoRequestDTO;
import com.multicompany.sales_system.dto.gestion.GestionResponseDTO;
import com.multicompany.sales_system.dto.gestion.InteresRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.service.ProductoInteresadoService;
import com.multicompany.sales_system.service.ProductoRestriccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controlador para funcionalidades avanzadas de gestión de productos:
 * - Funcionalidad "me interesa"
 * - Apelaciones de vendedores
 * - Gestión de estados y restricciones
 */
@RestController
@RequestMapping("/api/productos/gestion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ProductoGestionController {

    private final ProductoInteresadoService productoInteresadoService;
    private final ProductoRestriccionService productoRestriccionService;

    // ================================
    // FUNCIONALIDAD "ME INTERESA"
    // ================================
    @GetMapping("/interes/total-por-vendedor/{vendedorId}")
    public ResponseEntity<Map<String, Object>> getTotalInteresesByVendedor(@PathVariable Long vendedorId) {
        Long total = productoInteresadoService.getTotalInteresesByVendedor(vendedorId);
        return ResponseEntity.ok(Map.of("vendedorId", vendedorId, "totalIntereses", total));
    }
    /**
     * Agregar producto a lista de "me interesa"
     */
    @PostMapping("/interes/agregar")
    public ResponseEntity<GestionResponseDTO> agregarInteres(@Valid @RequestBody InteresRequestDTO request) {
        try {
            boolean agregado = productoInteresadoService.agregarInteres(request.getUsuarioId(),
                    request.getProductoId());

            if (agregado) {
                return ResponseEntity
                        .ok(new GestionResponseDTO(true, "Producto agregado a 'me interesa' exitosamente"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "El producto ya está en la lista de 'me interesa'"));
            }
        } catch (Exception e) {
            log.error("Error al agregar interés: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Quitar producto de lista de "me interesa"
     */
    @DeleteMapping("/interes/quitar")
    public ResponseEntity<GestionResponseDTO> quitarInteres(@Valid @RequestBody InteresRequestDTO request) {
        try {
            boolean quitado = productoInteresadoService.quitarInteres(request.getUsuarioId(), request.getProductoId());

            if (quitado) {
                return ResponseEntity
                        .ok(new GestionResponseDTO(true, "Producto quitado de 'me interesa' exitosamente"));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "El producto no estaba en la lista de 'me interesa'"));
            }
        } catch (Exception e) {
            log.error("Error al quitar interés: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Verificar si usuario tiene interés en producto
     */
    @GetMapping("/interes/verificar")
    public ResponseEntity<Map<String, Object>> verificarInteres(
            @RequestParam Long usuarioId,
            @RequestParam Long productoId) {

        boolean tieneInteres = productoInteresadoService.tieneInteres(usuarioId, productoId);
        return ResponseEntity.ok(Map.of(
                "usuarioId", usuarioId,
                "productoId", productoId,
                "tieneInteres", tieneInteres));
    }

    /**
     * Obtener productos de interés de un usuario
     */
    @GetMapping("/interes/usuario/{usuarioId}")
    public ResponseEntity<Page<ProductResponseDTO>> obtenerProductosInteres(
            @PathVariable Long usuarioId,
            Pageable pageable) {

        Page<ProductResponseDTO> productos = productoInteresadoService.obtenerProductosInteres(usuarioId, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * Obtener productos de interés filtrados por tipo
     */
    @GetMapping("/interes/usuario/{usuarioId}/tipo/{tipo}")
    public ResponseEntity<Page<ProductResponseDTO>> obtenerProductosInteresPorTipo(
            @PathVariable Long usuarioId,
            @PathVariable String tipo,
            Pageable pageable) {

        try {
            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
            Page<ProductResponseDTO> productos = productoInteresadoService
                    .obtenerProductosInteresPorTipo(usuarioId, tipoEnum, pageable);
            return ResponseEntity.ok(productos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * Obtener productos de interés filtrados por rango de precio
     */
    @GetMapping("/interes/usuario/{usuarioId}/precio")
    public ResponseEntity<Page<ProductResponseDTO>> obtenerProductosInteresPorPrecio(
            @PathVariable Long usuarioId,
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            Pageable pageable) {

        Page<ProductResponseDTO> productos = productoInteresadoService
                .obtenerProductosInteresPorPrecio(usuarioId, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(productos);
    }

    /**
     * Contar intereses por producto
     */
    @GetMapping("/interes/producto/{productoId}/count")
    public ResponseEntity<Map<String, Object>> contarInteresesPorProducto(@PathVariable Long productoId) {
        Long count = productoInteresadoService.contarInteresesPorProducto(productoId);
        return ResponseEntity.ok(Map.of(
                "productoId", productoId,
                "totalIntereses", count));
    }

    /**
     * Obtener productos más populares
     */
    @GetMapping("/interes/populares")
    public ResponseEntity<Page<Object[]>> obtenerProductosMasPopulares(Pageable pageable) {
        Page<Object[]> productos = productoInteresadoService.obtenerProductosMasPopulares(pageable);
        return ResponseEntity.ok(productos);
    }

    // ================================
    // GESTIÓN DE ESTADOS Y RESTRICCIONES
    // ================================

    /**
     * Cambiar estado de producto (para administradores/moderadores)
     */
    @PutMapping("/estado/cambiar")
    public ResponseEntity<GestionResponseDTO> cambiarEstadoProducto(
            @Valid @RequestBody CambioEstadoRequestDTO request) {
        try {
            EstadoProducto nuevoEstado = EstadoProducto.valueOf(request.getNuevoEstado().toUpperCase());
            boolean cambiado = productoRestriccionService.cambiarEstadoProducto(
                    request.getProductoId(),
                    nuevoEstado,
                    request.getMotivo());

            if (cambiado) {
                return ResponseEntity.ok(new GestionResponseDTO(
                        true,
                        "Estado del producto cambiado exitosamente",
                        request.getProductoId(),
                        null, // No tenemos el estado anterior aquí
                        nuevoEstado.name()));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "No se pudo cambiar el estado del producto"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Estado inválido: " + request.getNuevoEstado()));
        } catch (Exception e) {
            log.error("Error al cambiar estado de producto: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Procesar productos expirados (tarea de mantenimiento)
     */
    @PostMapping("/restricciones/procesar-expirados")
    public ResponseEntity<Map<String, Object>> procesarProductosExpirados() {
        try {
            int procesados = productoRestriccionService.procesarProductosExpirados();
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Productos expirados procesados exitosamente",
                    "productosProcessados", procesados));
        } catch (Exception e) {
            log.error("Error al procesar productos expirados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al procesar productos expirados: " + e.getMessage()));
        }
    }

    // ================================
    // FUNCIONALIDAD DE APELACIONES
    // ================================

    /**
     * Apelar un producto prohibido (para vendedores)
     */
    @PostMapping("/apelacion/crear")
    public ResponseEntity<GestionResponseDTO> crearApelacion(@Valid @RequestBody ApelacionRequestDTO request) {
        try {
            // Verificar que el producto existe y está prohibido
            boolean cambiado = productoRestriccionService.cambiarEstadoProducto(
                    request.getProductoId(),
                    EstadoProducto.APELADO,
                    "Apelación del vendedor: " + request.getJustificacion());

            if (cambiado) {
                log.info("Apelación creada para producto {} por vendedor {}",
                        request.getProductoId(), request.getVendedorId());

                return ResponseEntity.ok(new GestionResponseDTO(
                        true,
                        "Apelación creada exitosamente. Su producto será revisado.",
                        request.getProductoId(),
                        EstadoProducto.PROHIBIDO.name(),
                        EstadoProducto.APELADO.name()));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "No se pudo crear la apelación"));
            }
        } catch (Exception e) {
            log.error("Error al crear apelación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Aprobar apelación (para moderadores/administradores)
     */
    @PostMapping("/apelacion/aprobar/{productoId}")
    public ResponseEntity<GestionResponseDTO> aprobarApelacion(
            @PathVariable Long productoId,
            @RequestParam(required = false, defaultValue = "Apelación aprobada por moderador") String motivo) {

        try {
            boolean cambiado = productoRestriccionService.cambiarEstadoProducto(
                    productoId,
                    EstadoProducto.ACTIVO,
                    motivo);

            if (cambiado) {
                return ResponseEntity.ok(new GestionResponseDTO(
                        true,
                        "Apelación aprobada. Producto reactivado.",
                        productoId,
                        EstadoProducto.APELADO.name(),
                        EstadoProducto.ACTIVO.name()));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "No se pudo aprobar la apelación"));
            }
        } catch (Exception e) {
            log.error("Error al aprobar apelación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    /**
     * Rechazar apelación (para moderadores/administradores)
     */
    @PostMapping("/apelacion/rechazar/{productoId}")
    public ResponseEntity<GestionResponseDTO> rechazarApelacion(
            @PathVariable Long productoId,
            @RequestParam(required = false, defaultValue = "Apelación rechazada por moderador") String motivo) {

        try {
            boolean cambiado = productoRestriccionService.cambiarEstadoProducto(
                    productoId,
                    EstadoProducto.PROHIBIDO,
                    motivo);

            if (cambiado) {
                return ResponseEntity.ok(new GestionResponseDTO(
                        true,
                        "Apelación rechazada. Producto sigue prohibido.",
                        productoId,
                        EstadoProducto.APELADO.name(),
                        EstadoProducto.PROHIBIDO.name()));
            } else {
                return ResponseEntity.badRequest()
                        .body(new GestionResponseDTO(false, "No se pudo rechazar la apelación"));
            }
        } catch (Exception e) {
            log.error("Error al rechazar apelación: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new GestionResponseDTO(false, "Error: " + e.getMessage()));
        }
    }

    // ================================
    // MANEJO DE ERRORES
    // ================================

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GestionResponseDTO> handleRuntimeException(RuntimeException ex) {
        log.error("Error en ProductoGestionController: {}", ex.getMessage());
        return ResponseEntity.badRequest()
                .body(new GestionResponseDTO(false, "Error: " + ex.getMessage()));
    }
}
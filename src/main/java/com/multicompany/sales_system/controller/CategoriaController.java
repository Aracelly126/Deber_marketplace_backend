package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.categoria.CategoriaRequestDTO;
import com.multicompany.sales_system.dto.categoria.CategoriaResponseDTO;
import com.multicompany.sales_system.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaService categoriaService;

    /**
     * Crear una nueva categoría
     * POST /api/categorias
     */
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> createCategoria(@Valid @RequestBody CategoriaRequestDTO requestDTO) {
        try {
            CategoriaResponseDTO responseDTO = categoriaService.createCategoria(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al crear la categoría: " + e.getMessage());
        }
    }

    /**
     * Actualizar una categoría existente
     * PUT /api/categorias/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> updateCategoria(
            @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO requestDTO) {
        try {
            CategoriaResponseDTO responseDTO = categoriaService.updateCategoria(id, requestDTO);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            throw new RuntimeException("Error al actualizar la categoría: " + e.getMessage());
        }
    }

    /**
     * Obtener una categoría por ID
     * GET /api/categorias/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getCategoriaById(@PathVariable Long id) {
        CategoriaResponseDTO responseDTO = categoriaService.getCategoriaById(id);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Obtener todas las categorías (sin paginación)
     * GET /api/categorias
     */
    @GetMapping
    public ResponseEntity<List<CategoriaResponseDTO>> getAllCategorias() {
        List<CategoriaResponseDTO> categorias = categoriaService.getAllCategorias();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener todas las categorías con paginación
     * GET /api/categorias/paginated?page=0&size=10&sort=nombre,asc
     */
    @GetMapping("/paginated")
    public ResponseEntity<Page<CategoriaResponseDTO>> getAllCategoriasWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nombre") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CategoriaResponseDTO> categorias = categoriaService.getAllCategoriasWithPagination(pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener solo categorías activas
     * GET /api/categorias/activas
     */
    @GetMapping("/activas")
    public ResponseEntity<List<CategoriaResponseDTO>> getCategoriasActivas() {
        List<CategoriaResponseDTO> categorias = categoriaService.getCategoriasActivas();
        return ResponseEntity.ok(categorias);
    }

    /**
     * Obtener categorías activas con paginación
     * GET /api/categorias/activas/paginated?page=0&size=10
     */
    @GetMapping("/activas/paginated")
    public ResponseEntity<Page<CategoriaResponseDTO>> getCategoriasActivasWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<CategoriaResponseDTO> categorias = categoriaService.getCategoriasActivasWithPagination(pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Buscar categorías por nombre
     * GET /api/categorias/buscar?nombre=electro
     */
    @GetMapping("/buscar")
    public ResponseEntity<List<CategoriaResponseDTO>> searchCategoriasByNombre(
            @RequestParam String nombre) {
        List<CategoriaResponseDTO> categorias = categoriaService.searchCategoriasByNombre(nombre);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Buscar categorías por nombre con paginación
     * GET /api/categorias/buscar/paginated?nombre=electro&page=0&size=10
     */
    @GetMapping("/buscar/paginated")
    public ResponseEntity<Page<CategoriaResponseDTO>> searchCategoriasByNombreWithPagination(
            @RequestParam String nombre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("nombre").ascending());
        Page<CategoriaResponseDTO> categorias = categoriaService.searchCategoriasByNombreWithPagination(nombre,
                pageable);
        return ResponseEntity.ok(categorias);
    }

    /**
     * Eliminar una categoría (solo si no tiene productos asociados)
     * DELETE /api/categorias/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteCategoria(@PathVariable Long id) {
        try {
            categoriaService.deleteCategoria(id);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Categoría eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Activar/Desactivar una categoría
     * PATCH /api/categorias/{id}/toggle-activo
     */
    @PatchMapping("/{id}/toggle-activo")
    public ResponseEntity<CategoriaResponseDTO> toggleActivoCategoria(@PathVariable Long id) {
        CategoriaResponseDTO responseDTO = categoriaService.toggleActivoCategoria(id);
        return ResponseEntity.ok(responseDTO);
    }

    /**
     * Verificar si una categoría tiene productos asociados
     * GET /api/categorias/{id}/tiene-productos
     */
    @GetMapping("/{id}/tiene-productos")
    public ResponseEntity<Map<String, Object>> categoriaHasProductos(@PathVariable Long id) {
        boolean tieneProductos = categoriaService.categoriaHasProductos(id);
        Long cantidadProductos = categoriaService.countProductosInCategoria(id);

        Map<String, Object> response = new HashMap<>();
        response.put("tieneProductos", tieneProductos);
        response.put("cantidadProductos", cantidadProductos);

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener cantidad de productos de una categoría
     * GET /api/categorias/{id}/contar-productos
     */
    @GetMapping("/{id}/contar-productos")
    public ResponseEntity<Map<String, Long>> countProductosInCategoria(@PathVariable Long id) {
        Long cantidadProductos = categoriaService.countProductosInCategoria(id);

        Map<String, Long> response = new HashMap<>();
        response.put("cantidadProductos", cantidadProductos);

        return ResponseEntity.ok(response);
    }
}

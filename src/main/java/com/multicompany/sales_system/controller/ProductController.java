package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.service.ProductService;
import com.multicompany.sales_system.service.PhotoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductService productService;
    private final PhotoService photoService;
    private final ObjectMapper objectMapper;

    /**
     * Crear producto CON fotos (mínimo 1, máximo 5)
     * POST /api/products/with-photos
     */
    @PostMapping(value = "/with-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProductWithPhotos(
            @RequestPart("productData") String productDataJson,
            @RequestPart("files") List<MultipartFile> files) {
        try {
            // Validar cantidad de fotos
            if (files == null || files.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Debe subir al menos 1 foto");
                return ResponseEntity.badRequest().body(error);
            }

            if (files.size() > 5) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No puede subir más de 5 fotos");
                return ResponseEntity.badRequest().body(error);
            }

            // Convertir JSON a DTO
            ProductRequestDTO productRequestDTO = objectMapper.readValue(productDataJson, ProductRequestDTO.class);

            // Crear el producto
            ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);

            // Subir las fotos
            photoService.uploadMultiplePhotos(createdProduct.getIdProducto(), files);

            // Obtener el producto actualizado con las fotos
            ProductResponseDTO productWithPhotos = productService.getProductById(createdProduct.getIdProducto());

            return ResponseEntity.status(HttpStatus.CREATED).body(productWithPhotos);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear el producto: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO createdProduct = productService.createProduct(productRequestDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * Obtener solo servicios (aquellos que tienen horario)
     */
    @GetMapping("/services")
    public ResponseEntity<Page<ProductResponseDTO>> getAllServices(Pageable pageable) {
        Page<ProductResponseDTO> services = productService.getAllServices(pageable);
        return ResponseEntity.ok(services);
    }

    /**
     * Obtener productos y servicios combinados (página basada en productos, con horario rellenado cuando corresponda)
     */
    @GetMapping("/all")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProductsAndServices(Pageable pageable) {
        Page<ProductResponseDTO> combined = productService.getAllProductsAndServices(pageable);
        return ResponseEntity.ok(combined);
    }

    @GetMapping("/vendedor/{vendedorId}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByVendedor(@PathVariable Long vendedorId) {
        List<ProductResponseDTO> products = productService.getProductsByVendedor(vendedorId);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequestDTO productRequestDTO) {
        ProductResponseDTO updatedProduct = productService.updateProduct(id, productRequestDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Eliminar producto lógicamente (cambiar estado a ELIMINADO)
     * PATCH /api/products/{id}/eliminar-logico
     */
    @PatchMapping("/{id}/eliminar-logico")
    public ResponseEntity<?> deleteProductLogico(@PathVariable Long id) {
        try {
            productService.deleteProductLogico(id);
            Map<String, String> resp = new HashMap<>();
            resp.put("mensaje", "Producto marcado como ELIMINADO");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchProducts(@RequestParam String searchTerm) {
        List<ProductResponseDTO> products = productService.searchProducts(searchTerm);
        return ResponseEntity.ok(products);
    }

    // ENDPOINTS DE FILTRADO AVANZADO

    // Endpoint para filtrar productos con múltiples criterios
    @GetMapping("/filter")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsWithFilters(
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String ubicacion,
            @RequestParam(required = false) Boolean disponibilidad,
            Pageable pageable) {

        // Convertir string a enum si es necesario
        TipoProducto tipoEnum = null;
        if (tipo != null && !tipo.isEmpty()) {
            try {
                tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(null);
            }
        }

        Page<ProductResponseDTO> products = productService.getProductsWithFilters(
                minPrice, maxPrice, tipoEnum, searchTerm, ubicacion, disponibilidad, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint específico para filtrar por rango de precio
    @GetMapping("/filter/price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByPriceRange(minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint específico para filtrar por tipo (PRODUCTO o SERVICIO)
    @GetMapping("/filter/type")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByType(
            @RequestParam String tipo,
            Pageable pageable) {
        try {
            TipoProducto tipoEnum = TipoProducto.valueOf(tipo.toUpperCase());
            Page<ProductResponseDTO> products = productService.getProductsByTipo(tipoEnum, pageable);
            return ResponseEntity.ok(products);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Endpoint específico para filtrar por ubicación
    @GetMapping("/filter/location")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByLocation(
            @RequestParam String ubicacion,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByUbicacion(ubicacion, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoint para búsqueda con paginación
    @GetMapping("/search/paginated")
    public ResponseEntity<Page<ProductResponseDTO>> searchProductsWithPagination(
            @RequestParam String searchTerm,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.searchProductsWithPagination(searchTerm, pageable);
        return ResponseEntity.ok(products);
    }

    // Endpoints adicionales para filtros de precio individuales
    @GetMapping("/filter/min-price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByMinPrice(
            @RequestParam Double minPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByMinPrice(minPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/filter/max-price")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByMaxPrice(
            @RequestParam Double maxPrice,
            Pageable pageable) {
        Page<ProductResponseDTO> products = productService.getProductsByMaxPrice(maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}
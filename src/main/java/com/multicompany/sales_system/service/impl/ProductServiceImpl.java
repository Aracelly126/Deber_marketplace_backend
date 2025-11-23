package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.photo.PhotoResponseDTO;
import com.multicompany.sales_system.dto.product.ProductRequestDTO;
import com.multicompany.sales_system.dto.product.ProductResponseDTO;
import com.multicompany.sales_system.model.*;
import com.multicompany.sales_system.model.enums.EstadoProducto;
import com.multicompany.sales_system.model.enums.TipoProducto;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.ServicioRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.repository.CategoriaRepository;
import com.multicompany.sales_system.service.ConfiguracionService;
import com.multicompany.sales_system.service.ProductService;
import com.multicompany.sales_system.service.DetectorService;
import com.multicompany.sales_system.service.IncidenciaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ServicioRepository servicioRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetectorService detectorService;
    private final IncidenciaService incidenciaService;
    private final ConfiguracionService configuracionService;

    @Override
    public ProductResponseDTO createProduct(ProductRequestDTO productRequestDTO) {
        // Buscar vendedor
        Usuario vendedor = usuarioRepository.findById(productRequestDTO.getIdVendedor())
                .orElseThrow(() -> new RuntimeException(
                        "Vendedor no encontrado con ID: " + productRequestDTO.getIdVendedor()));

        // Buscar categoría
        Categoria categoria = categoriaRepository.findById(productRequestDTO.getIdCategoria())
                .orElseThrow(() -> new RuntimeException(
                        "Categoría no encontrada con ID: " + productRequestDTO.getIdCategoria()));

        // Validar categoría activa
        if (!categoria.getActivo()) {
            throw new RuntimeException("La categoría '" + categoria.getNombre()
                    + "' está desactivada. No se pueden crear productos en categorías inactivas.");
        }

    // Crear producto
        Producto producto = new Producto();
        // Establecer tipo primero para generar el código con el prefijo adecuado
        producto.setTipo(TipoProducto.valueOf(productRequestDTO.getTipo().toUpperCase()));
        // Generar código automáticamente con prefijo según tipo (prod- o serv-) y un sufijo corto UUID
        String shortUuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String prefix = producto.getTipo() == TipoProducto.SERVICIO ? "serv-" : "prod-";
        producto.setCodigo(prefix + shortUuid);
        producto.setNombre(productRequestDTO.getNombre());
        producto.setDescripcion(productRequestDTO.getDescripcion());
        producto.setPrecio(productRequestDTO.getPrecio());
        producto.setUbicacion(productRequestDTO.getUbicacion());
        producto.setDisponibilidad(productRequestDTO.getDisponibilidad());
        producto.setFechaPublicacion(LocalDateTime.now());

        // Calcular y establecer la fecha de expiración basándose en la configuración
        int diasExpiracion = configuracionService.getDiasExpiracion();
        producto.setFechaExpiracion(LocalDateTime.now().plusDays(diasExpiracion));

        producto.setVendedor(vendedor);
        producto.setCategoria(categoria);

        // Guardar y validar
        Producto savedProduct = productRepository.save(producto);
        boolean contieneProhibidas = validarYProcesarContenido(savedProduct, productRequestDTO);

        if (contieneProhibidas) {
            savedProduct.setEstado(EstadoProducto.OCULTO);
            savedProduct.setDisponibilidad(false);
        } else {
            savedProduct.setEstado(EstadoProducto.ACTIVO);
        }

        savedProduct = productRepository.save(savedProduct);

        if (contieneProhibidas) {
            crearIncidenciaAutomatica(savedProduct);
        }
        // Si el tipo es SERVICIO, crear la entidad Servicio con el horario provisto
        if (savedProduct.getTipo() == TipoProducto.SERVICIO) {
            Servicio servicio = new Servicio();
            servicio.setProducto(savedProduct);
            servicio.setHorario(productRequestDTO.getHorario());
            servicioRepository.save(servicio);
        }

        return convertToResponseDTO(savedProduct);
    }

    @Override
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO productRequestDTO) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));

        if (productRequestDTO.getIdCategoria() != null) {
            Categoria categoria = categoriaRepository.findById(productRequestDTO.getIdCategoria())
                    .orElseThrow(() -> new RuntimeException(
                            "Categoría no encontrada con ID: " + productRequestDTO.getIdCategoria()));

            if (!categoria.getActivo()) {
                throw new RuntimeException("La categoría '" + categoria.getNombre()
                        + "' está desactivada. No se pueden asignar productos a categorías inactivas.");
            }

            producto.setCategoria(categoria);
        }

    // Nota: el código es inmutable vía update; no se permite cambiar el `codigo` desde el frontend.
    // Se ignora cualquier valor enviado en productRequestDTO.codigo.
        producto.setNombre(productRequestDTO.getNombre());
        producto.setDescripcion(productRequestDTO.getDescripcion());
        producto.setPrecio(productRequestDTO.getPrecio());
        producto.setUbicacion(productRequestDTO.getUbicacion());
        producto.setDisponibilidad(productRequestDTO.getDisponibilidad());
        producto.setTipo(TipoProducto.valueOf(productRequestDTO.getTipo().toUpperCase()));

        Producto updatedProduct = productRepository.save(producto);

        EstadoProducto estadoAnterior = updatedProduct.getEstado();
        boolean contieneProhibidas = validarYProcesarContenido(updatedProduct, productRequestDTO);

        if (contieneProhibidas) {
            updatedProduct.setEstado(EstadoProducto.OCULTO);
            updatedProduct.setDisponibilidad(false);
        } else if (estadoAnterior == EstadoProducto.PROHIBIDO) {
            updatedProduct.setEstado(EstadoProducto.ACTIVO);
            updatedProduct.setDisponibilidad(true);
        }

        updatedProduct = productRepository.save(updatedProduct);

        if (contieneProhibidas) {
            crearIncidenciaAutomatica(updatedProduct);
        }

        // Manejo de Servicio: crear/actualizar/eliminar según tipo y DTO
        if (updatedProduct.getTipo() == TipoProducto.SERVICIO) {
            final String horarioDto = productRequestDTO.getHorario();
            final Producto productoForService = updatedProduct;
            servicioRepository.findById(updatedProduct.getIdProducto()).ifPresentOrElse(s -> {
                s.setHorario(horarioDto);
                servicioRepository.save(s);
            }, () -> {
                Servicio nuevo = new Servicio();
                nuevo.setProducto(productoForService);
                nuevo.setHorario(horarioDto);
                servicioRepository.save(nuevo);
            });
        } else {
            // Si cambió a tipo que no es servicio, eliminar entrada de Servicio si existía
            servicioRepository.findById(updatedProduct.getIdProducto()).ifPresent(servicioRepository::delete);
        }

        return convertToResponseDTO(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Producto producto = productRepository.findByIdWithFotos(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        // Si existe un servicio asociado, incluir su horario
        return servicioRepository.findById(id)
                .map(servicio -> {
                    ProductResponseDTO dto = convertToResponseDTO(producto);
                    dto.setHorario(servicio.getHorario());
                    return dto;
                })
                .orElseGet(() -> convertToResponseDTO(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllServices(Pageable pageable) {
        return servicioRepository.findAll(pageable)
                .map(servicio -> {
                    ProductResponseDTO dto = convertToResponseDTO(servicio.getProducto());
                    dto.setHorario(servicio.getHorario());
                    return dto;
                });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProductsAndServices(Pageable pageable) {
        // Página de productos (incluye tanto productos normales como potencialmente servicios si tienen entrada en producto table)
        Page<ProductResponseDTO> productos = productRepository.findAll(pageable).map(this::convertToResponseDTO);

        // Para simplificar y evitar complejidad de mezclar páginas, devolvemos la página de productos
        // pero rellenando el campo horario si ese producto tiene entrada en servicio.
        productos.forEach(dto -> servicioRepository.findById(dto.getIdProducto()).ifPresent(s -> dto.setHorario(s.getHorario())));

        return productos;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getProductsByVendedor(Long vendedorId) {
        return productRepository.findByVendedorIdUsuario(vendedorId).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public void deleteProductLogico(Long id) {
        Producto producto = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + id));
        producto.setEstado(EstadoProducto.ELIMINADO);
        productRepository.save(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchProducts(String searchTerm) {
        return productRepository.findByNombreContainingIgnoreCaseOrDescripcionContainingIgnoreCase(
                searchTerm, searchTerm).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    // --- FILTROS Y BÚSQUEDAS AVANZADAS ---
    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByPriceRange(Double minPrice, Double maxPrice, Pageable pageable) {
        return productRepository.findByPrecioBetween(minPrice, maxPrice, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByMinPrice(Double minPrice, Pageable pageable) {
        return productRepository.findByPrecioGreaterThanEqual(minPrice, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByMaxPrice(Double maxPrice, Pageable pageable) {
        return productRepository.findByPrecioLessThanEqual(maxPrice, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByTipo(TipoProducto tipo, Pageable pageable) {
        return productRepository.findByTipoAndEstadoActivo(tipo, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByUbicacion(String ubicacion, Pageable pageable) {
        String ubicacionPattern = "%" + ubicacion + "%";
        return productRepository.findByUbicacionContaining(ubicacionPattern, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProductsWithPagination(String searchTerm, Pageable pageable) {
        String searchPattern = "%" + searchTerm + "%";
        return productRepository.findByNombreOrDescripcionContaining(searchPattern, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsWithFilters(Double minPrice, Double maxPrice, TipoProducto tipo,
            String searchTerm, String ubicacion, Boolean disponibilidad, Pageable pageable) {
        String searchPattern = searchTerm != null ? "%" + searchTerm + "%" : null;
        String ubicacionPattern = ubicacion != null ? "%" + ubicacion + "%" : null;

        return productRepository.findWithFilters(minPrice, maxPrice, tipo, searchPattern, ubicacionPattern,
                disponibilidad, pageable)
                .map(this::convertToResponseDTO);
    }

    // --- AUXILIARES ---
    private ProductResponseDTO convertToResponseDTO(Producto producto) {
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

        if (producto.getCategoria() != null) {
            dto.setIdCategoria(producto.getCategoria().getIdCategoria());
            dto.setNombreCategoria(producto.getCategoria().getNombre());
        }

        List<FotoProducto> fotos = producto.getFotos();
        if (fotos != null && !fotos.isEmpty()) {
            fotos.size(); // Forzar carga
            dto.setFotos(fotos.stream()
                    .map(foto -> new PhotoResponseDTO(foto.getIdFoto(), foto.getUrl(), producto.getIdProducto()))
                    .collect(Collectors.toList()));
        } else {
            dto.setFotos(new ArrayList<>());
        }

        // Rellenar horario si existe un Servicio asociado
        servicioRepository.findById(producto.getIdProducto()).ifPresent(s -> dto.setHorario(s.getHorario()));

        return dto;
    }

    private boolean validarYProcesarContenido(Producto producto, ProductRequestDTO requestDTO) {
        String contenidoCompleto = (requestDTO.getNombre() != null ? requestDTO.getNombre() : "") + " " +
                (requestDTO.getDescripcion() != null ? requestDTO.getDescripcion() : "");
        return detectorService.containsProhibited(contenidoCompleto);
    }

    private void crearIncidenciaAutomatica(Producto producto) {
        try {
            incidenciaService.crearPorDeteccion(
                    producto.getIdProducto(),
                    producto.getVendedor().getIdUsuario(),
                    "Contenido prohibido detectado automáticamente",
                    "El sistema detectó automáticamente contenido prohibido en el producto: " + producto.getNombre());
        } catch (Exception e) {
            System.err.println("Error al crear incidencia automática: " + e.getMessage());
        }
    }
}

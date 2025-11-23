package com.multicompany.sales_system.service.impl;

import com.multicompany.sales_system.dto.categoria.CategoriaRequestDTO;
import com.multicompany.sales_system.dto.categoria.CategoriaResponseDTO;
import com.multicompany.sales_system.model.Categoria;
import com.multicompany.sales_system.repository.CategoriaRepository;
import com.multicompany.sales_system.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Override
    public CategoriaResponseDTO createCategoria(CategoriaRequestDTO requestDTO) {
        // Validar que no exista una categoría con el mismo nombre
        if (categoriaRepository.existsByNombreIgnoreCase(requestDTO.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + requestDTO.getNombre());
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());
        categoria.setActivo(requestDTO.getActivo() != null ? requestDTO.getActivo() : true);
        categoria.setFechaCreacion(LocalDateTime.now());

        Categoria savedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(savedCategoria);
    }

    @Override
    public CategoriaResponseDTO updateCategoria(Long id, CategoriaRequestDTO requestDTO) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar que no exista otra categoría con el mismo nombre
        categoriaRepository.findByNombreIgnoreCase(requestDTO.getNombre())
                .ifPresent(existingCategoria -> {
                    if (!existingCategoria.getIdCategoria().equals(id)) {
                        throw new RuntimeException("Ya existe otra categoría con el nombre: " + requestDTO.getNombre());
                    }
                });

        categoria.setNombre(requestDTO.getNombre());
        categoria.setDescripcion(requestDTO.getDescripcion());
        if (requestDTO.getActivo() != null) {
            categoria.setActivo(requestDTO.getActivo());
        }

        Categoria updatedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(updatedCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO getCategoriaById(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return convertToResponseDTO(categoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getAllCategorias() {
        return categoriaRepository.findAll().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> getAllCategoriasWithPagination(Pageable pageable) {
        return categoriaRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> getCategoriasActivas() {
        return categoriaRepository.findByActivoOrderByNombreAsc(true).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> getCategoriasActivasWithPagination(Pageable pageable) {
        return categoriaRepository.findByActivo(true, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> searchCategoriasByNombre(String nombre) {
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> searchCategoriasByNombreWithPagination(String nombre, Pageable pageable) {
        return categoriaRepository.findByNombreContainingIgnoreCase(nombre, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    public void deleteCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // ✅ VALIDACIÓN IMPORTANTE: No permitir eliminar si tiene productos asociados
        if (categoriaRepository.categoriaHasProductos(id)) {
            Long cantidadProductos = categoriaRepository.countProductosByCategoriaId(id);
            throw new RuntimeException(
                    "No se puede eliminar la categoría '" + categoria.getNombre() +
                            "' porque tiene " + cantidadProductos + " producto(s) asociado(s). " +
                            "Primero debe reasignar o eliminar los productos de esta categoría.");
        }

        categoriaRepository.deleteById(id);
    }

    @Override
    public CategoriaResponseDTO toggleActivoCategoria(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        categoria.setActivo(!categoria.getActivo());
        Categoria updatedCategoria = categoriaRepository.save(categoria);
        return convertToResponseDTO(updatedCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean categoriaHasProductos(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        return categoriaRepository.categoriaHasProductos(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductosInCategoria(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        return categoriaRepository.countProductosByCategoriaId(id);
    }

    // ✅ MÉTODO DE CONVERSIÓN A DTO
    private CategoriaResponseDTO convertToResponseDTO(Categoria categoria) {
        Long cantidadProductos = categoriaRepository.countProductosByCategoriaId(categoria.getIdCategoria());

        return new CategoriaResponseDTO(
                categoria.getIdCategoria(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getActivo(),
                categoria.getFechaCreacion(),
                cantidadProductos.intValue());
    }
}

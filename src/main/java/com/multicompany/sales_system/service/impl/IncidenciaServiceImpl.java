package com.multicompany.sales_system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.dto.incident.IncidenciaRequestDTO;
import com.multicompany.sales_system.dto.incident.IncidenciaResponseDTO;
import com.multicompany.sales_system.model.Incidencia;
import com.multicompany.sales_system.model.Producto;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.IncidenciaRepository;
import com.multicompany.sales_system.repository.ProductRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.IncidenciaService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class IncidenciaServiceImpl implements IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final ProductRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaServiceImpl(IncidenciaRepository incidenciaRepository,
            ProductRepository productoRepository,
            UsuarioRepository usuarioRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Método helper para convertir Entity a ResponseDTO
    private IncidenciaResponseDTO toResponseDTO(Incidencia incidencia) {
        IncidenciaResponseDTO dto = new IncidenciaResponseDTO();
        dto.setIdIncidencia(incidencia.getIdIncidencia());
        dto.setMotivo(incidencia.getMotivo());
        dto.setDescripcion(incidencia.getDescripcion());
        dto.setEstado(incidencia.getEstado().name());
        dto.setFechaRegistro(incidencia.getFechaRegistro());

        if (incidencia.getProducto() != null) {
            dto.setIdProducto(incidencia.getProducto().getIdProducto());
            dto.setNombreProducto(incidencia.getProducto().getNombre());
        }

        if (incidencia.getUsuarioReporta() != null) {
            dto.setIdUsuarioReporta(incidencia.getUsuarioReporta().getIdUsuario());
            dto.setNombreUsuarioReporta(incidencia.getUsuarioReporta().getNombre());
        }

        return dto;
    }

    // Método helper para convertir RequestDTO a Entity
    private Incidencia toEntity(IncidenciaRequestDTO requestDTO) {
        Incidencia incidencia = new Incidencia();
        incidencia.setMotivo(requestDTO.getMotivo());
        incidencia.setDescripcion(requestDTO.getDescripcion());
        return incidencia;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listarPendientes() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.PENDIENTE)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listarAtendidas() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.ATENDIDA)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listarDescartadas() {
        return incidenciaRepository.findByEstado(Incidencia.Estado.DESCARTADA)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<IncidenciaResponseDTO> listarTodas() {
        return incidenciaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IncidenciaResponseDTO obtenerPorId(Long idIncidencia) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        return toResponseDTO(incidencia);
    }

    @Override
    public IncidenciaResponseDTO marcarAtendida(Long idIncidencia) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(Incidencia.Estado.ATENDIDA);
        Incidencia incidenciaActualizada = incidenciaRepository.save(incidencia);
        return toResponseDTO(incidenciaActualizada);
    }

    @Override
    public IncidenciaResponseDTO descartar(Long idIncidencia) {
        Incidencia incidencia = incidenciaRepository.findById(idIncidencia)
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        incidencia.setEstado(Incidencia.Estado.DESCARTADA);
        Incidencia incidenciaActualizada = incidenciaRepository.save(incidencia);
        return toResponseDTO(incidenciaActualizada);
    }

    @Override
    public IncidenciaResponseDTO crearIncidencia(IncidenciaRequestDTO requestDTO) {
        Producto producto = productoRepository.findById(requestDTO.getIdProducto())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        Usuario usuario = usuarioRepository.findById(requestDTO.getIdUsuarioReporta())
                .orElseThrow(() -> new RuntimeException("Usuario reportante no encontrado"));

        Incidencia incidencia = toEntity(requestDTO);
        incidencia.setProducto(producto);
        incidencia.setUsuarioReporta(usuario);
        // El estado PENDIENTE y fechaRegistro se setean automáticamente en la entidad

        Incidencia incidenciaGuardada = incidenciaRepository.save(incidencia);
        return toResponseDTO(incidenciaGuardada);
    }

    @Override
    public IncidenciaResponseDTO crearPorDeteccion(Long idProducto, Long idUsuarioReporta, String motivo,
            String descripcion) {
        // Validaciones básicas
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo no puede estar vacío");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción no puede estar vacía");
        }

        // Buscar producto y usuario
        Producto producto = productoRepository.findById(idProducto)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + idProducto));
        Usuario usuario = usuarioRepository.findById(idUsuarioReporta)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + idUsuarioReporta));

        // Crear y guardar la incidencia
        Incidencia incidencia = new Incidencia();
        incidencia.setProducto(producto);
        incidencia.setUsuarioReporta(usuario);
        incidencia.setMotivo(motivo);
        incidencia.setDescripcion(descripcion);
        // El estado PENDIENTE y fechaRegistro se setean automáticamente en la entidad

        Incidencia incidenciaGuardada = incidenciaRepository.save(incidencia);
        return toResponseDTO(incidenciaGuardada);
    }
}
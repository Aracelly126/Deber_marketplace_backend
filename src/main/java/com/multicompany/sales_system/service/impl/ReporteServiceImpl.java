package com.multicompany.sales_system.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multicompany.sales_system.model.Reporte;
import com.multicompany.sales_system.dto.report.ReporteRequestDTO;
import com.multicompany.sales_system.dto.report.ReporteResponseDTO;
import com.multicompany.sales_system.model.Incidencia;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.ReporteRepository;
import com.multicompany.sales_system.repository.IncidenciaRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.ReporteService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ReporteServiceImpl implements ReporteService {

    private final ReporteRepository reporteRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public ReporteServiceImpl(ReporteRepository reporteRepository,
            IncidenciaRepository incidenciaRepository,
            UsuarioRepository usuarioRepository) {
        this.reporteRepository = reporteRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Método helper para convertir Entity a ResponseDTO
    private ReporteResponseDTO toResponseDTO(Reporte reporte) {
        ReporteResponseDTO dto = new ReporteResponseDTO();
        dto.setIdReporte(reporte.getIdReporte());
        dto.setAccionTomada(reporte.getAccionTomada());
        dto.setComentario(reporte.getComentario());
        dto.setFechaAccion(reporte.getFechaAccion());

        if (reporte.getIncidencia() != null) {
            dto.setIdIncidencia(reporte.getIncidencia().getIdIncidencia());
            dto.setMotivoIncidencia(reporte.getIncidencia().getMotivo());
        }

        if (reporte.getModerador() != null) {
            dto.setIdModerador(reporte.getModerador().getIdUsuario());
            dto.setNombreModerador(reporte.getModerador().getNombre());
        }

        return dto;
    }

    // Método helper para convertir RequestDTO a Entity
    private Reporte toEntity(ReporteRequestDTO requestDTO) {
        Reporte reporte = new Reporte();
        reporte.setAccionTomada(requestDTO.getAccionTomada());
        reporte.setComentario(requestDTO.getComentario());
        return reporte;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarTodos() {
        return reporteRepository.findByOrderByFechaAccionDesc()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarPorIncidencia(Long idIncidencia) {
        return reporteRepository.findByIncidenciaIdIncidencia(idIncidencia)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarPorModerador(Long idModerador) {
        return reporteRepository.findByModeradorIdUsuario(idModerador)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteResponseDTO obtenerPorId(Long idReporte) {
        Reporte reporte = reporteRepository.findById(idReporte)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        return toResponseDTO(reporte);
    }

    @Override
    public ReporteResponseDTO crearReporte(ReporteRequestDTO requestDTO) {
        Incidencia incidencia = incidenciaRepository.findById(requestDTO.getIdIncidencia())
                .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
        Usuario moderador = usuarioRepository.findById(requestDTO.getIdModerador())
                .orElseThrow(() -> new RuntimeException("Moderador no encontrado"));

        Reporte reporte = toEntity(requestDTO);
        reporte.setIncidencia(incidencia);
        reporte.setModerador(moderador);
        // La fechaAccion se setea automáticamente en la entidad

        Reporte reporteGuardado = reporteRepository.save(reporte);
        return toResponseDTO(reporteGuardado);
    }

    // @Override
    // public ReporteResponseDTO actualizarReporte(Long idReporte, ReporteRequestDTO
    // requestDTO) {
    // Reporte reporteExistente = reporteRepository.findById(idReporte)
    // .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

    // // Actualizar campos
    // reporteExistente.setAccionTomada(requestDTO.getAccionTomada());
    // reporteExistente.setComentario(requestDTO.getComentario());

    // // Si se cambian las relaciones, buscar nuevas entidades
    // if
    // (!reporteExistente.getIncidencia().getIdIncidencia().equals(requestDTO.getIdIncidencia()))
    // {
    // Incidencia incidencia =
    // incidenciaRepository.findById(requestDTO.getIdIncidencia())
    // .orElseThrow(() -> new RuntimeException("Incidencia no encontrada"));
    // reporteExistente.setIncidencia(incidencia);
    // }

    // if
    // (!reporteExistente.getModerador().getIdUsuario().equals(requestDTO.getIdModerador()))
    // {
    // Usuario moderador = usuarioRepository.findById(requestDTO.getIdModerador())
    // .orElseThrow(() -> new RuntimeException("Moderador no encontrado"));
    // reporteExistente.setModerador(moderador);
    // }

    // Reporte reporteActualizado = reporteRepository.save(reporteExistente);
    // return toResponseDTO(reporteActualizado);
    // }

    @Override
    public void eliminarReporte(Long idReporte) {
        Reporte reporte = reporteRepository.findById(idReporte)
                .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        reporteRepository.delete(reporte);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReporteResponseDTO> listarPorAnioYMes(int year, int month) {
        // Validar que el mes esté entre 1 y 12
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("El mes debe estar entre 1 y 12");
        }

        // Validar que el año sea razonable (ej: entre 2020 y 2030)
        int currentYear = java.time.Year.now().getValue();
        if (year < 2020 || year > currentYear + 1) {
            throw new IllegalArgumentException("El año debe ser válido");
        }

        return reporteRepository.findByYearAndMonth(year, month)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

}
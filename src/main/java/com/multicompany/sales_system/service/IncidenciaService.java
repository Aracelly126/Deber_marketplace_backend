package com.multicompany.sales_system.service;

import java.util.List;

import com.multicompany.sales_system.dto.incident.IncidenciaRequestDTO;
import com.multicompany.sales_system.dto.incident.IncidenciaResponseDTO;

public interface IncidenciaService {
    List<IncidenciaResponseDTO> listarPendientes();

    List<IncidenciaResponseDTO> listarAtendidas();

    List<IncidenciaResponseDTO> listarDescartadas();

    List<IncidenciaResponseDTO> listarTodas();

    IncidenciaResponseDTO obtenerPorId(Long idIncidencia);

    IncidenciaResponseDTO marcarAtendida(Long idIncidencia);

    IncidenciaResponseDTO descartar(Long idIncidencia);

    IncidenciaResponseDTO crearIncidencia(IncidenciaRequestDTO requestDTO);

    // Método adicional para creación directa (sin DTO)
    IncidenciaResponseDTO crearPorDeteccion(Long idProducto, Long idUsuarioReporta, String motivo, String descripcion);
}
package com.multicompany.sales_system.service;

import java.util.List;

import com.multicompany.sales_system.dto.report.ReporteRequestDTO;
import com.multicompany.sales_system.dto.report.ReporteResponseDTO;

public interface ReporteService {
    List<ReporteResponseDTO> listarTodos();

    List<ReporteResponseDTO> listarPorIncidencia(Long idIncidencia);

    List<ReporteResponseDTO> listarPorModerador(Long idModerador);

    ReporteResponseDTO obtenerPorId(Long idReporte);

    ReporteResponseDTO crearReporte(ReporteRequestDTO requestDTO);

    // ReporteResponseDTO actualizarReporte(Long idReporte, ReporteRequestDTO
    // requestDTO);

    void eliminarReporte(Long idReporte);

    List<ReporteResponseDTO> listarPorAnioYMes(int year, int month);
}
package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.report.ReporteRequestDTO;
import com.multicompany.sales_system.dto.report.ReporteResponseDTO;
import com.multicompany.sales_system.service.ReporteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(reporteService.listarTodos());
    }

    @GetMapping("/incidencia/{idIncidencia}")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorIncidencia(@PathVariable Long idIncidencia) {
        return ResponseEntity.ok(reporteService.listarPorIncidencia(idIncidencia));
    }

    @GetMapping("/moderador/{idModerador}")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorModerador(@PathVariable Long idModerador) {
        return ResponseEntity.ok(reporteService.listarPorModerador(idModerador));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ReporteResponseDTO> crearReporte(@Valid @RequestBody ReporteRequestDTO requestDTO) {
        return ResponseEntity.ok(reporteService.crearReporte(requestDTO));
    }

    // @PutMapping("/{id}")
    // public ResponseEntity<ReporteResponseDTO> actualizarReporte(
    // @PathVariable Long id,
    // @Valid @RequestBody ReporteRequestDTO requestDTO) {
    // return ResponseEntity.ok(reporteService.actualizarReporte(id, requestDTO));
    // }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarReporte(@PathVariable Long id) {
        reporteService.eliminarReporte(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filtros/anio-mes")
    public ResponseEntity<List<ReporteResponseDTO>> listarPorAnioYMes(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(reporteService.listarPorAnioYMes(year, month));
    }
}
package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.incident.IncidenciaRequestDTO;
import com.multicompany.sales_system.dto.incident.IncidenciaResponseDTO;
import com.multicompany.sales_system.service.IncidenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias")
public class IncidenciaController {

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    public ResponseEntity<List<IncidenciaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(incidenciaService.listarTodas());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<IncidenciaResponseDTO>> listarPendientes() {
        return ResponseEntity.ok(incidenciaService.listarPendientes());
    }

    @GetMapping("/atendidas")
    public ResponseEntity<List<IncidenciaResponseDTO>> listarAtendidas() {
        return ResponseEntity.ok(incidenciaService.listarAtendidas());
    }

    @GetMapping("/descartadas")
    public ResponseEntity<List<IncidenciaResponseDTO>> listarDescartadas() {
        return ResponseEntity.ok(incidenciaService.listarDescartadas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncidenciaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<IncidenciaResponseDTO> crearIncidencia(
            @Valid @RequestBody IncidenciaRequestDTO requestDTO) {
        return ResponseEntity.ok(incidenciaService.crearIncidencia(requestDTO));
    }

    @PostMapping("/deteccion")
    public ResponseEntity<IncidenciaResponseDTO> crearPorDeteccion(
            @RequestParam Long idProducto,
            @RequestParam Long idUsuarioReporta,
            @RequestParam String motivo,
            @RequestParam String descripcion) {
        return ResponseEntity.ok(incidenciaService.crearPorDeteccion(idProducto, idUsuarioReporta, motivo, descripcion));
    }

    @PutMapping("/{id}/atender")
    public ResponseEntity<IncidenciaResponseDTO> marcarAtendida(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.marcarAtendida(id));
    }

    @PutMapping("/{id}/descartar")
    public ResponseEntity<IncidenciaResponseDTO> descartar(@PathVariable Long id) {
        return ResponseEntity.ok(incidenciaService.descartar(id));
    }
}
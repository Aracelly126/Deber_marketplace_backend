package com.multicompany.sales_system.controller;

import com.multicompany.sales_system.dto.configuracion.AgregarPalabraRequestDTO;
import com.multicompany.sales_system.dto.configuracion.DiasExpiracionRequestDTO;
import com.multicompany.sales_system.dto.configuracion.DiasExpiracionResponseDTO;
import com.multicompany.sales_system.dto.configuracion.EliminarPalabraRequestDTO;
import com.multicompany.sales_system.dto.configuracion.PalabrasProhibidasResponseDTO;
import com.multicompany.sales_system.service.ConfiguracionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para gestionar configuraciones del sistema.
 * La configuración CORS se maneja globalmente desde CorsConfig.
 */
@RestController
@RequestMapping("/api/configuracion")
@RequiredArgsConstructor
public class ConfiguracionController {

    private final ConfiguracionService configuracionService;

    /**
     * Obtiene la cantidad de días configurados para la expiración de productos.
     * 
     * GET /api/configuracion/dias-expiracion
     * 
     * @return DiasExpiracionResponseDTO con los días actuales
     */
    @GetMapping("/dias-expiracion")
    public ResponseEntity<DiasExpiracionResponseDTO> getDiasExpiracion() {
        int dias = configuracionService.getDiasExpiracion();
        DiasExpiracionResponseDTO response = new DiasExpiracionResponseDTO(
            dias,
            "Días de expiración de productos"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Actualiza la cantidad de días para la expiración de productos.
     * 
     * PUT /api/configuracion/dias-expiracion
     * Body: { "dias": 45 }
     * 
     * @param request DTO con los nuevos días (debe ser >= 1)
     * @return DiasExpiracionResponseDTO con confirmación
     */
    @PutMapping("/dias-expiracion")
    public ResponseEntity<DiasExpiracionResponseDTO> updateDiasExpiracion(
            @Valid @RequestBody DiasExpiracionRequestDTO request) {
        
        int diasActualizados = configuracionService.updateDiasExpiracion(request.getDias());
        
        DiasExpiracionResponseDTO response = new DiasExpiracionResponseDTO(
            diasActualizados,
            "Días de expiración actualizados exitosamente"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la lista de palabras prohibidas.
     * 
     * GET /api/configuracion/palabras-prohibidas
     * 
     * @return PalabrasProhibidasResponseDTO con la lista de palabras
     */
    @GetMapping("/palabras-prohibidas")
    public ResponseEntity<PalabrasProhibidasResponseDTO> getPalabrasProhibidas() {
        List<String> palabras = configuracionService.obtenerPalabrasProhibidasAdmin();
        PalabrasProhibidasResponseDTO response = new PalabrasProhibidasResponseDTO(
            palabras,
            "Lista de palabras prohibidas"
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Agrega una nueva palabra al filtro de palabras prohibidas.
     * 
     * POST /api/configuracion/palabras-prohibidas
     * Body: { "palabra": "nuevaPalabra" }
     * 
     * @param request DTO con la palabra a agregar
     * @return PalabrasProhibidasResponseDTO con la lista actualizada
     */
    @PostMapping("/palabras-prohibidas")
    public ResponseEntity<PalabrasProhibidasResponseDTO> agregarPalabraProhibida(
            @Valid @RequestBody AgregarPalabraRequestDTO request) {
        
        List<String> palabrasActualizadas = configuracionService.agregarPalabraProhibida(request.getPalabra());
        
        PalabrasProhibidasResponseDTO response = new PalabrasProhibidasResponseDTO(
            palabrasActualizadas,
            "Palabra agregada exitosamente al filtro"
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Elimina una palabra del filtro de palabras prohibidas.
     * 
     * DELETE /api/configuracion/palabras-prohibidas
     * Body: { "palabra": "palabraAEliminar" }
     * 
     * @param request DTO con la palabra a eliminar
     * @return PalabrasProhibidasResponseDTO con la lista actualizada
     */
    @DeleteMapping("/palabras-prohibidas")
    public ResponseEntity<PalabrasProhibidasResponseDTO> eliminarPalabraProhibida(
            @Valid @RequestBody EliminarPalabraRequestDTO request) {
        
        List<String> palabrasActualizadas = configuracionService.eliminarPalabraProhibida(request.getPalabra());
        
        PalabrasProhibidasResponseDTO response = new PalabrasProhibidasResponseDTO(
            palabrasActualizadas,
            "Palabra eliminada exitosamente del filtro"
        );
        
        return ResponseEntity.ok(response);
    }

}

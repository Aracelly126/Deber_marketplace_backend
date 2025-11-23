package com.multicompany.sales_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.multicompany.sales_system.model.Configuracion;
import com.multicompany.sales_system.repository.ConfiguracionRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {
    private final ConfiguracionRepository configuracionRepository;

    /**
     * Devuelve la lista de palabras prohibidas, limpias y en minúsculas.
     */
    public List<String> getPalabrasProhibidas() {
        return configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS) // ✅ Usar enum
                .map(Configuracion::getValor)
                .map(v -> Arrays.stream(v.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    /**
     * Devuelve la cantidad de días configurados para la expiración de productos.
     * Por defecto retorna 30 días si no hay configuración regfistrada.
     */
    public int getDiasExpiracion() {
        return configuracionRepository.findByOpcion(Configuracion.Opcion.EXPIRACION_CONFIG)
                .map(Configuracion::getValor)
                .map(valor -> {
                    try {
                        return Integer.parseInt(valor.trim());
                    } catch (NumberFormatException e) {
                        return 30; 
                    }
                })
                .orElse(30); 
    }

    /**
     * Actualiza la cantidad de días para la expiración de productos.
     * Si no existe una configuración previa, crea una nueva.
     * 
     * @param dias cantidad de días (debe ser mayor a 0)
     * @return los días configurados
     * @throws IllegalArgumentException si los días son menores o iguales a 0
     */
    public int updateDiasExpiracion(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Los días de expiración deben ser mayor a 0");
        }

        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.EXPIRACION_CONFIG)
                .orElseGet(() -> {
                    Configuracion nuevaConfig = new Configuracion();
                    nuevaConfig.setOpcion(Configuracion.Opcion.EXPIRACION_CONFIG);
                    return nuevaConfig;
                });
        
        config.setValor(String.valueOf(dias));
        configuracionRepository.save(config);
        
        return dias;
    }

    /**
     * Agrega una nueva palabra al filtro de palabras prohibidas.
     * Si la palabra ya existe, no se agrega duplicada.
     * 
     * @param palabra palabra a agregar (será convertida a minúsculas y sin espacios)
     * @return lista actualizada de palabras prohibidas
     * @throws IllegalArgumentException si la palabra está vacía
     */
    public List<String> agregarPalabraProhibida(String palabra) {
        if (palabra == null || palabra.trim().isEmpty()) {
            throw new IllegalArgumentException("La palabra no puede estar vacía");
        }

        // Normalizar: minúsculas y sin espacios extras
        String palabraNormalizada = palabra.trim().toLowerCase();

        // Obtener la configuración actual o crear una nueva
        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS)
                .orElseGet(() -> {
                    Configuracion nuevaConfig = new Configuracion();
                    nuevaConfig.setOpcion(Configuracion.Opcion.FILTRO_PALABRAS);
                    nuevaConfig.setValor("");
                    return nuevaConfig;
                });

        // Obtener palabras actuales
        Set<String> palabrasSet = new LinkedHashSet<>();
        String valorActual = config.getValor();
        if (valorActual != null && !valorActual.trim().isEmpty()) {
            palabrasSet.addAll(Arrays.asList(valorActual.split(",")));
        }

        // Agregar la nueva palabra si no existe
        palabrasSet.add(palabraNormalizada);

        // Guardar actualización
        String nuevoValor = String.join(",", palabrasSet);
        config.setValor(nuevoValor);
        configuracionRepository.save(config);

        // Retornar lista actualizada
        return new ArrayList<>(palabrasSet);
    }

    /**
     * Elimina una palabra del filtro de palabras prohibidas.
     * 
     * @param palabra palabra a eliminar (será convertida a minúsculas)
     * @return lista actualizada de palabras prohibidas
     * @throws IllegalArgumentException si la palabra está vacía o no existe la configuración
     */
    public List<String> eliminarPalabraProhibida(String palabra) {
        if (palabra == null || palabra.trim().isEmpty()) {
            throw new IllegalArgumentException("La palabra no puede estar vacía");
        }

        // Normalizar: minúsculas
        String palabraNormalizada = palabra.trim().toLowerCase();

        // Obtener la configuración actual
        Configuracion config = configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS)
                .orElseThrow(() -> new IllegalArgumentException("No existe configuración de palabras prohibidas"));

        // Obtener palabras actuales
        Set<String> palabrasSet = new LinkedHashSet<>();
        String valorActual = config.getValor();
        if (valorActual != null && !valorActual.trim().isEmpty()) {
            // Normalizar todas las palabras a minúsculas al cargarlas
            palabrasSet.addAll(Arrays.stream(valorActual.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        }

        // Eliminar la palabra
        boolean removed = palabrasSet.remove(palabraNormalizada);
        if (!removed) {
            throw new IllegalArgumentException("La palabra '" + palabraNormalizada + "' no existe en el filtro");
        }

        // Guardar actualización
        String nuevoValor = String.join(",", palabrasSet);
        config.setValor(nuevoValor);
        configuracionRepository.save(config);

        // Retornar lista actualizada
        return new ArrayList<>(palabrasSet);
    }

    /**
     * Obtiene la lista completa de palabras prohibidas para visualización en administración.
     * Retorna las palabras normalizadas en minúsculas.
     * 
     * @return lista de palabras prohibidas
     */
    public List<String> obtenerPalabrasProhibidasAdmin() {
        return configuracionRepository.findByOpcion(Configuracion.Opcion.FILTRO_PALABRAS)
                .map(Configuracion::getValor)
                .map(v -> {
                    if (v == null || v.trim().isEmpty()) {
                        return List.<String>of();
                    }
                    return Arrays.stream(v.split(","))
                            .map(String::trim)
                            .map(String::toLowerCase)  // Normalizar a minúsculas
                            .filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
                })
                .orElse(List.of());
    }
}
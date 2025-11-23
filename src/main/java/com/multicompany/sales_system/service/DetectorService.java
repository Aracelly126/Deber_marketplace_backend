package com.multicompany.sales_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DetectorService {

    private final ConfiguracionService configuracionService;
    // Cache simple de patrones para evitar recompilar en cada llamada.
    private volatile List<Pattern> patronesCache = null;

    private void cargarPatrones() {
        List<String> palabras = configuracionService.getPalabrasProhibidas();
        List<Pattern> patrones = palabras.stream()
                .filter(s -> !s.isBlank())
                .map(p -> "\\b" + Pattern.quote(p) + "\\b")
                .map(regex -> Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE))
                .collect(Collectors.toList());
        patronesCache = patrones;
    }

    private List<Pattern> getPatrones() {
        if (patronesCache == null) {
            synchronized (this) {
                if (patronesCache == null) cargarPatrones();
            }
        }
        return patronesCache;
    }

    /** Indica si el texto contiene al menos una palabra prohibida. */
    public boolean containsProhibited(String texto) {
        if (texto == null || texto.isBlank()) return false;
        for (Pattern p : getPatrones()) {
            if (p.matcher(texto).find()) return true;
        }
        return false;
    }

    /** Devuelve las palabras encontradas (sin duplicados). */
    public List<String> findMatchedWords(String texto) {
        if (texto == null || texto.isBlank()) return List.of();
        LinkedHashSet<String> found = new LinkedHashSet<>();
        for (Pattern p : getPatrones()) {
            Matcher m = p.matcher(texto);
            if (m.find()) {
                found.add(m.group().toLowerCase());
            }
        }
        return new ArrayList<>(found);
    }

    /** Forzar recarga (por ejemplo admin actualizó el banco de palabras) */
    public void reload() {
        cargarPatrones();
    }
}
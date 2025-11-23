package com.multicompany.sales_system.scheduler;

import com.multicompany.sales_system.service.ProductoRestriccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para tareas automáticas relacionadas con productos.
 * 
 * Esta clase contiene métodos programados que se ejecutan periódicamente
 * para mantener la integridad y estado de los productos en el sistema.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ProductScheduler {

    private final ProductoRestriccionService productoRestriccionService;

    /**
     * Procesa automáticamente los productos que han expirado.
     * 
     * Se ejecuta cada 2 minutos para:
     * - Identificar productos cuya fechaExpiracion ha pasado
     * - Cambiar su estado a ELIMINADO
     * - Marcarlos como no disponibles
     * 
     * Configuración del cron: "0 0 * * * *"
     * - Segundo: 0 (en punto)
     * - Minuto: 0 (en putno )
     * - Hora: * (todas las horas)
     * - Día del mes: * (todos los días)
     * - Mes: * (todos los meses)
     * - Día de la semana: * (todos los días de la semana)
     * 
     * Resultado: Se ejecuta cada 2 minutos (00:00, 00:02, 00:04, 00:06, etc.)
     */
    @Scheduled(cron = "0 0 * * * *")
    public void procesarProductosExpirados() {
        log.info("Iniciando proceso automático de productos expirados...");
        
        try {
            int productosAfectados = productoRestriccionService.procesarProductosExpirados();
            
            if (productosAfectados > 0) {
                log.info("Proceso completado: {} producto(s) marcado(s) como expirado(s)", productosAfectados);
            } else {
                log.debug("Proceso completado: No hay productos expirados en este momento");
            }
            
        } catch (Exception e) {
            log.error("Error al procesar productos expirados: {}", e.getMessage(), e);
            // No relanzamos la excepción para que el scheduler continúe en la siguiente ejecución
        }
    }

}

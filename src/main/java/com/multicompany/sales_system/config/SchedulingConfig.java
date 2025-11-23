package com.multicompany.sales_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuración para habilitar tareas programadas (scheduled tasks) en la aplicación.
 * 
 * @EnableScheduling permite usar la anotación @Scheduled en componentes Spring
 * para ejecutar métodos de forma periódica.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Esta clase habilita el soporte de scheduling en Spring Boot
    // No requiere beans adicionales si se usa configuración por defecto
}

package com.multicompany.sales_system.model.enums;

/**
 * Enumerado que define los estados posibles de un producto en el sistema
 */
public enum EstadoProducto {
    ACTIVO("Producto activo y visible"),
    OCULTO("Producto oculto temporalmente"),
    PROHIBIDO("Producto prohibido por administración"),
    APELADO("Producto en proceso de apelación"),
    ELIMINADO("Producto eliminado del sistema");

    private final String descripcion;

    EstadoProducto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si el producto está disponible para la venta
     * 
     * @return true si el estado permite la venta
     */
    public boolean isDisponibleParaVenta() {
        return this == ACTIVO;
    }

    /**
     * Verifica si el producto es visible para los usuarios
     * 
     * @return true si el estado permite la visualización
     */
    public boolean isVisible() {
        return this == ACTIVO || this == OCULTO;
    }
}
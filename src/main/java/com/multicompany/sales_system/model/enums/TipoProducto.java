package com.multicompany.sales_system.model.enums;

/**
 * Enumerado que define los tipos de productos disponibles en el sistema
 */
public enum TipoProducto {
    PRODUCTO("Producto físico"),
    SERVICIO("Servicio");

    private final String descripcion;

    TipoProducto(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
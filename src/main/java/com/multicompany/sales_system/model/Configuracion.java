package com.multicompany.sales_system.model;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "configuracion")
public class Configuracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idConfig;

    @Enumerated(EnumType.STRING)
    private Opcion opcion;

    private String valor;

    public enum Opcion { FILTRO_PALABRAS, REGLAS_USO, PRIVACIDAD, EXPIRACION_CONFIG }
}

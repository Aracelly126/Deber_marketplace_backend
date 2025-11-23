package com.multicompany.sales_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multicompany.sales_system.model.Incidencia;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findByEstado(Incidencia.Estado estado);
}
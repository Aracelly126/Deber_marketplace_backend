package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Configuracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, Long> {
    Optional<Configuracion> findByOpcion(Configuracion.Opcion opcion);
}
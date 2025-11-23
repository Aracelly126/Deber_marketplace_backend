package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Servicio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioRepository extends JpaRepository<Servicio, Long> {
    Page<Servicio> findAll(Pageable pageable);

    List<Servicio> findByProductoVendedorIdUsuario(Long vendedorId);
}

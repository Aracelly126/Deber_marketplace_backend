package com.multicompany.sales_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.multicompany.sales_system.model.Reporte;

import java.util.List;

public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByIncidenciaIdIncidencia(Long idIncidencia);

    List<Reporte> findByModeradorIdUsuario(Long idModerador);

    List<Reporte> findByOrderByFechaAccionDesc();

    @Query("SELECT r FROM Reporte r WHERE YEAR(r.fechaAccion) = :year AND MONTH(r.fechaAccion) = :month ORDER BY r.fechaAccion DESC")
    List<Reporte> findByYearAndMonth(@Param("year") int year, @Param("month") int month);
}
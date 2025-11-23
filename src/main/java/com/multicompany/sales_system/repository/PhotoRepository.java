package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.FotoProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PhotoRepository extends JpaRepository<FotoProducto, Long> {

    List<FotoProducto> findByProductoIdProducto(Long productId);

    void deleteByProductoIdProducto(Long productId);
}

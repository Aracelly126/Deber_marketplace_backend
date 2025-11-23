package com.multicompany.sales_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.Usuario.EstadoUsuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByCorreoIgnoreCase(String correo);
    boolean existsByCedula(String cedula);
    Optional<Usuario> findByCorreoIgnoreCase(String correo);
    Optional<Usuario> findByCedula(String cedula);
    List<Usuario> findByEstadoNot(EstadoUsuario estado);


}

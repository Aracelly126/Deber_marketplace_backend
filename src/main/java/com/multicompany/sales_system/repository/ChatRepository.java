package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query("SELECT c FROM Chat c WHERE (c.usuario1.idUsuario = :idUsuario1 AND c.usuario2.idUsuario = :idUsuario2) OR (c.usuario1.idUsuario = :idUsuario2 AND c.usuario2.idUsuario = :idUsuario1)")
    Optional<Chat> findByUsuarios(@Param("idUsuario1") Long idUsuario1, @Param("idUsuario2") Long idUsuario2);

    @Query("SELECT c FROM Chat c WHERE c.usuario1.idUsuario = :idUsuario OR c.usuario2.idUsuario = :idUsuario")
    List<Chat> findByUsuarioId(@Param("idUsuario") Long idUsuario);
}
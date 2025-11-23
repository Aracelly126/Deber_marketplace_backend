package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    List<Mensaje> findByChatIdChatOrderByFechaEnvioAsc(Long chatId);

    List<Mensaje> findByChatIdChatAndLeidoFalseAndEmisorIdUsuarioNot(Long chatId, Long emisorId);

    @Query("SELECT COUNT(m) FROM Mensaje m WHERE m.leido = false AND " +
            "((m.chat.usuario1.idUsuario = :idUsuario OR m.chat.usuario2.idUsuario = :idUsuario) AND " +
            "m.emisor.idUsuario != :idUsuario)")
    Integer countMensajesNoLeidosPorUsuario(@Param("idUsuario") Long idUsuario);
}
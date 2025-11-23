package com.multicompany.sales_system.repository;

import com.multicompany.sales_system.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    Optional<EmailVerification> findTopByUserIdUsuarioAndUsedFalseOrderByExpiresAtDesc(Long userIdUsuario);
}
package com.multicompany.sales_system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class VerificationCodeGenerator {
    private final SecureRandom rnd = new SecureRandom();
    @Value("${app.verification.code.length:6}") private int length;

    public String generateNumeric() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
}

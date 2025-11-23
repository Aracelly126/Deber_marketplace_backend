package com.multicompany.sales_system.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VerifyEmailRequest {
    @NotBlank @Email
    private String correo;

    @NotBlank @Size(min = 4, max = 10)
    private String code;

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

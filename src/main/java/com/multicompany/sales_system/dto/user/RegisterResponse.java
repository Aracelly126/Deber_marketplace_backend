package com.multicompany.sales_system.dto.user;

public class RegisterResponse {
    private Long id;
    private String correo;
    private String rol;
    private String mensaje;

    public RegisterResponse(Long id, String correo, String rol, String mensaje) {
        this.id = id;
        this.correo = correo;
        this.rol = rol;
        this.mensaje = mensaje;
    }

    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public String getRol() { return rol; }
    public String getMensaje() { return mensaje; }
}

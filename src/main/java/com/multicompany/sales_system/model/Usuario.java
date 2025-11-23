// com.multicompany.sales_system.model.Usuario
package com.multicompany.sales_system.model;

import java.time.LocalDateTime;

import com.multicompany.sales_system.model.enums.UsuarioRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "contrasena")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(
        name = "usuario",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_usuario_correo", columnNames = "correo"),
            @UniqueConstraint(name = "uk_usuario_cedula", columnNames = "cedula")
        }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @EqualsAndHashCode.Include
    private Long idUsuario;

    @Column(name = "cedula", nullable = false, length = 25)
    private String cedula;

    @Column(name = "nombre", length = 255)
    private String nombre;

    @Column(name = "apellido", length = 255)
    private String apellido;

    @Column(name = "correo", nullable = false, length = 255)
    private String correo;

    @Column(name = "telefono", length = 25)
    private String telefono;

    @Column(name = "direccion", length = 255)
    private String direccion;

    @Column(name = "genero", length = 10)
    private String genero;

    @Column(name = "contrasena", nullable = false, length = 255)
    private String contrasena;

    @Column(name = "recovery_code", length = 255)
    private String recoveryCode;

    @Column(name = "recovery_code_expires_at")
    private LocalDateTime recoveryCodeExpiresAt;

    // === ÚNICO rol del sistema ===
    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false, length = 20)
    private UsuarioRole rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private EstadoUsuario estado = EstadoUsuario.ACTIVO;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado = false;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (rol == null) rol = UsuarioRole.USER; 
    }

    public enum EstadoUsuario {
        ACTIVO, SUSPENDIDO, ELIMINADO
    }
}

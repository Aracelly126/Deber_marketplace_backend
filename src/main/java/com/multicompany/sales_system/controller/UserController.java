package com.multicompany.sales_system.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.multicompany.sales_system.dto.user.AdminCreateModeratorRequest;
import com.multicompany.sales_system.dto.user.LoginRequest;
import com.multicompany.sales_system.dto.user.LoginResponse;
import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.ResendCodeRequest;
import com.multicompany.sales_system.dto.user.UserResponse;
import com.multicompany.sales_system.dto.user.UserUpdateRequest;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UsuarioService usuarioService;
    private final com.multicompany.sales_system.security.JwtService jwtService;
    private final UsuarioRepository usuarioRepo;

    // =========================
    // Registro normal
    // =========================
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest dto, BindingResult br) {
        var cedulaError = br.getFieldError("cedula");
        if (cedulaError != null) {
            return ResponseEntity.badRequest().body(cedulaError.getDefaultMessage());
        }
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(first.getDefaultMessage());
        }

        RegisterResponse created = usuarioService.registrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================
    // ADMIN: Crear moderador
    // =========================
@PostMapping("/admin/moderators")
public ResponseEntity<RegisterResponse> createModerator(
        @Valid @RequestBody AdminCreateModeratorRequest dto,
        BindingResult br,
        @RequestHeader(value = "X-ADMIN-KEY", required = false) String adminKeyHeader) {

    if (br.hasErrors()) {
        var first = br.getFieldErrors().get(0);
        return ResponseEntity.badRequest().body(
                new RegisterResponse(null, dto.getCorreo(), null, first.getDefaultMessage())
        );
    }
    if (adminKeyHeader == null || adminKeyHeader.isBlank()) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new RegisterResponse(null, dto.getCorreo(), null, "Falta el header X-ADMIN-KEY"));
    }

    RegisterResponse created = usuarioService.crearModerador(dto, adminKeyHeader);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
}
    // =========================
    // Verificación de correo
    // =========================
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verify(@Valid @RequestBody VerifyEmailRequest dto) {
        String message = usuarioService.verificarCorreo(dto);
        return ResponseEntity.ok(Map.of("message", message));
    }

    // =========================
    // Reenviar código
    // =========================
    @PostMapping("/resend-code")
    public ResponseEntity<Map<String, Object>> resend(@Valid @RequestBody ResendCodeRequest dto) {
        usuarioService.reenviarCodigo(dto.getCorreo());
        return ResponseEntity.ok(Map.of("message", "Nuevo código enviado"));
    }

    // =========================
    // Check email
    // =========================
    @GetMapping("/check-email")
    public ResponseEntity<Map<String, Object>> checkEmail(@RequestParam String email) {
        boolean available = usuarioService.correoDisponible(email);
        return ResponseEntity.ok(Map.of("email", email, "available", available));
    }

    // =========================
    // Login
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest dto, BindingResult br) {
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(
                    Map.of("field", first.getField(), "message", first.getDefaultMessage())
            );
        }
        LoginResponse res = usuarioService.login(dto);
        return ResponseEntity.ok(res);
    }

    // =========================
    // Recuperación de contraseña
    // =========================
    @PostMapping("/recover-password")
    public ResponseEntity<Map<String, Object>> recoverPassword(@RequestParam String email) {
        usuarioService.iniciarRecuperacionContrasena(email);
        return ResponseEntity.ok(Map.of("message", "Correo de recuperación enviado"));
    }

    // =========================
    // Cambio de contraseña por recuperación
    // =========================
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody com.multicompany.sales_system.dto.user.PasswordResetRequest request) {
        usuarioService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Contraseña cambiada correctamente"));
    }

    // =========================
    // ADMIN/MODERATOR: Listar usuarios
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<java.util.List<UserResponse>> listUsers(@RequestParam(value = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        java.util.List<UserResponse> users = usuarioService.listUsers(includeDeleted);
        return ResponseEntity.ok(users);
    }

    // =========================
    // ADMIN/MODERATOR: Obtener usuario específico
    // =========================
    @GetMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<UserResponse> getUser(@PathVariable String cedula) {
        UserResponse user = usuarioService.getUserByCedula(cedula);
        return ResponseEntity.ok(user);
    }



    // =========================
    // ADMIN: Actualizar usuario
    // =========================
    @PutMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    public ResponseEntity<UserResponse> updateUser(@PathVariable String cedula, @Valid @RequestBody UserUpdateRequest request, BindingResult br, @RequestHeader("Authorization") String token) {
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(null); // Or handle properly
        }

        // Obtener rol del usuario autenticado
        String userRole = jwtService.getRole(token.replace("Bearer ", ""));

        // Validar permisos para estado
        if (request.getEstado() != null) {
            if ("MODERATOR".equals(userRole)) {
                // Moderator solo puede setear ACTIVO o SUSPENDIDO
                if (!request.getEstado().equals(Usuario.EstadoUsuario.ACTIVO) && !request.getEstado().equals(Usuario.EstadoUsuario.SUSPENDIDO)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
                }
            } else if ("ADMIN".equals(userRole)) {
                // Admin puede setear cualquier estado
            } else {
                // Otro rol no puede cambiar estado
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
            }
        }

        UserResponse updated = usuarioService.updateUser(cedula, request);
        return ResponseEntity.ok(updated);
    }

    // =========================
    // ADMIN: Eliminar usuario (lógico)
    // =========================
    @DeleteMapping("/{cedula}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable String cedula) {
        usuarioService.deleteUser(cedula);
        return ResponseEntity.ok(Map.of("message", "Usuario eliminado lógicamente"));
    }



    // =========================
    // Profile management (own profile)
    // =========================
    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getOwnProfile(@RequestHeader("Authorization") String token) {
        // Extraer cédula del JWT token
        String cedula = jwtService.extractCedula(token.replace("Bearer ", ""));
        UserResponse user = usuarioService.getUserByCedula(cedula);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateOwnProfile(@RequestHeader("Authorization") String token,
                                                         @Valid @RequestBody UserUpdateRequest request,
                                                         BindingResult br) {
        if (br.hasErrors()) {
            var first = br.getFieldErrors().get(0);
            return ResponseEntity.badRequest().body(null);
        }

        // Extraer cédula del JWT token
        String cedula = jwtService.extractCedula(token.replace("Bearer ", ""));
        UserResponse updated = usuarioService.updateOwnProfile(cedula, request);
        return ResponseEntity.ok(updated);
    }

    // =========================
    // Manejo de errores
    // =========================
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleRse(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }
}

package com.multicompany.sales_system.service.impl;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.multicompany.sales_system.dto.user.AdminCreateModeratorRequest;
import com.multicompany.sales_system.dto.user.LoginRequest;
import com.multicompany.sales_system.dto.user.LoginResponse;
import com.multicompany.sales_system.dto.user.RegisterRequest;
import com.multicompany.sales_system.dto.user.RegisterResponse;
import com.multicompany.sales_system.dto.user.UserResponse;
import com.multicompany.sales_system.dto.user.UserUpdateRequest;
import com.multicompany.sales_system.dto.user.VerifyEmailRequest;
import com.multicompany.sales_system.model.EmailVerification;
import com.multicompany.sales_system.model.Usuario;
import com.multicompany.sales_system.model.enums.UsuarioRole;
import com.multicompany.sales_system.repository.EmailVerificationRepository;
import com.multicompany.sales_system.repository.UsuarioRepository;
import com.multicompany.sales_system.security.JwtService;
import com.multicompany.sales_system.service.MailService;
import com.multicompany.sales_system.service.UsuarioService;
import com.multicompany.sales_system.service.VerificationCodeGenerator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepo;
    private final EmailVerificationRepository verifyRepo;
    private final VerificationCodeGenerator codeGen;
    private final MailService mail;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    @Value("${app.verification.code.ttl-minutes:15}")
    private int ttlMinutes;

    @Value("${app.verification.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.app-name:Multi-Company Sales System}")
    private String appName;

    // 🔑 Clave maestra desde .env
    @Value("${app.admin.key}")
    private String adminKey;

    // ==========================
    // Registro general (siempre USER)
    // ==========================
    @Override
    @Transactional
    public RegisterResponse registrar(RegisterRequest dto) {
        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepo.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCedula(dto.getCedula());
        u.setCorreo(dto.getCorreo().toLowerCase().trim());
        u.setContrasena(encoder.encode(dto.getContrasena()));
        u.setTelefono(dto.getTelefono());
        u.setDireccion(dto.getDireccion());
        u.setGenero(dto.getGenero());
        u.setRol(UsuarioRole.USER); // Asigna rol por defecto aquí

        Usuario saved = usuarioRepo.save(u);
        crearYEnviarCodigo(saved);

        return new RegisterResponse(
                saved.getIdUsuario(),
                saved.getCorreo(),
                saved.getRol().name(),
                "Usuario creado. Se envió un código a tu correo.");
    }

    // ==========================
    // Verificación de correo
    // ==========================
    @Override
    @Transactional
    public String verificarCorreo(VerifyEmailRequest req) {
        Usuario user = usuarioRepo.findByCorreoIgnoreCase(req.getCorreo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.isEmailVerificado()) {
            return "El correo ya está verificado";
        }

        EmailVerification ev = verifyRepo
                .findTopByUserIdUsuarioAndUsedFalseOrderByExpiresAtDesc(user.getIdUsuario())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solicita un nuevo código"));

        if (OffsetDateTime.now().isAfter(ev.getExpiresAt())) {
            ev.setUsed(true);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código expirado");
        }
        if (ev.getAttempts() >= maxAttempts) {
            ev.setUsed(true);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Demasiados intentos");
        }
        if (!ev.getCode().equals(req.getCode())) {
            ev.setAttempts(ev.getAttempts() + 1);
            verifyRepo.save(ev);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código incorrecto");
        }

        ev.setUsed(true);
        verifyRepo.save(ev);

        user.setEmailVerificado(true);
        usuarioRepo.save(user);
        return "Correo verificado correctamente";
    }

    // ==========================
    // Reenviar código
    // ==========================
    @Override
    @Transactional
    public void reenviarCodigo(String email) {
        Usuario user = usuarioRepo.findByCorreoIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (user.isEmailVerificado()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo ya está verificado");
        }

        crearYEnviarCodigo(user);
    }

    // ==========================
    // Check disponibilidad email
    // ==========================
    @Override
    public boolean correoDisponible(String email) {
        return !usuarioRepo.existsByCorreoIgnoreCase(email);
    }

    private void crearYEnviarCodigo(Usuario user) {
        verifyRepo.findTopByUserIdUsuarioAndUsedFalseOrderByExpiresAtDesc(user.getIdUsuario())
                .ifPresent(prev -> {
                    prev.setUsed(true);
                    verifyRepo.save(prev);
                });

        String code = codeGen.generateNumeric();

        EmailVerification ev = new EmailVerification();
        ev.setUser(user);
        ev.setCode(code);
        ev.setExpiresAt(OffsetDateTime.now().plusMinutes(ttlMinutes));
        verifyRepo.save(ev);

        // Usar el nuevo método del MailService
        mail.sendVerificationEmail(user.getCorreo(), user.getNombre(), code);
    }

    // ==========================
    // ADMIN: Crear MODERADOR
    // ==========================
    @Override
    @Transactional
    public RegisterResponse crearModerador(AdminCreateModeratorRequest dto) {
        // Valida adminKey del request contra el .env
        if (adminKey == null || !adminKey.equals(adminKey)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AdminKey inválida");
        }

        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepo.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCedula(dto.getCedula());
        u.setCorreo(dto.getCorreo().toLowerCase().trim());
        u.setContrasena(encoder.encode(dto.getContrasena()));
        u.setRol(UsuarioRole.MODERATOR);
        u.setTelefono(dto.getTelefono());
        u.setDireccion(dto.getDireccion());
        u.setGenero(dto.getGenero());

        Usuario saved = usuarioRepo.save(u);
        crearYEnviarCodigo(saved);

        return new RegisterResponse(
                saved.getIdUsuario(),
                saved.getCorreo(),
                saved.getRol().name(),
                "Usuario moderador creado. Se envió un código a su correo.");
    }

    // ==========================
    // Login
    // ==========================
    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepo.findByCorreoIgnoreCase(request.getCorreo())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Credenciales inválidas"));

        if (!encoder.matches(request.getContrasena(), usuario.getContrasena())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Credenciales inválidas");
        }

        // ✅ Generar JWT con rol USER/MODERATOR/ADMIN y estado
        String token = jwtService.generateToken(
                usuario.getIdUsuario(),
                usuario.getCedula(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.getEstado().name());

        return LoginResponse.builder()
                .id(usuario.getIdUsuario())
                .correo(usuario.getCorreo())
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .rol(usuario.getRol().name())
                .estado(usuario.getEstado().name())
                .emailVerificado(usuario.isEmailVerificado())
                .message(usuario.isEmailVerificado()
                        ? "Login OK"
                        : "Login OK, pero el correo no está verificado")
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse crearModerador(AdminCreateModeratorRequest dto, String adminKeyHeader) {
        // Valida adminKey del header contra el .env
        if (adminKey == null || !adminKey.equals(adminKeyHeader)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "AdminKey inválida");
        }

        if (usuarioRepo.existsByCorreoIgnoreCase(dto.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (usuarioRepo.existsByCedula(dto.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setApellido(dto.getApellido());
        u.setCedula(dto.getCedula());
        u.setCorreo(dto.getCorreo().toLowerCase().trim());
        u.setContrasena(encoder.encode(dto.getContrasena()));
        u.setRol(UsuarioRole.MODERATOR);
        u.setTelefono(dto.getTelefono());
        u.setDireccion(dto.getDireccion());
        u.setGenero(dto.getGenero());

        Usuario saved = usuarioRepo.save(u);
        crearYEnviarCodigo(saved);

        return new RegisterResponse(
                saved.getIdUsuario(),
                saved.getCorreo(),
                saved.getRol().name(),
                "Usuario moderador creado. Se envió un código a su correo.");
    }

    // ==========================
    // Recuperación de contraseña
    // ==========================
    @Transactional
    public void iniciarRecuperacionContrasena(String correo) {
        Usuario usuario = usuarioRepo.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        // Genera un código/token de recuperación
        String recoveryCode = codeGen.generateNumeric(); // O UUID.randomUUID().toString()
        usuario.setRecoveryCode(recoveryCode);
        usuario.setRecoveryCodeExpiresAt(OffsetDateTime.now().plusMinutes(ttlMinutes).toLocalDateTime());
        // Log para depuración
        System.out.println("[RECUPERACION] Usuario: " + usuario.getCorreo() + ", Código: " + recoveryCode + ", Expira: "
                + usuario.getRecoveryCodeExpiresAt());
        usuarioRepo.save(usuario);
        System.out.println("[RECUPERACION] Guardado en BD: " + usuario.getRecoveryCode() + " | "
                + usuario.getRecoveryCodeExpiresAt());

        // Construye el enlace de recuperación (ajusta la URL según tu frontend)
        String recoveryLink = "http://localhost:5173/reset-password?code=" + recoveryCode;

        mail.sendPasswordRecoveryEmail(usuario.getCorreo(), usuario.getNombre(), recoveryLink);
    }

    @Transactional
    public void resetPassword(com.multicompany.sales_system.dto.user.PasswordResetRequest request) {
        Usuario usuario = usuarioRepo.findByCorreoIgnoreCase(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getRecoveryCode() == null || usuario.getRecoveryCodeExpiresAt() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay código de recuperación activo");
        }
        if (!usuario.getRecoveryCode().equals(request.getRecoveryCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código de recuperación inválido");
        }
        if (usuario.getRecoveryCodeExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código de recuperación ha expirado");
        }
        // Cambia la contraseña y limpia el código
        usuario.setContrasena(encoder.encode(request.getNewPassword()));
        usuario.setRecoveryCode(null);
        usuario.setRecoveryCodeExpiresAt(null);
        usuarioRepo.save(usuario);
    }

    // ==========================
    // CRUD Operations
    // ==========================
    @Override
    public List<UserResponse> listUsers(boolean includeDeleted) {
        List<Usuario> usuarios;

        if (includeDeleted) {
            usuarios = usuarioRepo.findAll();
        } else {
            usuarios = usuarioRepo.findByEstadoNot(Usuario.EstadoUsuario.ELIMINADO);
        }

        return usuarios.stream()
                .filter(u -> !u.getRol().toString().equalsIgnoreCase("ADMIN")) // 🔥 excluye al admin
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserByCedula(String cedula) {
        Usuario usuario = usuarioRepo.findByCedula(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.EstadoUsuario.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario eliminado");
        }

        return mapToUserResponse(usuario);
    }

    @Override
    @Transactional
    public UserResponse updateUser(String cedula, UserUpdateRequest request) {
        Usuario usuario = usuarioRepo.findByCedula(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.EstadoUsuario.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario eliminado");
        }

        // Check for unique constraints if email or cedula changed
        if (!usuario.getCorreo().equalsIgnoreCase(request.getCorreo())
                && usuarioRepo.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (!usuario.getCedula().equals(request.getCedula()) && usuarioRepo.existsByCedula(request.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCedula(request.getCedula());
        usuario.setCorreo(request.getCorreo().toLowerCase().trim());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setGenero(request.getGenero());
        if (request.getRol() != null) {
            usuario.setRol(request.getRol());
        }
        if (request.getEstado() != null) {
            usuario.setEstado(request.getEstado());
        }

        Usuario saved = usuarioRepo.save(usuario);
        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public void deleteUser(String cedula) {
        Usuario usuario = usuarioRepo.findByCedula(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuario.setEstado(Usuario.EstadoUsuario.ELIMINADO);
        usuarioRepo.save(usuario);
    }

    @Override
    @Transactional
    public UserResponse updateOwnProfile(String cedula, UserUpdateRequest request) {
        Usuario usuario = usuarioRepo.findByCedula(cedula)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.EstadoUsuario.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario eliminado");
        }

        // Check for unique constraints if email or cedula changed
        if (!usuario.getCorreo().equalsIgnoreCase(request.getCorreo())
                && usuarioRepo.existsByCorreoIgnoreCase(request.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
        if (!usuario.getCedula().equals(request.getCedula()) && usuarioRepo.existsByCedula(request.getCedula())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cédula ya está registrada");
        }

        // Solo permitir actualizar campos no sensibles (excluir rol y estado)
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setCedula(request.getCedula());
        usuario.setCorreo(request.getCorreo().toLowerCase().trim());
        usuario.setTelefono(request.getTelefono());
        usuario.setDireccion(request.getDireccion());
        usuario.setGenero(request.getGenero());

        Usuario saved = usuarioRepo.save(usuario);
        return mapToUserResponse(saved);
    }

    @Override
    public UserResponse getUserById(Long id) {
        Usuario usuario = usuarioRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuario.getEstado() == Usuario.EstadoUsuario.ELIMINADO) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario eliminado");
        }

        return mapToUserResponse(usuario);
    }

    private UserResponse mapToUserResponse(Usuario usuario) {
        return new UserResponse(
                usuario.getIdUsuario(),
                usuario.getCedula(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getTelefono(),
                usuario.getDireccion(),
                usuario.getGenero(),
                usuario.getRol(),
                usuario.getEstado().name(),
                usuario.isEmailVerificado(),
                usuario.getFechaRegistro());
    }
}

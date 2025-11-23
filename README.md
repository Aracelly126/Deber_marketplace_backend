# MULTI-COMPANY_SALES_SYSTEM

## 1. Spring Web 🌐

**Uso:** Permite construir la API REST para manejar las funcionalidades del sistema (gestión de usuarios, productos, incidencias, reportes).

**Requerimiento cubierto:** Proveer servicios HTTP (GET, POST, PUT, DELETE) que serán consumidos por el frontend y clientes externos.

---

## 2. Spring Data JPA 💾

**Uso:** Manejo de persistencia con Hibernate y JPA para mapear entidades como Usuario, Producto, Servicio, Incidencia.

**Requerimiento cubierto:** Registro y administración de información en la base de datos (usuarios, productos, reportes, servicios, incidencias).

---

## 3. PostgreSQL Driver 🐘

**Uso:** Conexión de la aplicación con la base de datos PostgreSQL.

**Requerimiento cubierto:** Almacenamiento de datos estructurados de usuarios, productos, servicios, reportes e incidencias.

---

## 4. Validation (Bean Validation con Hibernate Validator) ✅

**Uso:** Validación de datos de entrada en entidades y DTOs (ej. correos válidos, contraseñas fuertes, campos requeridos).

**Requerimiento cubierto:**
- Validación de correo electrónico al registrar usuarios.
- Asegurar el uso de contraseñas seguras.
- Restricciones de datos obligatorios en productos y servicios.

---

## 5. Spring Security 🔐

**Uso:** Gestión de autenticación y autorización de usuarios.

**Requerimiento cubierto:**
- Control de acceso por roles (compradores, vendedores, moderadores, administradores).
- Seguridad en el acceso a la API.
- Encriptación de contraseñas con BCrypt.

---

## 6. Lombok ✨

**Uso:** Simplificación del código mediante anotaciones (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).

**Requerimiento cubierto:** Mejora de productividad del equipo y reducción de código repetitivo en entidades y DTOs.

---

## 7. Spring Boot DevTools ⚡

**Uso:** Proporciona reinicios automáticos y recarga en caliente en el entorno de desarrollo.

**Requerimiento cubierto:** Aumentar la eficiencia del equipo durante el desarrollo, reduciendo tiempos de prueba y depuración.

---

## 8. WebSocket 💬

**Uso:** Habilita comunicación en tiempo real entre clientes y el backend.

**Requerimiento cubierto:** Implementación del chat entre compradores y vendedores con soporte para valoración.

---

## 9. Java Mail Sender 📧

**Uso:** Envío de correos electrónicos a través de JavaMail.

**Requerimiento cubierto:**
- Verificación de correo electrónico al registrarse.
- Recuperación de contraseña.
- Notificaciones automáticas al usuario (ej. estado de incidencias o apelaciones).
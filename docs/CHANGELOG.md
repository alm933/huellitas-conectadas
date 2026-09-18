# Historial de cambios

## Versión 0.2.0 - Seguridad inicial

Fecha: 2026-09-18

### Agregado

- Entidad UsuarioEntity.
- Entidad TokenEntity.
- Enum UsuarioRol.
- Enum TokenType.
- UsuarioRepository.
- TokenRepository.
- PasswordEncoder con BCrypt.
- Registro de usuarios.
- Login de usuarios.
- Generación de tokens JWT.
- Configuración inicial de Spring Security.
- Validaciones para registro y login.
- DTOs RegistroRequest, LoginRequest, UsuarioResponse y AuthResponse.

### Endpoints agregados

#### Registrar usuario

```http
POST /api/v1/auth/register
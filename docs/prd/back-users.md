# Backend - Users

## DNA (Document Metadata)

| Field | Value |
|-------|-------|
| **Jira** | TBD |
| **Status** | Draft |
| **Author** | Pablo Garay |
| **Date** | 2026-05-16 |
| **Stakeholders** | Equipo TPI |
| **Version** | 0.1 |

---

## 1. Problem Statement

El sistema necesita gestionar usuarios con dos roles: ADMIN y USUARIO. Se requiere un CRUD administrativo de usuarios donde el admin pueda listar, ver, editar y eliminar usuarios registrados. El registro y login son responsabilidad de `back-auth-jwt`.

Los usuarios tienen: nombre, apellido, email (único), celular, contraseña encriptada y rol.

---

## 2. Context & Background

- **Depende de**: back-infrastructure (Base entity, exceptions, security)
- **Depende de**: back-auth-jwt (registro y login)
- **Solo ADMIN** puede acceder a CRUD de usuarios
- **Contraseñas**: almacenadas con BCrypt (responsabilidad de back-auth-jwt)
- **Respuestas**: NUNCA incluyen el campo password
- **Soft delete**: usuarios eliminados no pueden autenticarse

---

## 3. Goals & Success Metrics

### Primary Goal
CRUD de usuarios para administración, con validaciones y soft delete.

---

## 4. Target Users

- **Administrador**: gestiona usuarios del sistema

---

## 5. User Stories

### US-01 (HU-006): Registrar Usuario
> CUBIERTO por `back-auth-jwt` — el registro público se hace via `/api/auth/register`.

**Nota**: El admin NO puede crear usuarios via CRUD. Los usuarios se registran solos. Si se necesita crear un admin, se hace via seed data.

### US-02 (HU-007): Listar Usuarios
**As a** Administrador
**I want** ver todos los usuarios registrados
**So that** tener visibilidad de quién usa el sistema

**Acceptance Criteria:**
- [ ] GET `/api/v1/usuarios` con token ADMIN → 200
- [ ] Retorna array de usuarios sin contraseña
- [ ] No incluye usuarios con `eliminado = true`
- [ ] Lista vacía → 200 con `[]`
- [ ] Usuario sin ADMIN → 403

**Response:**
```json
[
  {
    "id": 1,
    "nombre": "Admin",
    "apellido": "Sistema",
    "email": "admin@admin.com",
    "celular": null,
    "rol": "ADMIN",
    "createdAt": "2026-05-16T12:00:00"
  }
]
```

### US-03 (HU-008): Obtener Usuario por ID
**As a** Administrador
**I want** ver detalle de un usuario específico
**So that** conocer su información

**Acceptance Criteria:**
- [ ] GET `/api/v1/usuarios/1` con token ADMIN → 200
- [ ] No incluye password
- [ ] ID no existe → 404
- [ ] Usuario eliminado → 404
- [ ] Sin ADMIN → 403

### US-04 (HU-009): Actualizar Usuario
**As a** Administrador
**I want** modificar datos de un usuario
**So that** mantener información actualizada

**Acceptance Criteria:**
- [ ] PUT `/api/v1/usuarios/1` con token ADMIN → 200
- [ ] Actualización parcial (solo campos enviados)
- [ ] Si se cambia email, debe ser único
- [ ] Si se cambia password, se encripta con BCrypt
- [ ] Puede cambiar el rol de un usuario
- [ ] ID no existe → 404
- [ ] Sin ADMIN → 403

**Request:**
```json
{
  "nombre": "Juan Actualizado",
  "email": "nuevo@email.com"
}
```

### US-05 (HU-010): Eliminar Usuario (Soft Delete)
**As a** Administrador
**I want** eliminar un usuario del sistema
**So that** mantener la base de usuarios limpia

**Acceptance Criteria:**
- [ ] DELETE `/api/v1/usuarios/1` con token ADMIN → 204
- [ ] Soft delete: `eliminado = true`
- [ ] Pedidos del usuario NO se eliminan
- [ ] Usuario no puede autenticarse después de eliminado
- [ ] ID no existe → 404
- [ ] Sin ADMIN → 403

---

## 6. Functional Requirements

### FR-01: Endpoints CRUD Admin
- `GET /api/v1/usuarios` — listar (ADMIN)
- `GET /api/v1/usuarios/{id}` — obtener (ADMIN)
- `PUT /api/v1/usuarios/{id}` — actualizar (ADMIN)
- `DELETE /api/v1/usuarios/{id}` — eliminar (ADMIN)

### FR-02: Reglas de Negocio
- Password nunca en responses
- Email único entre usuarios activos
- Actualización parcial (merge de campos)
- Si se actualiza password, encriptar con BCrypt

---

## 7. Non-Functional Requirements

| Category | ID | Requirement |
|----------|--------|-------------|
| **Security** | NFR-01 | Password nunca en responses ni logs |
| **Security** | NFR-02 | Solo ADMIN accede a estos endpoints |
| **Integrity** | NFR-03 | Email único en BD |

---

## 8. User Flow

```mermaid
flowchart TD
    A[Admin en Panel Usuarios] --> B[GET /api/v1/usuarios]
    B --> C[Tabla con usuarios]
    C --> D{Selecciona acción}
    D -->|Ver detalle| E[Abre modal con datos]
    D -->|Editar| F[Abre formulario precargado]
    D -->|Eliminar| G[Confirmación]
    F --> H[PUT /api/v1/usuarios/{id}]
    H --> I{Actualizado?}
    I -->|Sí| J[Toast éxito + refresh]
    I -->|No| K[Toast error]
    G --> L[DELETE /api/v1/usuarios/{id}]
    L --> J
```

---

## 9. API / Interface Contracts

### GET `/api/v1/usuarios`
**Response 200:**
```json
[
  {
    "id": 1,
    "nombre": "Admin",
    "apellido": "Sistema",
    "email": "admin@admin.com",
    "celular": "1234567890",
    "rol": "ADMIN",
    "createdAt": "2026-05-16T12:00:00"
  }
]
```

### PUT `/api/v1/usuarios/{id}`
**Request:**
```json
{
  "nombre": "string (opcional, 2-50)",
  "apellido": "string (opcional, 2-50)",
  "email": "string (opcional, email, único)",
  "celular": "string (opcional, máx 20)",
  "password": "string (opcional, min 6)",
  "rol": "ADMIN | USUARIO (opcional)"
}
```

**Response 200:** Mismo formato que GET.

---

## 10. Data Model

### Usuario (extiende Base)

| Campo | Tipo | Constraint | Descripción |
|-------|------|------------|-------------|
| id | Long | PK, auto | Heredado de Base |
| nombre | String | NOT NULL, 2-50 | Nombre del usuario |
| apellido | String | NOT NULL, 2-50 | Apellido del usuario |
| email | String | NOT NULL, unique | Email de login |
| celular | String | nullable, máx 20 | Teléfono de contacto |
| password | String | NOT NULL | Hash BCrypt |
| rol | Rol | NOT NULL | Enum: ADMIN, USUARIO |
| eliminado | Boolean | default false | Soft delete |

### Rol (enum)
```java
public enum Rol {
    ADMIN,
    USUARIO
}
```

---

## 11. DoD (Definition of Done)

### Testing
- [ ] Tests: listar usuarios retorna array sin passwords
- [ ] Tests: obtener usuario por ID retorna datos sin password
- [ ] Tests: actualizar usuario (campos individuales, email duplicado)
- [ ] Tests: eliminar usuario (soft delete, no puede autenticarse)
- [ ] Tests: usuario sin ADMIN → 403 en todos los endpoints
- [ ] Tests: usuario eliminado no aparece en listados

### Código
- [ ] Entidad `Usuario` con herencia de `Base` y enum `Rol`
- [ ] `UsuarioRepository` hereda de `BaseRepository`
- [ ] DTOs: `UsuarioRequest`, `UsuarioResponse`
- [ ] `UsuarioService` con validaciones de negocio
- [ ] `UsuarioController` con endpoints REST
- [ ] `@PreAuthorize("hasRole('ADMIN')")` en todos los endpoints
- [ ] Password no expuesto en ningún response

---

## 12. Out of Scope

- Que un usuario pueda actualizar sus propios datos (perfil propio)
- Roles dinámicos (solo ADMIN y USUARIO fijos)

---

## 13. Dependencies

| Dependency | Type |
|------------|------|
| back-infrastructure | Internal (Base, exceptions, security) |

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-05-16 | Pablo Garay | Initial draft |

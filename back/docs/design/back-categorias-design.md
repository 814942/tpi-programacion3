# Design: CRUD Categorías (feature/back-categorias)

## Technical Approach

CRUD completo de categorías siguiendo el patrón exacto del módulo Usuario existente. Capa estricta: Entity → Repository → Service → Controller. Soft delete vía `Base.eliminado`, unique constraint sobre nombre con partial unique index en DB, y autorización granular vía `@PreAuthorize` a nivel de método.

## Architecture Decisions

### Decision: `@PreAuthorize` a nivel de método (no clase)

**Choice**: `@PreAuthorize("hasRole('ADMIN')")` solo en POST/PUT/DELETE del controller. GET sin anotación de clase pero protegido por `.anyRequest().authenticated()` en SecurityConfig.

**Alternatives considered**: Clase completa con `@PreAuthorize("hasRole('ADMIN')")` como UsuarioController.

**Rationale**: La propuesta requiere que los GET sean accesibles para cualquier autenticado (USUARIO, ADMIN). SecurityConfig ya exige `authenticated()` para todo excepto `/api/auth/**`. Si pusiera `@PreAuthorize` a nivel de clase, los GET también requerirían ADMIN, contradiciendo el requerimiento.

### Decision: PUT con semántica PATCH (partial update)

**Choice**: Endpoint nominado `PUT` pero implementa actualización parcial (null checks en service), exactamente como `UsuarioService.update`.

**Alternatives considered**: Usar `PATCH` (más correcto REST), o usar `PUT` con reemplazo total.

**Rationale**: La propuesta documenta explícitamente "PUT con null checks". El PRD dice `PUT` pero pide partial update. Consistencia con `UpdateUsuarioRequest` que también permite nulls. Se documenta claramente en el controller y en OpenAPI.

### Decision: No `@AssertTrue` en UpdateCategoriaRequest

**Choice**: Permitir enviar solo `nombre` sin requerir al menos un campo.

**Alternatives considered**: `@AssertTrue hasAnyFieldToUpdate()` como en `UpdateUsuarioRequest`.

**Rationale**: `UpdateUsuarioRequest` lo tiene porque es PATCH estricto. En este caso el endpoint es PUT con semántica parcial — enviar body vacío podría ser un error del cliente, pero no se bloquea a nivel de DTO. El service igual no modifica nada si todos los campos son null. Simplifica el código y evita edge cases con `imagen` opcional.

## Data Flow

```
Cliente HTTP → CategoriaController → CategoriaService → CategoriaRepository → DB PostgreSQL
                    │                      │                    │
                    ↓                      ↓                    ↓
              DTO validation         Business logic        Soft-delete aware
              (Jakarta @Valid)       (unique nombre)       (BaseRepository)
```

```
GET /api/v1/categorias
  → CategoriaService.findAll()
    → CategoriaRepository.findAll()  [WHERE eliminado = false]
    → map to CategoriaResponse list

POST /api/v1/categorias (ADMIN)
  → CategoriaRequest validation
  → CategoriaService.create(request)
    → validate nombre unique
    → CategoriaRepository.save(categoria)
    → return CategoriaResponse 201

DELETE /api/v1/categorias/{id} (ADMIN)
  → CategoriaService.deleteById(id)
    → findByIdOrThrow (404 if not found or soft-deleted)
    → CategoriaRepository.deleteById(id)  [SET eliminado = true]
    → return 204
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `model/Categoria.java` | Create | Entity extends Base |
| `repository/CategoriaRepository.java` | Create | CRUD + existsByNombre |
| `dto/request/CategoriaRequest.java` | Create | Create DTO record |
| `dto/request/UpdateCategoriaRequest.java` | Create | Update DTO record |
| `dto/response/CategoriaResponse.java` | Create | Response DTO record |
| `service/CategoriaService.java` | Create | CRUD business logic |
| `controller/CategoriaController.java` | Create | REST endpoints |
| `resources/db/migration/V2__categorias_unique_nombre_activo.sql` | Create | Partial unique index |
| `service/CategoriaServiceTest.java` | Create | Unit tests (Mockito) |
| `controller/CategoriaControllerTest.java` | Create | Integration tests (MockMvc) |

## Data Model

### Categoria (extends Base)

| Field | Type | DB Column | Constraints |
|-------|------|-----------|-------------|
| `nombre` | `String` | `nombre` | `nullable = false, length = 100` |
| `descripcion` | `String` | `descripcion` | `length = 500` (nullable) |
| `imagen` | `String` | `imagen` | `length = 500` (nullable) |
| *(inherited)* `id` | `Long` | `id` | PK, auto-increment |
| *(inherited)* `eliminado` | `boolean` | `eliminado` | Default false |
| *(inherited)* `createdAt` | `LocalDateTime` | `created_at` | Auto-set |
| *(inherited)* `updatedAt` | `LocalDateTime` | `updated_at` | Auto-update |
| *(inherited)* `version` | `Long` | `version` | `@Version` |

### Table mapping

```java
@Entity
@Table(name = "categorias")
public class Categoria extends Base {
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(length = 500)
    private String imagen;
}
```

### Unique constraint

Partial unique index en DB (doble safety con validación en service):

```sql
CREATE UNIQUE INDEX IF NOT EXISTS ux_categorias_nombre_activo
    ON categorias (nombre)
    WHERE eliminado = false;
```

## API Design

Base URL: `/api/v1/categorias`

### Endpoints

| Method | Path | Auth | Status | Description |
|--------|------|------|--------|-------------|
| GET | `/` | Any auth | 200 | Lista categorías activas |
| GET | `/{id}` | Any auth | 200 / 404 | Obtener por ID |
| POST | `/` | ADMIN | 201 / 400 | Crear categoría |
| PUT | `/{id}` | ADMIN | 200 / 404 / 400 | Actualizar parcial |
| DELETE | `/{id}` | ADMIN | 204 / 404 | Soft delete |

### DTO Schemas

**CategoriaRequest** (POST):

```java
public record CategoriaRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La descripción debe tener hasta 500 caracteres")
    String descripcion,

    @Size(max = 500, message = "La URL de imagen debe tener hasta 500 caracteres")
    @Pattern(regexp = "^(http|https)://.*", message = "La imagen debe ser una URL válida")
    String imagen
) {}
```

**UpdateCategoriaRequest** (PUT):

```java
public record UpdateCategoriaRequest(
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    String nombre,

    @Size(max = 500, message = "La descripción debe tener hasta 500 caracteres")
    String descripcion,

    @Size(max = 500, message = "La URL de imagen debe tener hasta 500 caracteres")
    @Pattern(regexp = "^(http|https)://.*", message = "La imagen debe ser una URL válida")
    String imagen
) {}
```

> Nota: `UpdateCategoriaRequest` no tiene `@AssertTrue` — los campos son todos nullable y el service solo actualiza los no-null.

**CategoriaResponse**:

```java
public record CategoriaResponse(
    Long id,
    String nombre,
    String descripcion,
    String imagen,
    LocalDateTime createdAt
) {}
```

### HTTP Response Matrix

| Scenario | Status | Body |
|----------|--------|------|
| GET list (success) | 200 | `CategoriaResponse[]` |
| GET by id (found) | 200 | `CategoriaResponse` |
| GET by id (not found) | 404 | `ErrorResponse{error: "resource_not_found"}` |
| POST (created) | 201 | `CategoriaResponse` |
| POST (nombre duplicado) | 400 | `ErrorResponse{error: "business_error"}` |
| POST (validation error) | 400 | `ErrorResponse{error: "validation_error", fields: {...}}` |
| PUT (updated) | 200 | `CategoriaResponse` |
| PUT (not found) | 404 | `ErrorResponse{error: "resource_not_found"}` |
| PUT (nombre duplicado) | 400 | `ErrorResponse{error: "business_error"}` |
| DELETE (deleted) | 204 | No body |
| DELETE (not found) | 404 | `ErrorResponse{error: "resource_not_found"}` |
| Any (not auth) | 401 | `ErrorResponse{error: "unauthorized"}` |
| Any (forbidden, non-ADMIN on write) | 403 | `ErrorResponse{error: "forbidden"}` |

## Service Design

### CategoriaService

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // READ — no @Transactional needed
    public List<CategoriaResponse> findAll();

    public CategoriaResponse findById(Long id);

    // WRITE — @Transactional
    @Transactional
    public CategoriaResponse create(CategoriaRequest request);
    // 1. Validate nombre unique → throw BusinessException if exists
    // 2. Build Categoria entity via @SuperBuilder
    // 3. save()
    // 4. Log and return response

    @Transactional
    public CategoriaResponse update(Long id, UpdateCategoriaRequest request);
    // 1. findByIdOrThrow → ResourceNotFoundException if not found
    // 2. Partial update: null checks on each field
    // 3. If nombre changed → validate unique (excluir propio ID)
    // 4. save()
    // 5. Log and return response

    @Transactional
    public void deleteById(Long id);
    // 1. findByIdOrThrow → ResourceNotFoundException if not found
    // 2. deleteById() → soft delete via BaseRepository
    // 3. Log
}
```

### Business validation: nombre unique

```java
// Create
if (categoriaRepository.existsByNombreAndEliminadoFalse(request.nombre())) {
    throw new BusinessException("El nombre '" + request.nombre() + "' ya está en uso");
}

// Update (edge case: mismo nombre → skip validation)
if (request.nombre() != null && !request.nombre().equals(usuario.getNombre())) {
    if (categoriaRepository.existsByNombreAndEliminadoFalse(request.nombre())) {
        throw new BusinessException("El nombre '" + request.nombre() + "' ya está en uso");
    }
    usuario.setNombre(request.nombre());
}
```

### Delete: 204 No Content

A diferencia de `UsuarioController.deleteById` que devuelve 200 con body, el endpoint DELETE de categorías devuelve `204 No Content` sin body. Esto es más estándar REST. Se elimina la verificación de `currentUserId` porque no aplica (no se borra a sí mismo).

## Security Design

### Configuration

- **SecurityConfig** no cambia — `.anyRequest().authenticated()` ya cubre `/api/v1/categorias/**`
- **Auth endpoints** exentos: `/api/auth/**` (sin cambios)
- **Swagger** exento: `/swagger-ui/**`, `/api-docs/**` (sin cambios)

### @PreAuthorize placement

```java
@RestController
@RequestMapping("/api/v1/categorias")
@Slf4j
@RequiredArgsConstructor
public class CategoriaController {

    // GET endpoints — sin @PreAuthorize, cualquier auth
    @GetMapping
    public ResponseEntity<List<CategoriaResponse>> findAll();

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponse> findById(@PathVariable Long id);

    // POST — solo ADMIN
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> create(@RequestBody @Valid CategoriaRequest request);

    // PUT — solo ADMIN
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoriaResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateCategoriaRequest request);

    // DELETE — solo ADMIN
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteById(@PathVariable Long id);
}
```

### Auth flow

1. Request → `JwtAuthenticationFilter` extrae y valida JWT del header `Authorization: Bearer <token>`
2. Si JWT válido → `SecurityContext` tiene `Authentication` con roles
3. Endpoint match → SecurityConfig exige `authenticated()` — pasa porque tiene JWT válido
4. Si endpoint tiene `@PreAuthorize("hasRole('ADMIN')")` → verifica `ROLE_ADMIN` en authorities
5. Si no tiene el rol → `AccessDeniedException` → GlobalExceptionHandler → 403

## Error Handling

| Escenario | Excepción | Código | Error Code |
|-----------|-----------|--------|------------|
| Categoría no encontrada | `ResourceNotFoundException` | 404 | `resource_not_found` |
| Nombre duplicado (create) | `BusinessException` | 400 | `business_error` |
| Nombre duplicado (update) | `BusinessException` | 400 | `business_error` |
| Validación de campos falla | `MethodArgumentNotValidException` | 400 | `validation_error` |
| Request body inválido | `HttpMessageNotReadableException` | 400 | `bad_request` |
| Auth token faltante/inválido | EntryPoint | 401 | `unauthorized` |
| No autorizado (rol) | `AccessDeniedException` | 403 | `forbidden` |
| Violación unique index DB | `DataIntegrityViolationException` | 409 | `conflict` |
| Error interno | `Exception` genérica | 500 | `internal_error` |

> Todos estos handlers YA existen en `GlobalExceptionHandler` — no se requiere modificación.

## Testing Strategy

### Unit Tests — `CategoriaServiceTest`

Framework: JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`), AssertJ assertions.

| Test Group | Scenarios |
|-----------|-----------|
| `FindAllTests` | Lista con categorías, lista vacía |
| `FindByIdTests` | Encontrada, no encontrada (404) |
| `CreateTests` | Creación exitosa, nombre duplicado, validación (campos nulos/vacíos) |
| `UpdateTests` | Actualización completa, actualización parcial (solo nombre, solo descripción), nombre duplicado (mismo nombre permite, otro nombre duplicado rechaza), no encontrada (404) |
| `DeleteTests` | Soft delete exitoso, no encontrada (404) |

### Integration Tests — `CategoriaControllerTest`

Framework: Spring Boot Test (`@SpringBootTest` + `@AutoConfigureMockMvc`), `@WithMockUser`, MockMvc.

| Test Group | Scenarios |
|-----------|-----------|
| `FindAllTests` | 200 con lista (ADMIN), 200 con lista (USUARIO), 200 lista vacía |
| `FindByIdTests` | 200 encontrada (ADMIN), 200 encontrada (USUARIO), 404 no encontrada |
| `CreateTests` | 201 creada, 400 nombre duplicado, 400 validación, 403 USUARIO no autorizado |
| `UpdateTests` | 200 actualizada, 404 no encontrada, 400 nombre duplicado, 403 USUARIO no autorizado |
| `DeleteTests` | 204 eliminada, 404 no encontrada, 403 USUARIO no autorizado |

### Key test patterns (from existing codebase)

```java
// Service test — Mockito + AssertJ
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock private CategoriaRepository categoriaRepository;
    @InjectMocks private CategoriaService categoriaService;

    @Test
    void shouldCreateCategoria() {
        when(categoriaRepository.existsByNombreAndEliminadoFalse("Bebidas")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenReturn(categoria);

        CategoriaResponse result = categoriaService.create(request);

        assertThat(result.nombre()).isEqualTo("Bebidas");
    }
}

// Controller test — Spring Boot + MockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoriaControllerTest {
    @Autowired private MockMvc mockMvc;
    @MockBean private CategoriaService categoriaService;

    @Test
    @WithMockUser(roles = "USUARIO")
    void shouldReturn200WhenUserListsCategories() throws Exception {
        when(categoriaService.findAll()).thenReturn(List.of(response));
        mockMvc.perform(get("/api/v1/categorias"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void shouldReturn403WhenUserCreatesCategory() throws Exception {
        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
            .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn204WhenAdminDeletesCategory() throws Exception {
        doNothing().when(categoriaService).deleteById(1L);
        mockMvc.perform(delete("/api/v1/categorias/1")
                .with(authentication(adminAuthentication(2L))))
            .andExpect(status().isNoContent());
    }
}
```

## Migration

### V2__categorias_unique_nombre_activo.sql

```sql
CREATE UNIQUE INDEX IF NOT EXISTS ux_categorias_nombre_activo
    ON categorias (nombre)
    WHERE eliminado = false;
```

Sigue exactamente el mismo patrón que `V1__usuarios_email_unique_activos.sql` (sin necesidad de dropear constraint previo porque `categorias` es tabla nueva sin unique previo).

Dependencia: esta migration corre después de que Hibernate crea la tabla `categorias` (vía `ddl-auto=update` + Flyway baseline).

## Open Questions

- [ ] **Ninguna.** El diseño cubre todos los escenarios identificados en la propuesta. La implementación puede proceder sin blockers.

---

**Change**: feature/back-categorias
**Key Decisions**: 3 (método-level PreAuthorize, PUT con semántica parcial, sin @AssertTrue en update DTO)
**Files Affected**: 10 new files, 0 modified
**Testing Strategy**: Unit (service) + Integration (controller) con Mockito y MockMvc

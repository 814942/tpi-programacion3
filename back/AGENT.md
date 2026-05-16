# Backend — API REST con Spring Boot 3.x + Java 17

**Parte del monorepo `tpi/`** junto a `client/`. Cada servicio tiene su propio AGENT.md.

## Stack

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.x |
| Build | Gradle (Kotlin DSL o Groovy) |
| Base de datos | PostgreSQL 15+ |
| Persistencia | Spring Data JPA + Hibernate |
| Migraciones | Hibernate `ddl-auto=update` (dev) |
| Seguridad | Spring Security + JWT (jjwt) |
| Documentación | SpringDoc OpenAPI (Swagger UI) |
| Testing | JUnit 5 + Mockito + Spring Boot Test |

---

## Estructura de Directorios

```
back/
├── build.gradle                  # Dependencias y config de build
├── settings.gradle               # Configuración del proyecto
├── gradlew / gradlew.bat         # Wrapper de Gradle
└── src/
    ├── main/
    │   ├── java/com/foodstore/
    │   │   ├── FoodstoreApplication.java   # Entry point
    │   │   ├── model/                      # Entidades JPA
    │   │   │   ├── Base.java               # Clase abstracta base
    │   │   │   ├── Usuario.java
    │   │   │   ├── Categoria.java
    │   │   │   ├── Producto.java
    │   │   │   ├── Pedido.java
    │   │   │   ├── DetallePedido.java
    │   │   │   └── enums/
    │   │   │       ├── Rol.java
    │   │   │       ├── Estado.java
    │   │   │       └── FormaPago.java
    │   │   ├── repository/                 # Repositorios JPA
    │   │   │   ├── BaseRepository.java
    │   │   │   └── ...Repository.java
    │   │   ├── dto/                        # Data Transfer Objects
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   ├── service/                    # Lógica de negocio
    │   │   │   ├── impl/
    │   │   │   └── ...Service.java
    │   │   ├── controller/                 # REST Controllers
    │   │   │   ├── AuthController.java
    │   │   │   ├── CategoriaController.java
    │   │   │   └── ...
    │   │   ├── exception/                  # Excepciones + handler
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   └── BusinessException.java
    │   │   ├── security/                   # JWT + Security Config
    │   │   │   ├── SecurityConfig.java
    │   │   │   ├── JwtProvider.java
    │   │   │   ├── JwtAuthFilter.java
    │   │   │   └── CustomUserDetailsService.java
    │   │   └── config/                     # Configuraciones
    │   │       ├── DataLoader.java
    │   │       └── CorsConfig.java
    │   └── resources/
    │       ├── application.properties       # Config principal
    │       └── application-dev.properties   # Config desarrollo
    └── test/
        └── java/com/foodstore/
            ├── controller/                  # Tests de integración
            ├── service/                     # Tests unitarios
            └── security/                    # Tests de JWT
```

---

## Convenciones de Código

### Nomenclatura

| Tipo | Convención | Ejemplo |
|------|------------|---------|
| Clases | PascalCase | `CategoriaService`, `ProductoController` |
| Métodos | camelCase | `findAll()`, `findByIdOrThrow()` |
| Variables | camelCase | `categoriaService`, `productoRepository` |
| Constantes | UPPER_SNAKE | `API_BASE_PATH`, `JWT_EXPIRATION` |
| Enums | PascalCase | `Rol.ADMIN`, `Estado.PENDIENTE` |
| Paquetes | minúsculas | `com.foodstore.service` |
| Archivos | PascalCase | `GlobalExceptionHandler.java` |
| Tablas BD | snake_case (minúsculas) | `usuarios`, `detalle_pedido` |
| Columnas BD | snake_case | `created_at`, `forma_pago` |

### API REST

```
Base path: /api/v1

/auth      → POST login, POST register  (público)
/categorias → CRUD completo              (ADMIN escritura, auth lectura)
/productos → CRUD completo               (ADMIN escritura, auth lectura)
/pedidos   → CRUD completo               (ADMIN todo, usuario solo propio)
/usuarios  → CRUD completo               (solo ADMIN)
/admin/**  → Rutas exclusivas ADMIN
```

### Formato de Respuesta

**Éxito:**
```json
{
  "id": 1,
  "nombre": "Hamburguesas",
  ...
}
```

**Error (consistente):**
```json
{
  "error": "resource_not_found",
  "message": "Categoría con id 999 no encontrada",
  "status": 404,
  "timestamp": "2026-05-16T12:00:00",
  "path": "/api/v1/categorias/999"
}
```

**Error de validación:**
```json
{
  "error": "validation_error",
  "message": "Error de validación",
  "status": 400,
  "fields": {
    "nombre": "El nombre es obligatorio"
  }
}
```

### Paginación (cuando aplica)

```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5
}
```

---

## Reglas de Arquitectura

### 1. Capas
```
Controller → Service → Repository
     ↓           ↓
    DTOs      Entity
```

- **Controller**: solo recibe request, delega a Service, retorna DTOs
- **Service**: lógica de negocio, validaciones, transacciones
- **Repository**: acceso a datos, queries personalizadas
- **NUNCA** llamar Repository desde Controller
- **NUNCA** exponer Entities en la API (siempre usar DTOs)

### 2. Validaciones
- Validaciones de formato/campos en DTOs con Jakarta Validation
- Validaciones de negocio en Service (existencia, stock, etc.)
- `@Valid` en los controllers para validar request bodies

### 3. Transacciones
- `@Transactional` en Service para operaciones que modifican múltiples entidades
- Creación de pedido = transaccional (todo o nada)
- Optimistic locking con `@Version`

### 4. Seguridad JWT
- `JwtAuthFilter` intercepta todos los requests
- Rutas públicas configuradas en `SecurityConfig`
- `@PreAuthorize("hasRole('ADMIN')")` en endpoints de admin
- CSRF deshabilitado, sesiones STATELESS
- Estados HTTP: 401 (no autenticado), 403 (no autorizado)

### 5. Soft Delete
- Todas las entidades heredan `eliminado` de `Base`
- `BaseRepository` filtra `eliminado = false` automáticamente
- Para eliminar: setear `eliminado = true`, no borrar físicamente
- GET por ID de eliminado → 404

### 6. Excepciones
- `ResourceNotFoundException` → 404
- `BusinessException` → 400 (validación de negocio)
- `AccessDeniedException` → 403 (Spring Security)
- `MethodArgumentNotValidException` → 400 (validación DTO)
- `Exception` genérica → 500
- Todas manejadas por `GlobalExceptionHandler`

### 7. DTOs
- **Request DTO**: campos de entrada + validaciones Jakarta
- **Response DTO**: datos de salida sin password ni datos internos
- Usar `record` de Java 17 para DTOs inmutables
- No exponer relaciones lazy en DTOs (cargar lo necesario)

---

## Estructura típica de un módulo

```java
// 1. Entity
@Entity @Table(name = "categorias")
public class Categoria extends Base {
    private String nombre;
    private String descripcion;
    private String imagen;
}

// 2. Repository
public interface CategoriaRepository extends BaseRepository<Categoria, Long> {
    // Queries personalizadas acá
}

// 3. Request DTO
public record CategoriaRequest(
    @NotBlank @Size(min = 2, max = 100) String nombre,
    @Size(max = 500) String descripcion,
    String imagen
) {}

// 4. Service
@Service @Transactional
public class CategoriaService {
    // Lógica de negocio
}

// 5. Controller
@RestController @RequestMapping("/api/v1/categorias")
public class CategoriaController {
    // Endpoints REST
}
```

---

## Reglas Importantes

### SÍ
- ✅ Java records para DTOs
- ✅ `@Builder` + `@SuperBuilder` en entities
- ✅ `@PreAuthorize` para control de acceso
- ✅ `@Valid` en request bodies
- ✅ `@Transactional` en operaciones de escritura
- ✅ `ResponseEntity` para respuestas HTTP explícitas
- ✅ Logging con SLF4J (Lombok `@Slf4j`)
- ✅ Tests unitarios + tests de integración
- ✅ BigDecimal para precios y totales
- ✅ Soft delete en todas las entidades

### NO
- ❌ `ResponseEntity` genérico sin tipo (usar `<T>`)
- ❌ `@Autowired` (usar constructor injection con `@RequiredArgsConstructor`)
- ❌ Exponer entities directamente en la API
- ❌ `@GetMapping` que modifique datos
- ❌ Contraseñas en logs, responses, o toString
- ❌ Queries nativas si se puede evitar (usar JPQL/Criteria)
- ❌ `@Transactional(readOnly = true)` en queries de solo lectura (buena práctica)

---

## Dependencias (build.gradle)

```groovy
dependencies {
    // Spring Boot
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.5'
    
    // OpenAPI
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0'
    
    // DB
    runtimeOnly 'org.postgresql:postgresql'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // Testing
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
    testImplementation 'com.h2database:h2' // DB para tests
}
```

---

## Scripts

```bash
# Development
./gradlew bootRun                    # Iniciar servidor en :8080

# Build
./gradlew build                      # Compilar + tests

# Testing
./gradlew test                       # Todos los tests
./gradlew test --tests "*CategoriaServiceTest"  # Test específico

# Type check (compila sin ejecutar)
./gradlew compileJava
```

---

## Perfiles

- `dev` (default): PostgreSQL local, `ddl-auto=update`
- `test`: H2 en memoria (para tests de integración)
- `prod`: PostgreSQL con pool de conexiones

---

## Variables de Entorno

```properties
# application.properties (base)
spring.profiles.active=dev

# application-dev.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/foodstore
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

app.jwt.secret=clave-secreta-para-jwt-mas-larga-que-256-bits-por-favor
app.jwt.expiration=86400000

springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui/index.html
```

---

## Testing

```java
// Test unitario de service
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {
    @Mock private CategoriaRepository repository;
    @InjectMocks private CategoriaService service;
    
    @Test
    void crearCategoria_ConDatosValidos_RetornaResponse() {
        // given
        var request = new CategoriaRequest("Nueva", "Desc", null);
        var entity = Categoria.builder().nombre("Nueva").build();
        when(repository.save(any())).thenReturn(entity);
        
        // when
        var response = service.crear(request);
        
        // then
        assertThat(response.nombre()).isEqualTo("Nueva");
    }
}

// Test de integración de controller
@SpringBootTest @AutoConfigureMockMvc
class CategoriaControllerTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void crearCategoria_ConDatosValidos_Retorna201() throws Exception {
        var request = """
            { "nombre": "Hamburguesas", "descripcion": "Desc" }
        """;
        
        mockMvc.perform(post("/api/v1/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated());
    }
}
```

---

## Editor Config

```ini
[*.java]
indent_style = tab
indent_size = 4
continuation_indent_size = 8
```

---

## PRDs Relacionados

Ver `docs/prd/` para la especificación completa:
- `back-infrastructure.md` — Base, JWT, exceptions, OpenAPI, CORS
- `back-auth-jwt.md` — Login/register endpoints
- `back-categories.md` — CRUD categorías
- `back-users.md` — CRUD usuarios
- `back-products.md` — CRUD productos
- `back-orders.md` — Pedidos con snapshot y stock

# Food Store - TPI Programación III UTN

> Sistema de delivery de comidas saludables — Full Stack

| Alumno | Pablo Garay |
|--------|-------------|
| **Institución** | Universidad Tecnológica Nacional |
| **Carrera** | Tecnicatura Universitaria en Programación |
| **Materia** | Programación III |

---

## Stack Tecnológico

| Capa | Tecnología |
|------|------------|
| **Frontend** | TypeScript 5 (strict), Vite 5, HTML5, CSS3 — **0 librerías externas** |
| **Backend** | Java 17, Spring Boot 3.4, Gradle |
| **Base de datos** | PostgreSQL 15+ / H2 (test) |
| **Autenticación** | JWT Stateless (jjwt 0.12.5) + BCrypt |
| **API** | RESTful, documentada con OpenAPI / Swagger UI |
| **Testing Backend** | JUnit 5 + Mockito + MockMvc (21 tests) |
| **Testing Frontend** | Vitest + jsdom (14 tests) |

---

## Estructura del Proyecto

```
tpi/
├── client/                          # Frontend SPA (TypeScript + Vite)
│   ├── AGENT.md                     # Convenciones de código frontend
│   ├── index.html                   # Landing page
│   ├── src/
│   │   ├── main.ts                  # Entry point + route guard
│   │   ├── style.css                # Estilos globales
│   │   ├── types/                   # Interfaces TypeScript
│   │   ├── utils/                   # Utilidades (auth, api, router, header)
│   │   └── pages/
│   │       ├── auth/                # Login, registro, forbidden
│   │       ├── store/               # Detalle de producto
│   │       ├── client/              # Catálogo, carrito, checkout, pedidos
│   │       └── admin/               # Dashboard, CRUDs, gestión
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── back/                            # Backend API REST (Spring Boot)
│   ├── AGENT.md                     # Convenciones de código backend
│   ├── build.gradle
│   └── src/main/java/com/foodstore/
│       ├── model/                   # Entidades JPA (Base, Producto, Pedido, etc.)
│       ├── repository/              # Repositorios + BaseRepository
│       ├── service/                 # Lógica de negocio
│       ├── controller/              # REST Controllers
│       ├── dto/                     # 21 DTOs (request/response)
│       ├── security/                # JWT + Security Config
│       ├── exception/               # GlobalExceptionHandler
│       └── config/                  # OpenAPI, CORS, seed data
│
├── docs/
│   ├── Consigna_TPI_Prog-3.md       # Consigna completa (29 HUs)
│   ├── DOCUMENTACION_TPI.md         # Documentación técnica completa
│   └── prd/                         # PRDs por feature
│       ├── front-auth.md
│       ├── front-store.md
│       ├── front-cart.md
│       ├── front-header.md
│       ├── front-client-orders.md
│       └── front-admin.md
│
├── .github/workflows/               # CI: backend + frontend
├── .gitignore
└── README.md
```

---

## Funcionalidades Implementadas

### Cliente (USUARIO)

| Funcionalidad | Estado | Detalle |
|---------------|--------|---------|
| Registro de usuario | ✅ | Con nombre, apellido, email, celular, password validado |
| Inicio de sesión con JWT | ✅ | JWT real con expiración, almacenado en localStorage |
| Catálogo de productos | ✅ | Desde API real con paginación, loading y error states |
| Filtro por categorías | ✅ | Sidebar con categorías desde API + highlight activo |
| Búsqueda de productos | ✅ | Con botón X para limpiar, search + categoría combinados |
| Ordenamiento | ✅ | Por nombre (A-Z, Z-A) y precio (menor, mayor) |
| Detalle de producto | ✅ | Selector cantidad con límite de stock, badge disponibilidad |
| Carrito de compras | ✅ | 2 columnas, persistencia localStorage, controles +/- |
| Validación de productos | ✅ | Warnings amarillos si no existe/no disponible/sin stock |
| Checkout / Confirmar pedido | ✅ | Modal con forma de pago + teléfono, re-validación |
| Resultado del pedido | ✅ | Verde éxito / Rojo error |
| Historial de pedidos | ✅ | Agrupado por fecha, tipo Meli |
| Cancelar pedido | ✅ | Solo PENDIENTE, confirmación inline |

### Administrador (ADMIN)

| Funcionalidad | Estado |
|---------------|--------|
| Dashboard con estadísticas | ✅ (sidebar #000, endpoint /admin/stats) |
| CRUD Categorías | ✅ (tabla paginada, modal, búsqueda) |
| CRUD Productos | ✅ (filtro por categoría, modal con 7 campos) |
| Gestión de Pedidos | ✅ (filtro estado, modal detalle, cambio estado) |
| Gestión de Usuarios | ✅ (búsqueda, filtro rol, editar, eliminar) |

### Backend API

| Funcionalidad | Estado |
|---------------|--------|
| Infraestructura (Base, repos, exceptions) | ✅ |
| Autenticación JWT + BCrypt | ✅ |
| CRUD Categorías con soft delete | ✅ |
| CRUD Usuarios | ✅ |
| CRUD Productos con categoría | ✅ |
| Pedidos transaccionales con snapshot | ✅ |
| Validación de productos | ✅ (POST /productos/validate) |
| Estadísticas admin | ✅ (GET /admin/stats) |
| OpenAPI / Swagger | ✅ |
| 21 tests | ✅ |

---

## Cobertura vs Consigna (29 HUs)

| Épica | HUs | Estado |
|-------|-----|--------|
| EP-01: Categorías | HU-001 a 005 | ✅ CRUD con soft delete |
| EP-02: Usuarios | HU-006 a 010 | ✅ Registro + CRUD admin |
| EP-03: Productos | HU-011 a 016 | ✅ CRUD con stock y categoría |
| EP-04: Pedidos | HU-017 a 022 | ✅ Transaccional + estados |
| EP-05: Infraestructura | HU-023 a 029 | ✅ Base, JWT, OpenAPI, CORS |

Ver `docs/Consigna_TPI_Prog-3.md` para detalle de cada HU.

---

## Instalación y Ejecución

### Requisitos

- **Frontend:** Node.js 18+ (testeado con v24)
- **Backend:** Java 17+ (JDK 17 o 21)
- **Base de datos:** PostgreSQL 15+ (o usar H2 en memoria)

### 1. Base de datos

```bash
# PostgreSQL (opcional — se puede usar H2 sin instalación)
sudo -u postgres psql -c "CREATE DATABASE foodstore;"
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
```

### 2. Backend

```bash
cd back

# Build + tests
./gradlew build

# Iniciar servidor (localhost:8080)
./gradlew bootRun

# Con H2 en memoria (sin PostgreSQL)
./gradlew bootRun --args='--spring.profiles.active=test'
```

### 3. Frontend

```bash
cd client
npm install
npm run dev        # http://localhost:5173
```

### 4. Verificar

```bash
# Swagger UI
open http://localhost:8080/swagger-ui/index.html

# API
curl http://localhost:8080/v3/api-docs
```

### Seed Data

Al iniciar por primera vez, se crea automáticamente:

| Rol | Email | Password |
|-----|-------|----------|
| **ADMIN** | `admin@admin.com` | `123456` |

---

## Scripts

### Frontend

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Desarrollo con hot reload |
| `npm run build` | Compilar para producción |
| `npm run preview` | Previsualizar build |
| `npm test` | Ejecutar tests (Vitest) |

### Backend

| Comando | Descripción |
|---------|-------------|
| `./gradlew bootRun` | Iniciar servidor |
| `./gradlew build` | Compilar + ejecutar tests |
| `./gradlew test` | Solo tests |
| `./gradlew compileJava` | Solo compilar |

---

## API Endpoints

Base path: `/api/v1`

### Auth (público)
| Método | Endpoint |
|--------|----------|
| POST | `/auth/login` |
| POST | `/auth/register` |

### Productos
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/productos` | Authenticated |
| GET | `/productos/{id}` | Authenticated |
| GET | `/productos/categoria/{id}` | Authenticated |
| POST | `/productos` | ADMIN |
| POST | `/productos/validate` | Authenticated |

### Categorías
| Método | Endpoint | Acceso |
|--------|----------|--------|
| GET | `/categorias` | Authenticated |
| POST / PUT / DELETE | `/categorias/{id}` | ADMIN |

### Pedidos
| Método | Endpoint | Acceso |
|--------|----------|--------|
| POST | `/pedidos` | USUARIO |
| GET | `/pedidos` | ADMIN |
| GET | `/pedidos/usuario` | Authenticated (propios) |
| PATCH | `/pedidos/{id}/cancelar` | Owner o ADMIN |
| PATCH | `/pedidos/{id}/estado` | ADMIN |

### Usuarios (todos ADMIN)
| Método | Endpoint |
|--------|----------|
| GET | `/usuarios` |
| PUT | `/usuarios/{id}` |
| DELETE | `/usuarios/{id}` |

### Admin
| Método | Endpoint |
|--------|----------|
| GET | `/admin/stats` |

---

## Documentación

| Documento | Descripción |
|-----------|-------------|
| `docs/DOCUMENTACION_TPI.md` | Documentación técnica completa (12 secciones) |
| `docs/Consigna_TPI_Prog-3.md` | Consigna original con 29 HUs |
| `docs/prd/` | PRDs por feature (6 documentos) |
| `client/AGENT.md` | Convenciones de código frontend |
| `back/AGENT.md` | Convenciones de código backend |
| `http://localhost:8080/swagger-ui/index.html` | Documentación interactiva de la API |

---

## CI/CD

El workflow de GitHub Actions (`build-and-bootrun.yml`) ejecuta en cada PR:

| Job | Descripción |
|-----|-------------|
| `build-and-bootrun` | Backend: compila + tests + smoke test |
| `frontend` | Frontend: `npm ci` → `tsc --noEmit` → `npm run build` |

---

## Testing

```bash
# Backend (21 tests)
cd back && ./gradlew test

# Frontend (14 tests)
cd client && npm test
```

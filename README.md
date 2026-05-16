# Food Store - TPI Programación III UTN

> Sistema de delivery de viandas saludables — Frontend + Backend

## Stack

| Capa | Tecnología |
|------|------------|
| **Frontend** | TypeScript 5, Vite 5, HTML5, CSS3 |
| **Backend** | Java 17, Spring Boot 3.x, Gradle |
| **Base de datos** | PostgreSQL 15+ |
| **Autenticación** | JWT Stateless (jjwt) |
| **API** | RESTful, documentada con OpenAPI / Swagger UI |
| **Build** | Gradle (backend) + NPM (frontend) |

---

## Estructura del Proyecto

```
tpi/
├── client/               # Frontend SPA (TypeScript + Vite)
│   ├── AGENT.md          # Convenciones de código frontend
│   ├── index.html        # Entry point (Vite)
│   ├── package.json      # Dependencias frontend
│   ├── tsconfig.json     # TypeScript strict
│   ├── vite.config.ts    # Config Vite
│   └── src/
│       ├── main.ts       # Punto de entrada + route guard
│       ├── types/        # Interfaces TypeScript
│       ├── utils/        # Utilidades (auth, router, API client)
│       └── pages/        # Páginas de la aplicación
│           ├── auth/     # Login, registro, forbidden
│           ├── store/    # Home, detalle producto, carrito
│           ├── client/   # Área del cliente (pedidos)
│           └── admin/    # Panel de administración
│
├── back/                 # Backend API REST (Spring Boot)
│   ├── AGENT.md          # Convenciones de código backend
│   ├── build.gradle      # Dependencias backend
│   └── src/main/java/com/foodstore/
│       ├── model/        # Entidades JPA
│       ├── repository/   # Repositorios
│       ├── service/      # Lógica de negocio
│       ├── controller/   # REST Controllers
│       ├── dto/          # Data Transfer Objects
│       ├── security/     # JWT + Security Config
│       ├── exception/    # Manejo de excepciones
│       └── config/       # Configuraciones (CORS, seed data)
│
├── docs/                 # Documentación del proyecto
│   ├── Consigna_TPI_Prog-3.md   # Consigna completa
│   ├── prd/              # PRDs por feature
│   │   ├── back-infrastructure.md
│   │   ├── back-auth-jwt.md
│   │   ├── back-categories.md
│   │   ├── back-users.md
│   │   ├── back-products.md
│   │   ├── back-orders.md
│   │   ├── front-sync-audit.md
│   │   ├── front-auth.md
│   │   ├── front-store.md
│   │   ├── front-client-orders.md
│   │   └── front-admin.md
│   └── Trabajo\ Integrador\ Javascript.md
│   └── Trabajo\ Integrador\ typescript.md
│
├── .gitignore
└── README.md
```

---

## Funcionalidades

### Cliente (USUARIO)
| Funcionalidad | Estado |
|---------------|--------|
| Registro de usuario | ⏳ Migrar a API real |
| Inicio de sesión con JWT | ⏳ Migrar a API real |
| Catálogo de productos | ✅ Parcial (mockeado) |
| Filtro por categorías | ✅ Parcial (mockeado) |
| Búsqueda de productos | ✅ Parcial (mockeado) |
| Detalle de producto | ❌ Pendiente |
| Carrito de compras | ⏳ Parcial (en memoria, sin checkout) |
| Checkout / Confirmar pedido | ❌ Pendiente |
| Historial de pedidos | ❌ Pendiente |
| Cancelar pedido | ❌ Pendiente |

### Administrador (ADMIN)
| Funcionalidad | Estado |
|---------------|--------|
| Dashboard con estadísticas | ❌ Pendiente |
| CRUD Categorías | ❌ Pendiente |
| CRUD Productos | ❌ Pendiente |
| Gestión de Pedidos | ❌ Pendiente |
| Gestión de Usuarios | ❌ Pendiente |

### Backend API
| Funcionalidad | Estado |
|---------------|--------|
| Infraestructura (Base, repos, exceptions) | ❌ Pendiente |
| Autenticación JWT | ❌ Pendiente |
| CRUD Categorías | ❌ Pendiente |
| CRUD Usuarios | ❌ Pendiente |
| CRUD Productos | ❌ Pendiente |
| Pedidos con snapshot y stock | ❌ Pendiente |

---

## Requisitos

### Frontend
- Node.js 18+
- NPM 9+

### Backend
- Java 17+ (OpenJDK 17)
- PostgreSQL 15+
- Gradle (usar `./gradlew` incluido en el proyecto)

---

## Instalación y Ejecución

### 1. Instalar PostgreSQL (Linux - Ubuntu/Debian)

```bash
# Instalar PostgreSQL
sudo apt update
sudo apt install -y postgresql postgresql-contrib

# Iniciar el servicio
sudo systemctl start postgresql

# Opcional: que arranque solo al prender la PC
sudo systemctl enable postgresql

# Verificar que está corriendo
sudo systemctl status postgresql
```

### 2. Crear la base de datos

```bash
# Crear la base de datos foodstore
sudo -u postgres psql -c "CREATE DATABASE foodstore;"

# (Opcional) Setear contraseña al usuario postgres
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"

# Verificar que se creó correctamente
sudo -u postgres psql -c "\l" | grep foodstore
```

### 3. Compilar y ejecutar el backend

```bash
# Ir al directorio del backend
cd back

# Compilar (descarga dependencias + compila + corre tests)
./gradlew build

# Compilar sin tests (más rápido para desarrollo)
./gradlew build -x test

# Solo tests
./gradlew test

# Solo compilar (sin tests)
./gradlew compileJava

# Iniciar servidor (puerto 8080)
./gradlew bootRun
```

> Al iniciar por primera vez, se crea automáticamente un usuario admin (seed data):
> - **Email**: `admin@admin.com`
> - **Password**: `123456`

### 4. Verificar que funciona

```bash
# La API debería responder con el JSON de OpenAPI
curl http://localhost:8080/api-docs

# O abrí en el navegador:
# http://localhost:8080/swagger-ui/index.html
```

### 5. (Alternativa) Ejecutar con H2 en memoria — sin PostgreSQL

Si no tenés PostgreSQL o solo querés probar rápido:

```bash
./gradlew bootRun --args='--spring.profiles.active=test'
```

Esto usa H2 (base de datos en memoria) en vez de PostgreSQL. **Los datos se pierden al apagar el servidor.** Ideal para desarrollo rápido o demostraciones.

### Configuración de base de datos

Las credenciales de PostgreSQL se configuran en `back/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/foodstore
spring.datasource.username=postgres
spring.datasource.password=postgres
```

Si tus credenciales son distintas, ajustalas ahí.

### Frontend
```bash
cd client
npm install
npm run dev
# Abrir en http://localhost:5173
```

### API Documentation (una vez iniciado el backend)
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- OpenAPI spec: http://localhost:8080/api-docs

---

## Scripts

### Frontend
```bash
cd client
npm run dev        # Desarrollo con hot reload
npm run build      # Compilar para producción
npm run preview    # Previsualizar build
npm run typecheck  # TypeScript type checking
```

### Backend
```bash
cd back
./gradlew bootRun      # Iniciar servidor
./gradlew build         # Compilar + tests
./gradlew test          # Ejecutar tests
./gradlew compileJava   # Verificar que compila
```

---

## Documentación del Proyecto

| Documento | Descripción |
|-----------|-------------|
| `docs/Consigna_TPI_Prog-3.md` | Consigna completa con 29 HUs |
| `docs/prd/back-infrastructure.md` | Base entity, JWT, exceptions, CORS, OpenAPI |
| `docs/prd/back-auth-jwt.md` | Login/Register con JWT |
| `docs/prd/back-categories.md` | CRUD categorías |
| `docs/prd/back-users.md` | CRUD usuarios |
| `docs/prd/back-products.md` | CRUD productos con categoría |
| `docs/prd/back-orders.md` | Pedidos con snapshot y control de stock |
| `docs/prd/front-sync-audit.md` | Relevamiento: qué existe vs qué falta |
| `docs/prd/front-auth.md` | Login/Register conectado a API |
| `docs/prd/front-store.md` | Catálogo, carrito, checkout |
| `docs/prd/front-client-orders.md` | Historial de pedidos del cliente |
| `docs/prd/front-admin.md` | Dashboard + CRUDs + gestión pedidos |
| `client/AGENT.md` | Convenciones de código frontend |
| `back/AGENT.md` | Convenciones de código backend |

---

## Plan de Desarrollo (Sprints)

### Sprint 1 — Fundamentos (back-infrastructure + categorías + auth)
- [ ] HU-023: Entidad Base
- [ ] HU-024: Repositorio Base
- [ ] HU-025: Manejo de Excepciones
- [ ] HU-026: Encriptación BCrypt
- [ ] HU-001..005: CRUD Categorías
- [ ] HU-006: Registro de Usuario
- [ ] HU-027: Carga Inicial (seed)
- [ ] HU-028: OpenAPI
- [ ] HU-029: CORS
- [ ] JWT: Provider, Filter, SecurityConfig

### Sprint 2 — Usuarios y Productos
- [ ] HU-007..010: CRUD Usuarios
- [ ] HU-011..016: CRUD Productos

### Sprint 3 — Pedidos y Frontend
- [ ] HU-017..022: CRUD Pedidos
- [ ] Frontend: store, client orders, admin panel

---

## PRDs

Todos los PRDs están en `docs/prd/`. Cada uno detalla:
- Clases / componentes involucrados
- Flujos de usuario (mermaid)
- Validaciones y reglas de negocio
- API contracts (request/response)
- DoD con testing
- Dependencias

---

## Alumno

**Pablo Garay**  
Universidad Tecnológica Nacional — Programación III

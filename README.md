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
- Java 17+ (JDK 17 o 21)
- PostgreSQL 15+
- Gradle (usar `./gradlew` o `gradlew.bat` incluido en el proyecto)

---

## Instalación y Ejecución

### Linux (Ubuntu/Debian)

#### 1. Instalar Java

```bash
# Instalar OpenJDK 17
sudo apt update
sudo apt install -y openjdk-17-jdk

# Verificar
java -version
# → openjdk version "17.x.x"
```

#### 2. Instalar PostgreSQL

```bash
sudo apt install -y postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql   # arranque automático al iniciar
sudo systemctl status postgresql   # verificar que está corriendo
```

#### 3. Crear la base de datos

```bash
sudo -u postgres psql -c "CREATE DATABASE foodstore;"
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
sudo -u postgres psql -c "\l" | grep foodstore   # verificar
```

#### 4. Compilar y ejecutar

```bash
cd back

# Build (descarga dependencias + compila + tests)
./gradlew build

# Build sin tests (más rápido)
./gradlew build -x test

# Solo tests
./gradlew test

# Iniciar servidor (localhost:8080)
./gradlew bootRun
```

---

### Windows (PowerShell / Git Bash / CMD)

#### 1. Instalar Java

- Bajá el instalador desde [Adoptium Temurin 17](https://adoptium.net/temurin/releases/?version=17) (archivo `.msi`)
- Ejecutalo, siguiente, siguiente
- **Verificar** desde **PowerShell** o **CMD**:
  ```cmd
  java -version
  ```
  → tiene que mostrar `openjdk version "17.x.x"`

  También funciona con **Java 21** (el proyecto compila a bytecode 17):
  ```cmd
  java -version
  # → openjdk version "21.x.x"  ✅ compatible
  ```

> 💡 Si tenés varias versiones de Java, creá la variable de entorno `JAVA_HOME` apuntando a la instalación de Java 17 o 21.

#### 2. Instalar PostgreSQL

- Bajá el installer de [EnterpriseDB PostgreSQL](https://www.enterprisedb.com/downloads/postgres-postgresql-downloads)
- Ejecutalo, siguiente, siguiente
- Cuando pregunte:
  - **Password para `postgres`**: poné `postgres`
  - **Puerto**: dejá `5432`
- Al final, **Stack Builder** preguntará si querés instalar extras — podés saltarlo

#### 3. Crear la base de datos (desde PowerShell/CMD)

```powershell
# Opción A — desde terminal
"C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres -c "CREATE DATABASE foodstore;"

# Te va a pedir la contraseña que pusiste durante la instalación
```

**Opción B — desde DBeaver** (recomendado si ya lo tenés):
1. Abrí DBeaver
2. Nueva Conexión → PostgreSQL
3. Host: `localhost`, Puerto: `5432`
4. Usuario: `postgres`, Password: la que pusiste
5. Test Connection → Finish
6. Click derecho en la conexión → Create New Database → nombre: `foodstore`

#### 4. Compilar y ejecutar

Desde **PowerShell** o **CMD**:

```powershell
cd tpi\back

# Build completo (descarga dependencias + compila + tests)
.\gradlew.bat build

# Build sin tests
.\gradlew.bat build -x test

# Solo tests
.\gradlew.bat test

# Iniciar servidor (localhost:8080)
.\gradlew.bat bootRun
```

Desde **Git Bash** (MINGW64):

```bash
cd tpi/back

# Usar ./gradlew en vez de .\gradlew.bat
./gradlew build
./gradlew bootRun
```

> Si tenés **Git Bash**, usá `./gradlew` (el script para Unix). Si estás en **PowerShell** o **CMD**, usá `.\gradlew.bat`.

#### 5. VS Code Extensions recomendadas

| Extensión | ID | Para qué |
|-----------|-----|----------|
| Extension Pack for Java | `vscjava.vscode-java-pack` | Lenguaje, debug, test |
| Spring Boot Extension Pack | `vmware.vscode-boot-dev-pack` | Spring Boot, properties, snippets |
| Gradle for Java | `vscjava.vscode-gradle` | Tareas de Gradle desde VS Code |
| Lombok Annotations | `vscjava.vscode-lombok` | Soporte `@Data`, `@Builder` |

Instalálas desde `Ctrl+Shift+X` buscando por ID.

---

### Para ambas plataformas

#### Verificar que funciona

```bash
curl http://localhost:8080/api-docs
# → JSON con la especificación OpenAPI

# O abrí en el navegador:
# http://localhost:8080/swagger-ui/index.html
```

> Al iniciar por primera vez, se crea automáticamente un usuario admin (seed data):
> - **Email**: `admin@admin.com`
> - **Password**: `123456`

#### (Alternativa) Ejecutar con H2 en memoria — sin PostgreSQL

Si no tenés PostgreSQL instalado o solo querés probar rápido:

```bash
# Linux / Git Bash
./gradlew bootRun --args='--spring.profiles.active=test'

# PowerShell / CMD
.\gradlew.bat bootRun --args='--spring.profiles.active=test'
```

Esto usa H2 (base de datos en memoria) en vez de PostgreSQL. **Los datos se pierden al apagar.** Ideal para desarrollo rápido o demos.

### Variables de Entorno

Los valores sensibles (contraseña de DB, secreto JWT) se configuran **también** vía variables de entorno, con fallback a valores por defecto para desarrollo.

| Variable | Default | Descripción |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/foodstore` | URL de conexión a PostgreSQL |
| `DB_USERNAME` | `postgres` | Usuario de la base de datos |
| `DB_PASSWORD` | `postgres` | Contraseña de la base de datos |
| `JWT_SECRET` | *default hardcodeado* | Clave secreta para firmar tokens JWT |
| `JWT_EXPIRATION` | `86400000` (24h) | Expiración del token en milisegundos |
| `SERVER_PORT` | `8080` | Puerto del servidor |

Ver `back/.env.example` para un template completo.

**Cómo setearlas:**

```bash
# Linux / Git Bash
export DB_PASSWORD=miPassword123
./gradlew bootRun

# Windows PowerShell
$env:DB_PASSWORD="miPassword123"
.\gradlew.bat bootRun

# Windows CMD
set DB_PASSWORD=miPassword123
.\gradlew.bat bootRun
```

El archivo `back/.env.example` documenta todas las variables disponibles (copiar a `.env` como referencia, Spring Boot no lo lee automáticamente).

### Troubleshooting

| Error | Causa | Solución |
|-------|-------|----------|
| `Error: no se ha encontrado o cargado la clase principal org.gradle.wrapper.GradleWrapperMain` | Falta `gradle/wrapper/gradle-wrapper.jar` | `git pull` para traer el archivo o descargalo manualmente |
| `JAVA_HOME is not set` | Java no está instalado o no está en el PATH | Instalar JDK 17+ y verificar con `java -version` |
| `Failed to load ApplicationContext` en tests | Tests intentan conectar a PostgreSQL | El test ya usa `@ActiveProfiles("test")` con H2. Si sigue fallando, revisá `application-test.properties` |
| `Port 8080 already in use` | Otro proceso usando el puerto | Cambiá el puerto con `--server.port=8081` o matá el proceso anterior |

#### Si `./gradlew build` falla con el wrapper

```bash
# Asegurate de tener la última versión del repo
git pull

# Si el gradle-wrapper.jar sigue sin existir, descargalo:
# Linux / Git Bash
curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradlew" -o gradlew
chmod +x gradlew
curl -sL "https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar" -o gradle/wrapper/gradle-wrapper.jar

# Windows (PowerShell)
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.7.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle/wrapper/gradle-wrapper.jar"
```

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

# Trabajo Final - Food Store
## Sistema de Gestión de Pedidos de Comida

Se propone desarrollar un ecommerce llamado **Food Store**, orientado a la venta de productos de un negocio de comidas mediante una aplicación web full stack. El sistema permitirá gestionar categorías, productos y pedidos, y contará con una gestión de usuarios con dos perfiles principales: **ADMIN**, con acceso al panel de administración para realizar operaciones CRUD y gestionar el estado de los pedidos, y **USUARIO**, que podrá registrarse, iniciar sesión, navegar el catálogo, seleccionar productos, administrar un carrito y confirmar compras, además de consultar el historial y el estado de sus pedidos.

El desarrollo deberá realizarse tomando como guía el backlog de épicas e historias de usuario, de modo que cada funcionalidad implementada cumpla con sus criterios de aceptación y represente el comportamiento esperado del sistema.

---

## Tabla de Contenidos

- [Información del Proyecto](#información-del-proyecto)
- [Objetivos del Proyecto](#objetivos-del-proyecto)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Sistema de Autenticación y Autorización](#sistema-de-autenticación-y-autorización)
- [Roles y Permisos](#roles-y-permisos)
- [Modelo UML](#modelo-uml)
- [Funcionalidades por Módulo](#funcionalidades-por-módulo)
- [Diseño y UX](#diseño-y-ux)
- [Flujos de Usuario](#flujos-de-usuario)
- [Consideraciones Importantes](#consideraciones-importantes)
- [Historias de Usuario (Backlog Scrum)](#historias-de-usuario-backlog-scrum)
- [Entrega del Proyecto](#entrega-del-proyecto)
- [Épicas del Proyecto](#épicas-del-proyecto)
- [Historias de Usuario por Épica](#historias-de-usuario-por-épica)
- [Matriz de Trazabilidad](#matriz-de-trazabilidad)
- [Priorización del Backlog](#priorización-del-backlog)
- [Definición de Completado (DoD)](#definición-de-completado-dod)

---

## Información del Proyecto

| Campo | Valor |
|-------|-------|
| **Nombre** | Food Store - Sistema de Gestión de Pedidos de Comida |
| **Frontend** | TypeScript, Vite, HTML5, CSS3, Tailwind CSS |
| **Backend** | Spring Boot 3.x, Java 17+, PostgreSQL/MySQL |
| **Autenticación** | Gestión básica con localStorage (sólo fines educativos) |

---

## Objetivos del Proyecto

Desarrollar una aplicación web full stack completa para la gestión de un negocio de comidas, que permita:

1. **A los administradores:** Gestionar categorías, productos y pedidos
2. **A los clientes:** Navegar productos, realizar compras y seguir sus pedidos
3. **Sistema de carrito:** Funcional con persistencia en localStorage
4. **Integración completa:** Conexión Frontend-Backend mediante REST API

---

## Estructura del Proyecto

### Estructura Frontend

```
final-prog3/
├── index.html              # Redirección a login
├── package.json            # Dependencias y scripts
├── tsconfig.json           # Configuración TypeScript
├── vite.config.ts          # Configuración Vite
└── src/
    ├── main.ts             # Punto de entrada
    ├── style.css           # Estilos globales
    ├── types/              # Definiciones de tipos TypeScript
    ├── utils/              # Utilidades y helpers
    └── pages/              # Páginas de la aplicación
        ├── auth/           # Autenticación
        ├── store/          # Páginas del cliente
        │   ├── home/
        │   ├── productDetail/
        │   └── cart/
        ├── client/         # Área del cliente
        │   └── orders/
        └── admin/          # Panel de administración
```

### Estructura Backend

```
foodstore-backend/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/tuuniversidad/foodstore/
    │   │       ├── FoodstoreApplication.java
    │   │       ├── model/
    │   │       ├── repository/
    │   │       ├── service/
    │   │       │   └── impl/
    │   │       ├── controller/
    │   │       ├── dto/
    │   │       └── exception/
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/
```

---

## Sistema de Autenticación y Autorización

### Flujo de Autenticación

1. **Login/Registro:**
   - Usuario ingresa credenciales
   - Frontend envía POST a `/api/auth/login` o `/api/auth/register`
   - Backend valida credenciales
   - Si es exitoso, retorna datos del usuario
   - Frontend guarda datos en localStorage
   - Redirecciona según el rol

2. **Validación de Sesión:**
   - Cada página protegida verifica localStorage
   - Si no hay sesión, redirecciona a login
   - Valida permisos según el rol

3. **Cierre de Sesión:**
   - Limpia localStorage
   - Redirecciona al login

---

## Roles y Permisos

### Administrador (Admin)

- ✅ Acceso completo al panel de administración
- ✅ Gestión de categorías (CRUD)
- ✅ Gestión de productos (CRUD)
- ✅ Gestión de pedidos (ver todos, actualizar estado)

### Cliente (Usuario)

- ✅ Ver catálogo de productos
- ✅ Filtrar por categorías
- ✅ Buscar productos
- ✅ Ver detalle de productos
- ✅ Agregar productos al carrito
- ✅ Gestionar carrito (agregar, quitar, modificar cantidades)
- ✅ Realizar pedidos
- ✅ Ver historial de pedidos propios
- ✅ Ver detalle y estado de sus pedidos
- ❌ NO tiene acceso al panel de administración

---

## Modelo UML

### Entidades principales

**Base** (clase abstracta)
- `-id: Long`
- `-eliminado: boolean`
- `-createdAt: LocalDateTime`

**Usuario** extiende Base
- `-nombre: String`
- `-apellido: String`
- `-mail: String`
- `-celular: String`
- `-contraseña: String`
- `-rol: Rol`

**Pedido** extiende Base
- `-fecha: LocalDate`
- `-estado: Estado`
- `-total: Double`
- `-formaPago: FormaPago`
- `+addDetallePedido(int, Double, Producto): void`
- `+findeDetallePedidoByProducto(Producto): DetallePedido`
- `+deleteDetallePedidoByProducto(Producto): void`

**DetallePedido** extiende Base
- `-cantidad: int`
- `-subtotal: Double`

**Producto** extiende Base
- `-nombre: String`
- `-precio: Double`
- `-descripcion: String`
- `-stock: int`
- `-imagen: String`
- `-disponible: Boolean`

**Categoria** extiende Base
- `-nombre: String`
- `-descripcion: String`

### Enumeraciones

**FormaPago:** `TARJETA | TRANSFERENCIA | EFECTIVO`

**Rol:** `ADMIN | USUARIO`

**Estado:** `PENDIENTE | CONFIRMADO | TERMINADO | CANCELADO`

---

## Funcionalidades por Módulo

### 1. Módulo de Autenticación

#### Login (`/src/pages/auth/login/`)

- Formulario con email y contraseña
- Validación de campos requeridos
- Conexión con API `POST /api/auth/login`
- Manejo de errores de autenticación
- Redirección según rol del usuario

#### Registro (`/src/pages/auth/register/`)

- Formulario con nombre, email y contraseña
- Validación de campos (email válido, contraseña mínimo 6 caracteres)
- Conexión con API `POST /api/auth/register`
- Solo se pueden registrar clientes
- Auto-login después del registro

---

### 2. Módulo de Cliente - Store

#### Home / Catálogo (`/src/pages/store/home/`)

- Sidebar con categorías (`GET /api/categories`)
- Búsqueda en tiempo real
- Filtros por categoría y ordenamiento (nombre A-Z, Z-A, precio ascendente/descendente)
- Grid de productos (`GET /api/products`) con imagen, nombre, descripción, precio, badge de disponibilidad y click al detalle
- Badge del carrito con cantidad de ítems
- Contador de productos encontrados
- Toggle del sidebar para mobile

#### Detalle de Producto (`/src/pages/store/productDetail/`)

- `GET /api/products/{id}`
- Imagen grande del producto
- Información completa: nombre, descripción, precio, stock disponible, estado
- Selector de cantidad con validación de stock
- Botón "Agregar al Carrito" y mensaje de confirmación
- Botón "Volver"
- **Validaciones:**
  - No permite agregar si no hay stock
  - No permite cantidad mayor al stock disponible
  - No permite agregar productos inactivos

#### Carrito de Compras (`/src/pages/store/cart/`)

- Lista de productos con imagen, nombre, precio unitario, controles de cantidad (+/-), precio total por producto y botón eliminar
- Resumen del pedido: subtotal y total
- Botón "Vaciar Carrito"
- Modal de checkout con teléfono (requerido)
- Al confirmar: `POST /api/orders`
- **Validaciones:** stock disponible al modificar cantidad, campos requeridos en checkout
- Estado vacío con mensaje y botón a la tienda
- **Persistencia:** carrito guardado en localStorage, persiste entre sesiones, se limpia al confirmar pedido

---

### 3. Módulo de Cliente - Mis Pedidos

#### Historial de Pedidos (`/src/pages/client/orders/`)

- `GET /api/orders` (solo pedidos del usuario)
- Lista con tarjetas mostrando: número de pedido, fecha y hora, estado con badge de color, resumen de productos (primeros 3 + contador), total del pedido
- Click en pedido abre modal con detalle completo
- Modal de detalle: estado con icono, información de entrega, lista completa de productos, desglose de costos, mensaje según estado
- Estado vacío si no hay pedidos

**Estados de Pedido:**

| Código | Descripción |
|--------|-------------|
| `pending` | ⏳ Pendiente |
| `processing` | 🔍 En Preparación |
| `completed` | ✅ Entregado |
| `cancelled` | ❌ Cancelado |

---

### 4. Módulo de Administración

#### Dashboard (`/src/pages/admin/adminHome/`)

- Sidebar de navegación administrativa
- 4 tarjetas con estadísticas: total categorías, productos, pedidos y productos disponibles
- Panel de resumen: categorías activas, productos activos/inactivos, pedidos por estado
- Enlaces directos a cada módulo

#### Gestión de Categorías (`/src/pages/admin/categories/`)

- `GET /api/categories` — tabla con ID, imagen, nombre, descripción, acciones
- Botón "Nueva Categoría"
- Modal crear/editar: `POST /api/categories` | `PUT /api/categories/{id}`
  - Nombre (requerido), Descripción (requerida), URL de imagen (requerida)
- Validaciones: campos requeridos, URL válida
- Confirmación al eliminar (`DELETE /api/categories/{id}`)

#### Gestión de Productos (`/src/pages/admin/products/`)

- `GET /api/products` — tabla con ID, imagen, nombre, descripción, precio, categoría, stock, estado, acciones
- Botón "Nuevo Producto"
- Modal crear/editar: `POST /api/products` | `PUT /api/products/{id}`
  - Nombre, descripción, precio (> 0), stock (>= 0), categoría (select), URL de imagen, checkbox "Producto disponible"
- Validaciones: todos los campos requeridos, precio y stock numéricos válidos, categoría debe existir
- Confirmación al eliminar (`DELETE /api/products/{id}`)

#### Gestión de Pedidos Admin (`/src/pages/admin/orders/`)

- `GET /api/orders` (todos los pedidos)
- Filtro por estado de pedido
- Tarjetas con: número de pedido, nombre del cliente, fecha/hora, estado con badge, cantidad de productos, total
- Modal de detalle: información del cliente, dirección/teléfono, método de pago, notas, lista de productos, desglose de costos, select para cambiar estado
- Botón "Actualizar Estado" (`PATCH /api/orders/{id}/status`)
- Ordenados por fecha (más recientes primero)

---

## Flujos de Usuario

### Flujo de Compra (Cliente)

1. Usuario se autentica
2. Navega por el catálogo (puede filtrar/buscar)
3. Click en producto → Ver detalle
4. Selecciona cantidad → Agregar al carrito
5. Continúa comprando o va al carrito
6. En el carrito: revisa productos, modifica cantidades
7. Click "Proceder al Pago"
8. Completa formulario de checkout
9. Confirma pedido (`POST /api/orders`)
10. Ver mensaje de éxito
11. Es redirigido a "Mis Pedidos"
12. Puede ver estado y detalle del pedido

### Flujo de Gestión de Producto (Admin)

1. Admin se autentica
2. Va a "Panel Admin" → "Productos"
3. Click en "Nuevo Producto"
4. Completa formulario
5. Selecciona categoría del dropdown
6. Marca checkbox si está disponible
7. Guarda producto (`POST /api/products`)
8. Ve el producto en la tabla
9. Puede editar/eliminar cuando necesite

### Flujo de Gestión de Pedido (Admin)

1. Admin va a "Pedidos"
2. Ver lista de todos los pedidos (`GET /api/orders`)
3. Puede filtrar por estado
4. Click en pedido → Ver detalle completo
5. Cambia estado en el select
6. Click "Actualizar Estado" (`PATCH /api/orders/{id}/status`)
7. Cliente ve el cambio en "Mis Pedidos"

---

## Consideraciones Importantes

### ⚠️ Seguridad

> **IMPORTANTE:** Este proyecto NO implementa seguridad real.

- Las contraseñas se almacenan encriptadas en SHA-256
- No hay tokens JWT
- La validación de rol es solo frontend
- localStorage es fácilmente modificable
- **SOLO para fines educativos**

---

## Historias de Usuario (Backlog Scrum)

### Guía rápida: qué funcionalidades programar

- **EP-01** – Gestión de Categorías: CRUD completo con validaciones y soft delete.
- **EP-02** – Gestión de Usuarios: Registro/autenticación y administración, email único, password encriptada y DTOs sin password.
- **EP-03** – Gestión de Productos: CRUD con control de stock/precio, asociación a categoría y filtros.
- **EP-04** – Gestión de Pedidos: Creación/consulta con detalle, cálculo de totales, reducción de stock y transacciones.
- **EP-05** – Infraestructura y Arquitectura: Capas, DTOs, excepciones centralizadas, auditoría y buenas prácticas.

---

## Entrega del Proyecto

### Contenido de la Entrega

**1. Código Fuente**
- Repositorio GitHub con frontend y backend
- Archivo `README.md` con instrucciones de instalación, configuración de BD y comandos de ejecución

**2. Documentación Académica y Técnica (PDF)**
- Carátula: institución, carrera, materia, título, datos del estudiante, fecha
- Índice con números de página
- Marco Teórico: tecnologías utilizadas y conceptos clave
- Decisiones Técnicas y Arquitectura: patrones aplicados (DTOs, manejo de excepciones) y capturas de pantalla
- Dificultades y Soluciones: obstáculos técnicos y resoluciones
- Bibliografía / Webgrafía

**3. Video Demostración**
- Grabación de 10 a 15 minutos del sistema funcionando
- Debe recorrer el flujo del cliente (registro, navegación, carrito, confirmación) y el flujo del administrador (gestión de catálogo y actualización de estados)

### Método de Entrega

La entrega se realiza **exclusivamente en formato `.zip`** con:
- El código fuente completo
- El documento en formato PDF

El `README.md` debe incluir:
- Enlace al video demostrativo (con permisos públicos)
- Enlace a la documentación en PDF (o el PDF en la raíz del repositorio)

> No se considerarán entregas incompletas o que no respeten el formato indicado.

---

## Glosario de Términos

| Término | Definición |
|---------|-----------|
| **API REST** | Interfaz que utiliza HTTP para operaciones CRUD |
| **Soft Delete** | Eliminación lógica: el registro se marca como eliminado pero permanece en la BD |
| **DTO** | Data Transfer Object - objeto para transferir datos entre capas |
| **JWT** | JSON Web Token - estándar para autenticación (consideración futura) |
| **CRUD** | Create, Read, Update, Delete - operaciones básicas de persistencia |
| **Endpoint** | Punto de acceso a un recurso de la API |
| **Transacción** | Conjunto de operaciones que se ejecutan como unidad atómica |
| **Validación** | Proceso de verificar que los datos cumplen con las reglas de negocio |

---

## Roles del Sistema

### ROL-01: Administrador (ADMIN)

Usuario con acceso completo al sistema. Puede gestionar todas las entidades, usuarios y configuraciones.

**Permisos:**
- Gestión completa de categorías (CRUD)
- Gestión completa de productos (CRUD)
- Gestión completa de usuarios (CRUD)
- Gestión completa de pedidos (CRUD)
- Cambio de estados de pedidos
- Acceso a reportes y estadísticas

### ROL-02: Usuario (USUARIO)

Usuario regular del sistema. Puede realizar compras y gestionar sus propios pedidos.

**Permisos:**
- Visualización de categorías
- Visualización de productos
- Gestión de su perfil
- Creación y visualización de sus pedidos
- Cancelación de sus pedidos (en estado PENDIENTE)

### ROL-03: Sistema (SYSTEM)

Procesos automáticos del sistema.

**Responsabilidades:**
- Carga inicial de datos
- Auditoría automática (timestamps)
- Validaciones automáticas
- Control de concurrencia

---

## Épicas del Proyecto

| Épica | Descripción | Valor | Complejidad |
|-------|-------------|-------|-------------|
| **EP-01: Gestión de Categorías** | Administración de categorías de productos | Alto | Baja |
| **EP-02: Gestión de Usuarios** | Registro, autenticación y administración de usuarios | Crítico | Media |
| **EP-03: Gestión de Productos** | Administración del catálogo de productos | Alto | Media |
| **EP-04: Gestión de Pedidos** | Creación, seguimiento y gestión de pedidos | Crítico | Alta |
| **EP-05: Infraestructura y Arquitectura** | Arquitectura, seguridad y mantenibilidad | Crítico | Alta |

---

## Historias de Usuario por Épica

### EP-01: Gestión de Categorías

---

#### HU-001: Crear Categoría

| Campo | Valor |
|-------|-------|
| **ID** | HU-001 |
| **Épica** | EP-01: Gestión de Categorías |
| **Prioridad** | Alta |
| **Story Points** | 3 |
| **Sprint** | 1 |

**Como** Administrador del sistema  
**Quiero** poder crear nuevas categorías de productos  
**Para** organizar el catálogo de manera estructurada

**Criterios de Aceptación:**

- **Escenario 1 - Creación exitosa:** POST a `/categoria` con datos válidos → 201 Created con ID generado y datos persistidos
- **Escenario 2 - Nombre vacío:** → 400 Bad Request "El nombre es obligatorio"
- **Escenario 3 - Nombre muy corto (< 2 chars):** → 400 Bad Request "El nombre debe tener entre 2 y 100 caracteres"
- **Escenario 4 - Nombre muy largo (> 100 chars):** → 400 Bad Request "El nombre debe tener entre 2 y 100 caracteres"
- **Escenario 5 - Descripción muy larga (> 500 chars):** → 400 Bad Request "La descripción no puede exceder 500 caracteres"

**Request:**
```json
{
  "nombre": "string (obligatorio, 2-100 caracteres)",
  "descripcion": "string (opcional, máx 500 caracteres)"
}
```

**Response:**
```json
{
  "id": "long (autogenerado)",
  "nombre": "string",
  "descripcion": "string"
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-001-01 | El nombre de la categoría es obligatorio |
| RN-001-02 | El nombre debe tener entre 2 y 100 caracteres |
| RN-001-03 | La descripción es opcional |
| RN-001-04 | La descripción no puede exceder 500 caracteres |
| RN-001-05 | El ID se genera automáticamente |
| RN-001-06 | Los campos `createdAt` y `updatedAt` se generan automáticamente |

**Tareas Técnicas:**
- [ ] Crear entidad `Categoria` con campos id, nombre, descripcion
- [ ] Crear DTO `CategoriaCreate` con validaciones Jakarta
- [ ] Crear DTO `CategoriaDto` para respuesta
- [ ] Implementar método `save()` en `CategoriaService`
- [ ] Implementar endpoint POST en `CategoriaController`

---

#### HU-002: Listar Categorías

| Campo | Valor |
|-------|-------|
| **ID** | HU-002 |
| **Épica** | EP-01 |
| **Prioridad** | Alta |
| **Story Points** | 2 |
| **Sprint** | 1 |

**Como** Usuario del sistema  
**Quiero** ver todas las categorías disponibles  
**Para** poder navegar y filtrar productos por categoría

**Criterios de Aceptación:**

- **Escenario 1 - Listar existentes:** GET `/categoria` → 200 OK con array de categorías activas (sin eliminadas)
- **Escenario 2 - Lista vacía:** GET `/categoria` sin categorías → 200 OK con `[]`

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-002-01 | Solo se muestran categorías con `eliminado = false` |
| RN-002-02 | El listado no requiere autenticación |
| RN-002-03 | El orden no está definido |

---

#### HU-003: Obtener Categoría por ID

| Campo | Valor |
|-------|-------|
| **ID** | HU-003 |
| **Story Points** | 2 |
| **Sprint** | 1 |

- **Escenario 1:** GET `/categoria/1` con categoría activa → 200 OK
- **Escenario 2:** GET `/categoria/999` no encontrada → 404 "Entidad con id 999 no encontrado"
- **Escenario 3:** Categoría eliminada → 404 Not Found

---

#### HU-004: Actualizar Categoría

| Campo | Valor |
|-------|-------|
| **ID** | HU-004 |
| **Story Points** | 3 |
| **Sprint** | 1 |

**Request:**
```json
{
  "nombre": "string (opcional, 2-100 caracteres si se envía)",
  "descripcion": "string (opcional, máx 500 caracteres si se envía)"
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-004-01 | Solo se actualizan los campos enviados (actualización parcial) |
| RN-004-02 | Los campos nulos no modifican el valor existente |
| RN-004-03 | El campo `updatedAt` se actualiza automáticamente |
| RN-004-04 | El campo `version` se incrementa (optimistic locking) |

---

#### HU-005: Eliminar Categoría (Soft Delete)

| Campo | Valor |
|-------|-------|
| **ID** | HU-005 |
| **Story Points** | 2 |
| **Sprint** | 1 |

- **Escenario 1:** DELETE `/categoria/1` → 204 No Content, `eliminado = true`, permanece en BD
- **Escenario 2:** No existe → 404 Not Found
- **Escenario 3:** Ya eliminada → 404 Not Found

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-005-01 | La eliminación es lógica (soft delete) |
| RN-005-02 | El campo `eliminado` se establece en `true` |
| RN-005-03 | El registro permanece en la base de datos |
| RN-005-04 | Los productos asociados NO se eliminan automáticamente |

---

### EP-02: Gestión de Usuarios

---

#### HU-006: Registrar Usuario

| Campo | Valor |
|-------|-------|
| **ID** | HU-006 |
| **Prioridad** | Crítica |
| **Story Points** | 5 |
| **Sprint** | 1 |

**Como** Visitante del sistema  
**Quiero** poder registrarme como usuario  
**Para** poder realizar compras y gestionar mis pedidos

**Request:**
```json
{
  "nombre": "string (obligatorio, 2-50 caracteres)",
  "apellido": "string (obligatorio, 2-50 caracteres)",
  "email": "string (obligatorio, formato email, único)",
  "celular": "string (opcional, máx 20 caracteres)",
  "password": "string (obligatorio, min 6 caracteres)"
}
```

**Response:**
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Perez",
  "mail": "juan@email.com",
  "celular": "1234567890",
  "rol": "USUARIO"
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-006-01 | El nombre es obligatorio (2-50 caracteres) |
| RN-006-02 | El apellido es obligatorio (2-50 caracteres) |
| RN-006-03 | El email es obligatorio y debe ser único |
| RN-006-04 | El email debe tener formato válido |
| RN-006-05 | La contraseña es obligatoria (min 6 caracteres) |
| RN-006-06 | La contraseña se encripta con BCrypt |
| RN-006-07 | El rol por defecto es USUARIO |
| RN-006-08 | La contraseña nunca se retorna en las respuestas |

---

#### HU-007: Listar Usuarios

| Campo | Valor |
|-------|-------|
| **ID** | HU-007 |
| **Story Points** | 2 |
| **Sprint** | 1 |

Solo administradores. La respuesta nunca incluye contraseñas. Solo usuarios con `eliminado = false`.

---

#### HU-008: Obtener Usuario por ID

| Campo | Valor |
|-------|-------|
| **ID** | HU-008 |
| **Story Points** | 2 |
| **Sprint** | 1 |

GET `/usuario/{id}` → 200 OK (sin password) | 404 si no existe.

---

#### HU-009: Actualizar Usuario

| Campo | Valor |
|-------|-------|
| **ID** | HU-009 |
| **Story Points** | 5 |
| **Sprint** | 2 |

**Request:**
```json
{
  "nombre": "string (opcional)",
  "apellido": "string (opcional)",
  "email": "string (opcional, debe ser único)",
  "celular": "string (opcional)",
  "password": "string (opcional, min 6 caracteres)"
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-009-01 | Solo se actualizan los campos enviados |
| RN-009-02 | Si se cambia el email, debe ser único |
| RN-009-03 | Si se cambia la contraseña, debe encriptarse |
| RN-009-04 | El campo `updatedAt` se actualiza automáticamente |

---

#### HU-010: Eliminar Usuario (Soft Delete)

| Campo | Valor |
|-------|-------|
| **ID** | HU-010 |
| **Story Points** | 2 |
| **Sprint** | 2 |

DELETE `/usuario/{id}` → 204. Los pedidos del usuario permanecen en el sistema.

| ID | Regla |
|----|-------|
| RN-010-01 | La eliminación es lógica (soft delete) |
| RN-010-02 | Los pedidos del usuario NO se eliminan |
| RN-010-03 | El usuario no puede autenticarse después de eliminado |

---

### EP-03: Gestión de Productos

---

#### HU-011: Crear Producto

| Campo | Valor |
|-------|-------|
| **ID** | HU-011 |
| **Prioridad** | Crítica |
| **Story Points** | 5 |
| **Sprint** | 2 |

**Request:**
```json
{
  "nombre": "string (obligatorio, 2-100 caracteres)",
  "precio": "decimal (obligatorio, > 0.01)",
  "descripcion": "string (opcional, máx 500 caracteres)",
  "stock": "integer (obligatorio, >= 0)",
  "imagen": "string (opcional)",
  "disponible": "boolean (opcional, default: true)",
  "idCategoria": "long (obligatorio)"
}
```

**Response:**
```json
{
  "id": 1,
  "nombre": "Laptop Gaming Pro",
  "precio": 1599.99,
  "descripcion": "Laptop de alto rendimiento",
  "stock": 25,
  "imagen": "laptop.jpg",
  "disponible": true,
  "categoria": {
    "id": 1,
    "nombre": "Electrónica",
    "descripcion": "Productos electrónicos"
  }
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-011-01 | El nombre es obligatorio (2-100 caracteres) |
| RN-011-02 | El precio es obligatorio y mayor a 0.01 |
| RN-011-03 | El stock debe ser >= 0 |
| RN-011-04 | La categoría es obligatoria y debe existir |
| RN-011-05 | Si no se especifica `disponible`, es `true` por defecto |
| RN-011-06 | El precio se almacena como BigDecimal |

---

#### HU-012: Listar Productos

| Campo | Valor |
|-------|-------|
| **ID** | HU-012 |
| **Story Points** | 2 |
| **Sprint** | 2 |

GET `/producto` → 200 OK con array de productos activos con su categoría. Lista vacía → `[]`.

---

#### HU-013: Obtener Producto por ID

| Campo | Valor |
|-------|-------|
| **ID** | HU-013 |
| **Story Points** | 2 |
| **Sprint** | 2 |

GET `/producto/{id}` → 200 OK con datos completos y categoría | 404 si no existe.

---

#### HU-014: Listar Productos por Categoría

| Campo | Valor |
|-------|-------|
| **ID** | HU-014 |
| **Story Points** | 3 |
| **Sprint** | 2 |

GET `/producto/categoria/{id}` → productos de esa categoría | 404 si categoría no existe.

| ID | Regla |
|----|-------|
| RN-014-01 | La categoría debe existir |
| RN-014-02 | Solo se retornan productos con `eliminado = false` |
| RN-014-03 | Se incluyen productos disponibles e indisponibles |

---

#### HU-015: Actualizar Producto

| Campo | Valor |
|-------|-------|
| **ID** | HU-015 |
| **Story Points** | 5 |
| **Sprint** | 2 |

PUT `/producto/{id}` con campos opcionales. Actualización parcial.

| ID | Regla |
|----|-------|
| RN-015-01 | Solo se actualizan los campos enviados |
| RN-015-02 | Si se cambia categoría, debe existir |
| RN-015-03 | El precio debe ser > 0.01 si se actualiza |
| RN-015-04 | El stock debe ser >= 0 si se actualiza |

---

#### HU-016: Eliminar Producto (Soft Delete)

| Campo | Valor |
|-------|-------|
| **ID** | HU-016 |
| **Story Points** | 2 |
| **Sprint** | 2 |

DELETE `/producto/{id}` → 204. Los detalles de pedido históricos mantienen la referencia.

---

### EP-04: Gestión de Pedidos

---

#### HU-017: Crear Pedido

| Campo | Valor |
|-------|-------|
| **ID** | HU-017 |
| **Prioridad** | Crítica |
| **Story Points** | 8 |
| **Sprint** | 3 |

**Como** Usuario registrado  
**Quiero** poder crear un pedido con los productos seleccionados  
**Para** realizar una compra en el sistema

**Request:**
```json
{
  "estado": "PENDIENTE | CONFIRMADO | TERMINADO | CANCELADO",
  "formaPago": "TARJETA | TRANSFERENCIA | EFECTIVO",
  "idUsuario": "long (obligatorio)",
  "detallePedido": [
    {
      "idProducto": "long (obligatorio)",
      "cantidad": "integer (obligatorio, >= 1)"
    }
  ]
}
```

**Reglas de Negocio:**

| ID | Regla |
|----|-------|
| RN-017-01 | El usuario debe existir |
| RN-017-02 | El estado es obligatorio |
| RN-017-03 | La forma de pago es obligatoria |
| RN-017-04 | Debe haber al menos un detalle de pedido |
| RN-017-05 | Cada producto debe existir |
| RN-017-06 | Cada producto debe estar disponible (`disponible = true`) |
| RN-017-07 | Cada producto debe tener stock suficiente |
| RN-017-08 | La cantidad de cada detalle debe ser >= 1 |
| RN-017-09 | El subtotal se calcula: `precio * cantidad` |
| RN-017-10 | El total se calcula: suma de todos los subtotales |
| RN-017-11 | La fecha se establece automáticamente |
| RN-017-12 | El stock se reduce al crear el pedido |
| RN-017-13 | Si alguna validación falla, se hace rollback completo |

> Usar `@Transactional` para asegurar atomicidad.

---

#### HU-018: Listar Pedidos

| Campo | Valor |
|-------|-------|
| **ID** | HU-018 |
| **Story Points** | 2 |
| **Sprint** | 3 |

GET `/pedido` (admin) → todos los pedidos con detalles. Lista vacía → `[]`.

---

#### HU-019: Obtener Pedido por ID

| Campo | Valor |
|-------|-------|
| **ID** | HU-019 |
| **Story Points** | 2 |
| **Sprint** | 3 |

GET `/pedido/{id}` → 200 OK con datos completos y detalles | 404 si no existe.

---

#### HU-020: Listar Pedidos por Usuario

| Campo | Valor |
|-------|-------|
| **ID** | HU-020 |
| **Story Points** | 3 |
| **Sprint** | 3 |

GET `/pedido/usuario/{id}` → pedidos del usuario | 404 si usuario no existe.

| ID | Regla |
|----|-------|
| RN-020-01 | El usuario debe existir |
| RN-020-02 | Solo se retornan pedidos con `eliminado = false` |
| RN-020-03 | Solo se retornan pedidos del usuario especificado |

---

#### HU-021: Actualizar Estado de Pedido

| Campo | Valor |
|-------|-------|
| **ID** | HU-021 |
| **Story Points** | 3 |
| **Sprint** | 3 |

PUT `/pedido/{id}` con `estado` y/o `formaPago` opcionales.

| ID | Regla |
|----|-------|
| RN-021-01 | Solo se actualizan estado y formaPago |
| RN-021-02 | Los detalles del pedido NO se pueden modificar |
| RN-021-03 | El total NO se recalcula |
| RN-021-04 | Campos nulos no modifican el valor existente |

---

#### HU-022: Eliminar Pedido (Soft Delete)

| Campo | Valor |
|-------|-------|
| **ID** | HU-022 |
| **Story Points** | 2 |
| **Sprint** | 3 |

DELETE `/pedido/{id}` → 204. Los detalles permanecen. El stock NO se restaura.

---

### EP-05: Infraestructura y Arquitectura

---

#### HU-023: Implementar Entidad Base

| Campo | Valor |
|-------|-------|
| **ID** | HU-023 |
| **Prioridad** | Crítica |
| **Story Points** | 5 |
| **Sprint** | 1 |

Clase abstracta `Base` con `@MappedSuperclass`:
- `id` con `@Id` y `@GeneratedValue(IDENTITY)`
- `eliminado` con `@Builder.Default = false`
- `createdAt` con `@CreationTimestamp`
- `updatedAt` con `@UpdateTimestamp`
- `version` con `@Version` (optimistic locking)
- Usar `@SuperBuilder` para herencia de builders

---

#### HU-024: Implementar Repositorio Base

| Campo | Valor |
|-------|-------|
| **ID** | HU-024 |
| **Prioridad** | Crítica |
| **Story Points** | 5 |
| **Sprint** | 1 |

Interfaz `BaseRepository<E extends Base, ID>` que:
- Extienda de `JpaRepository`
- Override `findAll()` para filtrar eliminados
- Proporcione `findByIdOrThrow()` con excepción
- Implemente `deleteById()` como soft delete
- Marcar `deleteById` con `@Transactional` y `@Modifying`

---

#### HU-025: Implementar Manejo Global de Excepciones

| Campo | Valor |
|-------|-------|
| **ID** | HU-025 |
| **Prioridad** | Crítica |
| **Story Points** | 5 |
| **Sprint** | 1 |

`GlobalExceptionHandler` con `@RestControllerAdvice`:

| Excepción | Código HTTP |
|-----------|-------------|
| `ResourceNotFoundException` | 404 Not Found |
| `BusinessException` | 400 Bad Request |
| `MethodArgumentNotValidException` | 400 con detalle de campos |
| `IllegalStateException` | 409 Conflict |
| `Exception` (genérica) | 500 Internal Server Error |

---

#### HU-026: Implementar Encriptación de Contraseñas

| Campo | Valor |
|-------|-------|
| **ID** | HU-026 |
| **Prioridad** | Crítica |
| **Story Points** | 3 |
| **Sprint** | 1 |

Componente `PasswordEncoder` con BCrypt:
- `encode(String password)`
- `matches(String raw, String encoded)`

---

#### HU-027: Implementar Carga Inicial de Datos

| Campo | Valor |
|-------|-------|
| **ID** | HU-027 |
| **Story Points** | 2 |
| **Sprint** | 1 |

`UserLoad` implementa `CommandLineRunner`. Si no existen usuarios, crea:
- Email: `admin@admin.com`
- Password: `123456` (encriptado)
- Rol: `ADMIN`

---

#### HU-028: Configurar Documentación OpenAPI

| Campo | Valor |
|-------|-------|
| **ID** | HU-028 |
| **Story Points** | 2 |
| **Sprint** | 1 |

SpringDoc OpenAPI con Swagger UI accesible en `/swagger-ui/index.html` y especificación en `/api-docs`.

---

#### HU-029: Configurar CORS

| Campo | Valor |
|-------|-------|
| **ID** | HU-029 |
| **Story Points** | 1 |
| **Sprint** | 1 |

Agregar `@CrossOrigin("*")` en todos los controladores. Considerar configuración más restrictiva para producción.

---

## Matriz de Trazabilidad

### Historias de Usuario por Épica

| Épica | Historias | Total HU | Story Points |
|-------|-----------|----------|-------------|
| EP-01: Categorías | HU-001 a HU-005 | 5 | 12 |
| EP-02: Usuarios | HU-006 a HU-010 | 5 | 16 |
| EP-03: Productos | HU-011 a HU-016 | 6 | 19 |
| EP-04: Pedidos | HU-017 a HU-022 | 6 | 20 |
| EP-05: Infraestructura | HU-023 a HU-029 | 7 | 23 |
| **TOTAL** | | **29** | **90** |

### Dependencias entre Historias

```
HU-023 (Base Entity)
└── HU-024 (Base Repository)
    ├── HU-001 a HU-005 (Categoría)
    ├── HU-006 a HU-010 (Usuario)
    │   └── HU-026 (Password Encoder)
    │       └── HU-027 (User Load)
    ├── HU-011 a HU-016 (Producto)
    │   └── Depende de Categoría
    └── HU-017 a HU-022 (Pedido)
        └── Depende de Usuario y Producto

HU-025 (Exception Handler)
└── Usado por todas las HU de endpoints
```

---

## Priorización del Backlog

### Sprint 1 - Fundamentos (Semana 1-2)

| Prioridad | Historia | Story Points | Descripción |
|-----------|----------|-------------|-------------|
| 1 | HU-023 | 5 | Entidad Base |
| 2 | HU-024 | 5 | Repositorio Base |
| 3 | HU-025 | 5 | Manejo de Excepciones |
| 4 | HU-026 | 3 | Encriptación |
| 5 | HU-001 | 3 | Crear Categoría |
| 6 | HU-002 | 2 | Listar Categorías |
| 7 | HU-003 | 2 | Obtener Categoría |
| 8 | HU-004 | 3 | Actualizar Categoría |
| 9 | HU-005 | 2 | Eliminar Categoría |
| 10 | HU-006 | 5 | Registrar Usuario |
| 11 | HU-027 | 2 | Carga Inicial |
| 12 | HU-028 | 2 | OpenAPI |
| 13 | HU-029 | 1 | CORS |
| **Total** | | **40** | |

### Sprint 2 - Usuarios y Productos (Semana 3-4)

| Prioridad | Historia | Story Points | Descripción |
|-----------|----------|-------------|-------------|
| 1 | HU-007 | 2 | Listar Usuarios |
| 2 | HU-008 | 2 | Obtener Usuario |
| 3 | HU-009 | 5 | Actualizar Usuario |
| 4 | HU-010 | 2 | Eliminar Usuario |
| 5 | HU-011 | 5 | Crear Producto |
| 6 | HU-012 | 2 | Listar Productos |
| 7 | HU-013 | 2 | Obtener Producto |
| 8 | HU-014 | 3 | Productos por Categoría |
| 9 | HU-015 | 5 | Actualizar Producto |
| 10 | HU-016 | 2 | Eliminar Producto |
| **Total** | | **30** | |

### Sprint 3 - Pedidos (Semana 5-6)

| Prioridad | Historia | Story Points | Descripción |
|-----------|----------|-------------|-------------|
| 1 | HU-017 | 8 | Crear Pedido |
| 2 | HU-018 | 2 | Listar Pedidos |
| 3 | HU-019 | 2 | Obtener Pedido |
| 4 | HU-020 | 3 | Pedidos por Usuario |
| 5 | HU-021 | 3 | Actualizar Pedido |
| 6 | HU-022 | 2 | Eliminar Pedido |
| **Total** | | **20** | |

---

## Definición de Completado (DoD)

Una Historia de Usuario se considera **COMPLETADA** cuando:

### Código
- [ ] El código fuente está escrito y sigue los estándares del proyecto
- [ ] El código compila sin errores ni warnings
- [ ] Se utilizan los patrones de diseño definidos (DTO, Service, Repository)
- [ ] Se implementan todas las validaciones específicas
- [ ] Se manejan correctamente las excepciones

### Testing
- [ ] Tests unitarios escritos con cobertura >= 80%
- [ ] Tests de integración para endpoints
- [ ] Todos los tests pasan exitosamente
- [ ] Se probaron todos los escenarios de los criterios de aceptación

### Documentación
- [ ] Endpoint documentado automáticamente en Swagger
- [ ] Código comentado donde sea necesario
- [ ] README actualizado si aplica

### Revisión
- [ ] Code review aprobado por al menos un compañero
- [ ] No hay deuda técnica identificada sin documentar
- [ ] El código está integrado en la rama principal

### Funcionalidad
- [ ] La funcionalidad cumple todos los criterios de aceptación
- [ ] No hay regresiones en funcionalidades existentes
- [ ] La API retorna los códigos HTTP correctos

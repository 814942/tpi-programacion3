# Food Store - Documentación Técnica

**Sistema de Gestión de Pedidos de Comida**

| Campo | Valor |
|-------|-------|
| **Institución** | Universidad Tecnológica Nacional |
| **Carrera** | Tecnicatura Universitaria en Programación |
| **Materia** | Programación III |
| **Alumno** | Pablo Garay |
| **Fecha** | Mayo 2026 |

---

## Índice

1. [Marco Teórico](#1-marco-teórico)
2. [Arquitectura del Sistema](#2-arquitectura-del-sistema)
3. [Decisiones Técnicas](#3-decisiones-técnicas)
4. [Stack Tecnológico](#4-stack-tecnológico)
5. [Modelo de Datos](#5-modelo-de-datos)
6. [API REST](#6-api-rest)
7. [Frontend - Páginas y Flujos](#7-frontend)
8. [Seguridad](#8-seguridad)
9. [Bitácora de Desarrollo](#9-bitácora-de-desarrollo)
10. [Dificultades y Soluciones](#10-dificultades-y-soluciones)
11. [Wrap-Up Final](#11-wrap-up-final)
12. [Capturas de Pantalla](#12-capturas-de-pantalla)

---

## 1. Marco Teórico

### 1.1 Arquitectura de Capas (Layered Architecture)

El backend sigue el patrón **Controller → Service → Repository**, separando responsabilidades en tres capas:

- **Controller**: Recibe requests HTTP, delega a Service, retorna DTOs. No contiene lógica de negocio.
- **Service**: Contiene las reglas de negocio, validaciones y manejo transaccional.
- **Repository**: Acceso a datos via Spring Data JPA, encapsula queries.

### 1.2 DTO (Data Transfer Object)

Los DTOs son records de Java 17 que transportan datos entre capas sin exponer las entidades JPA. Separamos en:
- **Request DTOs**: Validación de entrada con Jakarta Validation
- **Response DTOs**: Salida sin datos sensibles (passwords, etc.)

### 1.3 Soft Delete

Eliminación lógica: en vez de borrar físicamente, se marca `eliminado = true`. Esto:
- Preserva integridad referencial en pedidos históricos
- Permite recuperación de datos
- El `BaseRepository` filtra automáticamente `eliminado = false`

### 1.4 JWT (JSON Web Token)

Token stateless para autenticación. El backend firma con HMAC-SHA256 y el frontend:
- Almacena el token en `localStorage`
- Lo envía en cada request vía header `Authorization: Bearer`
- Verifica expiración decodificando el payload con `atob()`

### 1.5 SPA con Vanilla TypeScript

Single Page Application construida sin frameworks. Cada página es un HTML independiente con su propio módulo TypeScript. La navegación usa `window.location.href` (full page reload). Esto es intencional: para un proyecto educativo sin framework, evita la complejidad de un router SPA.

---

## 2. Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENTE                              │
│  TypeScript 5 + Vite 5                                      │
│                                                             │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌───────────────┐  │
│  │   Auth   │ │  Store   │ │   Cart   │ │ Admin Panel   │  │
│  │ login/reg│ │ catalog  │ │ check-   │ │ dashboard     │  │
│  │          │ │ product  │ │ out      │ │ CRUDs         │  │
│  └──────────┘ └──────────┘ └──────────┘ └───────────────┘  │
│         │           │            │              │           │
│         └───────────┴────────────┴──────────────┘           │
│                           │ HTTP (fetch)                    │
│                      ┌────┴────┐                           │
│                      │ api.ts  │  (JWT injector)            │
│                      └────┬────┘                           │
└───────────────────────────┼─────────────────────────────────┘
                            │ REST API (JSON)
┌───────────────────────────┼─────────────────────────────────┐
│                    BACKEND (Spring Boot 3.x)                 │
│                            │                                 │
│  ┌──────────┐  ┌──────────┴──────────┐  ┌──────────────┐   │
│  │ Security │  │     Controller      │  │ OpenAPI/Sw. │   │
│  │  Config  │  │  Auth | Categoria  │  │             │   │
│  │ JWT filt │  │ Producto | Pedido  │  │   CORS      │   │
│  └──────────┘  │ Usuario | Admin    │  └──────────────┘   │
│                └──────────┬──────────┘                     │
│                ┌──────────┴──────────┐                     │
│                │      Service        │                     │
│                │  (lógica de negocio)│                     │
│                └──────────┬──────────┘                     │
│                ┌──────────┴──────────┐                     │
│                │     Repository      │                     │
│                │   (Spring Data JPA) │                     │
│                └──────────┬──────────┘                     │
│                ┌──────────┴──────────┐                     │
│                │  PostgreSQL / H2    │                     │
│                └─────────────────────┘                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 3. Decisiones Técnicas

| Decisión | Opción | Justificación |
|----------|--------|---------------|
| **Vanilla TS sin framework** | Sin React/Angular | Demostrar dominio de TypeScript puro, evita overhead de framework en proyecto educativo |
| **JWT en localStorage** | En vez de solo localStorage mock | La consigna sugería sin JWT, pero JWT stateless es el estándar actual y demuestra seguridad real |
| **BCrypt en vez de SHA-256** | BCrypt via Spring Security | BCrypt es el estándar para almacenar contraseñas (incluye salt automático) |
| **@PreAuthorize** | Method-level security | Control de acceso declarativo, consistente y testeable |
| **PaginatedResponse universal** | Todos los GET list retornan paginación | Escalabilidad y consistencia en la API |
| **Snapshot en DetallePedido** | Campos duplicados (nombre, precio) | Preserva el valor histórico del producto al momento de la compra |
| **Múltiples HTML** | 1 HTML por página | Sin framework, es la forma más directa de tener páginas independientes |
| **Header unificado** | initHeader() en todas las páginas | Consistencia visual sin duplicar lógica |
| **innerHTML prohibido** | Métodos DOM exclusivamente | Previene XSS, mejor rendimiento y control |

---

## 4. Stack Tecnológico

### Backend

| Capa | Tecnología |
|------|------------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.3 |
| Build | Gradle 8.x |
| Base de datos | PostgreSQL 15+ / H2 (test) |
| Persistencia | Spring Data JPA + Hibernate |
| Seguridad | Spring Security + jjwt 0.12.5 |
| Documentación | SpringDoc OpenAPI 2.5.0 |
| Testing | JUnit 5 + Mockito + MockMvc |

### Frontend

| Capa | Tecnología |
|------|------------|
| Lenguaje | TypeScript 5.4 (strict mode) |
| Build | Vite 5.4 |
| HTTP | Fetch API nativo |
| Testing | Vitest + jsdom |
| Estilos | CSS3 con variables |
| Dependencias externas | **0** (cero librerías) |

---

## 5. Modelo de Datos

### Entidades

```
Base (abstracta)
├── id: Long (PK, auto)
├── eliminado: boolean
├── createdAt: LocalDateTime
├── updatedAt: LocalDateTime
└── version: Integer (@Version)

Categoria extends Base
├── nombre: String (2-100 chars)
├── descripcion: String (max 500)
└── imagen: String

Producto extends Base
├── nombre: String (2-100)
├── precio: BigDecimal (> 0.01)
├── descripcion: String (max 500)
├── stock: Integer (>= 0)
├── imagen: String
├── disponible: Boolean
└── categoria: @ManyToOne → Categoria

Usuario extends Base
├── nombre: String
├── apellido: String
├── email: String (unique)
├── celular: String
├── password: String (BCrypt)
└── rol: Rol (ADMIN | USUARIO)

Pedido extends Base
├── fecha: LocalDateTime
├── estado: Estado (PENDIENTE/CONFIRMADO/TERMINADO/CANCELADO)
├── formaPago: FormaPago (TARJETA/TRANSFERENCIA/EFECTIVO)
├── total: BigDecimal
├── usuario: @ManyToOne → Usuario
└── detalles: @OneToMany → DetallePedido

DetallePedido extends Base
├── productoId: Long (snapshot)
├── productoNombre: String (snapshot)
├── productoPrecio: BigDecimal (snapshot)
├── productoImagen: String (snapshot)
├── cantidad: Integer
└── subtotal: BigDecimal
```

---

## 6. API REST

Base path: `/api/v1`

### Autenticación

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | `/auth/login` | Público | Login, retorna JWT |
| POST | `/auth/register` | Público | Registro, retorna JWT |

### Productos

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/productos` | Authenticated | Lista paginada con búsqueda y sort |
| GET | `/productos/{id}` | Authenticated | Detalle |
| GET | `/productos/categoria/{id}` | Authenticated | Por categoría |
| POST | `/productos` | ADMIN | Crear |
| PUT | `/productos/{id}` | ADMIN | Actualizar |
| DELETE | `/productos/{id}` | ADMIN | Soft delete |
| POST | `/productos/validate` | Authenticated | Validar productos en masa |

### Categorías

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/categorias` | Authenticated | Lista paginada |
| GET | `/categorias/{id}` | Authenticated | Detalle |
| POST | `/categorias` | ADMIN | Crear |
| PUT | `/categorias/{id}` | ADMIN | Actualizar |
| DELETE | `/categorias/{id}` | ADMIN | Soft delete |

### Pedidos

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | `/pedidos` | USUARIO | Crear pedido transaccional |
| GET | `/pedidos` | ADMIN | Listar todos paginado |
| GET | `/pedidos/{id}` | Owner o ADMIN | Detalle |
| GET | `/pedidos/usuario` | Authenticated | Pedidos del usuario actual |
| PATCH | `/pedidos/{id}/estado` | ADMIN | Cambiar estado |
| PATCH | `/pedidos/{id}/cancelar` | Owner o ADMIN | Cancelar |

### Usuarios

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/usuarios` | ADMIN | Listar paginado |
| GET | `/usuarios/{id}` | ADMIN | Detalle |
| PUT | `/usuarios/{id}` | ADMIN | Actualizar |
| DELETE | `/usuarios/{id}` | ADMIN | Soft delete |

### Admin

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| GET | `/admin/stats` | ADMIN | Estadísticas del dashboard |

---

## 7. Frontend

### Páginas

| Ruta | Archivo | Descripción |
|------|---------|-------------|
| `/` | `client/index.html` | Landing page |
| `/src/pages/auth/login/` | `login/index.html` + `login.ts` | Login con JWT |
| `/src/pages/auth/registro/` | `registro/index.html` + `registro.ts` | Registro |
| `/src/pages/client/` | `client/index.html` + `client.ts` | Catálogo store |
| `/src/pages/store/productDetail/` | `productDetail/index.html` + `productDetail.ts` | Detalle producto |
| `/src/pages/client/cart/` | `cart/index.html` + `cart.ts` | Carrito + checkout |
| `/src/pages/client/checkout/` | `resultado.html` + `resultado.ts` | Resultado checkout |
| `/src/pages/client/orders/` | `orders/index.html` + `orders.ts` | Historial pedidos |
| `/src/pages/admin/` | `admin/index.html` + `admin.ts` | Dashboard admin |
| `/src/pages/admin/categories/` | `categories/index.html` + `categories.ts` | CRUD categorías |
| `/src/pages/admin/products/` | `products/index.html` + `products.ts` | CRUD productos |
| `/src/pages/admin/orders/` | `orders/index.html` + `orders.ts` | Gestión pedidos |
| `/src/pages/admin/users/` | `users/index.html` + `users.ts` | Gestión usuarios |
| `/src/pages/auth/forbidden/` | `forbidden/index.html` | Acceso denegado |

### Flujo de Compra (Cliente)

```
1. Login/Register → obtiene JWT
2. Navega catálogo (filtra por categoría, busca, ordena)
3. Click producto → detalle (cantidad + agregar al carrito)
4. Click icono carrito → /cart/
5. Validación automática: POST /productos/validate
6. Si productos inválidos: warning amarillo, checkout bloqueado
7. Elimina inválidos → checkout se re-habilita
8. Click "Finalizar Compra" → modal (forma pago + teléfono)
9. Re-validación contra API
10. POST /pedidos → pedido creado
11. Redirige a resultado: verde éxito / rojo error
12. "Mis Pedidos" → historial con detalle y cancelación
```

### Flujo de Administración

```
1. Login como admin (admin@admin.com / 123456)
2. Dashboard con stats (categorías, productos, pedidos)
3. Sidebar: Categorías | Productos | Pedidos | Usuarios
4. CRUD completo con modales, búsqueda, paginación
5. Pedidos: filtro por estado, cambio de estado en detalle
```

---

## 8. Seguridad

### Implementado

- **JWT Stateless**: Firma HMAC-SHA256, expiración 24h
- **BCrypt**: Contraseñas hasheadas con salt automático
- **@PreAuthorize**: Control de acceso a nivel de método
- **Soft Delete**: Nunca se borran datos físicamente
- **DTOs sin password**: Las respuestas nunca incluyen contraseñas
- **XSS prevention**: Uso exclusivo de `textContent` en vez de `innerHTML`
- **CORS**: Permitido solo origen específico (`localhost:5173`)
- **Validación Jakarta**: En todos los request DTOs
- **Validación cliente**: Antes de enviar al servidor

### Consideraciones

> La consigna original indicaba que la validación de rol fuera "solo frontend" y que no se usaran tokens JWT. En esta implementación se fue más allá, implementando JWT real con Spring Security y @PreAuthorize en cada endpoint. Esto hace que el sistema sea más seguro que lo requerido, pero puede diferir de lo esperado en la consigna.

---

## 9. Bitácora de Desarrollo

### Sprint 1: Fundamentos (Backend)

| Fecha | Actividad |
|-------|-----------|
| 16-05 | Infraestructura Spring Boot, entidades, repositorios |
| 17-05 | Auth JWT, login/register |
| 18-05 | CRUD Categorías |
| 19-05 | CRUD Productos + Pedidos transaccionales |

### Sprint 2: Frontend Core

| Fecha | Actividad |
|-------|-----------|
| 20-05 | Migración auth de localStorage a JWT |
| 20-05 | Header unificado con user dropdown + cart badge |
| 21-05 | Store: layout refactor + catálogo API + paginación |
| 21-05 | Store: detalle de producto |
| 21-05 | Cart: página 2 columnas + validación + checkout |
| 21-05 | Órdenes: historial con agrupado por fecha |

### Sprint 3: Admin Panel

| Fecha | Actividad |
|-------|-----------|
| 22-05 | Dashboard + CRUD Categorías |
| 23-05 | CRUD Productos |
| 23-05 | Gestión Pedidos + Usuarios |
| 23-05 | Sorting catálogo + tests frontend |

---

## 10. Dificultades y Soluciones

### 1. Layout Grid Roto en Store

**Problema**: El grid de 2 columnas del store no funcionaba porque los hijos tenían `grid-column: 1 / -1` forzado.

**Solución**: Se envolvió el formulario de búsqueda y el grid de productos en un `<div class="contenido-principal">` y se eliminaron los `grid-column` forzados.

### 2. Redirect Loop a Forbidden

**Problema**: Al entrar a la app, se redirigía a forbidden en loop infinito.

**Solución**: El `index.html` tenía un inline script legacy que usaba la key vieja `userData` de localStorage. Al migrar a JWT, esta key ya no existía pero el script seguía redirigiendo a páginas protegidas, donde el nuevo sistema auth detectaba falta de JWT y redirigía a forbidden. Se eliminaron los scripts legacy.

### 3. Roles Desalineados

**Problema**: El frontend usaba `'admin'/'client'` y el backend `'ADMIN'/'USUARIO'`.

**Solución**: Se alinearon todos los roles al formato del backend (`ADMIN` / `USUARIO`) y se actualizaron todas las comparaciones.

### 4. Sin Framework para Componentes

**Problema**: Sin React, no hay componentes reutilizables. El header se duplicaba en cada página.

**Solución**: Se creó `header.ts` con una función `initHeader()` que cada página llama en su `DOMContentLoaded`. El HTML/CSS del header se copia en cada página, pero la lógica JS es compartida.

### 5. Confirmación sin `confirm()`

**Problema**: La consigna no prohibe `confirm()` pero es mala UX. Se reemplazó con toggles inline.

**Solución**: En lugar de `confirm()`, los botones de "Eliminar"/"Cancelar" cambian su texto a "¿Seguro?" y aparece un botón "No" al lado. Si el usuario confirma, se ejecuta la acción.

---

## 11. Wrap-Up Final

### Cobertura vs Consigna

| Épica | HU | Estado |
|-------|-----|--------|
| EP-01 Categorías | HU-001 a 005 | ✅ Completo |
| EP-02 Usuarios | HU-006 a 010 | ✅ Completo |
| EP-03 Productos | HU-011 a 016 | ✅ Completo |
| EP-04 Pedidos | HU-017 a 022 | ✅ Completo |
| EP-05 Infraestructura | HU-023 a 029 | ✅ Completo |

### Over-Delivery

| Feature | Consigna | Realidad |
|---------|----------|----------|
| Autenticación | localStorage | JWT real + Spring Security |
| Hash contraseñas | SHA-256 | BCrypt |
| Paginación | No mencionada | PaginatedResponse en todos los GET |
| Admin CRUD usuarios | HU-007 a 010 | ✅ Frontend admin completo |
| Validación productos | No existe | POST /productos/validate |
| Stats admin | No existe | GET /admin/stats |
| Toast notifications | No existe | Sistema de toasts custom |
| Confirmación | `confirm()` | Toggle inline |
| innerHTML | Sin restricción | Prohibido, solo DOM methods |
| Tests frontend | Ninguno | 14 tests con Vitest |

### Gaps

| Item | Estado | Riesgo |
|------|--------|--------|
| Documentación PDF | ✅ Completada | Bajo |
| Video demostración | ❌ Pendiente | **Alto** (requerido) |
| Sorting en UI | ✅ Agregado | Bajo |
| Tests frontend | ✅ 14 tests | Bajo |

### Estadísticas

| Métrica | Backend | Frontend |
|---------|---------|----------|
| Archivos | ~30 | ~30 |
| Tests | 21 | 14 |
| Endpoints | ~20 | — |
| Páginas | — | 13 |
| Librerías externas | Spring Boot + 4 | **0** |

---

## 12. Capturas de Pantalla

*(Insertar capturas de pantalla del sistema funcionando)*

### Cliente
1. Login/Registro
2. Catálogo con categorías y paginación
3. Detalle de producto con selector de cantidad
4. Carrito con validación de productos
5. Checkout modal
6. Historial de pedidos

### Admin
7. Dashboard con estadísticas
8. CRUD Categorías (tabla + modal)
9. CRUD Productos (tabla con filtro + modal)
10. Gestión de Pedidos (detalle + cambio de estado)
11. Gestión de Usuarios

---

> Documentación generada el 23 de mayo de 2026

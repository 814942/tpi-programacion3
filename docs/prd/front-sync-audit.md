# Frontend - Sync Audit

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

El frontend actual se construyó usando localStorage como backend simulado y con datos mockeados. Necesitamos un relevamiento completo de qué páginas existen, qué funcionalidad tienen y qué necesitan ser creadas/modificadas para conectarse a la API REST real con autenticación JWT.

Este documento NO es un PRD de nueva funcionalidad, sino un **inventario + plan de migración**.

---

## 2. Estado Actual del Frontend

### Páginas que existen

| Página | Ruta | Estado | Observaciones |
|--------|------|--------|---------------|
| **Index** | `/index.html` | ✅ Existe | Redirección a login/registro |
| **Login** | `/src/pages/auth/login/` | ✅ Existe | Formulario + validación, usa localStorage |
| **Registro** | `/src/pages/auth/registro/` | ✅ Existe | Formulario + validación, usa localStorage |
| **Forbidden** | `/src/pages/auth/forbidden/` | ✅ Existe | Página de acceso denegado |
| **Admin** | `/src/pages/admin/` | ✅ Existe | Dashboard placeholder + logout |
| **Client** | `/src/pages/client/` | ✅ Existe | Catálogo + categorías + búsqueda + carrito |

### Utilidades que existen

| Archivo | Función actual | Conexión a API |
|---------|----------------|----------------|
| `src/utils/auth.ts` | Login/Register/logout con localStorage | ❌ No, usa localStorage |
| `src/utils/navigate.ts` | Route guard, redirecciones | ❌ No, verifica localStorage |
| `src/utils/index.ts` | Re-export | ❌ |
| `src/main.ts` | Route guard inicial | ❌ |
| `src/types/IUser.ts` | Interfaces de usuario | ❌ Mock |
| `src/types/Role.ts` | Enum de roles | ❌ Mock |

### Funcionalidad del cliente (`client.ts`)

| Feature | Estado | Observaciones |
|---------|--------|---------------|
| Categorías hardcodeadas | ✅ Existe | Array `categories[]` en client.ts |
| Productos hardcodeados | ✅ Existe | Array `products[]` en client.ts |
| Renderizar categorías | ✅ Existe | `loadCategories()` |
| Renderizar productos | ✅ Existe | `loadProducts()` |
| Filtrar por categoría | ✅ Existe | `filterByCategory()` |
| Buscar productos | ✅ Existe | `configureSearch()` con filtro en memoria |
| Carrito en memoria | ✅ Existe | `addToCart()`, `updateCart()`, `removeFromCart()` |
| Carrito persistente | ❌ No | Se pierde al recargar |
| Detalle de producto | ❌ No | No hay página de detalle |
| Checkout / Confirmar pedido | ❌ No | No hay flujo de pago |
| Conexión a API real | ❌ No | Todo es mockeado |

---

## 3. Lo que Falta (Gap Analysis)

### Por crear desde cero

| Feature | Prioridad | PRD Relacionado |
|---------|-----------|-----------------|
| Página detalle de producto | Alta | front-store |
| Modal checkout + confirmación | Alta | front-store |
| Persistencia de carrito (localStorage) | Alta | front-store |
| Página historial de pedidos (cliente) | Alta | front-client-orders |
| Dashboard admin con estadísticas | Alta | front-admin |
| CRUD categorías (admin) | Alta | front-admin |
| CRUD productos (admin) | Alta | front-admin |
| Gestión pedidos (admin) | Alta | front-admin |
| Header/Nav responsive unificado | Media | Todos |

### Por modificar

| Archivo | Cambio necesario |
|---------|------------------|
| `auth.ts` | Reemplazar localStorage por llamadas a API REST con JWT |
| `navigate.ts` | Adaptar para redirigir según respuesta de API |
| `login.ts` | Llamar POST `/api/auth/login` en vez de localStorage |
| `registro.ts` | Llamar POST `/api/auth/register` en vez de localStorage |
| `admin/admin.ts` | Conectar a API real, agregar CRUD con paginación |
| `client/client.ts` | Reemplazar datos mock por GET `/api/productos?page=0&size=12` (paginado) |
| `client/index.html` | Agregar controles de paginación, enlace a detalle producto |
| `types/` | Actualizar interfaces para coincidir con DTOs del backend (incluir `PaginatedResponse<T>`) |
| `main.ts` | Route guard ahora depende de JWT (verificar token en localStorage) |
| `utils/` | Agregar api.ts (cliente HTTP con JWT en headers + manejo de PaginatedResponse) |

### Por eliminar

| Archivo | Razón |
|---------|-------|
| `pages/client/client.ts` | Refactor completo: datos mockeados reemplazados por API |
| Dependencia de `alert()` | Reemplazar por sistema de toasts |

---

## 4. Plan de Migración

### Fase 1: Base HTTP + JWT + Paginación
1. Crear `src/utils/api.ts` — cliente HTTP con fetch + JWT en headers
2. Actualizar `auth.ts` — login/register usan API, guardan token
3. Actualizar `navigate.ts` — leer token de localStorage
4. Actualizar `types/` — interfaces alineadas con DTOs backend (incluir `PaginatedResponse<T>`)
5. **Crear componente de paginación reutilizable** — botones anterior/siguiente, selector página, contador

### Fase 2: Store (Catálogo real con Paginación)
1. Adaptar `client.ts` — categorías y productos desde API paginada (`GET /api/v1/productos?page=0&size=12`)
2. Integrar controles de paginación en el catálogo
3. Búsqueda con query param `search` + reseteo a página 1
4. Carrito persistente en localStorage
5. Página detalle de producto (nueva)
6. Modal checkout (nuevo)

### Fase 3: Cliente - Pedidos con Paginación
1. Página historial de pedidos (nueva) con `GET /api/v1/pedidos/usuario?page=0&size=10`
2. Integrar controles de paginación en historial
3. Cancelar pedido desde el frontend

### Fase 4: Admin con Paginación Universal
1. Dashboard con stats desde API
2. CRUD categorías con paginación + búsqueda
3. CRUD productos con paginación + búsqueda + filtro categoría
4. Gestión pedidos con paginación + búsqueda + filtro estado
5. Gestión usuarios con paginación + búsqueda + filtro rol

### Fase 5: Polish
1. Header/Nav unificado
2. Sistema de toasts (reemplazar alerts)
3. Responsive y UX
4. Optimización de paginación (lazy loading, scroll infinito opcional)

---

## 5. Dependencias

| Dependencia | Tipo | Nota |
|-------------|------|------|
| back-infrastructure (JWT endpoints) | Externa - bloqueante para Fase 1 | — |
| back-auth-jwt (login/register) | Externa - bloqueante para Fase 1 | — |
| back-categories | Externa - bloqueante para Fase 2 | **✅ Paginados** (GET con PaginatedResponse) |
| back-products | Externa - bloqueante para Fase 2 | **✅ Paginados** (GET con PaginatedResponse) |
| back-orders | Externa - bloqueante para Fase 3 | **✅ Paginados** (GET con PaginatedResponse) |
| back-users | Externa - bloqueante para Fase 4 | **✅ Paginados** (GET con PaginatedResponse) |

**Cambio importante**: Todos los endpoints GET que devuelven colecciones ahora retornan `PaginatedResponse<T>` con `content`, `page`, `size`, `totalElements`, `totalPages`. El frontend debe implementar controles de paginación.

---

## 6. DoD

- [ ] Inventario validado contra código real
- [ ] Cada gap identificado tiene PRD asignado
- [ ] Dependencias entre fases documentadas
- [ ] API contract alineado entre frontend y backend
- [ ] Componentes de paginación implementados y reutilizables

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-05-16 | Pablo Garay | Initial draft |
| 0.2 | 2026-05-16 | Pablo Garay | Actualización: backend ahora usa PaginatedResponse en todos los endpoints GET de colecciones |

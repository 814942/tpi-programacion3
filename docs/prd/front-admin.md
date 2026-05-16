# Frontend - Admin Panel

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

El panel de administración actual es solo un placeholder con cards de navegación. Necesitamos un panel completo que incluya: dashboard con estadísticas, CRUD de categorías, CRUD de productos y gestión de pedidos. Todo conectado a la API REST y protegido para usuarios ADMIN.

---

## 2. Context & Background

- **Dashboard actual** en `/src/pages/admin/` con 3 cards placeholder (Productos, Usuarios, Pedidos)
- **Admin** se loguea con admin@admin.com / 123456 (seed data)
- **No hay CRUD** implementado en frontend
- **Datos mockeados**: TODO debe venir de API real

---

## 3. Goals

### Primary Goal
Panel administrativo completo con dashboard, CRUD de categorías y productos, y gestión de pedidos.

---

## 4. Target Users

- **Administrador (ADMIN)**: gestiona el sistema completo

---

## 5. User Stories

### US-01: Dashboard
**As a** Administrador
**I want** ver un dashboard con estadísticas del sistema
**So that** tener visibilidad del estado general

**Acceptance Criteria:**
- [ ] Sidebar de navegación: Dashboard, Categorías, Productos, Pedidos, Usuarios
- [ ] 4 tarjetas con stats: total categorías, productos, pedidos, productos disponibles
- [ ] Los números se cargan desde la API (GET /api/v1/categorias, /productos, /pedidos)
- [ ] Enlaces directos a cada módulo
- [ ] Header con info del admin + botón logout

### US-02: CRUD Categorías
**As a** Administrador
**I want** gestionar categorías desde el panel
**So that** mantener el catálogo organizado

**Acceptance Criteria:**
- [ ] Tabla con: ID, nombre, descripción, imagen (miniatura), acciones
- [ ] Botón "Nueva Categoría" → modal crear
- [ ] Modal crear: nombre (req), descripción (req), URL imagen (req)
- [ ] Modal editar: precargado con datos existentes
- [ ] Botón eliminar: confirmación antes de borrar
- [ ] DELETE lógico (soft delete)
- [ ] Toast de éxito/error en cada operación

### US-03: CRUD Productos
**As a** Administrador
**I want** gestionar productos desde el panel
**So that** mantener el catálogo de productos

**Acceptance Criteria:**
- [ ] Tabla con: ID, imagen (mini), nombre, descripción, precio, stock, categoría, disponible, acciones
- [ ] Botón "Nuevo Producto" → modal crear
- [ ] Modal crear: nombre, descripción, precio (> 0), stock (>= 0), categoría (select), URL imagen, checkbox "Disponible"
- [ ] Select de categorías cargado desde GET /api/v1/categorias
- [ ] Modal editar: precargado
- [ ] Eliminar con confirmación (soft delete)
- [ ] Filtro por categoría en la tabla

### US-04: Gestión de Pedidos
**As a** Administrador
**I want** gestionar pedidos desde el panel
**So that** cambiar estados y dar seguimiento

**Acceptance Criteria:**
- [ ] Lista de todos los pedidos (GET /api/v1/pedidos)
- [ ] Cada pedido muestra: ID, cliente, fecha, estado (badge), items, total
- [ ] Filtro por estado (PENDIENTE, CONFIRMADO, TERMINADO, CANCELADO)
- [ ] Click en pedido → modal detalle:
  - Datos del cliente (nombre, email, celular)
  - Lista de productos con snapshot
  - Total y forma de pago
  - Estado actual
- [ ] Select para cambiar estado + botón "Actualizar Estado" (PATCH)
- [ ] Ordenados por fecha descendente
- [ ] Estado vacío si no hay pedidos

### US-05: Gestión de Usuarios
**As a** Administrador
**I want** ver y gestionar usuarios desde el panel
**So that** administrar la base de usuarios

**Acceptance Criteria:**
- [ ] Tabla de usuarios (GET /api/v1/usuarios):
  - ID, nombre, apellido, email, celular, rol, fecha registro
  - Sin mostrar contraseña
- [ ] Editar usuario: modal con campos editables (nombre, apellido, email, celular, rol)
- [ ] Eliminar usuario: confirmación + soft delete
- [ ] Filtro por rol (ADMIN/USUARIO)

---

## 6. Functional Requirements

### FR-01: Navegación Admin
Sidebar con secciones: Dashboard, Categorías, Productos, Pedidos, Usuarios
Layout común con header + sidebar + content area

### FR-02: CRUD Categorías
Página `/src/pages/admin/categories/` con tabla + modales

### FR-03: CRUD Productos
Página `/src/pages/admin/products/` con tabla + modales + select de categorías

### FR-04: Gestión Pedidos
Página `/src/pages/admin/orders/` con lista + filtro + modal detalle + cambio de estado

### FR-05: Gestión Usuarios
Página `/src/pages/admin/users/` con tabla + modales

### FR-06: Estados de UI
- Loading spinner mientras cargan datos
- Empty state cuando no hay datos
- Error state cuando falla la API
- Toast notifications para éxito/error

---

## 7. User Flow

```mermaid
flowchart TD
    A[Admin logueado] --> B[Sidebar navegación]
    B --> C[Dashboard]
    B --> D[Categorías]
    B --> E[Productos]
    B --> F[Pedidos]
    B --> G[Usuarios]
    
    C --> C1[Stats: totales + enlaces]
    
    D --> D1[Tabla categorías]
    D1 --> D2[Nueva categoría]
    D1 --> D3[Editar]
    D1 --> D4[Eliminar]
    
    E --> E1[Tabla productos]
    E1 --> E2[Nuevo producto]
    E1 --> E3[Editar]
    E1 --> E4[Eliminar]
    
    F --> F1[Lista pedidos]
    F1 --> F2[Filtro por estado]
    F1 --> F3[Click → modal detalle]
    F3 --> F4[Cambiar estado]
    F4 --> F5[PATCH /api/v1/pedidos/{id}/estado]
    
    G --> G1[Tabla usuarios]
    G1 --> G2[Editar usuario]
    G1 --> G3[Eliminar usuario]
```

---

## 8. API / Interface Contracts

### GET `/api/v1/productos`
**Response 200:**
```json
[
  {
    "id": 1,
    "nombre": "Hamburguesa Triple",
    "precio": 25000.00,
    "stock": 50,
    "disponible": true,
    "categoria": { "id": 1, "nombre": "Hamburguesas" }
  }
]
```

### POST `/api/v1/productos`
**Request:**
```json
{
  "nombre": "string",
  "precio": 25000.00,
  "descripcion": "string",
  "stock": 50,
  "imagen": "string (URL)",
  "disponible": true,
  "idCategoria": 1
}
```

### GET `/api/v1/pedidos`
**Response 200:**
```json
[
  {
    "id": 1,
    "fecha": "2026-05-16T12:00:00",
    "estado": "PENDIENTE",
    "formaPago": "TARJETA",
    "total": 70000.00,
    "usuario": { "id": 2, "nombre": "Juan", "email": "juan@email.com" },
    "detalles": [...]
  }
]
```

### PATCH `/api/v1/pedidos/{id}/estado`
**Request:**
```json
{
  "estado": "CONFIRMADO"
}
```

---

## 9. Data Model (Frontend)

```typescript
interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string;
  imagen: string;
}

interface ProductoResponse {
  id: number;
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagen: string;
  disponible: boolean;
  categoria: { id: number; nombre: string };
}

interface PedidoResponse {
  id: number;
  fecha: string;
  estado: 'PENDIENTE' | 'CONFIRMADO' | 'TERMINADO' | 'CANCELADO';
  formaPago: 'TARJETA' | 'TRANSFERENCIA' | 'EFECTIVO';
  total: number;
  usuario: { id: number; nombre: string; email: string };
  detalles: DetallePedidoResponse[];
}
```

---

## 10. DoD (Definition of Done)

### Testing
- [ ] Dashboard carga stats correctamente
- [ ] CRUD categorías: crear, listar, editar, eliminar funcionan
- [ ] CRUD productos: crear con categoría, listar, editar, eliminar
- [ ] Select de categorías se carga correctamente en modal de producto
- [ ] Gestión pedidos: lista, filtrar, ver detalle, cambiar estado
- [ ] Gestión usuarios: listar, editar, eliminar
- [ ] Sidebar navegación cambia de sección correctamente
- [ ] Modal de confirmación antes de eliminar
- [ ] Estados: loading, empty, error en cada sección
- [ ] Toasts de éxito/error en cada operación

### Código
- [ ] Layout admin: sidebar + header + content area unificado
- [ ] Página dashboard con stats cards
- [ ] Página CRUD categorías
- [ ] Página CRUD productos
- [ ] Página gestión pedidos
- [ ] Página gestión usuarios
- [ ] Modales reutilizables para crear/editar
- [ ] Confirmación de eliminación
- [ ] Filtros en pedidos (por estado)
- [ ] Consumo de API via api.ts con JWT
- [ ] Sin `alert()`
- [ ] Loading, empty y error states

---

## 11. Out of Scope

- Reportes / gráficos en dashboard
- Exportar datos a CSV/Excel
- Rol super-admin
- Notificaciones en tiempo real

---

## 12. Dependencies

| Dependency | Type |
|------------|------|
| back-categories | API |
| back-products | API |
| back-orders | API |
| back-users | API |
| front-auth | Internal (JWT) |

---

## Changelog

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 0.1 | 2026-05-16 | Pablo Garay | Initial draft |

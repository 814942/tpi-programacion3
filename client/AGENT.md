# Client — Frontend SPA con Vite + TypeScript

**Parte del monorepo `tpi/`** junto a `back/`. Cada servicio tiene su propio AGENT.md.

## Stack

| Capa | Tecnología |
|------|------------|
| Build | Vite |
| Lenguaje | TypeScript (strict mode) |
| Estilos | CSS3 (sin preprocesador) |
| Routing | SPA con hash-based routing propio |
| HTTP | Fetch API nativo |

---

## Estructura de Directorios

```
client/                         # Servicio Frontend (monorepo tpi/)
├── index.html                  # Redirección a /#/login
├── package.json             # Dependencias y scripts
├── tsconfig.json            # TypeScript strict
├── vite.config.ts           # Config Vite
├── public/                  # Assets estáticos (favicon, robots.txt)
└── src/
    ├── main.ts              # Entry point
    ├── style.css            # Estilos globales + variables CSS
    ├── types/               # Definiciones de tipos TypeScript
    │   ├── user.ts
    │   ├── product.ts
    │   ├── cart.ts
    │   └── api.ts
    ├── utils/               # Utilidades y helpers
    │   ├── router.ts        # Hash-based router
    │   ├── api.ts           # Cliente HTTP
    │   ├── store.ts         # Estado global reactivo
    │   ├── auth.ts          # Helpers de auth (JWT)
    │   └── dom.ts           # Helpers de DOM (querySelector tipado)
    ├── components/           # Componentes reutilizables
    │   ├── Header.ts
    │   ├── Footer.ts
    │   ├── Button.ts
    │   ├── Modal.ts
    │   ├── Spinner.ts
    │   └── Toast.ts
    └── pages/
        ├── auth/
        │   ├── Login.ts
        │   └── Register.ts
        ├── store/
        │   ├── Home.ts
        │   ├── ProductDetail.ts
        │   └── Cart.ts
        ├── client/
        │   └── Orders.ts
        └── admin/
            ├── Dashboard.ts
            └── Products.ts
```

---

## Convenciones de Código

### TypeScript

```typescript
// types/user.ts

export interface User {
  id: string;
  email: string;
  name: string;
  role: 'client' | 'admin';
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}
```

```typescript
// utils/api.ts

const API_BASE = '/api/v1';

interface ApiError {
  message: string;
  status: number;
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error: ApiError = await response.json().catch(() => ({
      message: 'Error desconocido',
      status: response.status
    }));
    throw error;
  }
  return response.json();
}

function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export const api = {
  async get<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      headers: getAuthHeaders()
    });
    return handleResponse<T>(response);
  },

  async post<T>(endpoint: string, data: unknown): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(data)
    });
    return handleResponse<T>(response);
  },

  async put<T>(endpoint: string, data: unknown): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', ...getAuthHeaders() },
      body: JSON.stringify(data)
    });
    return handleResponse<T>(response);
  },

  async delete<T>(endpoint: string): Promise<T> {
    const response = await fetch(`${API_BASE}${endpoint}`, {
      method: 'DELETE',
      headers: getAuthHeaders()
    });
    return handleResponse<T>(response);
  }
};
```

```typescript
// utils/store.ts

type Listener<T> = (state: T) => void;

export function createStore<T>(initialState: T) {
  let state: T = initialState;
  const listeners = new Set<Listener<T>>();

  return {
    getState(): T {
      return state;
    },

    setState(newState: Partial<T>): void {
      state = { ...state, ...newState };
      listeners.forEach(fn => fn(state));
    },

    subscribe(fn: Listener<T>): () => void {
      listeners.add(fn);
      return () => listeners.delete(fn);
    }
  };
}
```

### CSS

```css
/* style.css */

:root {
  /* Colores */
  --color-primary: #2563eb;
  --color-primary-hover: #1d4ed8;
  --color-secondary: #64748b;
  --color-success: #16a34a;
  --color-error: #dc2626;
  --color-warning: #d97706;
  --color-text: #1f2937;
  --color-text-muted: #6b7280;
  --color-bg: #ffffff;
  --color-bg-alt: #f9fafb;
  --color-border: #e5e7eb;

  /* Tipografía */
  --font-sans: system-ui, -apple-system, sans-serif;
  --font-mono: ui-monospace, monospace;
  --text-xs: 0.75rem;
  --text-sm: 0.875rem;
  --text-base: 1rem;
  --text-lg: 1.125rem;
  --text-xl: 1.25rem;
  --text-2xl: 1.5rem;

  /* Espaciado (grid 8px) */
  --space-1: 0.25rem;
  --space-2: 0.5rem;
  --space-3: 0.75rem;
  --space-4: 1rem;
  --space-6: 1.5rem;
  --space-8: 2rem;
  --space-12: 3rem;

  /* Sombras */
  --shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);
  --shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);
  --shadow-lg: 0 10px 15px rgba(0, 0, 0, 0.1);

  /* Bordes */
  --radius-sm: 0.25rem;
  --radius-md: 0.5rem;
  --radius-lg: 0.75rem;
  --radius-full: 9999px;

  /* Z-index */
  --z-dropdown: 100;
  --z-modal: 200;
  --z-toast: 300;

  /* Transiciones */
  --transition-fast: 150ms ease;
  --transition-normal: 250ms ease;
}

/* Reset básico */
*,
*::before,
*::after {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: var(--font-sans);
  font-size: var(--text-base);
  color: var(--color-text);
  background-color: var(--color-bg);
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
}

img {
  max-width: 100%;
  height: auto;
  display: block;
}

button {
  cursor: pointer;
  font-family: inherit;
}

a {
  color: var(--color-primary);
  text-decoration: none;
}

a:hover {
  text-decoration: underline;
}
```

### Nomenclatura

| Tipo | Convención | Ejemplo |
|------|------------|---------|
| Archivos | kebab-case | `product-detail.ts`, `cart-page.ts` |
| Clases CSS | BEM | `.product-card__title--featured` |
| Interfaces TS | PascalCase | `ProductResponse`, `CartItem` |
| Funciones | camelCase | `renderProduct()`, `handleSubmit()` |
| Constantes | UPPER_SNAKE | `API_BASE`, `MAX_ITEMS` |

---

## Reglas de Arquitectura

### SEO Friendly

- Meta tags completos en `index.html` (title, description, og:*, twitter:*)
- Estructura semántica: `<header>`, `<nav>`, `<main>`, `<article>`, `<section>`, `<footer>`
- Atributos `alt` en todas las imágenes
- Breadcrumbs con `<nav aria-label="breadcrumb">`
- Canonical URL
- `lang="es"` en el html

### Routing (Hash-based SPA)

```typescript
// utils/router.ts

type Route = {
  path: string;
  render: () => void;
  guards?: Guard[];
};

type Guard = () => boolean;

const routes: Route[] = [
  { path: '', redirect: '/#/login' },
  { path: '/login', render: () => renderPage(LoginPage) },
  { path: '/register', render: () => renderPage(RegisterPage) },
  { path: '/store', render: () => renderPage(HomePage) },
  { path: '/store/product/:id', render: () => renderPage(ProductDetailPage) },
  { path: '/cart', render: () => renderPage(CartPage), guards: [requireAuth] },
  { path: '/client/orders', render: () => renderPage(OrdersPage), guards: [requireAuth] },
  { path: '/admin', render: () => renderPage(DashboardPage), guards: [requireAuth, requireAdmin] },
];

function requireAuth(): boolean {
  const token = localStorage.getItem('token');
  if (!token) {
    window.location.hash = '#/login';
    return false;
  }
  return true;
}

function requireAdmin(): boolean {
  const userStr = localStorage.getItem('user');
  if (!userStr) return false;
  const user = JSON.parse(userStr) as User;
  if (user.role !== 'admin') {
    window.location.hash = '#/store';
    return false;
  }
  return true;
}

export function initRouter(): void {
  window.addEventListener('hashchange', router);
  router();
}

function matchPath(pattern: string, path: string): boolean {
  const patternParts = pattern.split('/');
  const pathParts = path.split('/');
  
  if (patternParts.length !== pathParts.length) return false;
  
  return patternParts.every((part, i) => 
    part.startsWith(':') || part === pathParts[i]
  );
}

function router(): void {
  const hash = window.location.hash.slice(1) || '/';
  const route = routes.find(r => matchPath(r.path, hash));
  
  if (!route) {
    window.location.hash = '#/login';
    return;
  }

  if (route.guards?.every(guard => guard())) {
    route.render();
  }
}
```

### Patrones de Página

```typescript
// pages/store/Home.ts

import { api } from '../../utils/api';
import { Product } from '../../types/product';
import { ProductCard } from '../../components/ProductCard';

export async function renderHomePage(): Promise<void> {
  const app = document.getElementById('app')!;
  app.innerHTML = `
    <main class="page page--home">
      <header class="header" id="header"></header>
      <section class="hero">
        <h1>Bienvenido a la tienda</h1>
      </section>
      <section class="products" id="product-list">
        <div class="spinner" id="spinner"></div>
      </section>
      <footer class="footer" id="footer"></footer>
    </main>
  `;

  renderHeader();
  renderFooter();
  await loadProducts();
}

async function loadProducts(): Promise<void> {
  const spinner = document.getElementById('spinner');
  const productList = document.getElementById('product-list')!;

  try {
    const products = await api.get<Product[]>('/products');
    productList.innerHTML = products.map(p => ProductCard(p)).join('');
  } catch {
    productList.innerHTML = '<p class="error">Error al cargar productos</p>';
  } finally {
    spinner?.remove();
  }
}
```

---

## Reglas Importantes

### SÍ

- ✅ TypeScript strict mode
- ✅ Custom properties en CSS
- ✅ ES Modules (import/export)
- ✅ Funciones puras para render
- ✅ Event delegation en listas
- ✅ Lazy loading de imágenes
- ✅ Error handling con try/catch
- ✅ Guards de autenticación en rutas
- ✅ Separación clara: pages / components / utils / types
- ✅ SEO friendly

### NO

- ❌ `any` en TypeScript (usar `unknown` + type guards)
- ❌ Inline styles
- ❌ `var`, usar siempre `const`/`let`
- ❌ `innerHTML` — ANTIPATTERN. Siempre usar métodos DOM (`document.createElement`, `textContent`, `appendChild`, `removeChild`). Incluso para limpiar contenedores usar `while(el.firstChild) el.removeChild(el.firstChild)`
- ❌ `alert()` / `prompt()`
- ❌ jQuery, Lodash, o cualquier lib externa
- ❌ `!important` en CSS
- ❌ Dependencias innecesarias

---

## Accesibilidad

- `alt` en todas las imágenes
- `aria-label` en botones/icon-buttons
- Focus visible: `:focus-visible`
- Contraste mínimo 4.5:1
- Navegable por teclado
- Roles ARIA donde corresponda

---

## Contracto de API (Back)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Login |
| POST | `/api/v1/auth/register` | Registro |
| GET | `/api/v1/products` | Lista productos |
| GET | `/api/v1/products/:id` | Detalle producto |
| GET | `/api/v1/cart` | Carrito del usuario |
| POST | `/api/v1/cart/items` | Agregar al carrito |
| DELETE | `/api/v1/cart/items/:id` | Quitar del carrito |
| GET | `/api/v1/orders` | Órdenes del cliente |
| GET | `/api/v1/admin/products` | Admin: lista productos |
| POST | `/api/v1/admin/products` | Admin: crear producto |
| PUT | `/api/v1/admin/products/:id` | Admin: editar producto |
| DELETE | `/api/v1/admin/products/:id` | Admin: eliminar producto |

---

## Scripts npm

```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "typecheck": "tsc --noEmit"
  }
}
```

---

## Editor Config

```ini
root = true

[*]
indent_style = space
indent_size = 2
end_of_line = lf
charset = utf-8
trim_trailing_whitespace = true
insert_final_newline = true

[*.css]
indent_size = 2

[*.html]
indent_size = 2
```

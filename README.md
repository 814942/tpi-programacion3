# Food Store - TPI Programación III UTN

> Sistema de delivery de viandas saludables

## Consigna

Ver consigna del curso.

## Estructura del Proyecto

```
tpi/
├── client/               # Frontend SPA (TypeScript + Vite)
│   ├── src/              # Código fuente
│   │   ├── components/    # Componentes reutilizables
│   │   ├── pages/        # Vistas (auth, store, admin)
│   │   ├── types/        # Tipos TypeScript
│   │   └── utils/        # Utilidades (API, router, auth)
│   ├── public/           # Archivos estáticos
│   └── index.html
├── back/                 # Backend Java (pendiente)
└── README.md
```

## Funcionalidades

| Rol | Acceso |
|-----|--------|
| Cliente | Registro, catálogo, carrito, pedidos |
| Admin | Dashboard, CRUD productos, pedidos, usuarios |

## Stack

- **Frontend**: TypeScript, Vite, CSS
- **Backend**: Java 17, Spring Boot (futuro)
- **Storage**: LocalStorage (frontend)

## Scripts

```bash
npm install
cd client && npm run dev
```

## Alumno

**Pablo Garay**  
Universidad Tecnológica Nacional - Programación III

// main.ts — Punto de entrada y protector de rutas

import { initRouteGuard } from './utils/navigate';

// Inicializar el protector de rutas al cargar la página
document.addEventListener('DOMContentLoaded', () => {
  initRouteGuard();
});

// También ejecutar en cada navegación (SPA)
window.addEventListener('popstate', () => {
  initRouteGuard();
});
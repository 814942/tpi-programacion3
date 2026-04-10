// navigate.ts — Funciones de navegación y gestión de rutas

import { Role } from '../types';
import { getUserSession, isAuthenticated } from './auth';

/**
 * Rutas de la aplicación
 */
export const ROUTES = {
  HOME: '/',
  LOGIN: '/pages/auth/login/index.html',
  REGISTER: '/pages/auth/registro/index.html',
  ADMIN: '/pages/admin/index.html',
  CLIENT: '/pages/client/index.html',
} as const;

export type Route = typeof ROUTES[keyof typeof ROUTES];

/**
 * Navega a una ruta
 */
export function navigateTo(path: string): void {
  window.location.href = path;
}

/**
 * Redirige al login
 */
export function redirectToLogin(): void {
  navigateTo(ROUTES.LOGIN);
}

/**
 * Redirige al registro
 */
export function redirectToRegister(): void {
  navigateTo(ROUTES.REGISTER);
}

/**
 * Redirige al panel de admin
 */
export function redirectToAdmin(): void {
  navigateTo(ROUTES.ADMIN);
}

/**
 * Redirige al panel de cliente
 */
export function redirectToClient(): void {
  navigateTo(ROUTES.CLIENT);
}

/**
 * Redirige a la página principal
 */
export function redirectToHome(): void {
  navigateTo(ROUTES.HOME);
}

/**
 * Obtiene la ruta based en el rol del usuario
 */
export function getDashboardByRole(role: Role): string {
  return role === 'admin' ? ROUTES.ADMIN : ROUTES.CLIENT;
}

/**
 * Verifica si la ruta actual es una ruta protegida
 */
export function isProtectedRoute(pathname: string): boolean {
  const protectedPaths = [
    '/pages/admin/',
    '/pages/client/',
  ];
  return protectedPaths.some(p => pathname.includes(p));
}

/**
 * Obtiene el rol requerido para una ruta
 */
export function getRequiredRole(pathname: string): Role | null {
  if (pathname.includes('/pages/admin/')) {
    return 'admin';
  }
  if (pathname.includes('/pages/client/')) {
    return 'client';
  }
  return null;
}

/**
 * Protege la ruta actual basándose en el rol
 * Retorna true si el acceso es permitido, false si debe redirigir
 */
export function protectRoute(): boolean {
  const pathname = window.location.pathname;
  
  // Si no es ruta protegida, permitir
  if (!isProtectedRoute(pathname)) {
    return true;
  }

  // Si no hay sesión, redirigir al login
  if (!isAuthenticated()) {
    redirectToLogin();
    return false;
  }

  // Obtener rol requerido y rol actual
  const requiredRole = getRequiredRole(pathname);
  const user = getUserSession();

  if (!user) {
    redirectToLogin();
    return false;
  }

  // Verificar rol
  if (requiredRole && user.role !== requiredRole) {
    // Cliente intentando acceder a admin -> redirigir a su dashboard
    if (user.role === 'client' && requiredRole === 'admin') {
      redirectToClient();
      return false;
    }
    // Admin intentando acceder a client -> redirigir a su dashboard
    if (user.role === 'admin' && requiredRole === 'client') {
      redirectToAdmin();
      return false;
    }
  }

  return true;
}

/**
 * Inicializa el protector de rutas (para usar en main.ts)
 */
export function initRouteGuard(): void {
  // Ejecutar protección al cargar la página
  protectRoute();

  // También escuchar cambios de navegación (SPA)
  window.addEventListener('popstate', () => {
    protectRoute();
  });
}
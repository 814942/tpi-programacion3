import type { Role } from '../types';
import { getUserSession, isAuthenticated } from './auth';

export const ROUTES = {
  HOME: '/',
  LOGIN: '/src/pages/auth/login/index.html',
  REGISTER: '/src/pages/auth/registro/index.html',
  ADMIN: '/src/pages/admin/index.html',
  CLIENT: '/src/pages/client/index.html',
  FORBIDDEN: '/src/pages/auth/forbidden/index.html',
} as const;

export type Route = typeof ROUTES[keyof typeof ROUTES];

export function navigateTo(path: string): void {
  window.location.href = path;
}

export function redirectToLogin(): void {
  navigateTo(ROUTES.LOGIN);
}

export function redirectToRegister(): void {
  navigateTo(ROUTES.REGISTER);
}

export function redirectToAdmin(): void {
  navigateTo(ROUTES.ADMIN);
}

export function redirectToClient(): void {
  navigateTo(ROUTES.CLIENT);
}

export function redirectToHome(): void {
  navigateTo(ROUTES.HOME);
}

export function redirectToForbidden(): void {
  navigateTo(ROUTES.FORBIDDEN);
}

export function getDashboardByRole(role: Role): string {
  return role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.CLIENT;
}

export function isProtectedRoute(pathname: string): boolean {
  const protectedPaths = [
    '/src/pages/admin/',
    '/src/pages/client/',
  ];
  return protectedPaths.some(p => pathname.includes(p));
}

export function getRequiredRole(pathname: string): Role | null {
  if (pathname.includes('/src/pages/admin/')) {
    return 'ADMIN';
  }
  if (pathname.includes('/src/pages/client/')) {
    return 'USUARIO';
  }
  return null;
}

export function protectRoute(): boolean {
  const pathname = window.location.pathname;

  if (!isProtectedRoute(pathname)) {
    return true;
  }

  if (!isAuthenticated()) {
    redirectToForbidden();
    return false;
  }

  const requiredRole = getRequiredRole(pathname);
  const user = getUserSession();

  if (!user) {
    redirectToForbidden();
    return false;
  }

  if (requiredRole && user.role !== requiredRole) {
    redirectToForbidden();
    return false;
  }

  return true;
}

export function initRouteGuard(): void {
  protectRoute();
  window.addEventListener('popstate', () => {
    protectRoute();
  });
}

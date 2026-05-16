// navigate.ts — Navigation and route management functions

import type { Role } from '../types';
import { getUserSession, isAuthenticated } from './auth';

/**
 * Application routes - absolute paths from server root
 */
export const ROUTES = {
  HOME: '/',
  LOGIN: '/src/pages/auth/login/index.html',
  REGISTER: '/src/pages/auth/registro/index.html',
  ADMIN: '/src/pages/admin/index.html',
  CLIENT: '/src/pages/client/index.html',
  FORBIDDEN: '/src/pages/auth/forbidden/index.html',
} as const;

export type Route = typeof ROUTES[keyof typeof ROUTES];

/**
 * Navigate to a path (absolute from root)
 */
export function navigateTo(path: string): void {
  window.location.href = path;
}

/**
 * Redirect to login page
 */
export function redirectToLogin(): void {
  navigateTo(ROUTES.LOGIN);
}

/**
 * Redirect to registration page
 */
export function redirectToRegister(): void {
  navigateTo(ROUTES.REGISTER);
}

/**
 * Redirect to admin panel
 */
export function redirectToAdmin(): void {
  navigateTo(ROUTES.ADMIN);
}

/**
 * Redirect to client panel
 */
export function redirectToClient(): void {
  navigateTo(ROUTES.CLIENT);
}

/**
 * Redirect to home page
 */
export function redirectToHome(): void {
  navigateTo(ROUTES.HOME);
}

/**
 * Redirect to forbidden page
 */
export function redirectToForbidden(): void {
  navigateTo(ROUTES.FORBIDDEN);
}

/**
 * Get dashboard route based on role
 */
export function getDashboardByRole(role: Role): string {
  return role === 'admin' ? ROUTES.ADMIN : ROUTES.CLIENT;
}

/**
 * Check if current route is protected
 */
export function isProtectedRoute(pathname: string): boolean {
  const protectedPaths = [
    '/src/pages/admin/',
    '/src/pages/client/',
  ];
  return protectedPaths.some(p => pathname.includes(p));
}

/**
 * Get required role for a route
 */
export function getRequiredRole(pathname: string): Role | null {
  if (pathname.includes('/src/pages/admin/')) {
    return 'admin';
  }
  if (pathname.includes('/src/pages/client/')) {
    return 'client';
  }
  return null;
}

/**
 * Protect current route based on role
 * Returns true if access is allowed, false if should redirect
 */
export function protectRoute(): boolean {
  const pathname = window.location.pathname;
  
  // If not protected route, allow
  if (!isProtectedRoute(pathname)) {
    return true;
  }

  // If no session, redirect to forbidden
  if (!isAuthenticated()) {
    redirectToForbidden();
    return false;
  }

  // Get required role and current role
  const requiredRole = getRequiredRole(pathname);
  const user = getUserSession();

  if (!user) {
    redirectToForbidden();
    return false;
  }

  // Verify role - redirect to forbidden if role doesn't match
  if (requiredRole && user.role !== requiredRole) {
    redirectToForbidden();
    return false;
  }

  return true;
}

/**
 * Initialize route guard
 */
export function initRouteGuard(): void {
  protectRoute();
  window.addEventListener('popstate', () => {
    protectRoute();
  });
}
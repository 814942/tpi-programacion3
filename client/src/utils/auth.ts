// auth.ts — Funciones de autenticación con localStorage

import { IUser, IUserWithoutPassword, ILoginCredentials, IRegisterData, Role } from '../types';

// Keys para localStorage
const USERS_KEY = 'users';
const USER_DATA_KEY = 'userData';

/**
 * Genera un ID único para nuevos usuarios
 */
function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

/**
 * Obtiene todos los usuarios registrados
 */
export function getUsers(): IUser[] {
  const stored = localStorage.getItem(USERS_KEY);
  if (!stored) return [];
  try {
    return JSON.parse(stored);
  } catch {
    return [];
  }
}

/**
 * Guarda el array de usuarios en localStorage
 */
function saveUsers(users: IUser[]): void {
  localStorage.setItem(USERS_KEY, JSON.stringify(users));
}

/**
 * Busca un usuario por email
 */
export function findUserByEmail(email: string): IUser | undefined {
  const users = getUsers();
  return users.find(u => u.email.toLowerCase() === email.toLowerCase());
}

/**
 * Verifica si un email ya está registrado
 */
export function isEmailRegistered(email: string): boolean {
  return findUserByEmail(email) !== undefined;
}

/**
 * Registra un nuevo usuario
 */
export function register(data: IRegisterData): { success: boolean; message: string; user?: IUserWithoutPassword } {
  // Validar que el email no esté registrado
  if (isEmailRegistered(data.email)) {
    return { success: false, message: 'El email ya está registrado' };
  }

  // Validar datos básicos
  if (!data.email || !data.password) {
    return { success: false, message: 'Email y contraseña son requeridos' };
  }

  if (data.password.length < 6) {
    return { success: false, message: 'La contraseña debe tener al menos 6 caracteres' };
  }

  // Crear nuevo usuario
  const newUser: IUser = {
    id: generateId(),
    email: data.email.toLowerCase(),
    password: data.password, // ⚠️ En producción, usar hash (bcrypt)
    role: data.role || 'client',
    createdAt: new Date().toISOString(),
  };

  // Guardar en localStorage
  const users = getUsers();
  users.push(newUser);
  saveUsers(users);

  // Devolver usuario sin contraseña
  const { password, ...userWithoutPassword } = newUser;
  return { success: true, message: 'Usuario registrado correctamente', user: userWithoutPassword };
}

/**
 * Login de usuario
 */
export function login(credentials: ILoginCredentials): { success: boolean; message: string; user?: IUserWithoutPassword } {
  const { email, password } = credentials;

  // Buscar usuario
  const user = findUserByEmail(email);

  if (!user) {
    return { success: false, message: 'Email o contraseña incorrectos' };
  }

  // Verificar contraseña (comparación directa - en producción usar hash)
  if (user.password !== password) {
    return { success: false, message: 'Email o contraseña incorrectos' };
  }

  // Guardar sesión
  const { password: _, ...userWithoutPassword } = user;
  setUserSession(userWithoutPassword);

  return { success: true, message: 'Login exitoso', user: userWithoutPassword };
}

/**
 * Cierra la sesión del usuario
 */
export function logout(): void {
  localStorage.removeItem(USER_DATA_KEY);
}

/**
 * Guarda la sesión del usuario
 */
export function setUserSession(user: IUserWithoutPassword): void {
  localStorage.setItem(USER_DATA_KEY, JSON.stringify(user));
}

/**
 * Obtiene la sesión actual del usuario
 */
export function getUserSession(): IUserWithoutPassword | null {
  const stored = localStorage.getItem(USER_DATA_KEY);
  if (!stored) return null;
  try {
    return JSON.parse(stored);
  } catch {
    return null;
  }
}

/**
 * Verifica si hay una sesión activa
 */
export function isAuthenticated(): boolean {
  return getUserSession() !== null;
}

/**
 * Verifica si el usuario actual es admin
 */
export function isAdmin(): boolean {
  const user = getUserSession();
  return user?.role === 'admin';
}

/**
 * Verifica si el usuario actual es cliente
 */
export function isClient(): boolean {
  const user = getUserSession();
  return user?.role === 'client';
}

/**
 * Obtiene el rol del usuario actual
 */
export function getUserRole(): Role | null {
  const user = getUserSession();
  return user?.role || null;
}
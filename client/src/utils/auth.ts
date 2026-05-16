// auth.ts — Authentication functions with localStorage

import type { IUser, IUserWithoutPassword, ILoginCredentials, IRegisterData, Role } from '../types';

// localStorage keys
const USERS_KEY = 'users';
const USER_DATA_KEY = 'userData';

// Admin credentials for testing
const ADMIN_EMAIL = 'admin@foodstore.com';
const ADMIN_PASSWORD = 'admin123';

/**
 * Seed admin user if not exists
 */
export function seedAdminUser(): void {
  const users = getUsers();
  const adminExists = users.some(u => u.email === ADMIN_EMAIL);
  
  if (!adminExists) {
    const adminUser: IUser = {
      id: 'admin-001',
      email: ADMIN_EMAIL,
      password: ADMIN_PASSWORD,
      role: 'admin',
      createdAt: new Date().toISOString(),
    };
    users.push(adminUser);
    saveUsers(users);
    console.log('Admin user seeded: admin@foodstore.com / admin123');
  }
}

// Initialize admin on module load
seedAdminUser();

/**
 * Generate unique ID for new users
 */
function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
}

/**
 * Get all registered users
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
 * Save users array to localStorage
 */
function saveUsers(users: IUser[]): void {
  localStorage.setItem(USERS_KEY, JSON.stringify(users));
}

/**
 * Find user by email
 */
export function findUserByEmail(email: string): IUser | undefined {
  const users = getUsers();
  return users.find(u => u.email.toLowerCase() === email.toLowerCase());
}

/**
 * Check if email is already registered
 */
export function isEmailRegistered(email: string): boolean {
  return findUserByEmail(email) !== undefined;
}

/**
 * Register a new user
 */
export function register(data: IRegisterData): { success: boolean; message: string; user?: IUserWithoutPassword } {
  // Validate email not already registered
  if (isEmailRegistered(data.email)) {
    return { success: false, message: 'El email ya está registrado' };
  }

  // Validate basic data
  if (!data.email || !data.password) {
    return { success: false, message: 'Email y contraseña son requeridos' };
  }

  if (data.password.length < 6) {
    return { success: false, message: 'La contraseña debe tener al menos 6 caracteres' };
  }

  // Create new user
  const newUser: IUser = {
    id: generateId(),
    email: data.email.toLowerCase(),
    password: data.password, // Note: In production, use hash (bcrypt)
    role: data.role || 'client',
    createdAt: new Date().toISOString(),
  };

  // Save to localStorage
  const users = getUsers();
  users.push(newUser);
  saveUsers(users);

  // Return user without password
  const { password, ...userWithoutPassword } = newUser;
  return { success: true, message: 'Usuario registrado correctamente', user: userWithoutPassword };
}

/**
 * User login
 */
export function login(credentials: ILoginCredentials): { success: boolean; message: string; user?: IUserWithoutPassword } {
  const { email, password } = credentials;

  // Find user
  const user = findUserByEmail(email);

  if (!user) {
    return { success: false, message: 'Email o contraseña incorrectos' };
  }

  // Verify password (direct comparison - in production use hash)
  if (user.password !== password) {
    return { success: false, message: 'Email o contraseña incorrectos' };
  }

  // Save session
  const { password: _, ...userWithoutPassword } = user;
  setUserSession(userWithoutPassword);

  return { success: true, message: 'Login exitoso', user: userWithoutPassword };
}

/**
 * Logout user
 */
export function logout(): void {
  localStorage.removeItem(USER_DATA_KEY);
}

/**
 * Save user session
 */
export function setUserSession(user: IUserWithoutPassword): void {
  localStorage.setItem(USER_DATA_KEY, JSON.stringify(user));
}

/**
 * Get current user session
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
 * Check if user is authenticated
 */
export function isAuthenticated(): boolean {
  return getUserSession() !== null;
}

/**
 * Check if current user is admin
 */
export function isAdmin(): boolean {
  const user = getUserSession();
  return user?.role === 'admin';
}

/**
 * Check if current user is client
 */
export function isClient(): boolean {
  const user = getUserSession();
  return user?.role === 'client';
}

/**
 * Get current user role
 */
export function getUserRole(): Role | null {
  const user = getUserSession();
  return user?.role || null;
}
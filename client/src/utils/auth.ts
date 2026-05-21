import type { IAuthResponse, IUser, ILoginCredentials, IRegisterData, Role } from '../types';
import { api } from './api';

export const SESSION_KEY = 'foodstore_session';

// Cleanup old localStorage keys from previous mock auth
const OLD_KEYS = ['userData', 'users'];
OLD_KEYS.forEach(key => localStorage.removeItem(key));

interface Session {
  token: string;
  user: IUser;
}

interface JwtPayload {
  sub: string;
  email: string;
  role: string;
  exp: number;
  iat: number;
}

export function getSession(): Session | null {
  try {
    const stored = localStorage.getItem(SESSION_KEY);
    if (!stored) return null;
    return JSON.parse(stored) as Session;
  } catch {
    return null;
  }
}

function setSession(token: string, user: IUser): void {
  localStorage.setItem(SESSION_KEY, JSON.stringify({ token, user }));
}

export function clearSession(): void {
  localStorage.removeItem(SESSION_KEY);
}

export function getToken(): string | null {
  return getSession()?.token ?? null;
}

export function decodeToken(token: string): JwtPayload | null {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;

    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const paddedBase64 = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=');
    const binaryPayload = atob(paddedBase64);
    const bytes = Uint8Array.from(binaryPayload, (char) => char.charCodeAt(0));
    const jsonPayload = new TextDecoder().decode(bytes);

    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
}

export async function login(credentials: ILoginCredentials): Promise<IUser> {
  const response = await api.post<IAuthResponse>('/auth/login', credentials);
  const user: IUser = {
    id: response.id,
    email: response.email,
    nombre: response.nombre,
    apellido: response.apellido,
    celular: response.celular || null,
    role: response.role,
  };
  setSession(response.token, user);
  return user;
}

export async function register(data: IRegisterData): Promise<IUser> {
  const response = await api.post<IAuthResponse>('/auth/register', data);
  const user: IUser = {
    id: response.id,
    email: response.email,
    nombre: response.nombre,
    apellido: response.apellido,
    celular: response.celular || null,
    role: response.role,
  };
  setSession(response.token, user);
  return user;
}

export function logout(): void {
  clearSession();
}

export function isAuthenticated(): boolean {
  const session = getSession();
  if (!session) return false;

  const payload = decodeToken(session.token);
  if (!payload) return false;

  return payload.exp * 1000 > Date.now();
}

export function isAdmin(): boolean {
  return getSession()?.user.role === 'ADMIN';
}

export function isUsuario(): boolean {
  return getSession()?.user.role === 'USUARIO';
}

export function getUserRole(): Role | null {
  return getSession()?.user.role ?? null;
}

export function getUserSession(): IUser | null {
  const session = getSession();
  if (!session) return null;

  // Only return user if token is still valid
  const payload = decodeToken(session.token);
  if (!payload) return null;

  const expired = payload.exp * 1000 < Date.now();
  if (expired) return null;

  return session.user;
}

import type { IAuthResponse, IUser, ILoginCredentials, IRegisterData, Role } from '../types';
import { api } from './api';

export const SESSION_KEY = 'foodstore_session';

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

function decodeBase64UrlUtf8(value: string): string {
   const base64 = value.replace(/-/g, '+').replace(/_/g, '/');
   const padding = '='.repeat((4 - (base64.length % 4)) % 4);
   const normalized = base64 + padding;
   const binary = atob(normalized);
   const bytes = Uint8Array.from(binary, (char) => char.charCodeAt(0));
   return new TextDecoder().decode(bytes);
 }

export function decodeToken(token: string): JwtPayload | null {
  try {
    const base64Url = token.split('.')[1];
    if (!base64Url) return null;
    return JSON.parse(decodeBase64UrlUtf8(base64Url)) as JwtPayload;
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
  return getSession()?.user ?? null;
}

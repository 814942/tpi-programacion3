import type { Role } from './Role';

export interface IUser {
  id: number;
  email: string;
  nombre: string;
  apellido: string;
  celular: string | null;
  role: Role;
}

export interface ILoginCredentials {
  email: string;
  password: string;
}

export interface IRegisterData {
  nombre: string;
  apellido: string;
  email: string;
  celular?: string;
  password: string;
}

export interface IAuthResponse {
  token: string;
  type: string;
  id: number;
  email: string;
  nombre: string;
  apellido: string;
  celular: string | null;
  role: Role;
}

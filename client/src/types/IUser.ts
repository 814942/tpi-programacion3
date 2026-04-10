// IUser.ts — Interfaz de usuario con tipado fuerte

import { Role } from './Role';

export interface IUser {
  id: string;
  email: string;
  password: string;
  role: Role;
  createdAt: string;
}

export interface IUserWithoutPassword {
  id: string;
  email: string;
  role: Role;
  createdAt: string;
}

export interface ILoginCredentials {
  email: string;
  password: string;
}

export interface IRegisterData {
  email: string;
  password: string;
  role: Role;
}
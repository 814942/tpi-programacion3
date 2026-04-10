// Role.ts — Definición de roles de usuario

export type Role = 'admin' | 'client';

export const ROLES = {
  ADMIN: 'admin' as Role,
  CLIENT: 'client' as Role,
} as const;
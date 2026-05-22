/// <reference types="vite/client" />

import { getToken, logout } from './auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

interface ApiError {
  message?: string;
  status: number;
}

async function request<T>(method: string, endpoint: string, body?: unknown): Promise<T> {
  const token = getToken();

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
  });

  // Handle 401: redirect to login only if session expired (token was sent)
  if (response.status === 401) {
    if (token) {
      // Session expired — clear and redirect
      logout();
      window.location.href = '/src/pages/auth/login/index.html';
      throw new Error('Sesión expirada');
    }
    // No token was sent — just throw the API error (e.g. invalid credentials)
    throw new Error('Credenciales inválidas');
  }

  if (!response.ok) {
    let errorMessage = `Error ${response.status}`;
    try {
      const errorData: ApiError = await response.json();
      if (errorData.message) {
        errorMessage = errorData.message;
      }
    } catch {
      // Could not parse error body
    }
    throw new Error(errorMessage);
  }

  // Handle 204 No Content (e.g. DELETE responses)
  if (response.status === 204) {
    return undefined as T;
  }

  // Handle empty response body
  const text = await response.text();
  if (!text) {
    return undefined as T;
  }
  return JSON.parse(text) as T;
}

export const api = {
  get<T>(endpoint: string): Promise<T> {
    return request<T>('GET', endpoint);
  },
  post<T>(endpoint: string, body: unknown): Promise<T> {
    return request<T>('POST', endpoint, body);
  },
  put<T>(endpoint: string, body: unknown): Promise<T> {
    return request<T>('PUT', endpoint, body);
  },
  patch<T>(endpoint: string, body?: unknown): Promise<T> {
    return request<T>('PATCH', endpoint, body);
  },
  delete<T>(endpoint: string): Promise<T> {
    return request<T>('DELETE', endpoint);
  },
};

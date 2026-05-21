/// <reference types="vite/client" />

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

interface ApiError {
  message?: string;
  status: number;
}

async function request<T>(method: string, endpoint: string, body?: unknown): Promise<T> {
  let token: string | null = null;
  try {
    const session = JSON.parse(localStorage.getItem('foodstore_session') || 'null');
    token = session?.token || null;
  } catch {
    token = null;
  }

  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  if (response.status === 401) {
    localStorage.removeItem('foodstore_session');
    window.location.href = '/src/pages/auth/login/index.html';
    throw new Error('Sesión expirada');
  }

  if (!response.ok) {
    let errorMessage = `Error ${response.status}`;
    try {
      const errorData: ApiError = await response.json();
      if (errorData.message) {
        errorMessage = errorData.message;
      }
    } catch {
      // ignore
    }
    throw new Error(errorMessage);
  }

  return response.json();
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
  delete<T>(endpoint: string): Promise<T> {
    return request<T>('DELETE', endpoint);
  },
};

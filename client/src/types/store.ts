export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string | null;
  imagen: string | null;
}

export interface ProductoResponse {
  id: number;
  nombre: string;
  precio: number;
  descripcion: string | null;
  stock: number;
  imagen: string | null;
  disponible: boolean;
  categoria: {
    id: number;
    nombre: string;
  };
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface CategoriaResponse {
  id: number;
  nombre: string;
  descripcion: string;
  imagen: string;
}

export interface ProductoResponse {
  id: number;
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagen: string;
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

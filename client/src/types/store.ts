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

export interface DetallePedidoResponse {
  id: number;
  productoNombre: string;
  productoPrecio: number;
  productoDescripcion: string | null;
  productoImagen: string | null;
  productoId: number;
  cantidad: number;
  subtotal: number;
}

export type EstadoPedido = 'PENDIENTE' | 'CONFIRMADO' | 'TERMINADO' | 'CANCELADO';
export type FormaPago = 'TARJETA' | 'TRANSFERENCIA' | 'EFECTIVO';

export interface PedidoResponse {
  id: number;
  fecha: string;
  estado: EstadoPedido;
  formaPago: FormaPago;
  total: number;
  usuario: {
    id: number;
    nombre: string;
    apellido: string;
    email: string;
  };
  detalles: DetallePedidoResponse[];
}

export interface UsuarioResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  celular: string | null;
  rol: 'ADMIN' | 'USUARIO';
  createdAt: string;
}

// client.ts — Lógica del panel de cliente (catálogo, carrito, búsqueda)

import { getUserSession, logout, isClient, isAuthenticated } from '../../utils/auth';
import { redirectToLogin, redirectToHome } from '../../utils/navigate';

// ==================== DATOS (simulación de data.js) ====================

interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  imagen: string;
  categoria: string;
}

const categorias: string[] = ["Hamburguesas", "Pizzas", "Papas Fritas", "Bebidas"];

const productos: Producto[] = [
  {
    id: 1,
    nombre: "Hamburguesa Triple",
    descripcion: "Triple carne, cheddar y bacon",
    precio: 25000,
    imagen: "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300&h=200&fit=crop",
    categoria: "Hamburguesas"
  },
  {
    id: 2,
    nombre: "Pizza Muzzarella",
    descripcion: "Salsa casera y orégano",
    precio: 18000,
    imagen: "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300&h=200&fit=crop",
    categoria: "Pizzas"
  },
  {
    id: 3,
    nombre: "Hamburguesa Doble",
    descripcion: "Doble carne con lechuga y tomate",
    precio: 20000,
    imagen: "https://images.unsplash.com/photo-1550547660-d9450f859349?w=300&h=200&fit=crop",
    categoria: "Hamburguesas"
  },
  {
    id: 4,
    nombre: "Pizza Especial",
    descripcion: "Jamón, morrón y aceitunas",
    precio: 22000,
    imagen: "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=300&h=200&fit=crop",
    categoria: "Pizzas"
  },
  {
    id: 5,
    nombre: "Pizza Pepperoni",
    descripcion: "Pepperoni extra con queso",
    precio: 24000,
    imagen: "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=300&h=200&fit=crop",
    categoria: "Pizzas"
  },
  {
    id: 6,
    nombre: "Papas Fritas",
    descripcion: "Papas crocantes con sal gruesa",
    precio: 8000,
    imagen: "https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=300&h=200&fit=crop",
    categoria: "Papas Fritas"
  },
  {
    id: 7,
    nombre: "Papas con Cheddar",
    descripcion: "Papas fritas con salsa cheddar",
    precio: 10000,
    imagen: "https://images.unsplash.com/photo-1630384060421-cb20d0e0649d?w=300&h=200&fit=crop",
    categoria: "Papas Fritas"
  },
  {
    id: 8,
    nombre: "Coca Cola",
    descripcion: "Lata 350ml bien fría",
    precio: 3500,
    imagen: "https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=300&h=200&fit=crop",
    categoria: "Bebidas"
  },
  {
    id: 9,
    nombre: "Agua Mineral",
    descripcion: "Agua sin gas 500ml",
    precio: 2500,
    imagen: "https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=300&h=200&fit=crop",
    categoria: "Bebidas"
  }
];

// ==================== CARRITO ====================

interface CarritoItem {
  producto: Producto;
  cantidad: number;
}

let carrito: CarritoItem[] = [];

// ==================== FUNCIONES ====================

/**
 * Carga las categorías en el aside
 */
function cargarCategorias(): void {
  const listaCategorias = document.getElementById('lista-categorias');
  if (!listaCategorias) return;

  listaCategorias.innerHTML = '';
  
  // Opción "Todas" primero
  const liTodas = document.createElement('li');
  liTodas.innerHTML = '<a href="#" data-categoria="todas">Todas</a>';
  listaCategorias.appendChild(liTodas);

  // Cada categoría
  categorias.forEach(cat => {
    const li = document.createElement('li');
    li.innerHTML = `<a href="#" data-categoria="${cat}">${cat}</a>`;
    listaCategorias.appendChild(li);
  });

  // Event listeners para filtrar
  listaCategorias.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const categoria = (e.target as HTMLElement).dataset.categoria;
      filtrarPorCategoria(categoria || 'todas');
    });
  });
}

/**
 * Filtra productos por categoría
 */
function filtrarPorCategoria(categoria: string): void {
  if (categoria === 'todas') {
    cargarProductos(productos);
  } else {
    const filtrados = productos.filter(p => p.categoria === categoria);
    cargarProductos(filtrados);
  }
}

/**
 * Carga los productos en el contenedor
 */
function cargarProductos(productosAMostrar: Producto[]): void {
  const contenedor = document.getElementById('contenedor-productos');
  if (!contenedor) return;

  contenedor.innerHTML = '';

  productosAMostrar.forEach(producto => {
    const article = document.createElement('article');
    article.className = 'producto';
    article.innerHTML = `
      <img src="${producto.imagen}" alt="${producto.nombre}">
      <h3>${producto.nombre}</h3>
      <p class="descripcion">${producto.descripcion}</p>
      <p class="precio">$${producto.precio.toLocaleString('es-AR')}</p>
      <button class="btn-agregar" data-id="${producto.id}">Agregar al Carrito</button>
    `;
    contenedor.appendChild(article);
  });

  // Agregar event listeners a los botones
  contenedor.querySelectorAll('.btn-agregar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt((e.target as HTMLElement).dataset.id || '0');
      const producto = productos.find(p => p.id === id);
      if (producto) {
        agregarAlCarrito(producto);
      }
    });
  });
}

/**
 * Agrega un producto al carrito
 */
function agregarAlCarrito(producto: Producto): void {
  const existingItem = carrito.find(item => item.producto.id === producto.id);
  
  if (existingItem) {
    existingItem.cantidad++;
  } else {
    carrito.push({ producto, cantidad: 1 });
  }
  
  actualizarCarrito();
}

/**
 * Actualiza la visualización del carrito
 */
function actualizarCarrito(): void {
  const tbody = document.getElementById('carrito-body');
  const totalEl = document.getElementById('carrito-total');
  
  if (!tbody || !totalEl) return;

  if (carrito.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">El carrito está vacío</td></tr>';
    totalEl.textContent = '$0';
    return;
  }

  let total = 0;
  tbody.innerHTML = '';

  carrito.forEach((item, index) => {
    const subtotal = item.producto.precio * item.cantidad;
    total += subtotal;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.producto.nombre}</td>
      <td>$${item.producto.precio.toLocaleString('es-AR')}</td>
      <td>${item.cantidad}</td>
      <td>$${subtotal.toLocaleString('es-AR')}</td>
      <td><button class="eliminar" data-index="${index}">Eliminar</button></td>
    `;
    tbody.appendChild(tr);
  });

  totalEl.textContent = `$${total.toLocaleString('es-AR')}`;

  // Event listeners para eliminar
  tbody.querySelectorAll('.eliminar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const index = parseInt((e.target as HTMLElement).dataset.index || '0');
      eliminarDelCarrito(index);
    });
  });
}

/**
 * Elimina un producto del carrito
 */
function eliminarDelCarrito(index: number): void {
  if (index >= 0 && index < carrito.length) {
    if (carrito[index].cantidad > 1) {
      carrito[index].cantidad--;
    } else {
      carrito.splice(index, 1);
    }
    actualizarCarrito();
  }
}

/**
 * Configura el buscador
 */
function configurarBuscador(): void {
  const formBusqueda = document.getElementById('form-busqueda');
  const inputBusqueda = document.getElementById('input-busqueda') as HTMLInputElement;

  if (!formBusqueda || !inputBusqueda) return;

  formBusqueda.addEventListener('submit', (e) => {
    e.preventDefault();
    const query = inputBusqueda.value.toLowerCase().trim();
    
    if (!query) {
      cargarProductos(productos);
      return;
    }

    const resultados = productos.filter(p => 
      p.nombre.toLowerCase().includes(query) ||
      p.descripcion.toLowerCase().includes(query) ||
      p.categoria.toLowerCase().includes(query)
    );

    cargarProductos(resultados);
  });
}

/**
 * Muestra la info del usuario en el header
 */
function mostrarInfoUsuario(): void {
  const user = getUserSession();
  const userInfo = document.getElementById('user-info');
  const btnLogout = document.getElementById('btn-logout');

  if (userInfo && user) {
    userInfo.textContent = `Hola, ${user.email}`;
  }

  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      logout();
      redirectToLogin();
    });
  }
}

/**
 * Inicializa la página de cliente
 */
function initClient(): void {
  // Verificar que hay sesión y es cliente
  if (!isAuthenticated()) {
    redirectToLogin();
    return;
  }

  if (!isClient()) {
    redirectToHome();
    return;
  }

  // Inicializar componentes
  mostrarInfoUsuario();
  cargarCategorias();
  cargarProductos(productos);
  configurarBuscador();
  actualizarCarrito();
}

// Ejecutar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initClient);
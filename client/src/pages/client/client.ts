// client.ts — Client panel logic (catalog, cart, search)

import { getUserSession, logout } from '../../utils/auth';
import { api } from '../../utils/api';
import type { CategoriaResponse, ProductoResponse, PaginatedResponse } from '../../types';

// DOM refs
const contenedorProductos = document.getElementById('contenedor-productos')!;
const listaCategorias = document.getElementById('lista-categorias')!;
const carritoBody = document.getElementById('carrito-body')!;
const carritoTotal = document.getElementById('carrito-total')!;
const productosContador = document.getElementById('productos-contador')!;
const btnAnterior = document.getElementById('btn-anterior') as HTMLButtonElement;
const btnSiguiente = document.getElementById('btn-siguiente') as HTMLButtonElement;
const spinner = document.getElementById('loading-spinner')!;
const errorMessage = document.getElementById('error-message')!;

// ==================== CART ====================

interface CartItem {
  product: ProductoResponse;
  quantity: number;
}

let cart: CartItem[] = [];

function addToCart(product: ProductoResponse): void {
  const existing = cart.find(item => item.product.id === product.id);
  if (existing) {
    existing.quantity++;
  } else {
    cart.push({ product, quantity: 1 });
  }
  updateCart();
  alert('Producto agregado al carrito.');
}

function removeFromCart(index: number): void {
  if (index >= 0 && index < cart.length) {
    if (cart[index].quantity > 1) {
      cart[index].quantity--;
    } else {
      cart.splice(index, 1);
    }
    updateCart();
  }
}

function updateCart(): void {
  if (!carritoBody || !carritoTotal) return;
  if (cart.length === 0) {
    carritoBody.innerHTML = '<tr><td colspan="5" style="text-align:center;">El carrito está vacío</td></tr>';
    carritoTotal.textContent = '$0';
    return;
  }
  let total = 0;
  carritoBody.innerHTML = '';
  cart.forEach((item, index) => {
    const subtotal = item.product.precio * item.quantity;
    total += subtotal;
    const tr = document.createElement('tr');
    const nombreTd = document.createElement('td');
    nombreTd.textContent = item.product.nombre;
    const precioTd = document.createElement('td');
    precioTd.textContent = `$${item.product.precio.toLocaleString('es-AR')}`;
    const cantidadTd = document.createElement('td');
    cantidadTd.textContent = String(item.quantity);
    const subtotalTd = document.createElement('td');
    subtotalTd.textContent = `$${subtotal.toLocaleString('es-AR')}`;
    const actionTd = document.createElement('td');
    const eliminarBtn = document.createElement('button');
    eliminarBtn.className = 'eliminar';
    eliminarBtn.dataset.index = String(index);
    eliminarBtn.textContent = 'Eliminar';
    actionTd.appendChild(eliminarBtn);
    tr.append(nombreTd, precioTd, cantidadTd, subtotalTd, actionTd);
    carritoBody.appendChild(tr);
  });
  carritoTotal.textContent = `$${total.toLocaleString('es-AR')}`;
  carritoBody.querySelectorAll('.eliminar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const index = parseInt((e.target as HTMLElement).dataset.index || '0');
      removeFromCart(index);
    });
  });
}

// ==================== PAGINATION STATE ====================

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
const PAGE_SIZE = 12;
type ProductViewMode =
  | { type: 'all' }
  | { type: 'category'; categoriaId: number }
  | { type: 'search'; query: string };
let currentProductView: ProductViewMode = { type: 'all' };

function renderPagination(): void {
  if (btnAnterior) btnAnterior.disabled = currentPage <= 0;
  if (btnSiguiente) btnSiguiente.disabled = currentPage >= totalPages - 1;
  const pageSpan = document.getElementById('pagina-actual');
  if (pageSpan) {
    pageSpan.textContent = `Página ${currentPage + 1} de ${totalPages}`;
  }
}

function updateProductCounter(): void {
  const start = currentPage * PAGE_SIZE + 1;
  const end = Math.min((currentPage + 1) * PAGE_SIZE, totalItems);
  if (productosContador) {
    if (totalItems === 0) {
      productosContador.textContent = 'No hay productos';
    } else {
      productosContador.textContent = `Mostrando ${start}-${end} de ${totalItems} productos`;
    }
  }
}

// ==================== UI HELPERS ====================

function showLoading(): void {
  if (spinner) spinner.classList.remove('hidden');
  if (errorMessage) errorMessage.classList.add('hidden');
  if (contenedorProductos) contenedorProductos.innerHTML = '';
}

function hideLoading(): void {
  if (spinner) spinner.classList.add('hidden');
}

function showError(msg: string): void {
  hideLoading();
  if (errorMessage) {
    errorMessage.textContent = msg;
    errorMessage.classList.remove('hidden');
  }
  if (productosContador) productosContador.textContent = 'Error al cargar productos';
}

// ==================== API CALLS ====================

async function fetchCategories(): Promise<void> {
  try {
    const response = await api.get<PaginatedResponse<CategoriaResponse>>('/categorias?page=0&size=100');
    if (!listaCategorias) return;
    listaCategorias.innerHTML = '';
    const liAll = document.createElement('li');
    const allLink = document.createElement('a');
    allLink.href = '#';
    allLink.dataset.categoriaId = 'all';
    allLink.textContent = 'Todas';
    liAll.appendChild(allLink);
    listaCategorias.appendChild(liAll);
    response.content.forEach(cat => {
      const li = document.createElement('li');
      const link = document.createElement('a');
      link.href = '#';
      link.dataset.categoriaId = String(cat.id);
      link.textContent = cat.nombre;
      li.appendChild(link);
      listaCategorias.appendChild(li);
    });
    listaCategorias.querySelectorAll('a').forEach(link => {
      link.addEventListener('click', (e) => {
        e.preventDefault();
        const id = (e.target as HTMLElement).dataset.categoriaId;
        if (id === 'all') {
          currentProductView = { type: 'all' };
          fetchProducts(0);
        } else if (id) {
          currentProductView = { type: 'category', categoriaId: parseInt(id) };
          fetchProductsByCategoria(parseInt(id), 0);
        }
      });
    });
  } catch (err: unknown) {
    console.error('Error loading categories:', err);
  }
}

async function fetchProducts(page: number): Promise<void> {
  showLoading();
  currentPage = page;
  try {
    const response = await api.get<PaginatedResponse<ProductoResponse>>(`/productos?page=${page}&size=${PAGE_SIZE}`);
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderProducts(response.content);
    renderPagination();
    updateProductCounter();
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al cargar productos';
    showError(msg);
  } finally {
    hideLoading();
  }
}

async function fetchProductsByCategoria(categoriaId: number, page: number): Promise<void> {
  showLoading();
  currentPage = page;
  try {
    const response = await api.get<PaginatedResponse<ProductoResponse>>(`/productos/categoria/${categoriaId}?page=${page}&size=${PAGE_SIZE}`);
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderProducts(response.content);
    renderPagination();
    updateProductCounter();
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al cargar productos';
    showError(msg);
  } finally {
    hideLoading();
  }
}

function renderProducts(products: ProductoResponse[]): void {
  if (!contenedorProductos) return;
  if (products.length === 0) {
    contenedorProductos.innerHTML = '<p style="text-align:center;padding:40px;color:#666;">No hay productos disponibles</p>';
    return;
  }
  contenedorProductos.innerHTML = '';
  products.forEach(product => {
    const article = document.createElement('article');
    article.className = `producto${!product.disponible ? ' no-disponible' : ''}`;
    const img = document.createElement('img');
    img.setAttribute('src', String(product.imagen ?? ''));
    img.setAttribute('alt', product.nombre);
    img.setAttribute('loading', 'lazy');
    const title = document.createElement('h3');
    title.textContent = product.nombre;
    const description = document.createElement('p');
    description.className = 'descripcion';
    description.textContent = String(product.descripcion ?? '');
    const price = document.createElement('p');
    price.className = 'precio';
    price.textContent = `$${product.precio.toLocaleString('es-AR')}`;
    article.append(img, title, description, price);
    if (!product.disponible) {
      const badge = document.createElement('span');
      badge.className = 'badge-no-disponible';
      badge.textContent = 'No disponible';
      article.appendChild(badge);
    }
    const button = document.createElement('button');
    button.className = 'btn-agregar';
    button.dataset.id = String(product.id);
    button.textContent = product.disponible ? 'Agregar al Carrito' : 'Sin stock';
    if (!product.disponible) {
      button.disabled = true;
    }
    article.appendChild(button);
    contenedorProductos.appendChild(article);
  });
  contenedorProductos.querySelectorAll('.btn-agregar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt((e.target as HTMLElement).dataset.id || '0');
      const product = products.find(p => p.id === id);
      if (product && product.disponible) {
        addToCart(product);
      }
    });
  });
}

// ==================== SEARCH ====================

function configureSearch(): void {
  const formSearch = document.getElementById('form-busqueda');
  const inputSearch = document.getElementById('input-busqueda') as HTMLInputElement;
  if (!formSearch || !inputSearch) return;
  formSearch.addEventListener('submit', (e) => {
    e.preventDefault();
    const query = inputSearch.value.trim();
    if (query) {
      currentProductView = { type: 'search', query };
      fetchProductsBySearch(query, 0);
    } else {
      currentProductView = { type: 'all' };
      fetchProducts(0);
    }
  });
}

function fetchCurrentProducts(page: number): void {
  if (currentProductView.type === 'category') {
    fetchProductsByCategoria(currentProductView.categoriaId, page);
  } else if (currentProductView.type === 'search') {
    fetchProductsBySearch(currentProductView.query, page);
  } else {
    fetchProducts(page);
  }
}

async function fetchProductsBySearch(query: string, page: number): Promise<void> {
  showLoading();
  currentPage = page;
  try {
    const response = await api.get<PaginatedResponse<ProductoResponse>>(`/productos?search=${encodeURIComponent(query)}&page=${page}&size=${PAGE_SIZE}`);
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderProducts(response.content);
    renderPagination();
    updateProductCounter();
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al buscar productos';
    showError(msg);
  } finally {
    hideLoading();
  }
}

// ==================== USER INFO ====================

function displayUserInfo(): void {
  const user = getUserSession();
  const userInfo = document.getElementById('user-info');
  const btnLogout = document.getElementById('btn-logout');
  if (userInfo && user) {
    userInfo.textContent = `Hola, ${user.nombre || user.email}`;
  }
  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      logout();
      window.location.href = '/';
    });
  }
}

// ==================== PAGINATION EVENTS ====================

if (btnAnterior) {
  btnAnterior.addEventListener('click', () => {
    if (currentPage > 0) fetchCurrentProducts(currentPage - 1);
  });
}

if (btnSiguiente) {
  btnSiguiente.addEventListener('click', () => {
    if (currentPage < totalPages - 1) fetchCurrentProducts(currentPage + 1);
  });
}

// ==================== INIT ====================

function initClient(): void {
  const user = getUserSession();
  if (!user || user.role !== 'USUARIO') {
    document.body.innerHTML = `
      <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;font-family:Arial,sans-serif;">
        <div style="text-align:center;">
          <h1 style="color:#c33;">Acceso Denegado</h1>
          <p>No tenés permisos para ver esta página.</p>
          <a href="/" style="color:#ff4500;">Volver al inicio</a>
        </div>
      </div>
    `;
    return;
  }
  displayUserInfo();
  fetchCategories();
  currentProductView = { type: 'all' };
  fetchProducts(0);
  configureSearch();
  updateCart();
}

document.addEventListener('DOMContentLoaded', initClient);

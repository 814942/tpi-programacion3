// client.ts — Store page logic (catalog, search, categories)

import { getUserSession } from '../../utils/auth';
import { initHeader, updateCartBadge } from '../../utils/header';
import { api } from '../../utils/api';
import type { CategoriaResponse, ProductoResponse, PaginatedResponse } from '../../types';

// ==================== DOM REFS ====================

const contenedorProductos = document.getElementById('contenedor-productos')!;
const listaCategorias = document.getElementById('lista-categorias')!;
const productosContador = document.getElementById('productos-contador')!;
const btnAnterior = document.getElementById('btn-anterior') as HTMLButtonElement;
const btnSiguiente = document.getElementById('btn-siguiente') as HTMLButtonElement;
const spinner = document.getElementById('loading-spinner')!;
const errorMessage = document.getElementById('error-message')!;
const inputBusqueda = document.getElementById('input-busqueda') as HTMLInputElement;
const btnClearSearch = document.getElementById('btn-clear-search') as HTMLButtonElement;
const formBusqueda = document.getElementById('form-busqueda') as HTMLFormElement;

// ==================== TOAST ====================

function showToast(message: string, type: 'success' | 'error' = 'success'): void {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    if (toast.parentNode) {
      toast.parentNode.removeChild(toast);
    }
  }, 2500);
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
let activeCategoriaId: number | null = null;
let activeSearchQuery: string | null = null;

function renderPagination(): void {
  btnAnterior.disabled = currentPage <= 0;
  btnSiguiente.disabled = currentPage >= totalPages - 1;
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

function clearElement(el: HTMLElement): void {
  while (el.firstChild) el.removeChild(el.firstChild);
}

function showLoading(): void {
  spinner.classList.remove('hidden');
  errorMessage.classList.add('hidden');
  clearElement(contenedorProductos);
}

function hideLoading(): void {
  spinner.classList.add('hidden');
}

function showError(msg: string): void {
  hideLoading();
  errorMessage.textContent = msg;
  errorMessage.classList.remove('hidden');
  productosContador.textContent = 'Error al cargar productos';
}

function highlightCategory(categoriaId: string | null): void {
  listaCategorias.querySelectorAll('a').forEach(link => {
    link.classList.remove('categoria-activa');
  });
  if (categoriaId) {
    const activeLink = listaCategorias.querySelector(`a[data-categoria-id="${categoriaId}"]`);
    if (activeLink) activeLink.classList.add('categoria-activa');
  }
}

function clearSearch(): void {
  inputBusqueda.value = '';
  btnClearSearch.classList.remove('visible');
}

// ==================== API CALLS ====================

async function fetchCategories(): Promise<void> {
  spinner.classList.remove('hidden');
  try {
    const response = await api.get<PaginatedResponse<CategoriaResponse>>('/categorias?page=0&size=100');
    clearElement(listaCategorias);

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
          activeCategoriaId = null;
          activeSearchQuery = null;
          currentProductView = { type: 'all' };
          highlightCategory(null);
          clearSearch();
          fetchProducts(0);
        } else if (id) {
          const catId = parseInt(id);
          activeCategoriaId = catId;
          activeSearchQuery = null;
          currentProductView = { type: 'category', categoriaId: catId };
          highlightCategory(id);
          clearSearch();
          fetchProductsByCategoria(catId, 0);
        }
      });
    });
  } catch (err: unknown) {
    console.error('Error loading categories:', err);
  } finally {
    // Only hide spinner if products also finished or failed
    // Products has its own hideLoading
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

async function fetchProductsByCategoria(categoriaId: number, page: number, search?: string): Promise<void> {
  showLoading();
  currentPage = page;
  try {
    let url = `/productos/categoria/${categoriaId}?page=${page}&size=${PAGE_SIZE}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    const response = await api.get<PaginatedResponse<ProductoResponse>>(url);
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

function fetchCurrentProducts(page: number): void {
  if (activeCategoriaId !== null && activeSearchQuery !== null) {
    // Category + search combined
    fetchProductsByCategoria(activeCategoriaId, page, activeSearchQuery);
  } else if (currentProductView.type === 'category') {
    fetchProductsByCategoria(currentProductView.categoriaId, page);
  } else if (currentProductView.type === 'search') {
    fetchProductsBySearch(currentProductView.query, page);
  } else {
    fetchProducts(page);
  }
}

// ==================== RENDER ====================

function renderProducts(products: ProductoResponse[]): void {
  contenedorProductos.innerHTML = '';

  if (products.length === 0) {
    const emptyMsg = document.createElement('p');
    emptyMsg.style.cssText = 'text-align:center;padding:40px;color:#666;';
    emptyMsg.textContent = 'No hay productos disponibles';
    contenedorProductos.appendChild(emptyMsg);
    return;
  }

  products.forEach(product => {
    const sinStock = product.stock === 0;
    const noDisponible = !product.disponible || sinStock;

    const article = document.createElement('article');
    article.className = `producto${noDisponible ? ' no-disponible' : ''}`;
    article.style.cursor = 'pointer';
    article.setAttribute('tabindex', '0');
    article.setAttribute('role', 'link');
    article.setAttribute('aria-label', `Ver detalle de ${product.nombre}`);
    const navigateToDetail = (): void => {
      window.location.href = `/src/pages/store/productDetail/index.html?id=${product.id}`;
    };
    article.addEventListener('click', navigateToDetail);
    article.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === ' ' || e.code === 'Space') {
        e.preventDefault();
        navigateToDetail();
      }
    });

    const img = document.createElement('img');
    img.src = product.imagen || '';
    img.alt = product.nombre;
    img.loading = 'lazy';

    const title = document.createElement('h3');
    title.textContent = product.nombre;

    const description = document.createElement('p');
    description.className = 'descripcion';
    description.textContent = product.descripcion || '';

    const price = document.createElement('p');
    price.className = 'precio';
    price.textContent = `$${product.precio.toLocaleString('es-AR')}`;

    article.append(img, title, description, price);

    const button = document.createElement('button');
    button.className = 'btn-agregar';
    button.dataset.id = String(product.id);
    button.textContent = noDisponible ? 'Sin stock' : 'Agregar al Carrito';
    if (noDisponible) {
      button.className = 'btn-agregar btn-no-disponible';
      button.disabled = true;
    };

    if (!noDisponible) {
      button.addEventListener('click', (e) => {
        e.stopPropagation();
        try {
          const parsedCart = JSON.parse(localStorage.getItem('cart') || '[]');
          const cart = Array.isArray(parsedCart) ? parsedCart : [];
          if (!Array.isArray(parsedCart)) {
            localStorage.setItem('cart', '[]');
          }
          const existing = cart.find((item: any) => item.product.id === product.id);
          if (existing) {
            existing.quantity += 1;
          } else {
            cart.push({ product, quantity: 1 });
          }
          localStorage.setItem('cart', JSON.stringify(cart));
          updateCartBadge();
          showToast(`${product.nombre} agregado al carrito`, 'success');
        } catch {
          showToast('Error al agregar al carrito', 'error');
        }
      });
    }

    article.appendChild(button);
    contenedorProductos.appendChild(article);
  });
}

// ==================== SEARCH ====================

function configureSearch(): void {
  if (!formBusqueda || !inputBusqueda) return;

  formBusqueda.addEventListener('submit', (e) => {
    e.preventDefault();
    const query = inputBusqueda.value.trim();
    if (query) {
      activeSearchQuery = query;
      btnClearSearch.classList.add('visible');
      if (activeCategoriaId !== null) {
        // Search within selected category — keep highlight
        currentProductView = { type: 'search', query };
        fetchProductsByCategoria(activeCategoriaId, 0, query);
      } else {
        // Search all products
        currentProductView = { type: 'search', query };
        highlightCategory(null);
        fetchProductsBySearch(query, 0);
      }
    } else {
      activeSearchQuery = null;
      activeCategoriaId = null;
      currentProductView = { type: 'all' };
      highlightCategory(null);
      fetchProducts(0);
    }
  });

  inputBusqueda.addEventListener('input', () => {
    if (inputBusqueda.value.trim()) {
      btnClearSearch.classList.add('visible');
    } else {
      btnClearSearch.classList.remove('visible');
    }
  });

  btnClearSearch.addEventListener('click', () => {
    clearSearch();
    activeSearchQuery = null;
    currentProductView = { type: 'all' };
    if (activeCategoriaId !== null) {
      // Restore category view
      const catId = activeCategoriaId;
      currentProductView = { type: 'category', categoriaId: catId };
      highlightCategory(String(catId));
      fetchProductsByCategoria(catId, 0);
    } else {
      highlightCategory(null);
      fetchProducts(0);
    }
  });
}

// ==================== PAGINATION EVENTS ====================

btnAnterior.addEventListener('click', () => {
  if (currentPage > 0) fetchCurrentProducts(currentPage - 1);
});

btnSiguiente.addEventListener('click', () => {
  if (currentPage < totalPages - 1) fetchCurrentProducts(currentPage + 1);
});

// ==================== INIT ====================

function initClient(): void {
  const user = getUserSession();
  if (!user || user.role !== 'USUARIO') {
    while (document.body.firstChild) document.body.removeChild(document.body.firstChild);
    const outerDiv = document.createElement('div');
    outerDiv.style.cssText = 'display:flex;justify-content:center;align-items:center;min-height:100vh;font-family:Arial,sans-serif;';
    const innerDiv = document.createElement('div');
    innerDiv.style.textAlign = 'center';
    const h1 = document.createElement('h1');
    h1.style.color = '#c33';
    h1.textContent = 'Acceso Denegado';
    const p = document.createElement('p');
    p.textContent = 'No tenés permisos para ver esta página.';
    const a = document.createElement('a');
    a.href = '/';
    a.style.color = '#ff4500';
    a.textContent = 'Volver al inicio';
    innerDiv.append(h1, p, a);
    outerDiv.appendChild(innerDiv);
    document.body.appendChild(outerDiv);
    return;
  }
  initHeader();
  currentProductView = { type: 'all' };
  fetchCategories();
  fetchProducts(0);
  configureSearch();
}

document.addEventListener('DOMContentLoaded', initClient);

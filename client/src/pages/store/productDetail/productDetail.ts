// productDetail.ts — Product detail page

import { getUserSession } from '../../../utils/auth';
import { initHeader } from '../../../utils/header';
import { api } from '../../../utils/api';
import type { ProductoResponse } from '../../../types';

// ==================== DOM REFS ====================

const spinner = document.getElementById('loading-spinner')!;
const detalle = document.getElementById('producto-detalle')!;
const errorMsg = document.getElementById('error-message')!;
const img = document.getElementById('producto-imagen') as HTMLImageElement;
const nombre = document.getElementById('producto-nombre')!;
const descripcion = document.getElementById('producto-descripcion')!;
const precio = document.getElementById('producto-precio')!;
const stockInfo = document.getElementById('producto-stock')!;
const noDisponible = document.getElementById('producto-no-disponible')!;
const btnRestar = document.getElementById('btn-restar') as HTMLButtonElement;
const btnSumar = document.getElementById('btn-sumar') as HTMLButtonElement;
const cantidadValor = document.getElementById('cantidad-valor')!;
const btnAgregar = document.getElementById('btn-agregar-carrito') as HTMLButtonElement;
const cantidadControl = document.getElementById('cantidad-control')!;

// ==================== STATE ====================

let producto: ProductoResponse | null = null;
let cantidad = 1;

// ==================== TOAST ====================

function showToast(message: string, type: 'success' | 'error' = 'success'): void {
  const container = document.getElementById('toast-container');
  if (!container) return;
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => {
    if (toast.parentNode) toast.parentNode.removeChild(toast);
  }, 2500);
}

// ==================== QUANTITY ====================

function updateQuantityControls(): void {
  if (!producto) return;
  const sinStock = producto.stock === 0 || !producto.disponible;
  if (sinStock) {
    btnRestar.disabled = true;
    btnSumar.disabled = true;
    return;
  }
  btnRestar.disabled = cantidad <= 1;
  btnSumar.disabled = cantidad >= producto.stock;
  cantidadValor.textContent = String(cantidad);
}

function cambiarCantidad(delta: number): void {
  if (!producto) return;
  const nueva = cantidad + delta;
  if (nueva >= 1 && nueva <= producto.stock) {
    cantidad = nueva;
    updateQuantityControls();
  }
}

// ==================== ADD TO CART ====================

function addToCart(): void {
  if (!producto) return;
  const CART_KEY = 'cart';
  let cart: { product: ProductoResponse; quantity: number }[] = [];
  try {
    const stored = localStorage.getItem(CART_KEY);
    if (stored) cart = JSON.parse(stored);
  } catch { /* ignore */ }

  const existing = cart.find(item => item.product.id === producto!.id);
  if (existing) {
    const newQuantity = existing.quantity + cantidad;
    if (newQuantity > producto!.stock) {
      showToast('No hay suficiente stock disponible', 'error');
      existing.quantity = producto!.stock;
    } else {
      existing.quantity = newQuantity;
    }
  } else {
    cart.push({ product: producto, quantity: cantidad });
  }

  localStorage.setItem(CART_KEY, JSON.stringify(cart));
  showToast(`${producto.nombre} agregado al carrito`, 'success');
}

// ==================== LOAD PRODUCT ====================

async function loadProduct(): Promise<void> {
  const params = new URLSearchParams(window.location.search);
  const id = params.get('id');
  if (!id) {
    spinner.classList.add('hidden');
    errorMsg.classList.remove('hidden');
    const p = document.createElement('p');
    p.textContent = 'ID de producto no válido.';
    const btn = document.createElement('button');
    btn.className = 'btn-volver';
    btn.textContent = 'Volver';
    btn.addEventListener('click', () => history.back());
    errorMsg.innerHTML = '';
    errorMsg.append(p, btn);
    return;
  }

  try {
    const product = await api.get<ProductoResponse>(`/productos/${id}`);
    producto = product;
    renderProduct(product);
  } catch (err: unknown) {
    spinner.classList.add('hidden');
    errorMsg.classList.remove('hidden');
    const msg = err instanceof Error ? err.message : 'Error al cargar el producto';
    const p = document.createElement('p');
    p.textContent = msg;
    const btn = document.createElement('button');
    btn.className = 'btn-volver';
    btn.textContent = 'Volver';
    btn.addEventListener('click', () => history.back());
    errorMsg.innerHTML = '';
    errorMsg.append(p, btn);
  }
}

function renderProduct(product: ProductoResponse): void {
  spinner.classList.add('hidden');
  detalle.classList.remove('hidden');

  img.src = product.imagen || '';
  img.alt = product.nombre;
  nombre.textContent = product.nombre;
  descripcion.textContent = product.descripcion || '';
  precio.textContent = `$${product.precio.toLocaleString('es-AR')}`;

  if (!product.disponible || product.stock === 0) {
    noDisponible.classList.remove('hidden');
    stockInfo.textContent = 'Producto no disponible';
    stockInfo.className = 'stock-info sin-stock';
    cantidadControl.classList.add('hidden');
    btnAgregar.disabled = true;
    btnAgregar.textContent = 'Sin stock';
    return;
  }

  stockInfo.textContent = `Stock disponible: ${product.stock} unidades`;
  stockInfo.className = 'stock-info';

  cantidad = 1;
  updateQuantityControls();
}

// ==================== EVENTS ====================

btnRestar.addEventListener('click', () => cambiarCantidad(-1));
btnSumar.addEventListener('click', () => cambiarCantidad(1));
btnAgregar.addEventListener('click', addToCart);

// ==================== INIT ====================

function init(): void {
  const user = getUserSession();
  if (!user || user.role !== 'USUARIO') {
    document.body.innerHTML = `
      <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;">
        <div style="text-align:center;">
          <h1 style="color:#c33;">Acceso Denegado</h1>
          <p>No tenés permisos para ver esta página.</p>
          <a href="/src/pages/auth/login/index.html">Iniciar Sesión</a>
        </div>
      </div>
    `;
    return;
  }
  initHeader();
  loadProduct();
}

document.addEventListener('DOMContentLoaded', init);

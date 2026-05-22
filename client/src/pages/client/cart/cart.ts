import { initHeader, updateCartBadge } from '../../../utils/header';
import { api } from '../../../utils/api';
import { protectRoute } from '../../../utils/navigate';
import type { ProductoResponse } from '../../../types';

interface CartItem {
  product: ProductoResponse;
  quantity: number;
}

interface ProductoValidacionResponse {
  id: number;
  existe: boolean;
  disponible: boolean;
  stock: number;
}

interface DetallePedido {
  idProducto: number;
  cantidad: number;
}

interface PedidoRequest {
  formaPago: string;
  detalles: DetallePedido[];
}

let cartItems: CartItem[] = [];
let validaciones: Map<number, ProductoValidacionResponse> = new Map();
let hasValidationError = false;
let isSubmitting = false;

const contenedorProductos = document.getElementById('contenedor-productos')!;
const resumenCompra = document.getElementById('resumen-compra')!;
const emptyState = document.getElementById('empty-state')!;
const spinner = document.getElementById('loading-spinner')!;

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

function loadCart(): CartItem[] {
  try {
    const raw = localStorage.getItem('cart');
    if (!raw) return [];
    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];
    return parsed as CartItem[];
  } catch {
    return [];
  }
}

function saveCart(items: CartItem[]): void {
  localStorage.setItem('cart', JSON.stringify(items));
  updateCartBadge();
}

function showLoading(): void {
  spinner.classList.remove('hidden');
}

function hideLoading(): void {
  spinner.classList.add('hidden');
}

async function fetchValidaciones(ids: number[]): Promise<{ map: Map<number, ProductoValidacionResponse>; failed: boolean }> {
  if (ids.length === 0) return { map: new Map(), failed: false };
  try {
    const result = await api.post<ProductoValidacionResponse[]>('/productos/validate', { ids });
    const map = new Map<number, ProductoValidacionResponse>();
    result.forEach(v => map.set(v.id, v));
    return { map, failed: false };
  } catch {
    return { map: new Map(), failed: true };
  }
}

function hasInvalidProducts(): boolean {
  if (hasValidationError) return false;
  return cartItems.some(item => {
    const v = validaciones.get(item.product.id);
    return !v || !v.existe || !v.disponible || v.stock < item.quantity;
  });
}

function render(): void {
  contenedorProductos.innerHTML = '';
  resumenCompra.innerHTML = '';

  if (cartItems.length === 0) {
    emptyState.classList.remove('hidden');
    contenedorProductos.classList.add('hidden');
    resumenCompra.classList.add('hidden');
    return;
  }

  emptyState.classList.add('hidden');
  contenedorProductos.classList.remove('hidden');
  resumenCompra.classList.remove('hidden');

  cartItems.forEach(item => {
    const v = validaciones.get(item.product.id);
    const article = renderProduct(item, v);
    contenedorProductos.appendChild(article);
  });

  resumenCompra.appendChild(renderSummary());
}

function renderProduct(item: CartItem, v: ProductoValidacionResponse | undefined): HTMLElement {
  const isInvalid = v && (!v.existe || !v.disponible);
  const lowStock = v && v.existe && v.disponible && v.stock < item.quantity;

  const article = document.createElement('article');
  article.className = `cart-item${isInvalid ? ' item-invalido' : ''}`;

  const img = document.createElement('img');
  img.src = item.product.imagen || '';
  img.alt = item.product.nombre;

  const info = document.createElement('div');
  info.className = 'item-info';

  const title = document.createElement('h3');
  title.textContent = item.product.nombre;

  const price = document.createElement('p');
  price.className = 'item-precio';
  price.textContent = `$${item.product.precio.toLocaleString('es-AR')}`;

  info.append(title, price);

  if (isInvalid) {
    const warning = document.createElement('p');
    warning.className = 'warning-msg';
    warning.textContent = 'Este producto ya no está disponible';
    info.appendChild(warning);
  } else if (lowStock) {
    const warning = document.createElement('p');
    warning.className = 'warning-msg';
    warning.textContent = `Stock disponible: ${v!.stock}`;
    info.appendChild(warning);
  }

  const actions = document.createElement('div');
  actions.className = 'item-actions';

  const btnMinus = document.createElement('button');
  btnMinus.className = 'btn-cant';
  btnMinus.textContent = '−';
  btnMinus.disabled = item.quantity <= 1 || !!isInvalid;

  const qtySpan = document.createElement('span');
  qtySpan.className = 'cantidad';
  qtySpan.textContent = String(item.quantity);

  const btnPlus = document.createElement('button');
  btnPlus.className = 'btn-cant';
  btnPlus.textContent = '+';
  const maxStock = v ? v.stock : item.product.stock;
  btnPlus.disabled = item.quantity >= maxStock || !!isInvalid;

  const btnDelete = document.createElement('button');
  btnDelete.className = 'btn-eliminar';
  btnDelete.textContent = 'Eliminar';

  actions.append(btnMinus, qtySpan, btnPlus, btnDelete);
  article.append(img, info, actions);

  btnMinus.addEventListener('click', () => updateQuantity(item.product.id, -1));
  btnPlus.addEventListener('click', () => updateQuantity(item.product.id, 1));
  btnDelete.addEventListener('click', () => deleteItem(item.product.id));

  return article;
}

function renderSummary(): HTMLElement {
  const summary = document.createElement('div');

  const title = document.createElement('h2');
  title.textContent = 'Resumen';
  summary.appendChild(title);

  const invalid = hasInvalidProducts();

  let subtotal = 0;
  cartItems.forEach(item => {
    const v = validaciones.get(item.product.id);
    const isValid = !v || (v.existe && v.disponible && v.stock >= item.quantity);
    const precio = isValid ? item.product.precio : 0;
    const qty = isValid ? item.quantity : 0;
    subtotal += precio * qty;
  });

  const row = document.createElement('div');
  row.className = 'summary-row';
  row.innerHTML = `<span>Subtotal</span><span>$${subtotal.toLocaleString('es-AR')}</span>`;
  summary.appendChild(row);

  const total = document.createElement('div');
  total.className = 'summary-total';
  total.innerHTML = `<span>Total</span><span>$${subtotal.toLocaleString('es-AR')}</span>`;
  summary.appendChild(total);

  const btnCheckout = document.createElement('button');
  btnCheckout.className = 'btn-finalizar';
  btnCheckout.textContent = 'Finalizar Compra';
  btnCheckout.disabled = invalid || cartItems.length === 0;
  if (!btnCheckout.disabled) {
    btnCheckout.addEventListener('click', openCheckoutModal);
  }

  const btnClear = document.createElement('button');
  btnClear.className = 'btn-vaciar';
  btnClear.textContent = 'Vaciar Carrito';
  btnClear.addEventListener('click', clearCart);

  summary.append(btnCheckout, btnClear);
  return summary;
}

function updateQuantity(productId: number, delta: number): void {
  const item = cartItems.find(i => i.product.id === productId);
  if (!item) return;

  const v = validaciones.get(productId);
  const maxStock = v ? v.stock : item.product.stock;
  const newQty = Math.max(1, Math.min(maxStock, item.quantity + delta));
  item.quantity = newQty;

  saveCart(cartItems);
  render();
}

function deleteItem(productId: number): void {
  cartItems = cartItems.filter(i => i.product.id !== productId);
  saveCart(cartItems);
  validaciones.delete(productId);

  if (cartItems.length === 0) {
    render();
    return;
  }
  render();
}

function clearCart(): void {
  if (!confirm('¿Vaciar carrito?')) return;
  cartItems = [];
  validaciones = new Map();
  saveCart(cartItems);
  render();
}

function openCheckoutModal(): void {
  const modal = document.getElementById('checkout-modal');
  if (!modal) return;

  const totalEl = document.getElementById('modal-total');
  if (totalEl) {
    let total = 0;
    cartItems.forEach(item => {
      const v = validaciones.get(item.product.id);
      if (!v || (v.existe && v.disponible && v.stock >= item.quantity)) {
        total += item.product.precio * item.quantity;
      }
    });
    totalEl.textContent = `$${total.toLocaleString('es-AR')}`;
  }

  modal.classList.remove('hidden');
}

function closeCheckoutModal(): void {
  const modal = document.getElementById('checkout-modal');
  if (modal) modal.classList.add('hidden');
}

async function submitCheckout(): Promise<void> {
  if (isSubmitting) return;

  const formaPago = (document.getElementById('forma-pago') as HTMLSelectElement)?.value;
  if (!formaPago) {
    showToast('Seleccioná una forma de pago', 'error');
    return;
  }

  const ids = cartItems.map(item => item.product.id);
  const { map, failed } = await fetchValidaciones(ids);
  if (!failed) {
    hasValidationError = false;
    validaciones = map;
    const changed = cartItems.some(item => {
      const v = validaciones.get(item.product.id);
      return !v || !v.existe || !v.disponible || v.stock < item.quantity;
    });
    if (changed) {
      render();
      closeCheckoutModal();
      showToast('Algunos productos cambiaron. Revisá el carrito antes de confirmar.', 'error');
      return;
    }
  } else {
    hasValidationError = true;
    validaciones = new Map();
  }

  const detalles: DetallePedido[] = cartItems
    .filter(item => {
      const v = validaciones.get(item.product.id);
      return !v || (v.existe && v.disponible && v.stock >= item.quantity);
    })
    .map(item => ({ idProducto: item.product.id, cantidad: item.quantity }));

  if (detalles.length === 0) {
    showToast('No hay productos válidos para comprar', 'error');
    return;
  }

  isSubmitting = true;
  const confirmBtn = document.getElementById('btn-confirmar-pedido') as HTMLButtonElement;
  if (confirmBtn) confirmBtn.disabled = true;

  try {
    const body: PedidoRequest = { formaPago, detalles };
    await api.post('/pedidos', body);
    localStorage.removeItem('cart');
    window.location.href = '/src/pages/client/checkout/resultado.html?success=true';
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al procesar el pedido';
    closeCheckoutModal();
    showToast(msg, 'error');
    window.location.href = `/src/pages/client/checkout/resultado.html?success=false&message=${encodeURIComponent(msg)}`;
  } finally {
    isSubmitting = false;
    if (confirmBtn) confirmBtn.disabled = false;
  }
}

function initCart(): void {
  if (!protectRoute()) return;

  initHeader();
  cartItems = loadCart();

  if (cartItems.length === 0) {
    render();
    return;
  }

  showLoading();
  const ids = cartItems.map(i => i.product.id);

  fetchValidaciones(ids).then(({ map, failed }) => {
    validaciones = map;
    hasValidationError = failed;
    hideLoading();
    render();
  }).catch(() => {
    hasValidationError = true;
    validaciones = new Map();
    hideLoading();
    render();
  });

  const confirmBtn = document.getElementById('btn-confirmar-pedido');
  const cancelBtn = document.getElementById('btn-cancelar-pedido');

  confirmBtn?.addEventListener('click', submitCheckout);
  cancelBtn?.addEventListener('click', closeCheckoutModal);

  const modal = document.getElementById('checkout-modal');
  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeCheckoutModal();
  });
}

document.addEventListener('DOMContentLoaded', initCart);

import { initHeader } from '../../../utils/header';
import { api } from '../../../utils/api';
import { protectRoute } from '../../../utils/navigate';
import type { PaginatedResponse } from '../../../types';

interface DetallePedido {
  productoNombre: string;
  productoPrecio: number;
  cantidad: number;
  subtotal: number;
  productoImagen: string;
}

interface PedidoResumen {
  id: number;
  fecha: string;
  estado: 'PENDIENTE' | 'CONFIRMADO' | 'TERMINADO' | 'CANCELADO';
  formaPago: string;
  telefono?: string | null;
  total: number;
  detalles: DetallePedido[];
}

type PedidoDetalle = PedidoResumen;

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
let PAGE_SIZE = 10;

const STATUS_LABELS: Record<string, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADO: 'Confirmado',
  TERMINADO: 'Terminado',
  CANCELADO: 'Cancelado',
};

function formatDateShort(iso: string): string {
  const d = new Date(iso);
  const now = new Date();
  const opts: Intl.DateTimeFormatOptions = { day: 'numeric', month: 'long' };
  if (d.getFullYear() !== now.getFullYear()) {
    opts.year = 'numeric';
  }
  return d.toLocaleDateString('es-AR', opts);
}

function formatDateDetail(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString('es-AR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

function formatPeso(value: number): string {
  return `$${value.toLocaleString('es-AR')}`;
}

function getStatusBadgeClass(estado: string): string {
  return `status-${estado.toLowerCase()}`;
}

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

function showLoading(): void {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.classList.remove('hidden');
}

function hideLoading(): void {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.classList.add('hidden');
}

function clearElement(el: HTMLElement): void {
  while (el.firstChild) el.removeChild(el.firstChild);
}

async function fetchOrders(page: number, size: number): Promise<PaginatedResponse<PedidoResumen>> {
  return api.get<PaginatedResponse<PedidoResumen>>(`/pedidos/usuario?page=${page}&size=${size}`);
}

// ==================== GROUP BY DATE ====================

function groupByDate(orders: PedidoResumen[]): Map<string, PedidoResumen[]> {
  const groups = new Map<string, PedidoResumen[]>();
  orders.forEach(order => {
    const key = formatDateShort(order.fecha);
    if (!groups.has(key)) groups.set(key, []);
    groups.get(key)!.push(order);
  });
  return groups;
}

// ==================== RENDER ====================

function renderOrders(response: PaginatedResponse<PedidoResumen>): void {
  const container = document.getElementById('orders-list');
  const emptyEl = document.getElementById('orders-empty');
  const paginationEl = document.getElementById('orders-paginacion');
  const pageSizeSelector = document.getElementById('page-size-selector');
  if (!container || !emptyEl || !paginationEl || !pageSizeSelector) return;

  clearElement(container);

  if (response.content.length === 0) {
    emptyEl.classList.remove('hidden');
    paginationEl.classList.add('hidden');
    pageSizeSelector.classList.add('hidden');
    return;
  }

  emptyEl.classList.add('hidden');
  paginationEl.classList.remove('hidden');
  pageSizeSelector.classList.remove('hidden');

  const groups = groupByDate(response.content);

  groups.forEach((orders, dateLabel) => {
    // Date header
    const dateHeader = document.createElement('div');
    dateHeader.className = 'orders-date-header';
    dateHeader.textContent = dateLabel;
    container.appendChild(dateHeader);

    // Product rows for each order
    orders.forEach(order => {
      order.detalles.forEach(d => {
        const row = document.createElement('div');
        row.className = 'order-row';

        const img = document.createElement('img');
        img.className = 'order-row-img';
        img.src = d.productoImagen || '';
        img.alt = d.productoNombre;

        const nameEl = document.createElement('span');
        nameEl.className = 'order-row-name';
        nameEl.textContent = d.productoNombre;
        nameEl.addEventListener('click', () => openDetail(order.id));

        const qtyEl = document.createElement('span');
        qtyEl.className = 'order-row-qty';
        qtyEl.textContent = `x${d.cantidad}`;

        const badge = document.createElement('span');
        badge.className = `status-badge ${getStatusBadgeClass(order.estado)}`;
        badge.textContent = STATUS_LABELS[order.estado] || order.estado;

        row.append(img, nameEl, qtyEl, badge);
        container.appendChild(row);
      });

      // Total line for each order
      const totalLine = document.createElement('div');
      totalLine.className = 'order-total-line';
      totalLine.textContent = `Total: ${formatPeso(order.total)}`;
      container.appendChild(totalLine);
    });
  });

  renderPagination();
}

function renderPagination(): void {
  const btnPrev = document.getElementById('btn-prev') as HTMLButtonElement;
  const btnNext = document.getElementById('btn-next') as HTMLButtonElement;
  const pageInfo = document.getElementById('page-info');
  if (!btnPrev || !btnNext || !pageInfo) return;

  btnPrev.disabled = currentPage <= 0;
  btnNext.disabled = currentPage >= totalPages - 1;

  const start = totalItems === 0 ? 0 : currentPage * PAGE_SIZE + 1;
  const end = Math.min((currentPage + 1) * PAGE_SIZE, totalItems);
  const label = totalPages <= 1
    ? `${totalItems} ${totalItems === 1 ? 'pedido' : 'pedidos'}`
    : `Mostrando ${start}-${end} de ${totalItems} pedidos`;
  pageInfo.textContent = label;
}

// ==================== DETAIL MODAL ====================

async function openDetail(orderId: number): Promise<void> {
  const modal = document.getElementById('order-detail-modal');
  const body = document.getElementById('modal-body');
  const actions = document.getElementById('modal-actions');
  if (!modal || !body || !actions) return;

  clearElement(body);
  const spinnerDiv = document.createElement('div');
  spinnerDiv.className = 'loading-spinner';
  spinnerDiv.innerHTML = '<div class="spinner"></div>';
  body.appendChild(spinnerDiv);
  modal.classList.remove('hidden');

  try {
    const order = await api.get<PedidoDetalle>(`/pedidos/${orderId}`);
    clearElement(body);

    // Header: date | #number
    const header = document.createElement('div');
    header.className = 'detail-header';
    header.innerHTML = `
      <span>${formatDateDetail(order.fecha)}</span>
      <span class="detail-order-num"># ${order.id}</span>
    `;
    body.appendChild(header);

    // Status badge
    const badgeRow = document.createElement('div');
    badgeRow.style.cssText = 'margin-bottom:16px;';
    const badge = document.createElement('span');
    badge.className = `status-badge ${getStatusBadgeClass(order.estado)}`;
    badge.style.fontSize = '14px';
    badge.textContent = STATUS_LABELS[order.estado] || order.estado;
    badgeRow.appendChild(badge);
    body.appendChild(badgeRow);

    // Products list
    const productsTitle = document.createElement('div');
    productsTitle.className = 'detail-row detail-row-header';
    productsTitle.innerHTML = '<span>Producto</span><span style="text-align:right">Precio x Cant = Subtotal</span>';
    body.appendChild(productsTitle);

    order.detalles.forEach(d => {
      const row = document.createElement('div');
      row.className = 'detail-row';
      row.innerHTML = `
        <span>${d.productoNombre}</span>
        <span>${formatPeso(d.productoPrecio)} x ${d.cantidad} = ${formatPeso(d.subtotal)}</span>
      `;
      body.appendChild(row);
    });

    // Total
    const totalDiv = document.createElement('div');
    totalDiv.className = 'detail-total';
    totalDiv.textContent = `Total: ${formatPeso(order.total)}`;
    body.appendChild(totalDiv);

    // Payment method
    const payLabels: Record<string, string> = { TARJETA: 'Tarjeta', EFECTIVO: 'Efectivo', TRANSFERENCIA: 'Transferencia' };
    const payRow = document.createElement('div');
    payRow.className = 'detail-row';
    payRow.innerHTML = `<span>Forma de pago</span><span>${payLabels[order.formaPago] || order.formaPago}</span>`;
    body.appendChild(payRow);

    if (order.telefono) {
      const telRow = document.createElement('div');
      telRow.className = 'detail-row';
      telRow.innerHTML = `<span>Telefono</span><span>${order.telefono}</span>`;
      body.appendChild(telRow);
    }

    // Buttons - centered
    actions.innerHTML = '';
    const btnContainer = document.createElement('div');
    btnContainer.style.cssText = 'display:flex;justify-content:center;gap:8px;margin-top:16px;';

    const btnClose = document.createElement('button');
    btnClose.className = 'btn-cerrar';
    btnClose.textContent = 'Cerrar';
    btnClose.addEventListener('click', closeDetail);
    btnContainer.appendChild(btnClose);

    if (order.estado === 'PENDIENTE') {
      const btnCancel = document.createElement('button');
      btnCancel.className = 'btn-cancelar';
      btnCancel.textContent = 'Cancelar Pedido';
      btnCancel.addEventListener('click', () => handleCancelClick(orderId, btnCancel, btnContainer));
      btnContainer.appendChild(btnCancel);
    }

    actions.appendChild(btnContainer);
  } catch {
    clearElement(body);
    body.innerHTML = '<p style="color:#dc2626;text-align:center">Error al cargar el detalle del pedido</p>';
  }
}

function closeDetail(): void {
  const modal = document.getElementById('order-detail-modal');
  if (modal) modal.classList.add('hidden');
}

// ==================== CANCEL WITH CONFIRMATION (NO alert()) ====================

function handleCancelClick(orderId: number, btn: HTMLButtonElement, container: HTMLElement): void {
  // Change button to confirmation
  btn.textContent = 'Seguro?';
  btn.style.background = '#c33';

  const btnNo = document.createElement('button');
  btnNo.className = 'btn-cerrar';
  btnNo.textContent = 'No';
  btnNo.addEventListener('click', () => {
    btn.textContent = 'Cancelar Pedido';
    btn.style.background = '';
    if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
  });
  container.insertBefore(btnNo, btn.nextSibling);

  // Remove previous listeners by replacing with new one
  const newBtn = btn.cloneNode(true) as HTMLButtonElement;
  btn.parentNode?.replaceChild(newBtn, btn);

  newBtn.addEventListener('click', async () => {
    try {
      await api.patch<{ id: number; estado: string }>(`/pedidos/${orderId}/cancelar`);
      showToast('Pedido cancelado con exito', 'success');
      closeDetail();
      await loadPage(currentPage);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error al cancelar el pedido';
      if (msg.includes('ya no se puede') || msg.includes('PENDIENTE')) {
        showToast('El pedido ya no se puede cancelar', 'error');
      } else {
        showToast(msg, 'error');
      }
      closeDetail();
    }
  });
}

// ==================== LOAD PAGE ====================

async function loadPage(page: number): Promise<void> {
  showLoading();
  const container = document.getElementById('orders-list');
  const emptyEl = document.getElementById('orders-empty');
  const paginationEl = document.getElementById('orders-paginacion');
  const pageSizeSelector = document.getElementById('page-size-selector');
  if (container) clearElement(container);
  if (emptyEl) emptyEl.classList.add('hidden');
  if (paginationEl) paginationEl.classList.add('hidden');
  if (pageSizeSelector) pageSizeSelector.classList.add('hidden');

  try {
    const response = await fetchOrders(page, PAGE_SIZE);
    currentPage = response.page;
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderOrders(response);
  } catch {
    const containerEl = document.getElementById('orders-list');
    if (containerEl) {
      containerEl.innerHTML = '<p style="color:#dc2626;text-align:center">Error al cargar los pedidos. Intentá de nuevo.</p>';
    }
  } finally {
    hideLoading();
  }
}

// ==================== INIT ====================

function initOrders(): void {
  if (!protectRoute()) return;
  initHeader();
  loadPage(0);

  document.getElementById('btn-prev')?.addEventListener('click', () => {
    if (currentPage > 0) loadPage(currentPage - 1);
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadPage(currentPage + 1);
  });

  document.getElementById('page-size')?.addEventListener('change', (e) => {
    PAGE_SIZE = Number((e.target as HTMLSelectElement).value);
    loadPage(0);
  });

  document.getElementById('btn-cerrar-detalle')?.addEventListener('click', closeDetail);

  const modal = document.getElementById('order-detail-modal');
  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeDetail();
  });
}

document.addEventListener('DOMContentLoaded', initOrders);

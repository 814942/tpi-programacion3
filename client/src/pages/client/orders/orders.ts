import { initHeader } from '../../../utils/header';
import { api } from '../../../utils/api';
import { protectRoute } from '../../../utils/navigate';
import type { PaginatedResponse } from '../../../types';

interface DetallePedido {
  productoNombre: string;
  productoPrecio: number;
  cantidad: number;
  subtotal: number;
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

function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString('es-AR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

function formatPeso(value: number): string {
  return `$${value.toLocaleString('es-AR')}`;
}

function getStatusBadgeClass(estado: string): string {
  return `status-${estado.toLowerCase()}`;
}

function getStatusIcon(estado: string): string {
  switch (estado) {
    case 'PENDIENTE': return '⏳';
    case 'CONFIRMADO': return '✅';
    case 'TERMINADO': return '🎉';
    case 'CANCELADO': return '❌';
    default: return '❓';
  }
}

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

function showLoading(): void {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.classList.remove('hidden');
}

function hideLoading(): void {
  const spinner = document.getElementById('loading-spinner');
  if (spinner) spinner.classList.add('hidden');
}

async function fetchOrders(page: number, size: number): Promise<PaginatedResponse<PedidoResumen>> {
  return api.get<PaginatedResponse<PedidoResumen>>(`/pedidos/usuario?page=${page}&size=${size}`);
}

function renderOrders(response: PaginatedResponse<PedidoResumen>): void {
  const container = document.getElementById('orders-list');
  const emptyEl = document.getElementById('orders-empty');
  const paginationEl = document.getElementById('orders-paginacion');
  const pageSizeSelector = document.getElementById('page-size-selector');
  if (!container || !emptyEl || !paginationEl || !pageSizeSelector) return;

  container.innerHTML = '';

  if (response.content.length === 0) {
    emptyEl.classList.remove('hidden');
    paginationEl.classList.add('hidden');
    pageSizeSelector.classList.add('hidden');
    return;
  }

  emptyEl.classList.add('hidden');

  response.content.forEach(order => {
    const card = document.createElement('div');
    card.className = 'order-card';

    const info = document.createElement('div');
    info.className = 'order-info';

    const idEl = document.createElement('div');
    idEl.className = 'order-id';
    idEl.textContent = `#${order.id}`;

    const dateEl = document.createElement('div');
    dateEl.className = 'order-date';
    dateEl.textContent = formatDate(order.fecha);

    const summaryItems = order.detalles.slice(0, 3).map(d => d.productoNombre);
    const restCount = order.detalles.length - 3;
    let summaryText = summaryItems.join(', ');
    if (restCount > 0) {
      summaryText += ` y ${restCount} más`;
    }

    const summaryEl = document.createElement('div');
    summaryEl.className = 'order-summary';
    summaryEl.textContent = summaryText;

    info.append(idEl, dateEl, summaryEl);

    const right = document.createElement('div');
    right.className = 'order-right';

    const totalEl = document.createElement('div');
    totalEl.className = 'order-total';
    totalEl.textContent = formatPeso(order.total);

    const badge = document.createElement('span');
    badge.className = `status-badge ${getStatusBadgeClass(order.estado)}`;
    badge.textContent = STATUS_LABELS[order.estado] || order.estado;

    right.append(totalEl, badge);
    card.append(info, right);

    card.addEventListener('click', () => openDetail(order.id));

    container.appendChild(card);
  });

  paginationEl.classList.remove('hidden');
  pageSizeSelector.classList.remove('hidden');

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

async function openDetail(orderId: number): Promise<void> {
  const modal = document.getElementById('order-detail-modal');
  const body = document.getElementById('modal-body');
  const actions = document.getElementById('modal-actions');
  if (!modal || !body || !actions) return;

  body.innerHTML = '<div class="loading-spinner"><div class="spinner"></div></div>';
  modal.classList.remove('hidden');

  try {
    const order = await api.get<PedidoDetalle>(`/pedidos/${orderId}`);

    body.innerHTML = '';

    const statusDiv = document.createElement('div');
    statusDiv.className = 'detail-status';
    statusDiv.innerHTML = `
      <span style="font-size:24px">${getStatusIcon(order.estado)}</span>
      <span class="status-badge ${getStatusBadgeClass(order.estado)}" style="font-size:14px">
        ${STATUS_LABELS[order.estado] || order.estado}
      </span>
    `;
    body.appendChild(statusDiv);

    const dateRow = document.createElement('div');
    dateRow.className = 'detail-row';
    dateRow.innerHTML = `<span>Fecha</span><span>${formatDate(order.fecha)}</span>`;
    body.appendChild(dateRow);

    const payRow = document.createElement('div');
    payRow.className = 'detail-row';
    const payLabel = order.formaPago === 'TARJETA' ? 'Tarjeta'
      : order.formaPago === 'EFECTIVO' ? 'Efectivo'
      : order.formaPago === 'TRANSFERENCIA' ? 'Transferencia'
      : order.formaPago;
    payRow.innerHTML = `<span>Forma de pago</span><span>${payLabel}</span>`;
    body.appendChild(payRow);

    if (order.telefono) {
      const telRow = document.createElement('div');
      telRow.className = 'detail-row';
      telRow.innerHTML = `<span>Teléfono</span><span>${order.telefono}</span>`;
      body.appendChild(telRow);
    }

    const productsTitle = document.createElement('div');
    productsTitle.className = 'detail-row detail-row-header';
    productsTitle.innerHTML = '<span>Producto</span><span style="text-align:right">Precio × Cant = Subtotal</span>';
    body.appendChild(productsTitle);

    order.detalles.forEach(d => {
      const row = document.createElement('div');
      row.className = 'detail-row';
      row.innerHTML = `
        <span>${d.productoNombre}</span>
        <span>${formatPeso(d.productoPrecio)} × ${d.cantidad} = ${formatPeso(d.subtotal)}</span>
      `;
      body.appendChild(row);
    });

    const totalDiv = document.createElement('div');
    totalDiv.className = 'detail-total';
    totalDiv.textContent = `Total: ${formatPeso(order.total)}`;
    body.appendChild(totalDiv);

    actions.innerHTML = '';
    const btnClose = document.createElement('button');
    btnClose.className = 'btn-cerrar';
    btnClose.textContent = 'Cerrar';
    btnClose.addEventListener('click', closeDetail);
    actions.appendChild(btnClose);

    if (order.estado === 'PENDIENTE') {
      const btnCancel = document.createElement('button');
      btnCancel.className = 'btn-cancelar';
      btnCancel.textContent = 'Cancelar Pedido';
      btnCancel.addEventListener('click', () => cancelOrder(order.id));
      actions.appendChild(btnCancel);
    }
  } catch {
    body.innerHTML = '<p style="color:#dc2626;text-align:center">Error al cargar el detalle del pedido</p>';
  }
}

function closeDetail(): void {
  const modal = document.getElementById('order-detail-modal');
  if (modal) modal.classList.add('hidden');
}

async function cancelOrder(orderId: number): Promise<void> {
  if (!confirm('¿Estás seguro de que querés cancelar este pedido?')) return;

  try {
    await api.patch<{ id: number; estado: string }>(`/pedidos/${orderId}/cancelar`);
    showToast('Pedido cancelado con éxito', 'success');
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
}

async function loadPage(page: number): Promise<void> {
  showLoading();

  const container = document.getElementById('orders-list');
  const emptyEl = document.getElementById('orders-empty');
  const paginationEl = document.getElementById('orders-paginacion');
  const pageSizeSelector = document.getElementById('page-size-selector');
  if (container) container.innerHTML = '';
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

function initOrders(): void {
  if (!protectRoute()) return;

  initHeader();

  loadPage(0);

  const btnPrev = document.getElementById('btn-prev');
  const btnNext = document.getElementById('btn-next');
  const pageSizeSelect = document.getElementById('page-size') as HTMLSelectElement;
  const cerrarBtn = document.getElementById('btn-cerrar-detalle');
  const modal = document.getElementById('order-detail-modal');

  btnPrev?.addEventListener('click', () => {
    if (currentPage > 0) loadPage(currentPage - 1);
  });

  btnNext?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadPage(currentPage + 1);
  });

  pageSizeSelect?.addEventListener('change', () => {
    PAGE_SIZE = Number(pageSizeSelect.value);
    loadPage(0);
  });

  cerrarBtn?.addEventListener('click', closeDetail);

  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeDetail();
  });
}

document.addEventListener('DOMContentLoaded', initOrders);

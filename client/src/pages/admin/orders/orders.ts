import { getUserSession, logout } from '../../../utils/auth';
import { api } from '../../../utils/api';
import type { PedidoResponse, PaginatedResponse, DetallePedidoResponse } from '../../../types';

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
let PAGE_SIZE = 10;
let searchTerm = '';
let searchTimeout: number | null = null;
let filterEstado = '';
let currentOrderId: number | null = null;

const ESTADO_BADGE: Record<string, string> = {
  PENDIENTE: 'badge-pendiente',
  CONFIRMADO: 'badge-confirmado',
  TERMINADO: 'badge-terminado',
  CANCELADO: 'badge-cancelado',
};

const TRANSICIONES: Record<string, string[]> = {
  PENDIENTE: ['CONFIRMADO', 'CANCELADO'],
  CONFIRMADO: ['TERMINADO', 'CANCELADO'],
  TERMINADO: [],
  CANCELADO: [],
};

function showToast(message: string, type: 'success' | 'error'): void {
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

function formatCurrency(value: number): string {
  return `$ ${value.toFixed(2)}`;
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-AR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

function getResumenItems(detalles: { productoNombre: string; cantidad: number }[]): string {
  if (detalles.length === 0) return '-';
  if (detalles.length === 1) return `1x ${detalles[0].productoNombre}`;
  return `${detalles.length} productos`;
}

async function loadOrders(page: number, size: number, search?: string, estado?: string): Promise<void> {
  showLoading();
  const table = document.getElementById('orders-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  const errorState = document.getElementById('error-state');
  if (table) table.classList.add('hidden');
  if (pagination) pagination.classList.add('hidden');
  if (emptyState) emptyState.classList.add('hidden');
  if (errorState) errorState.classList.add('hidden');

  try {
    let url = `/pedidos?page=${page}&size=${size}`;
    const effectiveSearch = estado || search;
    if (effectiveSearch) url += `&search=${encodeURIComponent(effectiveSearch)}`;
    const response = await api.get<PaginatedResponse<PedidoResponse>>(url);
    currentPage = response.page;
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderTable(response.content);
    renderPagination();
  } catch (err) {
    if (errorState) {
      errorState.textContent = err instanceof Error ? err.message : 'Error al cargar pedidos';
      errorState.classList.remove('hidden');
    }
  } finally {
    hideLoading();
  }
}

function renderTable(pedidos: PedidoResponse[]): void {
  const tbody = document.getElementById('orders-tbody');
  const table = document.getElementById('orders-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  if (!tbody) return;

  clearElement(tbody);

  if (pedidos.length === 0) {
    if (table) table.classList.add('hidden');
    if (pagination) pagination.classList.add('hidden');
    if (emptyState) emptyState.classList.remove('hidden');
    return;
  }

  if (table) table.classList.remove('hidden');
  if (pagination) pagination.classList.remove('hidden');
  if (emptyState) emptyState.classList.add('hidden');

  pedidos.forEach(pedido => {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = String(pedido.id);

    const tdCliente = document.createElement('td');
    tdCliente.textContent = `${pedido.usuario.nombre} ${pedido.usuario.apellido}`;

    const tdFecha = document.createElement('td');
    tdFecha.textContent = formatDate(pedido.fecha);

    const tdEstado = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = `badge ${ESTADO_BADGE[pedido.estado] || 'badge-pendiente'}`;
    badge.textContent = pedido.estado;
    tdEstado.appendChild(badge);

    const tdItems = document.createElement('td');
    const items = pedido.detalles || [];
    tdItems.textContent = getResumenItems(items);

    const tdTotal = document.createElement('td');
    tdTotal.textContent = formatCurrency(pedido.total);

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'actions';

    const btnDetail = document.createElement('button');
    btnDetail.className = 'btn btn-detail btn-sm';
    btnDetail.textContent = 'Ver Detalle';
    btnDetail.addEventListener('click', () => openDetail(pedido.id));

    tdAcciones.appendChild(btnDetail);

    tr.append(tdId, tdCliente, tdFecha, tdEstado, tdItems, tdTotal, tdAcciones);
    tbody.appendChild(tr);
  });
}

function renderPagination(): void {
  const btnPrev = document.getElementById('btn-prev') as HTMLButtonElement;
  const btnNext = document.getElementById('btn-next') as HTMLButtonElement;
  const pageInfo = document.getElementById('page-info');
  if (!btnPrev || !btnNext || !pageInfo) return;

  btnPrev.disabled = currentPage <= 0;
  btnNext.disabled = currentPage >= totalPages - 1;

  if (totalItems === 0) {
    pageInfo.textContent = '';
    return;
  }
  const start = currentPage * PAGE_SIZE + 1;
  const end = Math.min((currentPage + 1) * PAGE_SIZE, totalItems);
  const label = totalPages <= 1
    ? `${totalItems} ${totalItems === 1 ? 'pedido' : 'pedidos'}`
    : `Mostrando ${start}-${end} de ${totalItems} pedidos`;
  pageInfo.textContent = label;
}

function openDetail(orderId: number): void {
  const modal = document.getElementById('detail-modal-overlay');
  if (!modal) return;
  currentOrderId = orderId;
  modal.classList.remove('hidden');
  loadDetail(orderId);
}

async function loadDetail(orderId: number): Promise<void> {
  const body = document.getElementById('detail-body');
  if (!body) return;
  body.style.opacity = '0.5';

  try {
    const pedido = await api.get<PedidoResponse>(`/pedidos/${orderId}`);

    const dClienteNombre = document.getElementById('d-cliente-nombre');
    const dClienteEmail = document.getElementById('d-cliente-email');
    const dFecha = document.getElementById('d-fecha');
    const dEstado = document.getElementById('d-estado');
    const dFormaPago = document.getElementById('d-forma-pago');
    const dTotal = document.getElementById('d-total');
    const dTotalFooter = document.getElementById('d-total-footer');
    const dProductosTbody = document.getElementById('d-productos-tbody');
    const dEstadoSection = document.getElementById('d-estado-section');
    const dEstadoSelect = document.getElementById('d-estado-select') as HTMLSelectElement;

    if (dClienteNombre) dClienteNombre.textContent = `${pedido.usuario.nombre} ${pedido.usuario.apellido}`;
    if (dClienteEmail) dClienteEmail.textContent = pedido.usuario.email;
    if (dFecha) dFecha.textContent = formatDate(pedido.fecha);
    if (dEstado) {
      while (dEstado.firstChild) dEstado.removeChild(dEstado.firstChild);
      const badge = document.createElement('span');
      badge.className = `badge ${ESTADO_BADGE[pedido.estado] || 'badge-pendiente'}`;
      badge.textContent = pedido.estado;
      dEstado.appendChild(badge);
    }
    if (dFormaPago) dFormaPago.textContent = pedido.formaPago;
    if (dTotal) dTotal.textContent = formatCurrency(pedido.total);
    if (dTotalFooter) dTotalFooter.textContent = formatCurrency(pedido.total);

    if (dProductosTbody) {
      clearElement(dProductosTbody);
      (pedido.detalles || []).forEach((det: DetallePedidoResponse) => {
        const tr = document.createElement('tr');
        const tdNombre = document.createElement('td');
        tdNombre.textContent = det.productoNombre;
        const tdPrecio = document.createElement('td');
        tdPrecio.textContent = formatCurrency(det.productoPrecio);
        const tdCant = document.createElement('td');
        tdCant.textContent = String(det.cantidad);
        const tdSub = document.createElement('td');
        tdSub.textContent = formatCurrency(det.subtotal);
        tr.append(tdNombre, tdPrecio, tdCant, tdSub);
        dProductosTbody.appendChild(tr);
      });
    }

    if (dEstadoSection && dEstadoSelect) {
      const transiciones = TRANSICIONES[pedido.estado] || [];
      if (transiciones.length === 0) {
        dEstadoSection.classList.add('hidden');
      } else {
        dEstadoSection.classList.remove('hidden');
        while (dEstadoSelect.firstChild) dEstadoSelect.removeChild(dEstadoSelect.firstChild);
        transiciones.forEach(est => {
          const opt = document.createElement('option');
          opt.value = est;
          opt.textContent = est;
          dEstadoSelect.appendChild(opt);
        });
      }
    }

    body.style.opacity = '';
  } catch (err) {
    showToast('Error al cargar detalle del pedido', 'error');
    body.style.opacity = '';
  }
}

async function updateEstado(): Promise<void> {
  if (currentOrderId === null) return;
  const select = document.getElementById('d-estado-select') as HTMLSelectElement;
  if (!select || !select.value) return;

  const btn = document.getElementById('d-update-estado') as HTMLButtonElement;
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Actualizando...';
  }

  try {
    await api.patch(`/pedidos/${currentOrderId}/estado`, { estado: select.value });
    showToast('Estado actualizado correctamente', 'success');
    closeDetailModal();
    await loadOrders(currentPage, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al actualizar estado';
    showToast(msg, 'error');
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = 'Actualizar Estado';
    }
  }
}

function closeDetailModal(): void {
  const modal = document.getElementById('detail-modal-overlay');
  if (modal) modal.classList.add('hidden');
  currentOrderId = null;
}

function setupSearch(): void {
  const input = document.getElementById('search-input') as HTMLInputElement;
  if (!input) return;
  input.addEventListener('input', () => {
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = window.setTimeout(() => {
      searchTerm = input.value.trim();
      loadOrders(0, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
    }, 300);
  });
}

function setupEstadoFilter(): void {
  const select = document.getElementById('filter-estado') as HTMLSelectElement;
  if (!select) return;
  select.addEventListener('change', () => {
    filterEstado = select.value;
    loadOrders(0, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
  });
}

function init(): void {
  const user = getUserSession();
  if (!user || user.role !== 'ADMIN') {
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

  const userInfo = document.getElementById('user-info');
  if (userInfo) userInfo.textContent = `${user.nombre} (Admin)`;

  document.getElementById('btn-logout')?.addEventListener('click', () => {
    logout();
    window.location.href = '/src/pages/auth/login/index.html';
  });

  document.getElementById('detail-close')?.addEventListener('click', closeDetailModal);
  document.getElementById('detail-btn-close')?.addEventListener('click', closeDetailModal);
  document.getElementById('d-update-estado')?.addEventListener('click', updateEstado);

  document.getElementById('btn-prev')?.addEventListener('click', () => {
    if (currentPage > 0) loadOrders(currentPage - 1, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadOrders(currentPage + 1, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
  });

  document.getElementById('page-size')?.addEventListener('change', (e) => {
    PAGE_SIZE = Number((e.target as HTMLSelectElement).value);
    loadOrders(0, PAGE_SIZE, searchTerm || undefined, filterEstado || undefined);
  });

  const detailModal = document.getElementById('detail-modal-overlay');
  detailModal?.addEventListener('click', (e) => {
    if (e.target === detailModal) closeDetailModal();
  });

  setupSearch();
  setupEstadoFilter();
  loadOrders(0, PAGE_SIZE);
}

document.addEventListener('DOMContentLoaded', init);

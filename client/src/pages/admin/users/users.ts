import { getUserSession, logout } from '../../../utils/auth';
import { api } from '../../../utils/api';
import type { UsuarioResponse } from '../../../types';
import type { PaginatedResponse } from '../../../types';

interface UpdateUsuarioRequest {
  nombre?: string;
  apellido?: string;
  email?: string;
  celular?: string;
  rol?: 'ADMIN' | 'USUARIO';
}

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
let PAGE_SIZE = 10;
let searchTerm = '';
let searchTimeout: number | null = null;
let filterRol = '';
let editingId: number | null = null;

const ROL_BADGE: Record<string, string> = {
  ADMIN: 'badge-admin',
  USUARIO: 'badge-usuario',
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

function formatDate(dateStr: string): string {
  const d = new Date(dateStr);
  return d.toLocaleDateString('es-AR', {
    day: '2-digit', month: '2-digit', year: 'numeric',
  });
}

async function loadUsers(page: number, size: number, search?: string, rol?: string): Promise<void> {
  showLoading();
  const table = document.getElementById('users-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  const errorState = document.getElementById('error-state');
  if (table) table.classList.add('hidden');
  if (pagination) pagination.classList.add('hidden');
  if (emptyState) emptyState.classList.add('hidden');
  if (errorState) errorState.classList.add('hidden');

  try {
    let url = `/usuarios?page=${page}&size=${size}`;
    const effectiveSearch = rol || search;
    if (effectiveSearch) url += `&search=${encodeURIComponent(effectiveSearch)}`;
    const response = await api.get<PaginatedResponse<UsuarioResponse>>(url);
    currentPage = response.page;
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderTable(response.content);
    renderPagination();
  } catch (err) {
    if (errorState) {
      errorState.textContent = err instanceof Error ? err.message : 'Error al cargar usuarios';
      errorState.classList.remove('hidden');
    }
  } finally {
    hideLoading();
  }
}

function renderTable(usuarios: UsuarioResponse[]): void {
  const tbody = document.getElementById('users-tbody');
  const table = document.getElementById('users-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  if (!tbody) return;

  clearElement(tbody);

  if (usuarios.length === 0) {
    if (table) table.classList.add('hidden');
    if (pagination) pagination.classList.add('hidden');
    if (emptyState) emptyState.classList.remove('hidden');
    return;
  }

  if (table) table.classList.remove('hidden');
  if (pagination) pagination.classList.remove('hidden');
  if (emptyState) emptyState.classList.add('hidden');

  usuarios.forEach(user => {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = String(user.id);

    const tdNombre = document.createElement('td');
    tdNombre.textContent = user.nombre;

    const tdApellido = document.createElement('td');
    tdApellido.textContent = user.apellido;

    const tdEmail = document.createElement('td');
    tdEmail.textContent = user.email;

    const tdCelular = document.createElement('td');
    tdCelular.textContent = user.celular || '-';

    const tdRol = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = `badge ${ROL_BADGE[user.rol] || 'badge-usuario'}`;
    badge.textContent = user.rol;
    tdRol.appendChild(badge);

    const tdFecha = document.createElement('td');
    tdFecha.textContent = formatDate(user.createdAt);

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'actions';

    const btnEdit = document.createElement('button');
    btnEdit.className = 'btn btn-edit btn-sm';
    btnEdit.textContent = 'Editar';
    btnEdit.addEventListener('click', () => openEditModal(user));

    const btnDelete = document.createElement('button');
    btnDelete.className = 'btn btn-danger btn-sm';
    btnDelete.textContent = 'Eliminar';
    btnDelete.addEventListener('click', () => confirmDelete(user.id, btnDelete));

    tdAcciones.append(btnEdit, btnDelete);

    tr.append(tdId, tdNombre, tdApellido, tdEmail, tdCelular, tdRol, tdFecha, tdAcciones);
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
    ? `${totalItems} ${totalItems === 1 ? 'usuario' : 'usuarios'}`
    : `Mostrando ${start}-${end} de ${totalItems} usuarios`;
  pageInfo.textContent = label;
}

function openEditModal(user: UsuarioResponse): void {
  const modal = document.getElementById('modal-overlay');
  const title = document.getElementById('modal-title');
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputApellido = document.getElementById('input-apellido') as HTMLInputElement;
  const inputEmail = document.getElementById('input-email') as HTMLInputElement;
  const inputCelular = document.getElementById('input-celular') as HTMLInputElement;
  const inputRol = document.getElementById('input-rol') as HTMLSelectElement;
  if (!modal || !title || !inputNombre || !inputApellido || !inputEmail || !inputCelular || !inputRol) return;

  editingId = user.id;
  title.textContent = 'Editar Usuario';
  inputNombre.value = user.nombre;
  inputApellido.value = user.apellido;
  inputEmail.value = user.email;
  inputCelular.value = user.celular || '';
  inputRol.value = user.rol;
  modal.classList.remove('hidden');
  inputNombre.focus();
}

function closeModal(): void {
  const modal = document.getElementById('modal-overlay');
  if (modal) modal.classList.add('hidden');
  editingId = null;
}

async function handleSave(): Promise<void> {
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputApellido = document.getElementById('input-apellido') as HTMLInputElement;
  const inputEmail = document.getElementById('input-email') as HTMLInputElement;
  const inputCelular = document.getElementById('input-celular') as HTMLInputElement;
  const inputRol = document.getElementById('input-rol') as HTMLSelectElement;
  const btnSave = document.getElementById('btn-save') as HTMLButtonElement;
  if (!inputNombre || !inputApellido || !inputEmail || !inputCelular || !inputRol || !btnSave) return;

  const body: UpdateUsuarioRequest = {};
  if (inputNombre.value.trim()) body.nombre = inputNombre.value.trim();
  if (inputApellido.value.trim()) body.apellido = inputApellido.value.trim();
  if (inputEmail.value.trim()) body.email = inputEmail.value.trim();
  if (inputCelular.value.trim()) body.celular = inputCelular.value.trim();
  body.rol = inputRol.value as 'ADMIN' | 'USUARIO';

  btnSave.disabled = true;
  btnSave.textContent = 'Guardando...';

  try {
    if (editingId !== null) {
      await api.patch(`/usuarios/${editingId}`, body);
      showToast('Usuario actualizado correctamente', 'success');
    }
    closeModal();
    await loadUsers(currentPage, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al guardar el usuario';
    showToast(msg, 'error');
  } finally {
    btnSave.disabled = false;
    btnSave.textContent = 'Guardar';
  }
}

function confirmDelete(id: number, btn: HTMLButtonElement): void {
  const td = btn.parentNode as HTMLElement;
  if (!td) return;

  btn.textContent = '¿Seguro?';
  btn.style.background = '#b91c1c';

  const newBtn = btn.cloneNode(true) as HTMLButtonElement;
  btn.parentNode?.replaceChild(newBtn, btn);
  const rowsOnCurrentPage = document.getElementById('users-tbody')?.children.length || 0;

  const btnNo = document.createElement('button');
  btnNo.className = 'btn btn-secondary btn-sm';
  btnNo.textContent = 'No';
  btnNo.addEventListener('click', () => {
    const restoredBtn = newBtn.cloneNode(true) as HTMLButtonElement;
    restoredBtn.textContent = 'Eliminar';
    restoredBtn.style.background = '';
    restoredBtn.addEventListener('click', () => confirmDelete(id, restoredBtn));
    newBtn.parentNode?.replaceChild(restoredBtn, newBtn);
    if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
  });
  td.insertBefore(btnNo, newBtn.nextSibling);

  newBtn.addEventListener('click', async () => {
    try {
      await api.delete(`/usuarios/${id}`);
      showToast('Usuario eliminado correctamente', 'success');
      if (currentPage > 0 && rowsOnCurrentPage <= 1) {
        currentPage--;
      }
      if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
      await loadUsers(currentPage, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error al eliminar el usuario';
      showToast(msg, 'error');
      if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
    }
  });
}

function setupSearch(): void {
  const input = document.getElementById('search-input') as HTMLInputElement;
  if (!input) return;
  input.addEventListener('input', () => {
    if (searchTimeout) clearTimeout(searchTimeout);
    searchTimeout = window.setTimeout(() => {
      searchTerm = input.value.trim();
      loadUsers(0, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
    }, 300);
  });
}

function setupRolFilter(): void {
  const select = document.getElementById('filter-rol') as HTMLSelectElement;
  if (!select) return;
  select.addEventListener('change', () => {
    filterRol = select.value;
    loadUsers(0, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
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

  document.getElementById('modal-close')?.addEventListener('click', closeModal);
  document.getElementById('btn-cancel')?.addEventListener('click', closeModal);
  document.getElementById('btn-save')?.addEventListener('click', (e) => {
    e.preventDefault();
    handleSave();
  });

  document.getElementById('btn-prev')?.addEventListener('click', () => {
    if (currentPage > 0) loadUsers(currentPage - 1, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadUsers(currentPage + 1, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
  });

  document.getElementById('page-size')?.addEventListener('change', (e) => {
    PAGE_SIZE = Number((e.target as HTMLSelectElement).value);
    loadUsers(0, PAGE_SIZE, searchTerm || undefined, filterRol || undefined);
  });

  const modal = document.getElementById('modal-overlay');
  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });

  setupSearch();
  setupRolFilter();
  loadUsers(0, PAGE_SIZE);
}

document.addEventListener('DOMContentLoaded', init);

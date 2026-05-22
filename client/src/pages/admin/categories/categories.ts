import { getUserSession, logout } from '../../../utils/auth';
import { api } from '../../../utils/api';
import type { CategoriaResponse } from '../../../types';
import type { PaginatedResponse } from '../../../types';

interface CategoriaRequest {
  nombre: string;
  descripcion: string;
  imagen: string;
}

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
let PAGE_SIZE = 10;
let searchTerm = '';
let searchTimeout: number | null = null;
let editingId: number | null = null;

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

async function loadCategories(page: number, size: number, search?: string): Promise<void> {
  showLoading();
  const table = document.getElementById('categories-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  const errorState = document.getElementById('error-state');
  if (table) table.classList.add('hidden');
  if (pagination) pagination.classList.add('hidden');
  if (emptyState) emptyState.classList.add('hidden');
  if (errorState) errorState.classList.add('hidden');

  try {
    let url = `/categorias?page=${page}&size=${size}`;
    if (search) url += `&search=${encodeURIComponent(search)}`;
    const response = await api.get<PaginatedResponse<CategoriaResponse>>(url);
    currentPage = response.page;
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderTable(response.content);
    renderPagination();
  } catch (err) {
    if (errorState) {
      errorState.textContent = err instanceof Error ? err.message : 'Error al cargar categorías';
      errorState.classList.remove('hidden');
    }
  } finally {
    hideLoading();
  }
}

function renderTable(categorias: CategoriaResponse[]): void {
  const tbody = document.getElementById('categories-tbody');
  const table = document.getElementById('categories-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  if (!tbody) return;

  clearElement(tbody);

  if (categorias.length === 0) {
    if (table) table.classList.add('hidden');
    if (pagination) pagination.classList.add('hidden');
    if (emptyState) emptyState.classList.remove('hidden');
    return;
  }

  if (table) table.classList.remove('hidden');
  if (pagination) pagination.classList.remove('hidden');
  if (emptyState) emptyState.classList.add('hidden');

  categorias.forEach(cat => {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = String(cat.id);

    const tdNombre = document.createElement('td');
    tdNombre.textContent = cat.nombre;

    const tdDesc = document.createElement('td');
    tdDesc.textContent = cat.descripcion || '';

    const tdImg = document.createElement('td');
    const img = document.createElement('img');
    img.src = cat.imagen || '';
    img.alt = cat.nombre;
    img.width = 50;
    img.height = 50;
    img.style.cssText = 'object-fit: cover; border-radius: 4px;';
    tdImg.appendChild(img);

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'actions';

    const btnEdit = document.createElement('button');
    btnEdit.className = 'btn btn-edit btn-sm';
    btnEdit.textContent = 'Editar';
    btnEdit.addEventListener('click', () => openEditModal(cat));

    const btnDelete = document.createElement('button');
    btnDelete.className = 'btn btn-delete btn-sm';
    btnDelete.textContent = 'Eliminar';
    btnDelete.addEventListener('click', () => confirmDelete(cat.id, btnDelete));

    tdAcciones.append(btnEdit, btnDelete);

    tr.append(tdId, tdNombre, tdDesc, tdImg, tdAcciones);
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
    ? `${totalItems} ${totalItems === 1 ? 'categoría' : 'categorías'}`
    : `Mostrando ${start}-${end} de ${totalItems} categorías`;
  pageInfo.textContent = label;
}

function openCreateModal(): void {
  const modal = document.getElementById('modal-overlay');
  const title = document.getElementById('modal-title');
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  if (!modal || !title || !inputNombre || !inputDesc || !inputImg) return;

  editingId = null;
  title.textContent = 'Nueva Categoría';
  inputNombre.value = '';
  inputDesc.value = '';
  inputImg.value = '';
  modal.classList.remove('hidden');
  inputNombre.focus();
}

function openEditModal(cat: CategoriaResponse): void {
  const modal = document.getElementById('modal-overlay');
  const title = document.getElementById('modal-title');
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  if (!modal || !title || !inputNombre || !inputDesc || !inputImg) return;

  editingId = cat.id;
  title.textContent = 'Editar Categoría';
  inputNombre.value = cat.nombre;
  inputDesc.value = cat.descripcion || '';
  inputImg.value = cat.imagen || '';
  modal.classList.remove('hidden');
  inputNombre.focus();
}

function closeModal(): void {
  const modal = document.getElementById('modal-overlay');
  if (modal) modal.classList.add('hidden');
}

async function handleSave(): Promise<void> {
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  const btnSave = document.getElementById('btn-save') as HTMLButtonElement;
  if (!inputNombre || !inputDesc || !inputImg || !btnSave) return;

  if (!inputNombre.value.trim()) {
    showToast('El nombre es obligatorio', 'error');
    return;
  }
  if (!inputImg.value.trim()) {
    showToast('La URL de la imagen es obligatoria', 'error');
    return;
  }

  btnSave.disabled = true;
  btnSave.textContent = 'Guardando...';

  const body: CategoriaRequest = {
    nombre: inputNombre.value.trim(),
    descripcion: inputDesc.value.trim(),
    imagen: inputImg.value.trim(),
  };

  try {
    if (editingId !== null) {
      await api.put(`/categorias/${editingId}`, body);
      showToast('Categoría actualizada correctamente', 'success');
    } else {
      await api.post('/categorias', body);
      showToast('Categoría creada correctamente', 'success');
      currentPage = 0;
    }
    closeModal();
    await loadCategories(currentPage, PAGE_SIZE, searchTerm || undefined);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al guardar la categoría';
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

  const btnNo = document.createElement('button');
  btnNo.className = 'btn btn-secondary btn-sm';
  btnNo.textContent = 'No';
  btnNo.addEventListener('click', () => {
    btn.textContent = 'Eliminar';
    btn.style.background = '';
    if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
  });
  td.insertBefore(btnNo, btn.nextSibling);

  const newBtn = btn.cloneNode(true) as HTMLButtonElement;
  btn.parentNode?.replaceChild(newBtn, btn);

  newBtn.addEventListener('click', async () => {
    try {
      if (currentPage > 0 && totalItems <= 1) {
        currentPage--;
      }
      await api.delete(`/categorias/${id}`);
      showToast('Categoría eliminada correctamente', 'success');
      if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
      await loadCategories(currentPage, PAGE_SIZE, searchTerm || undefined);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error al eliminar la categoría';
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
      loadCategories(0, PAGE_SIZE, searchTerm || undefined);
    }, 300);
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

  document.getElementById('btn-new')?.addEventListener('click', openCreateModal);
  document.getElementById('modal-close')?.addEventListener('click', closeModal);
  document.getElementById('btn-cancel')?.addEventListener('click', closeModal);
  document.getElementById('btn-save')?.addEventListener('click', (e) => {
    e.preventDefault();
    handleSave();
  });

  document.getElementById('btn-prev')?.addEventListener('click', () => {
    if (currentPage > 0) loadCategories(currentPage - 1, PAGE_SIZE, searchTerm || undefined);
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadCategories(currentPage + 1, PAGE_SIZE, searchTerm || undefined);
  });

  document.getElementById('page-size')?.addEventListener('change', (e) => {
    PAGE_SIZE = Number((e.target as HTMLSelectElement).value);
    loadCategories(0, PAGE_SIZE, searchTerm || undefined);
  });

  const modal = document.getElementById('modal-overlay');
  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });

  setupSearch();
  loadCategories(0, PAGE_SIZE);
}

document.addEventListener('DOMContentLoaded', init);

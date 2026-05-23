import { getUserSession, logout } from '../../../utils/auth';
import { api } from '../../../utils/api';
import type { CategoriaResponse, ProductoResponse } from '../../../types';
import type { PaginatedResponse } from '../../../types';

interface ProductoRequest {
  nombre: string;
  precio: number;
  descripcion: string;
  stock: number;
  imagen: string;
  disponible: boolean;
  idCategoria: number;
}

let currentPage = 0;
let totalPages = 0;
let totalItems = 0;
let PAGE_SIZE = 10;
let searchTerm = '';
let searchTimeout: number | null = null;
let editingId: number | null = null;
let currentCategoriaId: number | null = null;
let cachedCategorias: CategoriaResponse[] = [];

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

async function loadCategorias(): Promise<void> {
  try {
    const response = await api.get<PaginatedResponse<CategoriaResponse>>('/categorias?page=0&size=100');
    cachedCategorias = response.content;

    const filterSelect = document.getElementById('filter-categoria') as HTMLSelectElement;
    const modalSelect = document.getElementById('input-categoria') as HTMLSelectElement;

    if (filterSelect) {
      while (filterSelect.options.length > 1) filterSelect.remove(1);
      cachedCategorias.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = String(cat.id);
        opt.textContent = cat.nombre;
        filterSelect.appendChild(opt);
      });
    }

    if (modalSelect) {
      while (modalSelect.options.length > 1) modalSelect.remove(1);
      cachedCategorias.forEach(cat => {
        const opt = document.createElement('option');
        opt.value = String(cat.id);
        opt.textContent = cat.nombre;
        modalSelect.appendChild(opt);
      });
    }
  } catch (err) {
    showToast('Error al cargar categorías', 'error');
  }
}

async function loadProductos(page: number, size: number, search?: string, categoriaId?: number | null): Promise<void> {
  showLoading();
  const table = document.getElementById('products-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  const errorState = document.getElementById('error-state');
  if (table) table.classList.add('hidden');
  if (pagination) pagination.classList.add('hidden');
  if (emptyState) emptyState.classList.add('hidden');
  if (errorState) errorState.classList.add('hidden');

  try {
    let url: string;
    if (categoriaId != null) {
      url = `/productos/categoria/${categoriaId}?page=${page}&size=${size}`;
      if (search) url += `&search=${encodeURIComponent(search)}`;
    } else {
      url = `/productos?page=${page}&size=${size}`;
      if (search) url += `&search=${encodeURIComponent(search)}`;
    }
    const response = await api.get<PaginatedResponse<ProductoResponse>>(url);
    currentPage = response.page;
    totalPages = response.totalPages;
    totalItems = response.totalElements;
    renderTable(response.content);
    renderPagination();
  } catch (err) {
    if (errorState) {
      errorState.textContent = err instanceof Error ? err.message : 'Error al cargar productos';
      errorState.classList.remove('hidden');
    }
  } finally {
    hideLoading();
  }
}

function renderTable(productos: ProductoResponse[]): void {
  const tbody = document.getElementById('products-tbody');
  const table = document.getElementById('products-table');
  const pagination = document.getElementById('pagination');
  const emptyState = document.getElementById('empty-state');
  if (!tbody) return;

  clearElement(tbody);

  if (productos.length === 0) {
    if (table) table.classList.add('hidden');
    if (pagination) pagination.classList.add('hidden');
    if (emptyState) emptyState.classList.remove('hidden');
    return;
  }

  if (table) table.classList.remove('hidden');
  if (pagination) pagination.classList.remove('hidden');
  if (emptyState) emptyState.classList.add('hidden');

  productos.forEach(prod => {
    const tr = document.createElement('tr');

    const tdId = document.createElement('td');
    tdId.textContent = String(prod.id);

    const tdImg = document.createElement('td');
    if (prod.imagen && prod.imagen.trim()) {
      const img = document.createElement('img');
      img.src = prod.imagen;
      img.alt = prod.nombre;
      img.width = 50;
      img.height = 50;
      img.loading = 'lazy';
      img.style.cssText = 'object-fit: cover; border-radius: 4px;';
      tdImg.appendChild(img);
    } else {
      tdImg.textContent = '-';
    }

    const tdNombre = document.createElement('td');
    tdNombre.textContent = prod.nombre;

    const tdPrecio = document.createElement('td');
    tdPrecio.textContent = formatCurrency(prod.precio);

    const tdStock = document.createElement('td');
    tdStock.textContent = String(prod.stock);

    const tdCategoria = document.createElement('td');
    tdCategoria.textContent = prod.categoria.nombre;

    const tdDisponible = document.createElement('td');
    const badge = document.createElement('span');
    badge.className = `badge badge-${prod.disponible ? 'success' : 'danger'}`;
    badge.textContent = prod.disponible ? 'Sí' : 'No';
    tdDisponible.appendChild(badge);

    const tdAcciones = document.createElement('td');
    tdAcciones.className = 'actions';

    const btnEdit = document.createElement('button');
    btnEdit.className = 'btn btn-edit btn-sm';
    btnEdit.textContent = 'Editar';
    btnEdit.addEventListener('click', () => openEditModal(prod));

    const btnDelete = document.createElement('button');
    btnDelete.className = 'btn btn-danger btn-sm';
    btnDelete.textContent = 'Eliminar';
    btnDelete.addEventListener('click', () => confirmDelete(prod.id, btnDelete));

    tdAcciones.append(btnEdit, btnDelete);

    tr.append(tdId, tdImg, tdNombre, tdPrecio, tdStock, tdCategoria, tdDisponible, tdAcciones);
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
    ? `${totalItems} ${totalItems === 1 ? 'producto' : 'productos'}`
    : `Mostrando ${start}-${end} de ${totalItems} productos`;
  pageInfo.textContent = label;
}

function populateModalSelect(categoriaId?: number): void {
  const select = document.getElementById('input-categoria') as HTMLSelectElement;
  if (!select) return;
  if (categoriaId != null) {
    select.value = String(categoriaId);
  } else {
    select.value = '';
  }
}

function openCreateModal(): void {
  const modal = document.getElementById('modal-overlay');
  const title = document.getElementById('modal-title');
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputPrecio = document.getElementById('input-precio') as HTMLInputElement;
  const inputStock = document.getElementById('input-stock') as HTMLInputElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  const inputDisponible = document.getElementById('input-disponible') as HTMLInputElement;
  if (!modal || !title || !inputNombre || !inputDesc || !inputPrecio || !inputStock || !inputImg || !inputDisponible) return;

  editingId = null;
  title.textContent = 'Nuevo Producto';
  inputNombre.value = '';
  inputDesc.value = '';
  inputPrecio.value = '';
  inputStock.value = '';
  inputImg.value = '';
  inputDisponible.checked = true;
  populateModalSelect();
  modal.classList.remove('hidden');
  inputNombre.focus();
}

function openEditModal(producto: ProductoResponse): void {
  const modal = document.getElementById('modal-overlay');
  const title = document.getElementById('modal-title');
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputPrecio = document.getElementById('input-precio') as HTMLInputElement;
  const inputStock = document.getElementById('input-stock') as HTMLInputElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  const inputDisponible = document.getElementById('input-disponible') as HTMLInputElement;
  if (!modal || !title || !inputNombre || !inputDesc || !inputPrecio || !inputStock || !inputImg || !inputDisponible) return;

  editingId = producto.id;
  title.textContent = 'Editar Producto';
  inputNombre.value = producto.nombre;
  inputDesc.value = producto.descripcion || '';
  inputPrecio.value = String(producto.precio);
  inputStock.value = String(producto.stock);
  inputImg.value = producto.imagen || '';
  inputDisponible.checked = producto.disponible;
  populateModalSelect(producto.categoria.id);
  modal.classList.remove('hidden');
  inputNombre.focus();
}

function closeModal(): void {
  const modal = document.getElementById('modal-overlay');
  if (modal) modal.classList.add('hidden');
}

async function handleSave(): Promise<void> {
  const form = document.getElementById('product-form') as HTMLFormElement;
  const inputNombre = document.getElementById('input-nombre') as HTMLInputElement;
  const inputDesc = document.getElementById('input-descripcion') as HTMLTextAreaElement;
  const inputPrecio = document.getElementById('input-precio') as HTMLInputElement;
  const inputStock = document.getElementById('input-stock') as HTMLInputElement;
  const inputCategoria = document.getElementById('input-categoria') as HTMLSelectElement;
  const inputImg = document.getElementById('input-imagen') as HTMLInputElement;
  const inputDisponible = document.getElementById('input-disponible') as HTMLInputElement;
  const btnSave = document.getElementById('btn-save') as HTMLButtonElement;
  if (!form || !inputNombre || !inputDesc || !inputPrecio || !inputStock || !inputCategoria || !inputImg || !inputDisponible || !btnSave) return;

  if (!inputNombre.value.trim()) {
    showToast('El nombre es obligatorio', 'error');
    return;
  }
  const precio = parseFloat(inputPrecio.value);
  if (isNaN(precio) || precio <= 0.01) {
    showToast('El precio debe ser mayor a 0.01', 'error');
    return;
  }
  const stock = parseInt(inputStock.value, 10);
  if (isNaN(stock) || stock < 0) {
    showToast('El stock no puede ser negativo', 'error');
    return;
  }
  if (!inputCategoria.value) {
    showToast('Debe seleccionar una categoría', 'error');
    return;
  }
  if (!form.reportValidity()) {
    return;
  }

  btnSave.disabled = true;
  btnSave.textContent = 'Guardando...';

  const body: ProductoRequest = {
    nombre: inputNombre.value.trim(),
    descripcion: inputDesc.value.trim(),
    precio: precio,
    stock: stock,
    imagen: inputImg.value.trim(),
    disponible: inputDisponible.checked,
    idCategoria: Number(inputCategoria.value),
  };

  try {
    if (editingId !== null) {
      await api.put(`/productos/${editingId}`, body);
      showToast('Producto actualizado correctamente', 'success');
    } else {
      await api.post('/productos', body);
      showToast('Producto creado correctamente', 'success');
      currentPage = 0;
    }
    closeModal();
    await loadProductos(currentPage, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : 'Error al guardar el producto';
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
  const rowsOnCurrentPage = document.getElementById('products-tbody')?.children.length || 0;

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
      await api.delete(`/productos/${id}`);
      showToast('Producto eliminado correctamente', 'success');
      if (currentPage > 0 && rowsOnCurrentPage <= 1) {
        currentPage--;
      }
      if (btnNo.parentNode) btnNo.parentNode.removeChild(btnNo);
      await loadProductos(currentPage, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Error al eliminar el producto';
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
      loadProductos(0, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
    }, 300);
  });
}

function setupCategoryFilter(): void {
  const select = document.getElementById('filter-categoria') as HTMLSelectElement;
  if (!select) return;
  select.addEventListener('change', () => {
    const val = select.value;
    currentCategoriaId = val ? Number(val) : null;
    loadProductos(0, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
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
    if (currentPage > 0) loadProductos(currentPage - 1, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
  });

  document.getElementById('btn-next')?.addEventListener('click', () => {
    if (currentPage < totalPages - 1) loadProductos(currentPage + 1, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
  });

  document.getElementById('page-size')?.addEventListener('change', (e) => {
    PAGE_SIZE = Number((e.target as HTMLSelectElement).value);
    loadProductos(0, PAGE_SIZE, searchTerm || undefined, currentCategoriaId);
  });

  const modal = document.getElementById('modal-overlay');
  modal?.addEventListener('click', (e) => {
    if (e.target === modal) closeModal();
  });

  setupSearch();
  setupCategoryFilter();
  loadCategorias().then(() => loadProductos(0, PAGE_SIZE));
}

document.addEventListener('DOMContentLoaded', init);

// admin.ts — Dashboard with stats

import { getUserSession, logout } from '../../utils/auth';
import { api } from '../../utils/api';
import type { PaginatedResponse } from '../../types';

interface ProductoDisponibilidad {
  disponible?: boolean;
  stock?: number;
}

function isProductoDisponible(producto: ProductoDisponibilidad): boolean {
  return producto.disponible === true && (producto.stock ?? 0) > 0;
}

async function getProductosDisponiblesCount(): Promise<number> {
  const size = 100;
  const firstPage = await api.get<PaginatedResponse<ProductoDisponibilidad>>(`/productos?page=0&size=${size}`);
  const remainingPagePromises: Array<Promise<PaginatedResponse<ProductoDisponibilidad>>> = [];
  for (let page = 1; page < firstPage.totalPages; page += 1) {
    remainingPagePromises.push(api.get<PaginatedResponse<ProductoDisponibilidad>>(`/productos?page=${page}&size=${size}`));
  }
  const remainingPages = await Promise.all(remainingPagePromises);

  let disponiblesCount = firstPage.content.filter(isProductoDisponible).length;
  for (const pageResult of remainingPages) {
    disponiblesCount += pageResult.content.filter(isProductoDisponible).length;
  }

  return disponiblesCount;
}

async function loadStats(): Promise<void> {
  const spinner = document.getElementById('loading-spinner');
  const statsGrid = document.getElementById('stats-grid');
  if (!spinner || !statsGrid) return;

  try {
    const [catRes, prodRes, pedidosRes, productosDisponibles] = await Promise.all([
      api.get<PaginatedResponse<unknown>>('/categorias?page=0&size=1'),
      api.get<PaginatedResponse<unknown>>('/productos?page=0&size=1'),
      api.get<PaginatedResponse<unknown>>('/pedidos?page=0&size=1'),
      getProductosDisponiblesCount(),
    ]);

    document.getElementById('stat-categorias')!.textContent = String(catRes.totalElements);
    document.getElementById('stat-productos')!.textContent = String(prodRes.totalElements);
    document.getElementById('stat-pedidos')!.textContent = String(pedidosRes.totalElements);
    document.getElementById('stat-disponibles')!.textContent = String(productosDisponibles);

    spinner.classList.add('hidden');
    statsGrid.classList.remove('hidden');
  } catch (err) {
    spinner.classList.add('hidden');
    const errMsg = document.createElement('p');
    errMsg.style.cssText = 'color:#dc2626;text-align:center;padding:40px;';
    errMsg.textContent = err instanceof Error && err.message
      ? err.message
      : 'Error al cargar las estadísticas. Verifica que el backend esté corriendo.';
    statsGrid.parentNode?.insertBefore(errMsg, statsGrid);
  }
}

function initAdmin(): void {
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

  loadStats();
}

document.addEventListener('DOMContentLoaded', initAdmin);

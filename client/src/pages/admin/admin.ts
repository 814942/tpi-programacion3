// admin.ts — Dashboard with stats

import { getUserSession, logout } from '../../utils/auth';
import { api } from '../../utils/api';
import type { PaginatedResponse } from '../../types';

async function loadStats(): Promise<void> {
  const spinner = document.getElementById('loading-spinner');
  const statsGrid = document.getElementById('stats-grid');
  if (!spinner || !statsGrid) return;

  try {
    const [catRes, prodRes, pedidosRes] = await Promise.all([
      api.get<PaginatedResponse<unknown>>('/categorias?page=0&size=1'),
      api.get<PaginatedResponse<unknown>>('/productos?page=0&size=1'),
      api.get<PaginatedResponse<unknown>>('/pedidos?page=0&size=1'),
    ]);

    document.getElementById('stat-categorias')!.textContent = String(catRes.totalElements);
    document.getElementById('stat-productos')!.textContent = String(prodRes.totalElements);
    document.getElementById('stat-pedidos')!.textContent = String(pedidosRes.totalElements);
    document.getElementById('stat-disponibles')!.textContent = String(prodRes.totalElements);

    spinner.classList.add('hidden');
    statsGrid.classList.remove('hidden');
  } catch (err) {
    spinner.classList.add('hidden');
    const errMsg = document.createElement('p');
    errMsg.style.cssText = 'color:#dc2626;text-align:center;padding:40px;';
    errMsg.textContent = err instanceof Error && err.message
      ? err.message
      : 'Error al cargar las estadísticas. Verificá que el backend esté corriendo.';
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

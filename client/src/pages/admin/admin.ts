// admin.ts — Admin panel logic

import '../../main';
import { getUserSession } from '../../utils/auth';
import { initHeader } from '../../utils/header';

/**
 * Initialize admin page
 */
function initAdmin(): void {
  const user = getUserSession();
  
  // Check if user is authenticated and is admin
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

  initHeader();
}

// Run when DOM is ready
document.addEventListener('DOMContentLoaded', initAdmin);

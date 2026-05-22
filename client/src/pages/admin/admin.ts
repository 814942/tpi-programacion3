// admin.ts — Admin panel logic

import '../../main';
import { getUserSession, logout } from '../../utils/auth';

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

  // Display user info
  const userInfo = document.getElementById('user-info');
  if (userInfo) {
    userInfo.textContent = `Sesión: ${user.email} (${user.role})`;
  }

  // Configure logout button
  const btnLogout = document.getElementById('btn-logout');
  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      logout();
      alert('Sesión cerrada correctamente.');
      window.location.href = '/';
    });
  }
}

// Run when DOM is ready
document.addEventListener('DOMContentLoaded', initAdmin);

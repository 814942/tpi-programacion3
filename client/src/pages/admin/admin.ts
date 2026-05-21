// admin.ts — Admin panel logic

import { getUserSession, logout } from '../../utils/auth';

/**
 * Initialize admin page
 */
function initAdmin(): void {
  const user = getUserSession();
  
  // Check if user is authenticated and is admin
  if (!user || user.role !== 'ADMIN') {
    // Route guard should have handled this, but just in case show message
    document.body.innerHTML = `
      <div style="display:flex;justify-content:center;align-items:center;min-height:100vh;font-family:Arial,sans-serif;">
        <div style="text-align:center;">
          <h1 style="color:#c33;">Acceso Denegado</h1>
          <p>No tenés permisos para ver esta página.</p>
          <a href="/" style="color:#ff4500;">Volver al inicio</a>
        </div>
      </div>
    `;
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
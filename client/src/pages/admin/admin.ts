// admin.ts — Lógica del panel de administración

import { getUserSession, logout, isAdmin } from '../../utils/auth';
import { redirectToLogin, redirectToHome } from '../../utils/navigate';

/**
 * Inicializa la página de admin
 */
function initAdmin(): void {
  // Verificar que el usuario es admin
  if (!isAdmin()) {
    redirectToHome();
    return;
  }

  // Mostrar información del usuario
  const user = getUserSession();
  const userInfo = document.getElementById('user-info');
  if (userInfo && user) {
    userInfo.textContent = `Logged in as: ${user.email} (${user.role})`;
  }

  // Configurar botón de logout
  const btnLogout = document.getElementById('btn-logout');
  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      logout();
      redirectToLogin();
    });
  }
}

// Ejecutar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', initAdmin);
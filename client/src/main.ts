import { initRouteGuard } from './utils/navigate';
import { getSession, decodeToken, logout } from './utils/auth';

const LOGIN_PAGE = '/src/pages/auth/login/index.html';
const ADMIN_PAGE = '/src/pages/admin/index.html';
const CLIENT_PAGE = '/src/pages/client/index.html';

function getDashboardByRole(role: string): string {
  return role === 'ADMIN' ? ADMIN_PAGE : CLIENT_PAGE;
}

document.addEventListener('DOMContentLoaded', () => {
  const session = getSession();
  if (session) {
    const payload = decodeToken(session.token);
    if (payload) {
      const expired = payload.exp * 1000 < Date.now();
      if (expired) {
        logout();
        window.location.href = LOGIN_PAGE;
        return;
      }

      const path = window.location.pathname;
      if (path === '/' || path === '/index.html' || path.includes('/login/') || path.includes('/registro/')) {
        window.location.href = getDashboardByRole(payload.role);
        return;
      }
    } else {
      logout();
    }
  }

  initRouteGuard();
});

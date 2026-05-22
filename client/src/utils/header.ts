import { logout } from './auth';

export function initHeader(): void {
  const userBtn = document.getElementById('btn-user-menu');
  const dropdown = document.getElementById('user-dropdown');
  const logoutBtn = document.getElementById('btn-logout-header');
  const cartBadge = document.getElementById('cart-badge');

  if (userBtn && dropdown) {
    userBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      dropdown.classList.toggle('hidden');
    });

    document.addEventListener('click', () => {
      dropdown.classList.add('hidden');
    });

    dropdown.addEventListener('click', (e) => {
      e.stopPropagation();
    });
  }

  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      logout();
      window.location.href = '/src/pages/auth/login/index.html';
    });
  }

  if (cartBadge) {
    try {
      const cart = JSON.parse(localStorage.getItem('cart') || '[]');
      const total = Array.isArray(cart) ? cart.reduce((sum: number, item: any) => sum + (item.quantity || 0), 0) : 0;
      if (total > 0) {
        cartBadge.textContent = String(total);
        cartBadge.classList.remove('hidden');
      }
    } catch {
      // Invalid cart data — ignore
    }
  }
}

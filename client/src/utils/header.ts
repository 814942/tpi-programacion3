import { logout } from './auth';
import { ROUTES } from './navigate';

interface CartItem {
  quantity?: number | string;
}

export function updateCartBadge(cartBadge?: HTMLElement | null): void {
  const badge = cartBadge ?? document.getElementById('cart-badge');
  if (!badge) return;

  badge.textContent = '0';
  badge.classList.add('hidden');

  try {
    const cart = JSON.parse(localStorage.getItem('cart') || '[]');
    const total = Array.isArray(cart)
      ? cart.reduce((sum: number, item: CartItem) => sum + (Number(item.quantity) || 0), 0)
      : 0;

    if (total > 0) {
      badge.textContent = String(total);
      badge.classList.remove('hidden');
    }
  } catch {
    // Invalid cart data — ignore
  }
}

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
      window.location.href = ROUTES.LOGIN;
    });
  }

  updateCartBadge(cartBadge);
}

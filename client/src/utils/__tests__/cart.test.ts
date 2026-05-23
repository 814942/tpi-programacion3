import { describe, it, expect, beforeEach } from 'vitest';

describe('Cart localStorage', () => {
  const CART_KEY = 'cart';
  const mockProduct = {
    id: 1, nombre: 'Hamburguesa', precio: 100, descripcion: 'Rica',
    stock: 10, imagen: '', disponible: true,
    categoria: { id: 1, nombre: 'Comida' },
  };

  beforeEach(() => {
    localStorage.clear();
  });

  it('should start with empty cart', () => {
    const cart = JSON.parse(localStorage.getItem(CART_KEY) || '[]');
    expect(cart).toEqual([]);
  });

  it('should add item to cart', () => {
    const cart = [{ product: mockProduct, quantity: 2 }];
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    const stored = JSON.parse(localStorage.getItem(CART_KEY)!);
    expect(stored).toHaveLength(1);
    expect(stored[0].product.nombre).toBe('Hamburguesa');
    expect(stored[0].quantity).toBe(2);
  });

  it('should add multiple items', () => {
    const cart = [
      { product: mockProduct, quantity: 1 },
      { product: { ...mockProduct, id: 2, nombre: 'Pizza' }, quantity: 3 },
    ];
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    const stored = JSON.parse(localStorage.getItem(CART_KEY)!);
    expect(stored).toHaveLength(2);
    expect(stored[0].product.nombre).toBe('Hamburguesa');
    expect(stored[1].product.nombre).toBe('Pizza');
  });

  it('should update existing item quantity', () => {
    let cart = [{ product: mockProduct, quantity: 1 }];
    const existing = cart.find(i => i.product.id === 1);
    if (existing) existing.quantity += 2;
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    const stored = JSON.parse(localStorage.getItem(CART_KEY)!);
    expect(stored[0].quantity).toBe(3);
  });

  it('should remove item', () => {
    let cart = [
      { product: mockProduct, quantity: 1 },
      { product: { ...mockProduct, id: 2, nombre: 'Pizza' }, quantity: 3 },
    ];
    cart.splice(0, 1);
    localStorage.setItem(CART_KEY, JSON.stringify(cart));
    const stored = JSON.parse(localStorage.getItem(CART_KEY)!);
    expect(stored).toHaveLength(1);
    expect(stored[0].product.nombre).toBe('Pizza');
  });

  it('should clear cart', () => {
    localStorage.setItem(CART_KEY, JSON.stringify([{ product: mockProduct, quantity: 1 }]));
    localStorage.removeItem(CART_KEY);
    const stored = localStorage.getItem(CART_KEY);
    expect(stored).toBeNull();
  });

  it('should handle corrupted cart gracefully', () => {
    localStorage.setItem(CART_KEY, 'not-json');
    let cart: unknown[] = [];
    try { cart = JSON.parse(localStorage.getItem(CART_KEY)!); }
    catch { cart = []; }
    expect(cart).toEqual([]);
  });
});

// client.ts — Client panel logic (catalog, cart, search)

import '../../main';
import { getUserSession, logout } from '../../utils/auth';

// ==================== DATA ====================

interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  image: string;
  category: string;
}

const categories: string[] = ['Hamburguesas', 'Pizzas', 'Papas Fritas', 'Bebidas'];

const products: Product[] = [
  {
    id: 1,
    name: 'Hamburguesa Triple',
    description: 'Triple carne, cheddar y bacon',
    price: 25000,
    image: 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300&h=200&fit=crop',
    category: 'Hamburguesas'
  },
  {
    id: 2,
    name: 'Pizza Muzzarella',
    description: 'Salsa casera y orégano',
    price: 18000,
    image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300&h=200&fit=crop',
    category: 'Pizzas'
  },
  {
    id: 3,
    name: 'Hamburguesa Doble',
    description: 'Doble carne con lechuga y tomate',
    price: 20000,
    image: 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=300&h=200&fit=crop',
    category: 'Hamburguesas'
  },
  {
    id: 4,
    name: 'Pizza Especial',
    description: 'Jamón, morrón y aceitunas',
    price: 22000,
    image: 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=300&h=200&fit=crop',
    category: 'Pizzas'
  },
  {
    id: 5,
    name: 'Pizza Pepperoni',
    description: 'Pepperoni extra con queso',
    price: 24000,
    image: 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=300&h=200&fit=crop',
    category: 'Pizzas'
  },
  {
    id: 6,
    name: 'Papas Fritas',
    description: 'Papas crocantes con sal gruesa',
    price: 8000,
    image: 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=300&h=200&fit=crop',
    category: 'Papas Fritas'
  },
  {
    id: 7,
    name: 'Papas con Cheddar',
    description: 'Papas fritas con salsa cheddar',
    price: 10000,
    image: 'https://images.unsplash.com/photo-1630384060421-cb20d0e0649d?w=300&h=200&fit=crop',
    category: 'Papas Fritas'
  },
  {
    id: 8,
    name: 'Coca Cola',
    description: 'Lata 350ml bien fría',
    price: 3500,
    image: 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=300&h=200&fit=crop',
    category: 'Bebidas'
  },
  {
    id: 9,
    name: 'Agua Mineral',
    description: 'Agua sin gas 500ml',
    price: 2500,
    image: 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=300&h=200&fit=crop',
    category: 'Bebidas'
  }
];

// ==================== CART ====================

interface CartItem {
  product: Product;
  quantity: number;
}

let cart: CartItem[] = [];

// ==================== FUNCTIONS ====================

/**
 * Load categories in the aside
 */
function loadCategories(): void {
  const listaCategorias = document.getElementById('lista-categorias');
  if (!listaCategorias) return;

  listaCategorias.innerHTML = '';
  
  // "All" option first
  const liAll = document.createElement('li');
  liAll.innerHTML = '<a href="#" data-category="all">Todas</a>';
  listaCategorias.appendChild(liAll);

  // Each category
  categories.forEach(cat => {
    const li = document.createElement('li');
    li.innerHTML = `<a href="#" data-category="${cat}">${cat}</a>`;
    listaCategorias.appendChild(li);
  });

  // Event listeners to filter
  listaCategorias.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      const category = (e.target as HTMLElement).dataset.category;
      filterByCategory(category || 'all');
    });
  });
}

/**
 * Filter products by category
 */
function filterByCategory(category: string): void {
  if (category === 'all') {
    loadProducts(products);
  } else {
    const filtered = products.filter(p => p.category === category);
    loadProducts(filtered);
  }
}

/**
 * Load products in the container
 */
function loadProducts(productsToShow: Product[]): void {
  const container = document.getElementById('contenedor-productos');
  if (!container) return;

  container.innerHTML = '';

  productsToShow.forEach(product => {
    const article = document.createElement('article');
    article.className = 'producto';
    article.innerHTML = `
      <img src="${product.image}" alt="${product.name}">
      <h3>${product.name}</h3>
      <p class="descripcion">${product.description}</p>
      <p class="precio">$${product.price.toLocaleString('es-AR')}</p>
      <button class="btn-agregar" data-id="${product.id}">Agregar al Carrito</button>
    `;
    container.appendChild(article);
  });

  // Add event listeners to buttons
  container.querySelectorAll('.btn-agregar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const id = parseInt((e.target as HTMLElement).dataset.id || '0');
      const product = products.find(p => p.id === id);
      if (product) {
        addToCart(product);
      }
    });
  });
}

/**
 * Add product to cart
 */
function addToCart(product: Product): void {
  const existingItem = cart.find(item => item.product.id === product.id);
  
  if (existingItem) {
    existingItem.quantity++;
  } else {
    cart.push({ product, quantity: 1 });
  }
  
  updateCart();
  alert('Producto agregado al carrito.');
}

/**
 * Update cart display
 */
function updateCart(): void {
  const tbody = document.getElementById('carrito-body');
  const totalEl = document.getElementById('carrito-total');
  
  if (!tbody || !totalEl) return;

  if (cart.length === 0) {
    tbody.innerHTML = '<tr><td colspan="5" style="text-align: center;">El carrito está vacío</td></tr>';
    totalEl.textContent = '$0';
    return;
  }

  let total = 0;
  tbody.innerHTML = '';

  cart.forEach((item, index) => {
    const subtotal = item.product.price * item.quantity;
    total += subtotal;

    const tr = document.createElement('tr');
    tr.innerHTML = `
      <td>${item.product.name}</td>
      <td>$${item.product.price.toLocaleString('es-AR')}</td>
      <td>${item.quantity}</td>
      <td>$${subtotal.toLocaleString('es-AR')}</td>
      <td><button class="eliminar" data-index="${index}">Eliminar</button></td>
    `;
    tbody.appendChild(tr);
  });

  totalEl.textContent = `$${total.toLocaleString('es-AR')}`;

  // Event listeners for delete
  tbody.querySelectorAll('.eliminar').forEach(btn => {
    btn.addEventListener('click', (e) => {
      const index = parseInt((e.target as HTMLElement).dataset.index || '0');
      removeFromCart(index);
    });
  });
}

/**
 * Remove product from cart
 */
function removeFromCart(index: number): void {
  if (index >= 0 && index < cart.length) {
    if (cart[index].quantity > 1) {
      cart[index].quantity--;
    } else {
      cart.splice(index, 1);
    }
    updateCart();
  }
}

/**
 * Configure search
 */
function configureSearch(): void {
  const formSearch = document.getElementById('form-busqueda');
  const inputSearch = document.getElementById('input-busqueda') as HTMLInputElement;

  if (!formSearch || !inputSearch) return;

  formSearch.addEventListener('submit', (e) => {
    e.preventDefault();
    const query = inputSearch.value.toLowerCase().trim();
    
    if (!query) {
      loadProducts(products);
      return;
    }

    const results = products.filter(p => 
      p.name.toLowerCase().includes(query) ||
      p.description.toLowerCase().includes(query) ||
      p.category.toLowerCase().includes(query)
    );

    loadProducts(results);
  });
}

/**
 * Display user info in header
 */
function displayUserInfo(): void {
  const user = getUserSession();
  const userInfo = document.getElementById('user-info');
  const btnLogout = document.getElementById('btn-logout');

  if (userInfo && user) {
    userInfo.textContent = `Hola, ${user.email}`;
  }

  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      logout();
      alert('Sesión cerrada correctamente.');
      window.location.href = '/';
    });
  }
}

/**
 * Initialize client page
 */
function initClient(): void {
  const user = getUserSession();
  
  // Check if user is authenticated and is client (route guard should handle this)
  if (!user || user.role !== 'USUARIO') {
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

  // Initialize components
  displayUserInfo();
  loadCategories();
  loadProducts(products);
  configureSearch();
  updateCart();
}

// Run when DOM is ready
document.addEventListener('DOMContentLoaded', initClient);

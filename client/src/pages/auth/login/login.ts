// login.ts — Login logic

import { login, isAuthenticated } from '../../../utils/auth';

// DOM elements
const form = document.getElementById('form-login') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;

/**
 * Handle login form submission
 */
function handleLogin(event: Event): void {
  event.preventDefault();

  const email = emailInput.value.trim();
  const password = passwordInput.value;

  // Validations
  if (!email || !password) {
    alert('Error: Email y contraseña son requeridos');
    return;
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    alert('Error: Ingresa un email válido');
    return;
  }

  // Attempt login
  const result = login({ email, password });

  if (result.success && result.user) {
    alert('¡Login exitoso! Bienvenido a Food Store.');
    
    // Redirect based on role - use absolute path
    if (result.user.role === 'admin') {
      window.location.href = '/src/pages/admin/index.html';
    } else {
      window.location.href = '/src/pages/client/index.html';
    }
  } else {
    alert(`Error: ${result.message}`);
  }
}

// Event listeners
if (form) {
  form.addEventListener('submit', handleLogin);
}

// Check if already authenticated
if (isAuthenticated()) {
  const userData = localStorage.getItem('userData');
  if (userData) {
    const user = JSON.parse(userData);
    if (user.role === 'admin') {
      window.location.href = '/src/pages/admin/index.html';
    } else {
      window.location.href = '/src/pages/client/index.html';
    }
  }
}
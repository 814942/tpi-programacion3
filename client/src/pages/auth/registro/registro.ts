// registro.ts — Registration logic

import { register, setUserSession, isAuthenticated } from '../../../utils/auth';

// DOM elements
const form = document.getElementById('form-registro') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const confirmPasswordInput = document.getElementById('confirm-password') as HTMLInputElement;

/**
 * Handle registration form submission
 */
function handleRegister(event: Event): void {
  event.preventDefault();

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const confirmPassword = confirmPasswordInput.value;

  // Validations
  if (!email || !password || !confirmPassword) {
    alert('Error: Todos los campos son requeridos');
    return;
  }

  if (password !== confirmPassword) {
    alert('Error: Las contraseñas no coinciden');
    return;
  }

  if (password.length < 6) {
    alert('Error: La contraseña debe tener al menos 6 caracteres');
    return;
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    alert('Error: Ingresa un email válido');
    return;
  }

  // Attempt registration
  const result = register({
    email,
    password,
    role: 'client',
  });

  if (result.success && result.user) {
    alert('¡Registro exitoso! Bienvenido a Food Store.');
    setUserSession(result.user);
    window.location.href = '/src/pages/client/index.html';
  } else {
    alert(`Error: ${result.message}`);
  }
}

// Event listeners
if (form) {
  form.addEventListener('submit', handleRegister);
}

// Real-time password confirmation validation
if (confirmPasswordInput) {
  confirmPasswordInput.addEventListener('input', () => {
    if (confirmPasswordInput.value !== passwordInput.value) {
      confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
    } else {
      confirmPasswordInput.setCustomValidity('');
    }
  });
}

// Check if already authenticated
if (isAuthenticated()) {
  window.location.href = '/src/pages/client/index.html';
}
// login.ts — Lógica de login de usuarios

import { login, isAuthenticated } from '../../../utils/auth';
import { redirectToAdmin, redirectToClient } from '../../../utils/navigate';

// Elementos del DOM
const form = document.getElementById('form-login') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const mensajeError = document.getElementById('mensaje-error') as HTMLDivElement;
const mensajeExito = document.getElementById('mensaje-exito') as HTMLDivElement;

/**
 * Muestra un mensaje de error
 */
function showError(message: string): void {
  mensajeError.textContent = message;
  mensajeError.classList.remove('hidden');
  mensajeExito.classList.add('hidden');
}

/**
 * Muestra un mensaje de éxito
 */
function showSuccess(message: string): void {
  mensajeExito.textContent = message;
  mensajeExito.classList.remove('hidden');
  mensajeError.classList.add('hidden');
}

/**
 * Oculta todos los mensajes
 */
function hideMessages(): void {
  mensajeError.classList.add('hidden');
  mensajeExito.classList.add('hidden');
}

/**
 * Maneja el envío del formulario de login
 */
function handleLogin(event: Event): void {
  event.preventDefault();
  hideMessages();

  const email = emailInput.value.trim();
  const password = passwordInput.value;

  // Validaciones del cliente
  if (!email || !password) {
    showError('Email y contraseña son requeridos');
    return;
  }

  // Validar formato de email básico
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showError('Ingresa un email válido');
    return;
  }

  // Intentar login
  const result = login({ email, password });

  if (result.success && result.user) {
    showSuccess('¡Login exitoso! Redirigiendo...');
    
    // Redirigir según el rol
    setTimeout(() => {
      if (result.user?.role === 'admin') {
        redirectToAdmin();
      } else {
        redirectToClient();
      }
    }, 1000);
  } else {
    showError(result.message);
  }
}

// Event listeners
if (form) {
  form.addEventListener('submit', handleLogin);
}

// Verificar si ya hay sesión activa - redirigir automáticamente
if (isAuthenticated()) {
  const user = JSON.parse(localStorage.getItem('userData') || '{}');
  if (user.role === 'admin') {
    redirectToAdmin();
  } else {
    redirectToClient();
  }
}
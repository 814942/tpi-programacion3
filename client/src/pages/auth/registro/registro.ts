// registro.ts — Lógica de registro de usuarios

import { register, setUserSession, isAuthenticated } from '../../../utils/auth';
import { redirectToClient } from '../../../utils/navigate';

// Elementos del DOM
const form = document.getElementById('form-registro') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const confirmPasswordInput = document.getElementById('confirm-password') as HTMLInputElement;
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
 * Maneja el envío del formulario de registro
 */
function handleRegister(event: Event): void {
  event.preventDefault();
  hideMessages();

  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const confirmPassword = confirmPasswordInput.value;

  // Validaciones del cliente
  if (!email || !password || !confirmPassword) {
    showError('Todos los campos son requeridos');
    return;
  }

  if (password !== confirmPassword) {
    showError('Las contraseñas no coinciden');
    return;
  }

  if (password.length < 6) {
    showError('La contraseña debe tener al menos 6 caracteres');
    return;
  }

  // Validar formato de email básico
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showError('Ingresa un email válido');
    return;
  }

  // Intentar registro
  const result = register({
    email,
    password,
    role: 'client', // Por defecto, cliente
  });

  if (result.success && result.user) {
    showSuccess('¡Registro exitoso! Redirigiendo...');
    // Guardar sesión automáticamente
    setUserSession(result.user);
    // Redirigir después de un pequeño delay
    setTimeout(() => {
      redirectToClient();
    }, 1500);
  } else {
    showError(result.message);
  }
}

// Event listeners
if (form) {
  form.addEventListener('submit', handleRegister);
}

// Validación en tiempo real de confirmación de contraseña
if (confirmPasswordInput) {
  confirmPasswordInput.addEventListener('input', () => {
    if (confirmPasswordInput.value !== passwordInput.value) {
      confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
    } else {
      confirmPasswordInput.setCustomValidity('');
    }
  });
}

// Verificar si ya hay sesión activa
if (isAuthenticated()) {
  // Ya está logueado, redirigir
  redirectToClient();
}
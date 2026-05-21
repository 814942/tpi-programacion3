import '../../../main';
import { register, isAuthenticated, getUserSession } from '../../../utils/auth';
import { ROUTES } from '../../../utils/navigate';

const PASSWORD_REGEX = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%&!]).{8,}$/;

const form = document.getElementById('form-registro') as HTMLFormElement;
const nombreInput = document.getElementById('nombre') as HTMLInputElement;
const apellidoInput = document.getElementById('apellido') as HTMLInputElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const celularInput = document.getElementById('celular') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
const confirmPasswordInput = document.getElementById('confirm-password') as HTMLInputElement;
const errorDiv = document.getElementById('error-message') as HTMLDivElement;
const spinner = document.getElementById('loading-spinner') as HTMLDivElement;

function showError(message: string): void {
  if (errorDiv) {
    errorDiv.textContent = message;
    errorDiv.classList.remove('hidden');
  }
}

function hideError(): void {
  if (errorDiv) {
    errorDiv.textContent = '';
    errorDiv.classList.add('hidden');
  }
}

function setLoading(loading: boolean): void {
  const submitBtn = form?.querySelector('button[type="submit"]') as HTMLButtonElement;
  if (submitBtn) submitBtn.disabled = loading;
  if (spinner) {
    if (loading) {
      spinner.classList.remove('hidden');
    } else {
      spinner.classList.add('hidden');
    }
  }
}

async function handleRegister(event: Event): Promise<void> {
  event.preventDefault();
  hideError();

  const nombre = nombreInput?.value.trim() || '';
  const apellido = apellidoInput?.value.trim() || '';
  const email = emailInput?.value.trim() || '';
  const celular = celularInput?.value.trim() || '';
  const password = passwordInput?.value || '';
  const confirmPassword = confirmPasswordInput?.value || '';

  if (!nombre || !apellido || !email || !password || !confirmPassword) {
    showError('Todos los campos obligatorios deben completarse');
    return;
  }

  if (nombre.length < 2) {
    showError('El nombre debe tener al menos 2 caracteres');
    return;
  }

  if (apellido.length < 2) {
    showError('El apellido debe tener al menos 2 caracteres');
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showError('Ingresá un email válido');
    return;
  }

  if (!PASSWORD_REGEX.test(password)) {
    showError('La contraseña debe tener mínimo 8 caracteres, mayúscula, minúscula, número y símbolo (@#$%&!)');
    return;
  }

  if (celular && !/^\d{1,20}$/.test(celular)) {
    showError('El celular debe contener solo dígitos (máximo 20)');
    return;
  }

  if (password !== confirmPassword) {
    showError('Las contraseñas no coinciden');
    return;
  }

  setLoading(true);

  try {
    const registerData: { nombre: string; apellido: string; email: string; password: string; celular?: string } = {
      nombre,
      apellido,
      email,
      password,
    };
    if (celular) {
      registerData.celular = celular;
    }

    await register(registerData);
    window.location.href = ROUTES.CLIENT;
  } catch (err: unknown) {
    let message = 'Error al registrarse';
    if (err instanceof TypeError) {
      message = 'No se puede conectar con el servidor. Verificá que el backend esté corriendo.';
    } else if (err instanceof Error) {
      message = err.message;
    }
    showError(message);
  } finally {
    setLoading(false);
  }
}

if (form) {
  form.addEventListener('submit', handleRegister);
}

if (confirmPasswordInput && passwordInput) {
  confirmPasswordInput.addEventListener('input', () => {
    if (confirmPasswordInput.value !== passwordInput.value) {
      confirmPasswordInput.setCustomValidity('Las contraseñas no coinciden');
    } else {
      confirmPasswordInput.setCustomValidity('');
    }
  });
}

if (isAuthenticated()) {
  const user = getUserSession();
  if (user) {
    window.location.href = user.role === 'ADMIN' ? ROUTES.ADMIN : ROUTES.CLIENT;
  }
}

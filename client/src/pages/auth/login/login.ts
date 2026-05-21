import '../../../main';
import { login, isAuthenticated, getSession } from '../../../utils/auth';
import { getDashboardByRole } from '../../../utils/navigate';

const form = document.getElementById('form-login') as HTMLFormElement;
const emailInput = document.getElementById('email') as HTMLInputElement;
const passwordInput = document.getElementById('password') as HTMLInputElement;
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

async function handleLogin(event: Event): Promise<void> {
  event.preventDefault();
  hideError();

  const email = emailInput?.value.trim() || '';
  const password = passwordInput?.value || '';

  if (!email || !password) {
    showError('Email y contraseña son requeridos');
    return;
  }

  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    showError('Ingresá un email válido');
    return;
  }

  setLoading(true);

  try {
    const user = await login({ email, password });
    window.location.href = getDashboardByRole(user.role);
  } catch (err: unknown) {
    let message = 'Error al iniciar sesión';
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
  form.addEventListener('submit', handleLogin);
}

if (isAuthenticated()) {
  const session = getSession();
  if (session) {
    window.location.href = getDashboardByRole(session.user.role);
  }
}

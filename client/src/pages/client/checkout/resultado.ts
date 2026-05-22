function initResultado(): void {
  const params = new URLSearchParams(window.location.search);
  const success = params.get('success') === 'true';
  const message = params.get('message') || '';
  const card = document.getElementById('result-card');

  if (!card) return;

  if (success) {
    localStorage.removeItem('cart');
    card.className = 'result-card success';
    card.innerHTML = `
      <div class="icon">✅</div>
      <h2>¡Pedido confirmado!</h2>
      <p>Tu pedido fue procesado correctamente.</p>
      <a href="/src/pages/client/orders/" class="btn btn-success">Mis Pedidos</a>
    `;
  } else {
    card.className = 'result-card error';
    card.innerHTML = `
      <div class="icon">❌</div>
      <h2>Error al procesar el pedido</h2>
      <p>${message ? `Motivo: ${message}` : 'Ocurrió un error inesperado.'}</p>
      <a href="/src/pages/client/cart/" class="btn btn-error">Volver al carrito</a>
    `;
  }
}

document.addEventListener('DOMContentLoaded', initResultado);

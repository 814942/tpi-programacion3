// =============================================
// main.js — Lógica de renderizado y eventos
// =============================================

// Render categories into the sidebar list
function cargarCategorias() {
  const list = document.getElementById("lista-categorias");

  categorias.forEach(function (categoria) {
    const li = document.createElement("li");
    const link = document.createElement("a");
    link.href = "#";
    link.textContent = categoria;
    li.appendChild(link);
    list.appendChild(li);
  });
}

// Render products into the main container
function cargarProductos(lista) {
  const container = document.getElementById("contenedor-productos");
  container.innerHTML = "";

  lista.forEach(function (producto) {
    const article = document.createElement("article");
    article.className = "producto";

    article.innerHTML = `
      <img src="${producto.imagen}" alt="${producto.nombre}">
      <h3>${producto.nombre}</h3>
      <p>${producto.descripcion}</p>
      <data value="${producto.precio}">$${producto.precio}</data>
      <button type="button" class="btn-agregar">Agregar al Carrito</button>
    `;

    // Add click event to the "Agregar" button
    const button = article.querySelector(".btn-agregar");
    button.addEventListener("click", function () {
      alert("Agregaste: " + producto.nombre);
    });

    container.appendChild(article);
  });
}

// Initialize on page load
document.addEventListener("DOMContentLoaded", function () {
  cargarCategorias();
  cargarProductos(productos);

  // Search form: filter products by name
  const form = document.getElementById("form-busqueda");
  form.addEventListener("submit", function (event) {
    event.preventDefault();

    const query = document
      .getElementById("input-busqueda")
      .value.trim()
      .toLowerCase();

    if (query === "") {
      cargarProductos(productos);
      return;
    }

    const filtered = productos.filter(function (producto) {
      return producto.nombre.toLowerCase().includes(query);
    });

    cargarProductos(filtered);
  });
});

# PROGRAMACION III

# Trabajo Práctico Integrador: JavaScript

# OBJETIVO GENERAL

Transformar la maqueta estática delprojecto "Food Store"(desarrollada previamente en HTML y CSS) en una aplicación dinámica. El-alumni deben eliminar el contenido estálico("hardcodeado") del HTML y sustituirlo por una generación automática de elementos mediante JavaScript, aplicando conceptos de Arrays, Objetos, Funciones y Manipulación del DOM.

# MARCO TEORICO

<table><tr><td>Concepto</td><td>Aplicación en el proyecto</td></tr><tr><td>Arrays y Objetos</td><td>Simulación de una base de datos local. Se utilizes un Array de Objetos para almacenar la información de los Productos (这个名字,PRECIO,Imagen,etc.) y un Array de Strings para las categorías.</td></tr><tr><td>DOM (Document Object Model)</td><td>Selección de elementos contenedores (nodos padres) en el HTML(getElementByld oquerySelector)donde se inyectará el contenido.</td></tr><tr><td>Template Strings</td><td>Uso de comillas invertidas(')para insertar variables de JavaScript Dentro deestructuras HTML de forma limpia y legible.</td></tr><tr><td>Bucles (Iteradores)</td><td>Uso de métodos como .forEach() o .map() para recorrer los listados de productos/categorías y tener el HTML correspondiente por cada item.</td></tr><tr><td>Concepto</td><td>Aplicación en elprojecto</td></tr><tr><td>Separación de Responsabilitades</td><td>Entender que el HTML define laestructuravacía,elCSSel estilo,yJS el contenido yla lógica.</td></tr></table>

# Caso Práctico

Actualmente, el archivo index.html contiene todos los productos y categorías escritos manualmente. Esto hace queactualizar el catálogo sea unaarea tediosa y propensa a errors. Se require automatizar este proceso mediante dos scripts principales.

/root

index.html   
—css/   
- styles.css   
js/   
data.js (Aquí irán los arrays de datos)   
main.js (Aquí irá la lógica de renderizado)   
— assets/ (Tus imágenes)

# PASO 1: Preparar el HTML (index.html)

# PASO 1: Preparar el archivo index.html

1. Reutiliza tu base: Toma el index.html que entrega en el integrador anterior.   
2. Identifica los contenedores: Busca la sección sobre listaste sus produits y la lista de categorías.   
3. Limpia el contenido: Borra todos los produits y categorías manuales. Deja las etiquetas "padre" (como <main>, <section> o <ul>) Completely vacías.   
4. Asigna IDs: Asegúrate de que ellos contendoores tengan un id para que JavaScript sepa exactamente sobre "escribir" la información. +2

Ejemplode comodebequedar tu HTML:

HTML

```html
<header> <h1> Food Store</h1> <nav> <a href="#" Inicio</a> <a href="#" Mis Pedidos</a> <a href="#" Carrito</a> <a href="admin.html">Panel Admin</a> </nav> </header> </aside> <h2>Categorias</h2> <ul id=" lista-categorias"></ul> </aside> <main> <form> <input type="text" placeholder="Buscar produits..." <button type="submit">Buscar</button> </form> <h2>PRODUCTOS Destacados</h2> <section id="conteditor-productos"></section> </main> </footer> <p>© 2024 Food Store. Todos los derechos reservados.</p> <p>Contacto: info@foodstore.com</p> </footer> 
```

```html
<script src=" ./js/data.js"></script> <script src=" ./js/main.js"></script> 
```

# PASO 2: Definir los datos (js/data.js)

En este archivo simularás la base de datos. Debes crear:

1. Un Array de Strings para las categorías.   
2. Un Array de Objetos para los produits (mínimo 4 produits).

# Códio Guía:

JavaScript

```javascript
// Array de categorías  
const categorías = ["Hamburguesas", "Pizzas", "Papas Fritas", "Bebidas"];  
const productos = [  
{  
    id: 1,  
    nombre: "Hamburguesa Triple",  
    descripción: "Triple carne, cheddar y bacon",  
   PRECIO: 25000,  
   Imagen: "https://via.place holder.com/300x200/ff6347/FFFFFF?text=Hamburguesa",  
    category:"Hamburguesas"  
},  
{  
    id: 2,  
    nombre: "Pizza Muzzarella",  
    descripción: "Salsa casera y orégano",  
   PRECIO: 18000,  
   Imagen: "https://via.place holder.com/300x200/ff6347/FFFFFF?text=Pizza",  
    category:"Pizzas"  
},  
]; 
```

# PASO 3: Lógica de Renderizado (js/main.js)

Desarrolla las functions para "pinta" los datos en la pantalla.

# A. Renderizar Categorías

Debes create una referencia llamada cargarCategorias() que realice loCEEjiente:

- SeLECTIONE el elemento del DOM donde se insertarán las categorías (el <ul> con id " lista-categorias").   
Recorra el array de categorías definido en tu archivo de datos.   
- Por cada CATEGORY, deben create un elemento de lista (<li>) que contenga un enlace (<a>).   
- Invecate cada nuevo elemento dentro del contentedor correspondiente.

# B. Renderizar Productos

Desarrollo la funciona cargarPRODUCTos() suguiendo这些requireimientos技术和:

- Iteración: Utiliza un método de array (comoforEach) para procesar tu lista de Productos.   
- Creación de Estructura: Por cada producto, debes tener un[nodo y asignarle la clase CSS nécessaria para que mantenga el estilo de tu proyecto anterior.   
- Uso de Plantillas: Utiliza Template Strings (comillas invertidas `') para maquetar el contenido interno del article.   
- Contenso Dinálico: La estrutura debe inclui r la imagen, el nombre, la descripción y el precio, accediendo a las propiedades del objecto producto.   
- Interactividad: El botón de "Agregar" debe做不到 un mensaje (puede usar un alert) que indique el nombre del producto selectionado.

# Requisitos:

- Variables: Utilizar const para declarar arrays y unidades.   
- Semántica: El HTML generado por JavaScript debe respetar las etiquetas <article>, <h3>, <p> y <button> definidas en el TP de HTML.   
- Rutas: Las imagenes deben visualizarse correctamente. Recuerda que las rutas relativas en el array de produits se resuelven desde la ubicacion del index.html.   
- Validation: No debe haber errores en la consola del navigador (F12).

# CONCLUSIONES ESPERADAS

Al finalizar este Trabajo Práctico, se expectsa que el-alumni haya logrado:

- Comprender laSeparated de responsabilitades: Entender que el HTML define la estructura, el CSS la apariencia y JavaScript el contenido y el comportimiento.   
- Independizar los datos de la vista: Asimilar que la informacion (productos, categorias) no debe estar "atada" al@cdoimento HTML, sino residir enestructuras de datos (Arrays/ Objetos) que permiten su fácil modificacion y escalabilidad.   
- Dominar el flujo de renderizacion: Visualizar como JavaScript actúa como intermediario, tomando datos crudos y transformándolos en elementos visuales del DOM (Nodos HTML) en tiempo deexecution.   
- Automatizar tareas repetitivas: Valorar la calidad de los bucles (forEach) para tenermultiple elementos repetitivos (como tarjetas de productos)crirendo el@cdoigouna sola vez, reduciendo errors yfacilitando lemantenimiento.   
- Preparar la lógica para el futuro: Dejar el proyecto lista para, en lasuma etapa, reemplazar el archivo data. js local por una peteción real a una API externa sin tener que modifier la estrutura visual del situ.

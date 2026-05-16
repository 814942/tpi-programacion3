# PROGRAMACION III

# Trabajo Práctico Integrador: TypeScript & Autenticación

# OBJETIVO GENERAL

Evolución la aplicación dinámica "Food Store" hacía un sistema con Autenticación y Roles. El-alumnideferáimplementarun flujo de seguidad que proteja el contenido según el tipo de usuario, sustituyendo la carga abierta de datos por un acceso restringido mediante TypeScript y localStorage.

# MARCO TEORICO

<table><tr><td>Concepto</td><td>Aplicación en el proyecto</td></tr><tr><td>Tipado Fuerte (TS)</td><td>Uso de Interfaces para asegurar que el usuario y sus roles (admin o client) sigan unaestructura rígida y sin errors.</td></tr><tr><td>Persistencia (Local)</td><td>Uso de localStorage para simular una base de datos de sistemas ymantener la sesión activ del navigador.</td></tr><tr><td>Autenticación</td><td>Proceso de verificación de credencias (Email y Password) comparándolos contra el almacenimiento local.</td></tr><tr><td>Autorización (Roles)</td><td>Lógica que decide si un usuario pueda acceder a una rutaspecífica (ej. /admin/) basándose en su rol.</td></tr></table>

# Caso Práctico

Actualmente, el repositorio base https://github.com/chiro45/proteger_rutas tiene una estructura de carpetas que debes Respectar:

- /pages: Contenedores de las vistas (auth, admin, client).   
- /utils: Lógica de navigation y verificacion de sesión.   
- /types: Definión de contratos de datos (IUser, Ro1).

PASO 1: Registrar de Usarios (src/pages/auth/register/)

Debes transformar el formulario estático en un sistema de captación de datos.

- Modificar el HTML para incluir: Email, contraseña y quitar el selector de rol.   
- En el archivo . ts, capturar los datos y guardarlos en un Array de Objetos dentro del localStorage bajo la clave "users".

PASO 2: Login y Gestion de Sesión (src/pages/auth/login/)

El login ya no debe ser una simulación;Debe validar datos reales.

- Al intentar ingresar,UGC en el array de "users" si existe una coincidencia de email y restraseña.   
- Si es correcto, guardar el的对象 del usuario en la clave "UserData" para iniciar la Sesión.

PASO 3: Protección de Rutas (El "Guard")

Implementar la lógica para que las páginas no sean accesibles mediante URL si no hay sesión.

- Centralización: En src/main.ts, create una función que intercebe la carga de la頁a.   
- Validation: Si un usuario con rol clientintaenta entrada a una carpeta /admin/, deben ser redirigido automatistically al login o a su zona permitida.

# CONCLUSIONES ESPERADAS

Al finalizar, el-alumni habr logrado:

- Independizar la seguridad de la vista: Entender que la proteccion no es solo ocular, sino lógica.   
- Dominar el flujo de datos persistentes: Gestionar informacion que sobrevive al ciderre del navegador.   
- Preparar el terreno para APIs: Dejar la lógica lista para reemplazar el localStorage por una base de datos real en el futuro.

# 5. RUBRICA DE EVALUACION

<table><tr><td>Criterio</td><td>Excelente (100%)</td><td>Aceptable (70%)</td><td>Insufiente (0-40%)</td></tr><tr><td>Funcionalidad</td><td>Registrar y Login validan datos correctamente en localStorage.</td><td>El sistema funciona pero permite registrados duplicates.</td><td>No hay calidad real de datos o no persiste la sesión.</td></tr><tr><td>Protección</td><td>Las rutas de admin está totalmente bloqueadas para sistemas in permiso.</td><td>La protección funciona pero se pueda evadir recargando la頁a.</td><td>Cualquier usuario puedeentrar acualquier URLsin loguearse.</td></tr><tr><td>TypeScript</td><td>Se utilizes interfaces y temaspecíficos en todo el proyecto.</td><td>Uso excessivo de any o temas mal definidos.</td><td>El número no aprovecha las ventajas de TypeScript.</td></tr><tr><td>Entrega</td><td>Entrega archivo .zip correctamente comprimido y con estructura de carpetas respetada.</td><td>Entrega archivo .zip functional pero con detailles menores.</td><td>No entrega archivo .zip, está corrupto, no respeta la estructura solicitada o el proyecto no executa.</td></tr></table>

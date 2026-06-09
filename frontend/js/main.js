// 1. IMPORTACIÓN DE MÓDULOS (Controladores de las vistas dinámicas)
import { configurarNomina } from './modulos/nomina.js';
import { configurarGestionEmpleados } from './modulos/gestionEmpleados.js';
import { configurarRegistro } from './modulos/registro-form.js';
import { configurarAsistenciaTabla } from './modulos/asistenciaTabla.js';
import { configurarInicioDashboard } from './modulos/inicioModulo.js'; // Tu código estrella del Set()
import { configurarHorario } from './modulos/horario.js';
import { configurarPermisos } from './modulos/permiso.js'; // Ajuste 1: Acoplamos permisos

// 2. ELEMENTOS CLAVE DEL DOM
const contenedor = document.getElementById('contenedor-dinamico');

// 3. CONTROL DE SEGURIDAD (Validar sesión de inmediato - RF05)
if (!sessionStorage.getItem("usuarioActual")) {
    window.location.href = "login.html";
}

// 4. MAPA DE ENRUTAMIENTO (Centralizado y sincronizado con Ontime3BD)
const rutasApp = {
    'btn-home':             { html: '../html/inicio.html',             init: configurarInicioDashboard },
    'btn-Registro':         { html: '../html/registro-form.html',     init: configurarRegistro },
    'btn-generarNomina':    { html: '../html/nomina.html',           init: configurarNomina },
    'btn-gestionEmpleados': { html: '../html/gestion-empleados.html', init: configurarGestionEmpleados },
    'btn-Asistencia':       { html: '../html/asistenciaTabla.html',   init: configurarAsistenciaTabla },
    'btn-horario':          { html: '../html/horario.html',          init: configurarHorario }, 
    'btn-Permiso':         { html: '../html/permiso.html',          init: configurarPermisos } // Enlazado con éxito
};

// 5. MOTOR DE INYECCIÓN DINÁMICA
async function cargarSeccion(rutaHtml) {
    try {
        const respuesta = await fetch(rutaHtml);
        if (!respuesta.ok) throw new Error(`No se pudo cargar la vista: ${rutaHtml}`);
        
        const html = await respuesta.text();
        contenedor.innerHTML = html;
        return true;
    } catch (error) {
        console.error("Error crítico en el enrutador:", error);
        contenedor.innerHTML = `<div class="error-vista" style="padding:20px; text-align:center; color:red; font-weight:bold;">⚠️ Error al cargar la sección. Verifique la ruta del archivo HTML.</div>`;
        return false;
    }
}

// 6. INICIALIZADOR DEL MENÚ LATERAL
function inicializarMenu() {
    Object.entries(rutasApp).forEach(([idBoton, configuracion]) => {
        const boton = document.getElementById(idBoton);
        
        if (boton) {
            boton.addEventListener('click', async (e) => {
                e.preventDefault();
                
                // Removemos clase activa de otros botones y se la ponemos al actual si manejas estilos de selección
                document.querySelectorAll('.bt-menu-item').forEach(b => b.classList.remove('activo'));
                boton.classList.add('activo');

                const exito = await cargarSeccion(configuracion.html);
                if (exito && configuracion.init) {
                    configuracion.init(); // Levanta el JS del módulo inyectado
                }
            });
        }
    });
}

// 7. CONTROL DE CIERRE DE SESIÓN (Ajuste 3: Ruta unificada y limpia)
function configurarCerrarSesion() {
    const btnCerrarSesion = document.getElementById('btn-CerrarSesion');
    if (btnCerrarSesion) {
        btnCerrarSesion.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.clear(); // Limpia credenciales de memoria
            window.location.href = 'login.html'; 
        });
    }
}

// 8. ARRANQUE GLOBAL DEL DASHBOARD
window.addEventListener('DOMContentLoaded', async () => {
    inicializarMenu();
    configurarCerrarSesion();

    // Carga inicial automatizada: Simula el clic en el botón Home al entrar al Dashboard
    const botonHome = document.getElementById('btn-home');
    if (botonHome) {
        botonHome.click();
    } else {
        const exito = await cargarSeccion(rutasApp['btn-home'].html);
        if (exito) rutasApp['btn-home'].init();
    }
});

// 1. IMPORTACIÓN DE MÓDULOS (Controladores de las vistas dinámicas)
import { configurarCalendario } from './modulos/calendario.js';
import { configurarNomina } from './modulos/nomina.js';
import { configurarGestionEmpleados } from './modulos/gestionEmpleados.js';
import { configurarRegistro } from './modulos/registro-form.js';
import { configurarAsistenciaTabla } from './modulos/asistenciaTabla.js';
import { configurarInicioDashboard } from './modulos/inicioModulo.js';
import { configurarHorario } from './modulos/horario.js';

// 2. ELEMENTOS CLAVE DEL DOM
const contenedor = document.getElementById('contenedor-dinamico');

// 3. CONTROL DE SEGURIDAD (Validar sesión de inmediato)
if (!sessionStorage.getItem("usuarioActual")) {
    window.location.href = "login.html";
}

// 4. MAPA DE ENRUTAMIENTO (Centraliza aquí todo tu menú lateral para evitar repetir código)
const rutasApp = {
    'btn-home':             { html: '../html/inicio.html',             init: configurarInicioDashboard },
    'btn-calendario':       { html: '../html/calendario.html',       init: configurarCalendario },
    'btn-Registro':         { html: '../html/registro-form.html',     init: configurarRegistro },
    'btn-generarNomina':    { html: '../html/nomina.html',           init: configurarNomina },
    'btn-gestionEmpleados': { html: '../html/gestion-empleados.html', init: configurarGestionEmpleados },
    'btn-Asistencia':       { html: '../html/asistenciaTabla.html',   init: configurarAsistenciaTabla },
    'btn-horario':          { html: '../html/horario.html',          init: configurarHorario }, 
    'btn-Permisos':         { html: '../html/permisos.html',          init: null },
    'btn-Informes':         { html: '../html/informes.html',          init: null }
};

// 5. MOTOR DE INYECCIÓN DINÁMICA (Carga el HTML en el contenedor)
async function cargarSeccion(rutaHtml) {
    try {
        const respuesta = await fetch(rutaHtml);
        if (!respuesta.ok) throw new Error(`No se pudo cargar la vista: ${rutaHtml}`);
        
        const html = await respuesta.text();
        contenedor.innerHTML = html;
        return true;
    } catch (error) {
        console.error("Error crítico en el enrutador:", error);
        contenedor.innerHTML = `<div class="error-vista"> Error al cargar la sección. Intente de nuevo.</div>`;
        return false;
    }
}

// 6. INICIALIZADOR DEL MENÚ LATERAL (Escucha los clics usando el mapa de rutas)
function inicializarMenu() {
    Object.entries(rutasApp).forEach(([idBoton, configuracion]) => {
        const boton = document.getElementById(idBoton);
        
        if (boton) {
            boton.addEventListener('click', async (e) => {
                e.preventDefault();
                const exito = await cargarSeccion(configuracion.html);
                if (exito && configuracion.init) {
                    configuracion.init(); // Dispara la función JS del módulo correspondiente
                }
            });
        }
    });
}

// 7. CONTROL DE CIERRE DE SESIÓN
function configurarCerrarSesion() {
    const btnCerrarSesion = document.getElementById('btn-CerrarSesion');
    if (btnCerrarSesion) {
        btnCerrarSesion.addEventListener('click', (e) => {
            e.preventDefault();
            sessionStorage.clear();
            window.location.href = './login.html'; 
        });
    }
}

// 8. ARRANQUE GLOBAL DEL DASHBOARD
window.addEventListener('DOMContentLoaded', async () => {
    inicializarMenu();
    configurarCerrarSesion();

    // Carga inicial automatizada: Dispara la pantalla de inicio por defecto
    const botonHome = document.getElementById('btn-home');
    if (botonHome) {
        botonHome.click();
    } else {
        const exito = await cargarSeccion(rutasApp['btn-home'].html);
        if (exito) rutasApp['btn-home'].init();
    }
});
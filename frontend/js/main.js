// Jalamos absolutamente todo desde tu archivo índice centralizado
import { 
    configurarCalendario, 
    configurarNomina, 
    configurarGestionEmpleados, 
    configurarRegistro,
    configurarInicioDashboard 
} from './modulos/index.js';

const contenedor = document.getElementById('contenedor-dinamico');

// Validar sesión antes de renderizar la vista
const sesionStr = sessionStorage.getItem("usuarioActual");
if (!sesionStr) {
    window.location.href = "login.html";
}

async function cargarSeccion(ruta) {
    try {
        const respuesta = await fetch(ruta);
        if (!respuesta.ok) throw new Error(`No se pudo cargar la ruta: ${ruta}`);
        const html = await respuesta.text();
        contenedor.innerHTML = html;
    } catch (error) {
        console.error("Error cargando sección:", error);
    }
}

function asignarEvento(idBoton, rutaHtml, funcionModulo = null) {
    const boton = document.getElementById(idBoton);
    if (boton) {
        boton.addEventListener('click', async (e) => {
            e.preventDefault();
            await cargarSeccion(rutaHtml);
            if (funcionModulo) funcionModulo();
        });
    } else {
        console.warn(`Advertencia: El botón con id '${idBoton}' no se encontró en el HTML.`);
    }
}

// --- ASIGNACIÓN DE TU MENÚ LATERAL ---
asignarEvento('btn-calendario', '../html/calendario.html', configurarCalendario);
asignarEvento('btn-Registro', '../html/registro-form.html', configurarRegistro);
asignarEvento('btn-generarNomina', '../html/nomina.html', configurarNomina);
asignarEvento('btn-gestionEmpleados', '../html/gestion-empleados.html', configurarGestionEmpleados);

// El botón asistencia carga tu tabla nativa limpia sin funciones que lo rompan
asignarEvento('btn-Asistencia', '../html/asistenciaTabla.html');
asignarEvento('btn-Permisos', '../html/permisos.html');
asignarEvento('btn-Informes', '../html/informes.html');

// --- CERRAR SESIÓN ---
const btnCerrarSesion = document.getElementById('btn-CerrarSesion');
if (btnCerrarSesion) {
    btnCerrarSesion.addEventListener('click', (e) => {
        e.preventDefault();
        sessionStorage.clear();
        window.location.href = './login.html'; 
    });
}

// --- CARGA INICIAL Y ESCUCHA DEL BOTÓN HOME ---
window.addEventListener('DOMContentLoaded', async () => {
    const btnhome = document.getElementById('btn-home');
    
    if (btnhome) {
        // Le asignamos el evento real al botón físico de tu menú
        btnhome.addEventListener('click', async (e) => {
            e.preventDefault();
            await cargarSeccion('../html/inicio.html');
            configurarInicioDashboard();
        });

        // Disparamos el clic automático para que renderice el inicio apenas cargue la página
        btnhome.click();
    } else {
        // En caso de que el botón 'btn-home' no exista en dashb.html, cargamos la sección por defecto de todos modos
        await cargarSeccion('../html/inicio.html');
        configurarInicioDashboard();
    }
});
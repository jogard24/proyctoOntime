import { configurarCalendario, configurarNomina, configurarGestionEmpleados, configurarRegistro } from './modulos/index.js';
import { configurarAsistenciaPinpad } from './modulos/index.js';

const contenedor = document.getElementById('contenedor-dinamico');

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

// --- FUNCIÓN UTILITARIA PARA ASIGNAR EVENTOS SEGUROS ---
function asignarEvento(idBoton, rutaHtml, funcionModulo = null) {
    const boton = document.getElementById(idBoton);
    if (boton) {
        boton.addEventListener('click', async (e) => {
            e.preventDefault();
            await cargarSeccion(rutaHtml);
            if (funcionModulo) funcionModulo();
        });
    } else {
        console.warn(` Advertencia: El botón con id '${idBoton}' no se encontró en el HTML.`);
    }
}

// --- ASIGNACIÓN DE TU MENÚ LATERAL ---
asignarEvento('btn-calendario', '../html/calendario.html', configurarCalendario);
asignarEvento('btn-Registro', '../html/registro-form.html', configurarRegistro);
asignarEvento('btn-generarNomina', '../html/nomina.html', configurarNomina);
asignarEvento('btn-gestionEmpleados', '../html/gestion-empleados.html', configurarGestionEmpleados);

// Historial de asistencia (solo carga la tabla)
asignarEvento('btn-Asistencia', '../html/asistenciaTabla.html');
asignarEvento('btn-Permisos', '../html/permisos.html');
asignarEvento('btn-Informes', '../html/informes.html');


// --- LOGOUT: CERRAR SESIÓN (Código independiente y seguro) ---
const btnCerrarSesion = document.getElementById('btn-CerrarSesion');

if (btnCerrarSesion) {
    console.log(" El botón Cerrar Sesión fue detectado correctamente.");
    btnCerrarSesion.addEventListener('click', (e) => {
        e.preventDefault();
        console.log(" Cerrando sesión... Redirigiendo al Pinpad.");
        window.location.href = './registro-asistencia.html'; 
    });
} else {
    console.error(" Error Crítico: No se encontró ningún elemento con id='btn-CerrarSesion' en dashb.html");
}
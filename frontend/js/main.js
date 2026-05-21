
import { configurarCalendario, configurarNomina, configurarGestionEmpleados, configurarRegistro } from './modulos/index.js';

const contenedor = document.getElementById('contenedor-dinamico');

async function cargarSeccion(ruta) {
    const respuesta = await fetch(ruta);
    const html = await respuesta.text();
    contenedor.innerHTML = html;
}

// botón de calendario
document.getElementById('btn-calendario').addEventListener('click', async () => {
    await cargarSeccion('../html/calendario.html');
    configurarCalendario();

});

// botón de registro
document.getElementById('btn-Registro').addEventListener('click', async () => {
    await cargarSeccion('../html/registro-form.html');
    configurarRegistro();
});

// botón de nómina
document.getElementById('btn-generarNomina').addEventListener('click', async () => {
    await cargarSeccion('../html/nomina.html');
    configurarNomina();
});

// botón de asistencia
document.getElementById('btn-Asistencia').addEventListener('click', async () => {
    await cargarSeccion('../html/asistenciaTabla.html');
});

// botón de permisos
document.getElementById('btn-Permisos').addEventListener('click', async () => {
    await cargarSeccion('../html/permisos.html');
});

// botón de gestión de empleados
document.getElementById('btn-gestionEmpleados').addEventListener('click', async () => {
    await cargarSeccion('../html/gestion-empleados.html');
    configurarGestionEmpleados();
});

document.getElementById('btn-Informes').addEventListener('click', async () => {
    await cargarSeccion('../html/informes.html');

});

// Dentro del evento de nuevoBtnConfirmar en tu asistencia.js:
nuevoBtnConfirmar.addEventListener('click', async () => {
    if (cadenaId.trim() === "") {
        mostrarFeedback(" Por favor ingresa tu identificación", "error");
        return;
    }

    try {
        // Llamamos al Servlet que acabamos de crear en Java
        const respuesta = await fetch('/proyectoOntime/api/asistencia', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: `empleadoId=${encodeURIComponent(cadenaId)}`
        });

        const resultado = await respuesta.json();

        if (respuesta.ok) {
            mostrarFeedback(` ${resultado.message}`, "exito");
        } else {
            mostrarFeedback(` ${resultado.message}`, "error");
        }

    } catch (error) {
        console.error("Error de conexión:", error);
        mostrarFeedback("💥 Error al conectar con el servidor", "error");
    }

    // Limpiamos el pinpad
    cadenaId = "";
    actualizarPantalla();
});





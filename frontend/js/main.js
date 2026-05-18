
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
// botón de gestión de empleados
document.getElementById('btn-gestionEmpleados').addEventListener('click', async () => {
    await cargarSeccion('../html/gestion-empleados.html');
    configurarGestionEmpleados();
});







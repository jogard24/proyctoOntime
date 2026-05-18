const btngestionempleados = document.getElementById('btn-gestionEmpleados');
const contenedor = document.getElementById('contenedor-dinamico');

btngestionempleados.addEventListener('click', () => {
    await cargarSeccion('gestion-empleados.html');
    // Aquí puedes inicializar cualquier lógica específica para la gestión de empleados
    configurarGestionEmpleados();
});
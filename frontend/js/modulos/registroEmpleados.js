export function configurarRegistro() {
    const btnGuardar = document.getElementById('btn-guardar-empleado');
    if (btnGuardar) {
        btnGuardar.addEventListener('click', () => {
            console.log("Empleado guardado");
        });
    }
}

export function configurarContrato() {
    const btnAddContrato = document.getElementById('btn-add-contrato');
    const modal = document.getElementById('modalContrato');
    const btnCerrar = document.getElementById('btnCerrarModal');
    const formContrato = document.getElementById('formContrato');

    if (!btnAddContrato || !modal || !formContrato) {
        console.warn('Elementos del contrato no encontrados en la vista.');
        return;
    }

    function abrirModal() {
        modal.style.display = 'flex';
    }

    function cerrarModal() {
        modal.style.display = 'none';
    }

    btnAddContrato.addEventListener('click', (e) => {
        e.preventDefault();
        abrirModal();
    });

    if (btnCerrar) btnCerrar.addEventListener('click', (e) => {
        e.preventDefault();
        cerrarModal();
    });

    formContrato.addEventListener('submit', (e) => {
        e.preventDefault();

        // CORRECCIÓN CRÍTICA: Nombres alineados al 100% con registro-form.js y el Servlet
        const contrato = {
            tipoContrato: document.getElementById('tipoContrato')?.value.trim() || '',
            cargoContrato: document.getElementById('cargoContrato')?.value.trim() || '',
            salarioBase: parseFloat(document.getElementById('salarioBase')?.value) || 0,
            jornadaContrato: document.getElementById('jornadaContrato')?.value.trim() || '',
            fechaInicio: document.getElementById('fechaInicio')?.value || '',
            fechaFin: document.getElementById('fechaFin')?.value || '',
            observacion: document.getElementById('observacionContrato')?.value.trim() || ''
        };

        // Guardamos temporalmente el contrato en la ventana global para enviarlo junto al empleado
        window._contratoTemporal = contrato;

        alert('Contrato pre-guardado con éxito. Recuerde dar clic en "Guardar" al final del formulario principal.');
        cerrarModal();
    });
}


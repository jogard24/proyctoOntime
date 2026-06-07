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

        const contrato = {
            tipoContrato: document.getElementById('tipoContrato')?.value.trim() || '',
            cargo: document.getElementById('cargoContrato')?.value.trim() || '',
            salarioBase: parseFloat(document.getElementById('salarioBase')?.value) || 0,
            jornada: document.getElementById('jornadaContrato')?.value.trim() || '',
            fechaInicio: document.getElementById('fechaInicio')?.value || '',
            fechaFin: document.getElementById('fechaFin')?.value || '',
            observacion: document.getElementById('observacionContrato')?.value.trim() || ''
        };

        // Guardamos temporalmente el contrato para enviarlo junto al empleado
        window._contratoTemporal = contrato;

        alert('Contrato guardado temporalmente. Se añadirá al guardar el empleado.');
        cerrarModal();
    });
}

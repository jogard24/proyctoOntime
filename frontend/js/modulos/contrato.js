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

        // CORRECCIÓN INTEGRAL: Mapeamos los nombres de las llaves en formato Snake_Case 
        // para que request.getParameter() en Java Puro los lea sin fallas de red
        const contrato = {
            tipo_contrato: document.getElementById('tipoContrato')?.value.trim() || '',
            cargo: document.getElementById('cargoContrato')?.value.trim() || '',
            salario_base: parseFloat(document.getElementById('salarioBase')?.value) || 0,
            jornada_id: document.getElementById('jornadaContrato')?.value.trim() || '',
            
            // SINCRO: Cambiamos el nombre de las llaves para que coincidan con el Servlet
            fecha_inicio: document.getElementById('regFechaInicio')?.value || '',
            fecha_fin: document.getElementById('regFechaFin')?.value || '', // Cadena vacía si es indefinido
            
            observacion: document.getElementById('observacionContrato')?.value.trim() || ''
        };

        // Guardamos de forma modular en la ventana global
        window._contratoTemporal = contrato;

        alert('¡Contrato pre-guardado con éxito! Recuerde dar clic en el botón "Guardar" al final del formulario principal.');
        cerrarModal();
    });

}


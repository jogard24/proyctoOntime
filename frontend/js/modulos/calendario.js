export function configurarCalendario() {
    // 1. Referencias al DOM (deben existir después de cargar calendario.html)
    const mesAnioTexto = document.getElementById('mes-año-actual');
    const rejillaDias = document.getElementById('calendario-rejilla-dias');
    const btnAnt = document.getElementById('mes-ant');
    const btnSiguiente = document.getElementById('mes-siguiente');

    if (!mesAnioTexto || !rejillaDias || !btnAnt || !btnSiguiente) {
        console.warn('No se pudo inicializar el calendario: faltan elementos HTML.');
        return;
    }

    const meses = [
        'Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio',
        'Julio', 'Agosto', 'Septiembre', 'Octubre', 'Noviembre', 'Diciembre'
    ];

    let fechaActual = new Date();

    function renderizarCalendario() {
        const anio = fechaActual.getFullYear();
        const mes = fechaActual.getMonth();

        mesAnioTexto.textContent = `${meses[mes]} - ${anio}`;
        rejillaDias.innerHTML = '';

        const primerDiaMes = new Date(anio, mes, 1).getDay();
        const totalDias = new Date(anio, mes + 1, 0).getDate();
        const puntoInicio = (primerDiaMes === 0) ? 7 : primerDiaMes;

        for (let i = 1; i < puntoInicio; i++) {
            const divVacio = document.createElement('div');
            divVacio.classList.add('dia', 'vacio');
            rejillaDias.appendChild(divVacio);
        }

        for (let dia = 1; dia <= totalDias; dia++) {
            const divDia = document.createElement('div');
            divDia.classList.add('dia');
            divDia.textContent = dia;

            const hoy = new Date();
            if (dia === hoy.getDate() && mes === hoy.getMonth() && anio === hoy.getFullYear()) {
                divDia.style.border = '1px solid var(--white)';
                divDia.style.background = 'rgba(255,255,255,0.1)';
            }

            rejillaDias.appendChild(divDia);
        }
    }

    btnAnt.addEventListener('click', () => {
        fechaActual.setMonth(fechaActual.getMonth() - 1);
        renderizarCalendario();
    });

    btnSiguiente.addEventListener('click', () => {
        fechaActual.setMonth(fechaActual.getMonth() + 1);
        renderizarCalendario();
    });

    renderizarCalendario();
}

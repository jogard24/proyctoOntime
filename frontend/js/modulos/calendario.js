export function configurarCalendario() {
    const mesAnioTexto = document.getElementById('mes-año-actual');
    const rejillaDias = document.getElementById('calendario-rejilla-dias');
    const btnAnt = document.getElementById('mes-ant');
    const btnSiguiente = document.getElementById('mes-siguiente');

    let fechaActual = new Date();

    function renderizarCalendario() {
        const anio = fechaActual.getFullYear();
        const mes = fechaActual.getMonth();
        const meses = ["Enero","Febrero","Marzo","Abril","Mayo","Junio",
                       "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"];

        mesAnioTexto.textContent = `${meses[mes]} - ${anio}`;
        rejillaDias.innerHTML = '';

        const primerDiaMes = new Date(anio, mes, 1).getDay();
        const totalDias = new Date(anio, mes + 1, 0).getDate();
        let puntoInicio = (primerDiaMes === 0) ? 7 : primerDiaMes;

        for (let i = 1; i < puntoInicio; i++) {
            const divVacio = document.createElement('div');
            divVacio.classList.add('dia','vacio');
            rejillaDias.appendChild(divVacio);
        }

        for (let dia = 1; dia <= totalDias; dia++) {
            const divDia = document.createElement('div');
            divDia.classList.add('dia');
            divDia.textContent = dia;
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

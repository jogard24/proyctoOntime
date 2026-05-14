

    
    // export const meses = ["Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"];
    
    // export function renderizarCalendario(fecha, rejilla, titulo) {
    //     const anio = fecha.getFullYear();
    //     const mes = fecha.getMonth();
    //     titulo.textContent = `${meses[mes]} - ${anio}`;
    //     rejilla.innerHTML = '';
        
    //     const primerDia = new Date(anio, mes, 1).getDay();
    //     const totalDias = new Date(anio, mes + 1, 0).getDate();
    //     let inicio = (primerDia === 0) ? 7 : primerDia;
        
    //     for (let i = 1; i < inicio; i++) {
    //         const vacio = document.createElement('div');
    //         vacio.classList.add('dia', 'vacio');
    //         rejilla.appendChild(vacio);
    //     }
        
    //     for (let d = 1; d <= totalDias; d++) {
    //         const divDia = document.createElement('div');
    //         divDia.classList.add('dia');
    //         divDia.textContent = d;
    //         rejilla.appendChild(divDia);
    //     }
    // }

    // 1. Referencias al DOM (Tal cual están en tu HTML)
const mesAnioTexto = document.getElementById('mes-año-actual');
const rejillaDias = document.getElementById('calendario-rejilla-dias');
const btnAnt = document.getElementById('mes-ant');
const btnSiguiente = document.getElementById('mes-siguiente');

// 2. Variables de estado
const meses = [
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
];

let fechaActual = new Date(); // Fecha de hoy por defecto

// 3. Función Principal

function renderizarCalendario() {
    // Obtener año y mes actual del estado
    const anio = fechaActual.getFullYear();
    const mes = fechaActual.getMonth();

    // Actualizar el título (Ej: Abril - 2026)
    mesAnioTexto.textContent = `${meses[mes]} - ${anio}`;

    // Limpiar la rejilla antes de pintar
    rejillaDias.innerHTML = '';

    // --- LÓGICA DE DÍAS ---
    
    // primerDiaMes: 0 (Dom) a 6 (Sáb)
    const primerDiaMes = new Date(anio, mes, 1).getDay();
    
    // totalDias: El día 0 del mes siguiente es el último del actual
    const totalDias = new Date(anio, mes + 1, 0).getDate();

    // Ajuste para que la semana empiece en Lunes (L=1, M=2... D=7)
    // Si primerDiaMes es 0 (Domingo), lo pasamos a 7.
    let puntoInicio = (primerDiaMes === 0) ? 7 : primerDiaMes;

    // 4. Crear espacios vacíos para los días del mes anterior
    for (let i = 1; i < puntoInicio; i++) {
        const divVacio = document.createElement('div');
        divVacio.classList.add('dia', 'vacio');
        rejillaDias.appendChild(divVacio);
    }

    // 5. Crear los días del mes actual
    for (let dia = 1; dia <= totalDias; dia++) {
        const divDia = document.createElement('div');
        divDia.classList.add('dia');
        divDia.textContent = dia;

        // Aquí puedes añadir una marca si es "hoy"
        const hoy = new Date();
        if (dia === hoy.getDate() && mes === hoy.getMonth() && anio === hoy.getFullYear()) {
            divDia.style.border = "1px solid var(--white)";
            divDia.style.background = "rgba(255,255,255,0.1)";
        }

        rejillaDias.appendChild(divDia);
    }
}

const btn = document.querySelector("#btn-calendario");
const seccion = document.querySelector("#vista-calendario");

btn.addEventListener ( "click", async () => {
    seccion.style.display = "block";
    await cargarSeccion('calendario.html');

    renderizarCalendario();

});
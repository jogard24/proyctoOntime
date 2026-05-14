// const botones = document.querySelectorAll('.menu-item');

// botones.forEach(boton => {
//     boton.addEventListener('click', () => {
//         const idSeccion = boton.getAttribute('data-target'); 
        
//         // 1. Ocultamos todas las secciones
//         document.querySelectorAll('.seccion-dashboard').forEach(s => s.style.display = 'none');
        
//         // 2. Mostramos la sección que corresponde al botón presionado
//         const seccionAMostrar = document.getElementById(vistaAsistencia);
//         if(seccionAMostrar) {
//             seccionAMostrar.style.display = 'block';
//         }

//         // 3. (OPCIONAL) Resaltar el botón activo en el menú
//         botones.forEach(b => b.classList.remove('active'));
//         boton.classList.add('active');
//     });
// });


const btn = document.querySelector("#btn-Asistencia");
const seccion = document.querySelector("#vistaAsistencia");

btn.addEventListener ( "click", () => {
    seccion.style.display = "block";
})



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

// 6. Eventos de las flechas
btnAnt.addEventListener('click', () => {
    fechaActual.setMonth(fechaActual.getMonth() - 1);
    renderizarCalendario();
});

btnSiguiente.addEventListener('click', () => {
    fechaActual.setMonth(fechaActual.getMonth() + 1);
    renderizarCalendario();
});




// 7. Ejecutar al cargar
renderizarCalendario();

// Buscamos el botón que debe abrir el formulario
const btnAbrirRegistro = document.getElementById('btn-Registro'); // Asegúrate de que este ID coincida con tu menú lateral

if (btnAbrirRegistro) {
    btnAbrirRegistro.addEventListener('click', async () => {
        // Usamos la función de carga que ya conocemos
        await cargarSeccion('registro-form.html');
        
        // Una vez cargado, podemos inicializar lógica específica del formulario
        configurarFormularioRegistro();
    });
}

const btngenerarcalendario = document.getElementById('btn-calendario');
const seccionCalendario = document.getElementById('vista-calendario');



if (btngenerarcalendario) {
    btngenerarcalendario.addEventListener('click', async () => {
        await cargarSeccion('calendario.html');
        renderizarCalendario();
    });
}


async function cargarSeccion(archivo) {
    const contenedor = document.getElementById('contenedor-dinamico');
    try {
        const respuesta = await fetch(archivo);
        const html = await respuesta.text();
        contenedor.innerHTML = html;
    } catch (error) {
        console.error("Error al cargar el formulario:", error);
    }
}

function configurarFormularioRegistro() {
    const btnGuardar = document.getElementById('btn-guardar-empleado');
    if (btnGuardar) {
        btnGuardar.addEventListener('click', () => {
            console.log("¡Click en guardar!");
            // Aquí puedes validar los campos antes de enviarlos
        });
    }
}


const generarNominabtn = document.getElementById('btn-generarNomina');

if (generarNominabtn) {
    generarNominabtn.addEventListener('click', async () => {
        await cargarSeccion('nomina.html');
        generarNominabtn();
});

function generarNomina() {
    const generarNominaBtn = document.getElementById('btn-generarNomina');
    if (generarNominaBtn) {
        generarNominaBtn.addEventListener('click', () => {
            console.log('¡Nómina generada con éxito!');
            
        });
    }
}

// Llamamos a la función para configurar el botón de nómina
generarNomina();

}

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


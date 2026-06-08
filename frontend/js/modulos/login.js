function crearContenedorFeedback() {
    const contenedorLogin = document.querySelector('.card-login');
    if (!contenedorLogin) return null;

    let feedback = document.getElementById('login-feedback');
    if (!feedback) {
        feedback = document.createElement('p');
        feedback.id = 'login-feedback';
        feedback.className = 'login-feedback';
        contenedorLogin.appendChild(feedback);
    }
    return feedback;
}

function mostrarFeedback(mensaje, tipo = 'error') {
    const feedback = crearContenedorFeedback();
    if (!feedback) {
        alert(mensaje);
        return;
    }
    feedback.textContent = mensaje;
    feedback.className = `login-feedback login-feedback--${tipo}`;
}

function inicializarLogin() {
    const inputUsuario = document.querySelector('input[name="nombre"]');
    const inputPassword = document.getElementById('pass') || document.querySelector('input[name="contraseña"]');
    const botonIngresar = document.getElementById('btn-ingresar');

    if (!inputUsuario || !inputPassword || !botonIngresar) {
        console.warn('No se pudieron inicializar los elementos del login.');
        return;
    }

    botonIngresar.addEventListener('click', async (event) => {
        event.preventDefault();

        const usuarioVal = inputUsuario.value.trim();
        const claveVal = inputPassword.value.trim();

        if (usuarioVal === '' || claveVal === '') {
            mostrarFeedback('Por favor completa usuario y contraseña.', 'error');
            return;
        }

        try {
            // Ajuste 1: URL sincronizada con el estándar de nombres del Backend (LoginServlet)
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/LoginServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `usuario=${encodeURIComponent(usuarioVal)}&clave=${encodeURIComponent(claveVal)}`
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                // Guarda los datos en sessionStorage (Alimenta con éxito tu saludo en el Home)
                sessionStorage.setItem('usuarioActual', JSON.stringify({
                    nombre: resultado.nombre || usuarioVal,
                    rol: resultado.rol
                }));

                mostrarFeedback(`${resultado.message || 'Acceso concedido.'} Cargando panel...`, 'exito');

                // ENRUTAMIENTO CONTROLADO (RF04 y RF05)
                setTimeout(() => {
                    const rolServidor = resultado.rol ? resultado.rol.trim().toLowerCase() : "";
                    console.log("Rol recibido:", rolServidor);

                    if (rolServidor === 'administrador') {
                        window.location.href = '../html/dashb.html'; // Panel del Administrador
                    } else if (rolServidor === 'contador') {
                        window.location.href = '../html/dashboard_contador.html'; // Panel del Contador
                    } else {
                        mostrarFeedback('Acceso denegado: El rol no cuenta con privilegios de Dashboard.', 'error');
                    }
                }, 1500);

            } else {
                mostrarFeedback(resultado.message || 'Usuario o contraseña incorrectos.', 'error');
            }

        } catch (error) {
            console.error('Error al conectar con el Login:', error);
            mostrarFeedback('✘ Error de comunicación con el servidor principal.', 'error');
        }
    });
}

window.addEventListener('DOMContentLoaded', inicializarLogin);


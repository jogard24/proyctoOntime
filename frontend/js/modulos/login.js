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
    const enlaceRecordar = document.querySelector('a[href="recordar contraseña"]');

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
            // Disparamos la petición POST directo al Servlet en Tomcat
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `usuario=${encodeURIComponent(usuarioVal)}&clave=${encodeURIComponent(claveVal)}`
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                // Guardamos los datos de la sesión activa de forma segura en el navegador
                sessionStorage.setItem('usuarioActual', JSON.stringify({
                    nombre: resultado.nombre,
                    rol: resultado.rol
                }));

                mostrarFeedback(`${resultado.message} Cargando panel...`, 'exito');

                // ENRUTAMIENTO INTELIGENTE SEGÚN EL ROL DE LA BASE DE DATOS
                setTimeout(() => {

                    const rolServidor = resultado.rol ? resultado.rol.trim().toLowerCase() : "";
                    console.log("Rol recibido:", rolServidor); // Esto te dirá exactamente qué llega

                    if (rolServidor === 'administrador') {
                        window.location.href = '../html/dashb.html'; // Tu dashboard principal
                    } else if (rolServidor === 'contador') {
                        window.location.href = '../html/dashboard_contador.html'; // Panel contable si lo creas
                    } else {
                        mostrarFeedback('No tienes un panel asignado para este rol.', 'error');
                    }
                }, 1500);

            } else {
                // Muestra errores de credenciales incorrectas o cuentas inactivas controladas por Java
                mostrarFeedback(resultado.message, 'error');
            }

        } catch (error) {
            console.error('Error al conectar con el Login:', error);
            mostrarFeedback('✘ Error de comunicación con el servidor principal.', 'error');
        }
    });

    if (enlaceRecordar) {
        enlaceRecordar.addEventListener('click', (event) => {
            event.preventDefault();
            mostrarFeedback('Contacta al administrador para restablecer tu contraseña.', 'info');
        });
    }
}

window.addEventListener('DOMContentLoaded', inicializarLogin);

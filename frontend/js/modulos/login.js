// Variable para capturar el rol seleccionado en la interfaz
let rolSeleccionado = '';

/**
 * Función para seleccionar el rol en la interfaz.
 * Se asigna a window para que sea accesible desde el onclick del HTML.
 */
window.selectRole = function(role, element) {
    rolSeleccionado = role;
    
    // UI: Elimina la clase 'active' de todos los botones de rol y la añade al seleccionado
    document.querySelectorAll('.btn-role').forEach(btn => btn.classList.remove('active'));
    element.classList.add('active');
    
    console.log("Rol seleccionado:", rolSeleccionado);
};

/**
 * Crea o recupera el contenedor de mensajes de feedback
 */
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

/**
 * Inicializador principal del login
 */
function inicializarLogin() {
    const inputUsuario = document.querySelector('input[name="nombre"]');
    // Nota: Asegúrate de que el input de contraseña tenga el nombre correcto o id único
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

        // Validación de rol y campos vacíos
        if (!rolSeleccionado) {
            mostrarFeedback('Por favor, selecciona primero tu rol (Admin o Contador).', 'error');
            return;
        }

        if (usuarioVal === '' || claveVal === '') {
            mostrarFeedback('Por favor completa usuario y contraseña.', 'error');
            return;
        }

        try {
            // Petición al Backend con el rol incluido
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'X-Requested-With': 'XMLHttpRequest' // Importante para tu AuthFilter
                },
                body: `usuario=${encodeURIComponent(usuarioVal)}&clave=${encodeURIComponent(claveVal)}&rol=${encodeURIComponent(rolSeleccionado)}`
            });

            const resultado = await respuesta.json();

            if (respuesta.ok) {
                // Guardado de sesión
                sessionStorage.setItem('usuarioActual', JSON.stringify({
                    nombre: resultado.nombre,
                    Rol: resultado.Rol
                }));

                mostrarFeedback(`${resultado.message} Redirigiendo...`, 'exito');

                // Enrutamiento inteligente según el rol devuelto por Java
                setTimeout(() => {
                    if (resultado.Rol === 'administrador') {
                        window.location.href = '../html/dashb.html';
                    } else if (resultado.Rol === 'contador') {
                        window.location.href = '../html/dashb-contador.html';
                    } else {
                        mostrarFeedback('No tienes un panel asignado para este rol.', 'error');
                    }
                }, 1500);

            } else {
                mostrarFeedback(resultado.message || 'Error en la autenticación', 'error');
            }

        } catch (error) {
            console.error('Error al conectar con el Login:', error);
            mostrarFeedback('✘ Error de comunicación con el servidor.', 'error');
        }
    });

    if (enlaceRecordar) {
        enlaceRecordar.addEventListener('click', (event) => {
            event.preventDefault();
            mostrarFeedback('Contacta al administrador para restablecer tu contraseña.', 'info');
        });
    }
}

// Ejecución al cargar el DOM
document.addEventListener('DOMContentLoaded', inicializarLogin);

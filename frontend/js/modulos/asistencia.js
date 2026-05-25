export function configurarAsistenciaPinpad() {
    const display = document.getElementById('asistencia-id-display');
    const botonesNum = document.querySelectorAll('.btn-num');
    const btnBorrar = document.getElementById('pinpad-borrar');
    const btnConfirmar = document.getElementById('pinpad-confirmar');
    const feedback = document.getElementById('asistencia-feedback');
    const btnAdminCont = document.getElementById('btnAdminCont'); //  Apuntamos directo a la ID 

    // Guardián: si no encuentra la pantalla o los botones de control, no arranca
    if (!display || !btnBorrar || !btnConfirmar) return;

    let cadenaId = "";
    const MAX_DIGITOS = 12;

    function actualizarPantalla() {
        display.value = cadenaId;
    }

    function mostrarFeedback(mensaje, tipo) {
        feedback.textContent = mensaje;
        feedback.className = `feedback-mensaje feedback-${tipo}`;
        setTimeout(() => {
            feedback.textContent = "";
            feedback.className = "feedback-mensaje";
        }, 4000);
    }

    // Configurar números virtuales (Clics con el mouse)
    botonesNum.forEach(boton => {
        boton.addEventListener('click', (e) => {
            e.preventDefault();
            const valor = boton.getAttribute('data-valor');
            if (cadenaId.length < MAX_DIGITOS && valor !== null) {
                cadenaId += valor;
                actualizarPantalla();
            }
        });
    });

    // Configurar botón borrar
    btnBorrar.addEventListener('click', (e) => {
        e.preventDefault();
        cadenaId = cadenaId.slice(0, -1);
        actualizarPantalla();
    });

    // Configurar botón OK (Envío de datos al Backend)
    btnConfirmar.addEventListener('click', async (e) => {
        e.preventDefault();
        if (cadenaId.trim() === "") {
            mostrarFeedback(" Por favor ingresa tu identificación", "error");
            return;
        }

       try {
            const respuesta = await fetch('http://localhost:8080/error/AsistenciaServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `documento_identidad=${encodeURIComponent(cadenaId)}`
            });

            // Capturamos el texto crudo primero para diagnosticar en caso de fallos inesperados
            const textoRespuesta = await respuesta.text();
            let resultado;
            
            try {
                resultado = JSON.parse(textoRespuesta);
            } catch (jsonError) {
                console.error("Texto recibido no es JSON válido:", textoRespuesta);
                mostrarFeedback("✘ Respuesta del servidor ilegible", "error");
                return;
            }

            if (respuesta.ok) {
                mostrarFeedback(` ${resultado.message}`, "exito");
            } else {
                mostrarFeedback(` ${resultado.message}`, "error");
            }

        } catch (error) {
            console.error("Error de conexión:", error);
            mostrarFeedback(" ✘ Error al conectar con el servidor", "error");
        }

        cadenaId = "";
        actualizarPantalla();
    });

    // Redirección limpia al Login de administración
    if (btnAdminCont) {
        btnAdminCont.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = '../html/login.html'; 
        });
    }
}
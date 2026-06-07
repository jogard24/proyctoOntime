export function configurarAsistenciaPinpad() {
    const display = document.getElementById('asistencia-id-display');
    const botonesNum = document.querySelectorAll('.btn-num');
    const btnBorrar = document.getElementById('pinpad-borrar');
    const btnConfirmar = document.getElementById('pinpad-confirmar');
    const feedback = document.getElementById('asistencia-feedback');
    const btnAdminCont = document.getElementById('btnAdminCont');

    if (!display || !btnBorrar || !btnConfirmar) {
        console.warn('No se pudo inicializar el Pinpad: faltan elementos del DOM.');
        return;
    }

    let cadenaId = "";
    const MAX_DIGITOS = 12;

    function actualizarPantalla() {
        display.value = cadenaId;
    }

    function mostrarFeedback(mensaje, tipo) {
        if (!feedback) return;
        feedback.textContent = mensaje;
        feedback.className = `feedback-mensaje feedback-${tipo}`;
        setTimeout(() => {
            feedback.textContent = "";
            feedback.className = "feedback-mensaje";
        }, 4000);
    }

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

    btnBorrar.addEventListener('click', (e) => {
        e.preventDefault();
        cadenaId = cadenaId.slice(0, -1);
        actualizarPantalla();
    });

    btnConfirmar.addEventListener('click', async (e) => {
        e.preventDefault();
        if (cadenaId.trim() === "") {
            mostrarFeedback(" Por favor ingresa tu identificación", "error");
            return;
        }

        try {
            // CORREGIDO: URL apuntando a PinpadServlet y parámetro usuarioId
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/PinpadServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: `usuarioId=${encodeURIComponent(cadenaId)}`
            });

            const textoRespuesta = await respuesta.text();
            let resultado;
            
            try {
                resultado = JSON.parse(textoRespuesta);
            } catch (jsonError) {
                console.error("Respuesta no es JSON:", textoRespuesta);
                mostrarFeedback("Error en la comunicación con el servidor", "error");
                return;
            }

            if (respuesta.ok) {
                // Asumimos que el JSON trae 'exito' y 'evento' o 'message'
                mostrarFeedback(resultado.message || `Registro exitoso: ${resultado.evento || 'Completado'}`, "exito");
            } else {
                mostrarFeedback(resultado.message || "Error al registrar", "error");
            }

        } catch (error) {
            console.error("Error de conexión:", error);
            mostrarFeedback("Error al conectar con el servidor", "error");
        }

        cadenaId = "";
        actualizarPantalla();
    });

    if (btnAdminCont) {
        btnAdminCont.addEventListener('click', (e) => {
            e.preventDefault();
            window.location.href = '../html/login.html'; 
        });
    }
}
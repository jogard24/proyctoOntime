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
            // Ajuste de variables: se añade el parámetro de acción compatible con request.getParameter
            const params = new URLSearchParams();
            params.append('accion', 'registrarAsistencia');
            params.append('documento', cadenaId); // Enviamos el documento/cédula digitada

            // const respuesta = await fetch('http://localhost:8080/OnTimeBackend/PinpadServlet', {
            const respuesta = await fetch('http://10.97.53.170:8080/OnTimeBackend/PinpadServlet', {
            // const respuesta = await fetch('http://10.5.225.13:8080/OnTimeBackend/PinpadServlet', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: params.toString()
            });

            const textoRespuesta = await respuesta.text();
            let resultado;
            
            try {
                resultado = JSON.parse(textoRespuesta);
            } catch (jsonError) {
                console.error("Respuesta no es JSON:", textoRespuesta);
                mostrarFeedback(" Error en la comunicación con el servidor OnTime", "error");
                return;
            }

            if (respuesta.ok) {
                // El backend procesará de forma automática si es Entrada o Salida según el histórico diario
                mostrarFeedback(resultado.message || `¡Registro exitoso! Evento: ${resultado.evento}`, "exito");
            } else {
                mostrarFeedback(resultado.message || " No se pudo completar el registro de marca.", "error");
            }

        } catch (error) {
            console.error("Error de conexión:", error);
            mostrarFeedback(" Error al conectar con el servidor principal.", "error");
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

export function configurarAsistenciaPinpad() {
    const display = document.getElementById('asistencia-id-display');
    const botonesNum = document.querySelectorAll('.btn-num');
    const btnBorrar = document.getElementById('pinpad-borrar');
    const btnConfirmar = document.getElementById('pinpad-confirmar');
    const feedback = document.getElementById('asistencia-feedback');

    // Guardián modular para evitar que falle si la vista no está cargada
    if (!display || !btnBorrar || !btnConfirmar) return;

    let cadenaId = "";
    const MAX_DIGITOS = 12; // Límite estándar para un documento de identidad

    function actualizarPantalla() {
        display.value = cadenaId;
    }

    function mostrarFeedback(mensaje, tipo) {
        feedback.textContent = mensaje;
        feedback.className = `feedback-mensaje feedback-${tipo}`;
        
        // Limpiar el mensaje después de 4 segundos
        setTimeout(() => {
            feedback.textContent = "";
            feedback.className = "feedback-mensaje";
        }, 4000);
    }

    // Evento para los botones numéricos (0-9)
    botonesNum.forEach(boton => {
        // Clonamos para mitigar la duplicación de Listeners de la carga dinámica
        const nuevoBoton = boton.cloneNode(true);
        boton.parentNode.replaceChild(nuevoBoton, boton);

        nuevoBoton.addEventListener('click', () => {
            if (cadenaId.length < MAX_DIGITOS) {
                cadenaId += nuevoBoton.getAttribute('data-valor');
                actualizarPantalla();
            }
        });
    });

    // Evento para el botón de borrar (retroceso)
    const nuevoBtnBorrar = btnBorrar.cloneNode(true);
    btnBorrar.parentNode.replaceChild(nuevoBtnBorrar, btnBorrar);
    nuevoBtnBorrar.addEventListener('click', () => {
        cadenaId = cadenaId.slice(0, -1);
        actualizarPantalla();
    });

    // Evento para el botón de confirmar (OK)
    const nuevoBtnConfirmar = btnConfirmar.cloneNode(true);
    btnConfirmar.parentNode.replaceChild(nuevoBtnConfirmar, btnConfirmar);
    nuevoBtnConfirmar.addEventListener('click', () => {
        if (cadenaId.trim() === "") {
            mostrarFeedback("Por favor ingresa tu identificación", "error");
            return;
        }

        // --- ENLACE FUTURO CON BACKEND (JAVA SERVLET) ---
        console.log(`Enviando ID al sistema OnTime: ${cadenaId}`);
        
        // Simulación temporal de respuesta exitosa del servidor
        mostrarFeedback(`Registro exitoso. ¡Buen día! ID: ${cadenaId}`, "exito");
        
        // Limpiamos el pinpad para el siguiente empleado
        cadenaId = "";
        actualizarPantalla();
    });
}
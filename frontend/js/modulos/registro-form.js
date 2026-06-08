export function configurarRegistro() {
    const inputFoto = document.getElementById('fotoInput');
    const vistaPrevia = document.getElementById('vistaPrevia');
    const btnGuardar = document.getElementById('btn-guardar-empleado');
    const nombreInput = document.getElementById('nombre');
    const apellidoInput = document.getElementById('apellido');
    const emailInput = document.getElementById('email');
    const celularInput = document.getElementById('celular');
    const direccionInput = document.getElementById('direccion');
    const tipoSangreInput = document.getElementById('tipoSangre');
    const nombreContactoInput = document.getElementById('nombreContacto');
    const celularContactoInput = document.getElementById('celularContacto');
    const relacionContactoInput = document.getElementById('relacionContacto');
    const documentoInput = document.getElementById('documento_identidad'); // Asegúrate de tener este input en tu HTML

    if (!btnGuardar || !inputFoto || !vistaPrevia) {
        console.warn('No se encontró el formulario de registro o sus elementos principales.');
        return;
    }

    inputFoto.addEventListener('change', function () {
        const archivo = inputFoto.files && inputFoto.files[0];
        if (!archivo) {
            vistaPrevia.src = '../img/usuario-defecto.png';
            return;
        }

        const lector = new FileReader();
        lector.onload = function (evento) {
            vistaPrevia.src = evento.target.result;
        };
        lector.readAsDataURL(archivo);
    });

    btnGuardar.addEventListener('click', async (event) => {
        event.preventDefault();

        const datosEmpleado = {
            nombre: nombreInput?.value.trim() || '',
            apellido: apellidoInput?.value.trim() || '',
            documento_identidad: documentoInput?.value.trim() || '',
            email: emailInput?.value.trim() || '',
            celular: celularInput?.value.trim() || '',
            direccion: direccionInput?.value.trim() || '',
            tipoSangre: tipoSangreInput?.value.trim() || '',
            nombreContacto: nombreContactoInput?.value.trim() || '',
            celularContacto: celularContactoInput?.value.trim() || '',
            relacionContacto: relacionContactoInput?.value.trim() || '',
        };

        const errores = validarDatos(datosEmpleado);
        if (errores.length > 0) {
            alert('Por favor corrige los siguientes campos:\n' + errores.join('\n'));
            return;
        }

        try {
            await enviarEmpleado(datosEmpleado, inputFoto.files && inputFoto.files[0]);
            alert('Empleado guardado correctamente.');
            limpiarFormulario();
        } catch (error) {
            console.error('Error guardando el empleado:', error);
            alert('No se pudo guardar el empleado. Revisa la consola para más detalles.');
        }
    });

    function validarDatos(datos) {
        const errores = [];
        if (!datos.nombre) errores.push('Nombre es obligatorio.');
        if (!datos.apellido) errores.push('Apellido es obligatorio.');
        if (!datos.documento_identidad) errores.push('Documento de identidad es obligatorio.');
        if (!datos.email) errores.push('Email es obligatorio.');
        if (!datos.celular) errores.push('Celular es obligatorio.');
        if (!datos.direccion) errores.push('Dirección es obligatoria.');
        if (!datos.tipoSangre) errores.push('Tipo de sangre es obligatorio.');
        if (!datos.nombreContacto) errores.push('Nombre del contacto es obligatorio.');
        if (!datos.celularContacto) errores.push('Celular del contacto es obligatorio.');
        if (!datos.relacionContacto) errores.push('Parentesco es obligatorio.');
        return errores;
    }

    async function enviarEmpleado(datos, archivoFoto) {
        const url = 'http://localhost:8080/OnTimeBackend/RegistroServlet';
        const formData = new FormData();

        Object.keys(datos).forEach((key) => {
            formData.append(key, datos[key]);
        });

        if (archivoFoto) {
            formData.append('fotoPerfil', archivoFoto);
        }

        if (window._contratoTemporal) {
            // En lugar de enviarlo como JSON string, lo "aplanamos" directamente en el FormData
            Object.keys(window._contratoTemporal).forEach((key) => {
                formData.append(key, window._contratoTemporal[key]);
            });
        } else {
            //en caso de no llenar el modal 
            console.warn("No se detectó ningún contrato temporal.");
        }


        const respuesta = await fetch(url, {
            method: 'POST',
            body: formData
        });

        if (!respuesta.ok) {
            throw new Error('Error al guardar el empleado en el backend.');
        }

        return respuesta.json();
    }

    function limpiarFormulario() {
        nombreInput.value = '';
        apellidoInput.value = '';
        emailInput.value = '';
        celularInput.value = '';
        direccionInput.value = '';
        tipoSangreInput.value = '';
        nombreContactoInput.value = '';
        celularContactoInput.value = '';
        relacionContactoInput.value = '';
        inputFoto.value = '';
        vistaPrevia.src = '../img/usuario-defecto.png';
    }

    // Inicializar módulo de contrato (modal) si existe la vista
    try {
        import('./contrato.js').then(mod => {
            if (mod && mod.configurarContrato) mod.configurarContrato();
        }).catch(err => {
            console.warn('No se pudo cargar el módulo de contrato:', err);
        });
    } catch (error) {
        console.warn('Import dinámico de contrato no soportado:', error);
    }
}

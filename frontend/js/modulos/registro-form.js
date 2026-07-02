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
    const documentoInput = document.getElementById('documento_identidad');

    if (!btnGuardar || !inputFoto || !vistaPrevia) {
        console.warn('No se encontró el formulario de registro o sus elementos principales.');
        return;
    }

    // =========================================================================
    // 🛡️ BLOQUEO FÍSICO EN TIEMPO REAL (Evita que el usuario escriba basura)
    // =========================================================================

    // Función para forzar solo letras y espacios en caliente
    const forzarSoloLetras = (input) => {
        if (input) {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/[^a-zA-ZáéíóúÁÉÍÓÚñÑ\s]/g, '');
            });
        }
    };

    // Función para forzar solo números en caliente
    const forzarSoloNumeros = (input) => {
        if (input) {
            input.addEventListener('input', () => {
                input.value = input.value.replace(/[^0-9]/g, '');
            });
        }
    };

    // Aplicamos los escuchadores de teclado en tiempo real
    forzarSoloLetras(nombreInput);
    forzarSoloLetras(apellidoInput);
    forzarSoloLetras(nombreContactoInput);
    forzarSoloLetras(relacionContactoInput);
    forzarSoloNumeros(documentoInput);
    forzarSoloNumeros(celularInput);
    forzarSoloNumeros(celularContactoInput);

    // =========================================================================

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
            alert('¡Empleado registrado con éxito en el sistema!');
            limpiarFormulario();
        } catch (error) {
            console.error('Error guardando el empleado:', error);
            alert('No se pudo guardar el empleado.');
        }
    });

    function validarDatos(datos) {
        const errores = [];

        // CORRECCIÓN CRÍTICA: Declaración explícita de expresiones regulares
        const regexLetras = /^[a-zA-ZáéíóúÁÉÍÓÚñÑ\s]+$/;
        const regexNumeros = /^[0-9]+$/;

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

        if (errores.length > 0) return errores;

        // 2. VALIDACIÓN DE FORMATO DE TEXTO (Filtro perimetral)
        if (!regexLetras.test(datos.nombre)) {
            errores.push('El campo Nombre solo debe contener letras.');
        }
        if (!regexLetras.test(datos.apellido)) {
            errores.push('El campo Apellido solo debe contener letras.');
        }
        if (!regexLetras.test(datos.nombreContacto)) {
            errores.push('El Nombre del Contacto de Emergencia solo debe contener letras.');
        }

        // 3. VALIDACIÓN DE NÚMEROS Y LONGITUDES
        if (!regexNumeros.test(datos.documento_identidad)) {
            errores.push('El Documento de Identidad debe contener únicamente números.');
        } else if (datos.documento_identidad.length < 6 || datos.documento_identidad.length > 10) {
            errores.push('El Documento de Identidad debe tener entre 6 y 10 dígitos.');
        }

        if (!regexNumeros.test(datos.celular)) {
            errores.push('El Celular del empleado debe contener únicamente números.');
        } else if (datos.celular.length !== 10) {
            errores.push('El Celular del empleado debe tener exactamente 10 dígitos.');
        }

        if (!regexNumeros.test(datos.celularContacto)) {
            errores.push('El Celular del contacto debe contener únicamente números.');
        } else if (datos.celularContacto.length !== 10) {
            errores.push('El Celular del contacto debe tener exactamente 10 dígitos.');
        }

  

        // 4. VALIDACIÓN DE FORMATO DE CORREO
        if (!datos.email.includes('@') || !datos.email.includes('.')) {
            errores.push('El formato del Email ingresado no es válido (ejemplo@dominio.com).');
        }

        return errores;
    }

    async function enviarEmpleado(datos, archivoFoto) {
        const url = 'http://localhost:8080/OnTimeBackend/FormularioRegistroServlet';
        const formData = new FormData();

        Object.keys(datos).forEach((key) => {
            formData.append(key, datos[key]);
        });

        if (archivoFoto) {
            formData.append('fotoPerfil', archivoFoto);
        }

        if (window._contratoTemporal) {
            Object.keys(window._contratoTemporal).forEach((key) => {
                formData.append(key, window._contratoTemporal[key]);
            });
        } else {
            alert("Error: Debe hacer clic en 'Agregar contrato' y rellenar los datos laborales antes de guardar al empleado.");
            throw new Error("Contrato laboral omitido.");
        }

        const respuesta = await fetch(url, {
            method: 'POST',
            body: formData
        });

        if (!respuesta.ok) {
            const textoError = await respuesta.text();
            throw new Error(`Error al guardar el empleado en el backend: ${textoError}`);
        }

        return await respuesta.text();
    }

    function limpiarFormulario() {
        if (nombreInput) nombreInput.value = '';
        if (apellidoInput) apellidoInput.value = '';
        if (emailInput) emailInput.value = '';
        if (celularInput) celularInput.value = '';
        if (direccionInput) direccionInput.value = '';
        if (tipoSangreInput) tipoSangreInput.value = '';
        if (nombreContactoInput) nombreContactoInput.value = '';
        if (celularContactoInput) celularContactoInput.value = '';
        if (relacionContactoInput) relacionContactoInput.value = '';
        if (documentoInput) documentoInput.value = '';
        inputFoto.value = '';
        vistaPrevia.src = '../img/usuario-defecto.png';
    }

    // Inicializar módulo de contrato (modal)
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



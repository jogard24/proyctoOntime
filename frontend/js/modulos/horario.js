export function configurarHorario() {
    const tablaBody = document.getElementById('tabla-horario-body');
    const btnNuevoHorario = document.getElementById('btn-nuevo-horario');
    const modal = document.getElementById('modalHorario');
    const tituloModal = document.getElementById('tituloModalHorario');
    const inputId = document.getElementById('horarioId');
    const inputEntrada = document.getElementById('horarioEntrada');
    const inputSalida = document.getElementById('horarioSalida');
    const selectTurno = document.getElementById('horarioTurno');
    const btnGuardar = document.getElementById('btn-guardar-horario');
    const btnCancelar = document.getElementById('btn-cancelar-horario');

    if (!tablaBody) {
        console.warn('No se pudo inicializar Horario: falta tabla.');
        return;
    }

    let listaHorarios = [];

    async function cargarHorarios() {
        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/HorarioServlet', {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (!respuesta.ok) throw new Error(`Error de servidor: ${respuesta.status}`);

            const data = await respuesta.json();
            listaHorarios = Array.isArray(data) ? data : [];
            renderizarTabla(listaHorarios);
        } catch (error) {
            console.error('Error cargando horarios:', error);
            tablaBody.innerHTML = `<tr><td colspan="5" style="text-align:center; color:red;">No se pudieron cargar los horarios.</td></tr>`;
        }
    }

    function renderizarTabla(items) {
        tablaBody.innerHTML = '';

        if (!items || items.length === 0) {
            tablaBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No hay horarios registrados.</td></tr>`;
            return;
        }

        items.forEach(horario => {
            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td>${horario.id || '-'}</td>
                <td>${horario.entrada || '-'}</td>
                <td>${horario.salida || '-'}</td>
                <td>${horario.turno || '-'}</td>
                <td>
                    <button class="btn-menu-item bt-ejecucion-editar" data-id="${horario.id}">Editar</button>
                    <button class="btn-menu-item bt-ejecucion-eliminar" data-id="${horario.id}">Eliminar</button>
                </td>
            `;

            tablaBody.appendChild(fila);
        });
    }

    async function eliminarHorario(id) {
        if (!confirm('¿Eliminar este horario?')) return;

        try {
            const respuesta = await fetch(`http://localhost:8080/OnTimeBackend/HorarioServlet?id=${id}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!respuesta.ok) throw new Error(`Error eliminando horario: ${respuesta.status}`);

            await cargarHorarios();
            alert('Horario eliminado correctamente.');

        } catch (error) {
            console.error('Error eliminando horario:', error);
            alert('No se pudo eliminar el horario.');
        }
    }

    function abrirModal(horario = null) {
        inputId.value = horario?.id || '';
        inputEntrada.value = horario?.entrada || '';
        inputSalida.value = horario?.salida || '';
        selectTurno.value = horario?.turno || 'diurno';
        tituloModal.textContent = horario ? 'Editar horario' : 'Nuevo horario';
        modal.style.display = 'flex';
    }

    function cerrarModal() {
        modal.style.display = 'none';
    }

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

    async function guardarHorario() {

        const id = inputId.value;
        const datos = {
            entrada: inputEntrada.value,
            salida: inputSalida.value,
            turno: selectTurno.value
        };

        if (inputEntrada.value === '' || inputSalida.value === '') {
            mostrarFeedback('Por favor completa todos los campos.', 'error');
            return;
        }

        const metodo = id ? 'PUT' : 'POST';
        if (id) datos.id = id;

        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/HorarioServlet', {
                method: metodo,
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(datos)
            });

            if (!respuesta.ok) throw new Error(`Error guardando horario: ${respuesta.status}`);

            await cargarHorarios();
            cerrarModal();
            alert(id ? 'Horario actualizado correctamente.' : 'Horario agregado correctamente.');
        } catch (error) {
            console.error('Error guardando horario:', error);
            alert('No se pudo guardar el horario.');
        }
    }

    tablaBody.addEventListener('click', (event) => {
        const boton = event.target.closest('button');
        if (!boton) return;

        const id = boton.dataset.id;
        if (!id) return;

        if (boton.classList.contains('bt-ejecucion-eliminar')) {
            eliminarHorario(id);
            return;
        }

        if (boton.classList.contains('bt-ejecucion-editar')) {
            const horarioSeleccionado = listaHorarios.find(item => String(item.id) === String(id));
            if (!horarioSeleccionado) return;
            abrirModal(horarioSeleccionado);
        }
    });

    btnNuevoHorario?.addEventListener('click', (event) => {
        event.preventDefault();
        abrirModal();
    });

    btnGuardar?.addEventListener('click', (event) => {
        event.preventDefault();
        guardarHorario();
    });

    btnCancelar?.addEventListener('click', (event) => {
        event.preventDefault();
        cerrarModal();
    });

    window.cerrarModalHorario = cerrarModal;

    cargarHorarios();
}
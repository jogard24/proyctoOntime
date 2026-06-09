export function configurarHorario() {
    const tablaBody = document.getElementById('tabla-horario-body');
    const btnNuevoHorario = document.getElementById('btn-nuevo-horario');
    const modal = document.getElementById('modalHorario');
    const tituloModal = document.getElementById('tituloModalHorario');

    // Elementos del DOM ajustados a la nueva nomenclatura
    const inputId = document.getElementById('horarioId');
    const inputEntrada = document.getElementById('horarioEntrada');
    const inputSalida = document.getElementById('horarioSalida');
    const selectNombreJornada = document.getElementById('horarioNombreJornada');

    const btnGuardar = document.getElementById('btn-guardar-horario');
    const btnCancelar = document.getElementById('btn-cancelar-horario');

    if (!tablaBody) {
        console.warn('No se pudo inicializar Horario: falta tabla.');
        return;
    }

    let listaHorarios = [];

    // --- LÓGICA DE CARGA ---
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

    // --- RENDEREADO DE TABLA (Alineado a Ontime3BD) ---
    function renderizarTabla(items) {
        tablaBody.innerHTML = '';

        if (!items || items.length === 0) {
            tablaBody.innerHTML = `<tr><td colspan="5" style="text-align:center;">No hay horarios registrados.</td></tr>`;
            return;
        }

        items.forEach(horario => {
            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td><strong>${horario.id || '-'}</strong></td>
                <td>${horario.nombrejornada || '-'}</td>
                <td>${horario.horaEntrada || '-'}</td>
                <td>${horario.horaSalida || '-'}</td>
                <td>
                    <button class="btn-menu-item bt-ejecucion-editar" data-id="${horario.id}">Editar</button>
                    <button class="btn-menu-item bt-ejecucion-eliminar" data-id="${horario.id}">Eliminar</button>
                </td>
            `;
            tablaBody.appendChild(fila);
        });
    }

    // --- ELIMINACIÓN DIRECTA COMPATIBLE CON POST ---
    async function eliminarHorario(id) {
        if (!confirm('¿Está seguro de eliminar esta jornada laboral de forma permanente?')) return;

        const params = new URLSearchParams();
        params.append('accion', 'eliminar');
        params.append('id', id);

        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/HorarioServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params,
                credentials: 'include'
            });

            if (!respuesta.ok) throw new Error(`Error al eliminar`);
            await cargarHorarios();
            alert('Horario eliminado correctamente.');
        } catch (error) {
            console.error('Error eliminando horario:', error);
            alert('No se pudo eliminar el horario. Verifique que no esté enlazado a ningún contrato activo.');
        }
    }

    // --- CONTROL DE MODAL (Nuevo / Editar - Ajuste 3) ---
    function abrirModal(horario = null) {
        if (horario) {
            tituloModal.textContent = 'Editar horario';
            inputId.value = horario.id;
            inputId.disabled = true; // Bloqueamos ID en edición (Cumple restricción RF25)
            selectNombreJornada.value = horario.nombrejornada || 'Turno Diurno';
            inputEntrada.value = horario.horaEntrada || '';
            inputSalida.value = horario.horaSalida || '';
        } else {
            tituloModal.textContent = 'Nuevo horario';
            inputId.value = '';
            inputId.disabled = false; // Habilitado para ingresar el ID de forma manual
            selectNombreJornada.value = 'Turno Diurno';
            inputEntrada.value = '';
            inputSalida.value = '';
        }
        modal.style.display = 'flex';
    }

    function cerrarModal() {
        modal.style.display = 'none';
    }

    // --- ACCIÓN DE GUARDADO MODULAR ---
    async function guardarHorario() {
        if (inputId.value === '' || inputEntrada.value === '' || inputSalida.value === '') {
            alert('Por favor completa todos los campos del formulario.');
            return;
        }

        // Determinamos la acción para orientar de forma directa al Servlet
        const esEdicion = inputId.disabled;

        const params = new URLSearchParams();
        params.append('accion', esEdicion ? 'actualizar' : 'crear');
        params.append('id', inputId.value);
        params.append('nombrejornada', selectNombreJornada.value);
        params.append('horaEntrada', inputEntrada.value);
        params.append('horaSalida', inputSalida.value);

        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/HorarioServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params,
                credentials: 'include'
            });

            if (!respuesta.ok) throw new Error('Error al procesar el guardado');

            await cargarHorarios();
            cerrarModal();
            alert(esEdicion ? 'Horario actualizado correctamente.' : 'Horario agregado correctamente.');
        } catch (error) {
            console.error('Error guardando horario:', error);
            alert('No se pudo guardar el horario en el servidor.');
        }
    }

    // --- MANEJADORES DE EVENTOS DE INTERFAZ ---
    tablaBody.addEventListener('click', (event) => {
        const boton = event.target.closest('button');
        if (!boton) return;

        const id = boton.dataset.id;
        if (!id) return;

        if (boton.classList.contains('bt-ejecucion-eliminar')) {
            eliminarHorario(id);
        } else if (boton.classList.contains('bt-ejecucion-editar')) {
            const horarioSeleccionado = listaHorarios.find(item => String(item.id) === String(id));
            if (horarioSeleccionado) abrirModal(horarioSeleccionado);
        }
    });

    btnNuevoHorario?.addEventListener('click', (e) => { e.preventDefault(); abrirModal(); });
    btnGuardar?.addEventListener('click', (e) => { e.preventDefault(); guardarHorario(); });
    btnCancelar?.addEventListener('click', (e) => { e.preventDefault(); cerrarModal(); });

    // Carga inicial al montar el módulo
    cargarHorarios();
}

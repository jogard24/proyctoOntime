
export function configurarPermisos() {
	console.log('Módulo permisos cargado.');

	const tablaBody = document.querySelector('.panel-permisos table tbody');
	const btnNueva = document.getElementById('btn-nueva-solicitud');

	if (!tablaBody) return;

	let listaPermisos = [];

	// Carga inicial de datos desde el backend
	async function cargarPermisos() {
		try {
			const res = await fetch('http://localhost:8080/OnTimeBackend/PermisoServlet', { method: 'GET', credentials: 'include' });
			if (!res.ok) throw new Error('Error en servidor permisos');
			const data = await res.json();
			listaPermisos = Array.isArray(data) ? data : [];
			renderizarTabla(listaPermisos);
		} catch (err) {
			console.error('No se pudieron cargar permisos:', err);
			tablaBody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:red;">Error cargando permisos.</td></tr>`;
		}
	}

	// Render de la tabla
	function renderizarTabla(items) {
		tablaBody.innerHTML = '';
		if (!items || items.length === 0) {
			tablaBody.innerHTML = `<tr><td colspan="7" style="text-align:center;">No hay solicitudes de permiso.</td></tr>`;
			return;
		}

		items.forEach(p => {
			const tr = document.createElement('tr');
			const estadoLabel = etiquetaEstado(p.estado);

			tr.innerHTML = `
				<td>${p.nombreEmpleado || p.empleado || '-'}</td>
				<td>${p.tipo || '-'}</td>
				<td>${p.fechaSolicitud || '-'}</td>
				<td>${p.desde || '-'}</td>
				<td>${p.hasta || '-'}</td>
				<td><span class="status-pill ${estadoLabel.clase}">${estadoLabel.texto}</span></td>
				<td>
					<div class="acciones-flex">
						<button class="btn-menu-item bt-ejecucion-editar" data-id="${p.id}">✔</button>
						<button class="btn-menu-item bt-ejecucion-eliminar" data-id="${p.id}">✘</button>
					</div>
				</td>
			`;

			tablaBody.appendChild(tr);
		});
	}

	function etiquetaEstado(estado) {
		const e = (estado || '').toLowerCase();
		if (e === 'aprobado' || e === 'aceptado') return { texto: 'Aprobado', clase: 'green' };
		if (e === 'rechazado' || e === 'denegado') return { texto: 'Rechazado', clase: 'red' };
		return { texto: 'Pendiente', clase: 'yellow' };
	}

	// Crear nueva solicitud (interfaz simple por prompt)
	async function crearSolicitud() {
		const empleado = prompt('Nombre o ID del empleado:');
		if (!empleado) return;
		const tipo = prompt('Tipo de permiso (vacaciones, personal, enfermedad):');
		if (!tipo) return;
		const desde = prompt('Fecha desde (YYYY-MM-DD):');
		const hasta = prompt('Fecha hasta (YYYY-MM-DD):');

		const nueva = { empleado, tipo, desde, hasta };

		try {
			const res = await fetch('http://localhost:8080/OnTimeBackend/PermisoServlet', {
				method: 'POST',
				credentials: 'include',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(nueva)
			});
			if (!res.ok) throw new Error('Error creando solicitud');
			await cargarPermisos();
			alert('Solicitud enviada.');
		} catch (err) {
			console.error('Error creando solicitud:', err);
			alert('No se pudo enviar la solicitud.');
		}
	}

	// Manejo de acciones (aprobar / rechazar / eliminar)
	tablaBody.addEventListener('click', async (ev) => {
		const boton = ev.target.closest('button');
		if (!boton) return;
		const id = boton.dataset.id;

		if (boton.classList.contains('bt-ejecucion-editar')) {
			// Cambiar estado (simple flujo: aprobar)
			if (!confirm('¿Aprobar esta solicitud?')) return;
			try {
				const res = await fetch(`http://localhost:8080/OnTimeBackend/PermisoServlet?id=${id}&accion=aprobar`, { method: 'POST', credentials: 'include' });
				if (!res.ok) throw new Error('Error al aprobar');
				await cargarPermisos();
			} catch (err) {
				console.error('Error aprobando solicitud:', err);
				alert('No se pudo aprobar la solicitud.');
			}
		}

		if (boton.classList.contains('bt-ejecucion-eliminar')) {
			if (!confirm('¿Eliminar / rechazar esta solicitud?')) return;
			try {
				const res = await fetch(`http://localhost:8080/OnTimeBackend/PermisoServlet?id=${id}`, { method: 'DELETE', credentials: 'include' });
				if (!res.ok) throw new Error('Error eliminando');
				await cargarPermisos();
			} catch (err) {
				console.error('Error eliminando solicitud:', err);
				alert('No se pudo eliminar la solicitud.');
			}
		}
	});

	if (btnNueva) btnNueva.addEventListener('click', (e) => { e.preventDefault(); crearSolicitud(); });

	// Inicializar carga
	cargarPermisos();
}

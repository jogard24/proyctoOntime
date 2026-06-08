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
				<td>${p.nombreEmpleado || 'Desconocido'}</td>
				<td>${p.tipoPermiso || '-'}</td>
				<td>${p.fechaAsignacion || '-'}</td>
				<td>${p.fechaInicio || '-'}</td>
				<td>${p.fechaFin || '-'}</td>
				<td><span class="status-pill ${estadoLabel.clase}">${estadoLabel.texto}</span></td>
				<td>
					<div class="acciones-flex">
						<!-- Deshabilitamos acciones si el permiso ya no está pendiente (Inmutabilidad RF14) -->
						<button class="btn-menu-item bt-ejecucion-editar" data-id="${p.id}" ${p.estado !== 'pendiente' ? 'disabled style="opacity:0.5;"' : ''}>✔</button>
						<button class="btn-menu-item bt-ejecucion-eliminar" data-id="${p.id}" ${p.estado !== 'pendiente' ? 'disabled style="opacity:0.5;"' : ''}>✘</button>
					</div>
				</td>
			`;

			tablaBody.appendChild(tr);
		});
	}

	function etiquetaEstado(estado) {
		const e = (estado || '').toLowerCase();
		if (e === 'aprobado') return { texto: 'Aprobado', clase: 'green' };
		if (e === 'rechazado') return { texto: 'Rechazado', clase: 'red' };
		return { texto: 'Pendiente', clase: 'yellow' };
	}

	// Crear nueva solicitud (Alineada a parámetros planos compatibles con Java Puro)
	async function crearSolicitud() {
		const documento = prompt('Ingrese el Documento de Identidad (Cédula) del empleado:');
		if (!documento) return;
		const tipoPermiso = prompt('Tipo de permiso (Ej: Médico, Calamidad, Vacaciones):');
		if (!tipoPermiso) return;
		const fechaInicio = prompt('Fecha de inicio (YYYY-MM-DD):');
		if (!fechaInicio) return;
		const fechaFin = prompt('Fecha de fin (YYYY-MM-DD):');
		if (!fechaFin) return;

		// --- REGLA DE NEGOCIO CRÍTICA (Cumple RF12) ---
		// Validar que la fecha de inicio sea al menos para el día de mañana
		const hoy = new Date();
		hoy.setHours(0,0,0,0);
		const fechaInicioCompara = new Date(fechaInicio + 'T00:00:00');
		
		if (fechaInicioCompara <= hoy) {
            alert("Operación denegada: El sistema solo acepta solicitudes de permiso con al menos un día de anticipación.");
            return;
		}

		// Convertimos a URLSearchParams para lectura directa mediante request.getParameter en Java
		const params = new URLSearchParams();
		params.append('accion', 'crear');
		params.append('documento', documento);
		params.append('tipoPermiso', tipoPermiso);
		params.append('fechaInicio', fechaInicio);
		params.append('fechaFin', fechaFin);

		try {
			const res = await fetch('http://localhost:8080/OnTimeBackend/PermisoServlet', {
				method: 'POST',
				credentials: 'include',
				headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
				body: params
			});
			if (!res.ok) throw new Error('Error creando solicitud');
			await cargarPermisos();
			alert('Solicitud de permiso registrada con estado pendiente.');
		} catch (err) {
			console.error('Error creando solicitud:', err);
			alert('No se pudo enviar la solicitud. Verifique la cédula del empleado.');
		}
	}

	// Manejo de acciones coordinado mediante métodos POST tradicionales (Aprobar / Rechazar - RF13)
	tablaBody.addEventListener('click', async (ev) => {
		const boton = ev.target.closest('button');
		if (!boton) return;
		const id = boton.dataset.id;

		if (boton.classList.contains('bt-ejecucion-editar')) {
			if (!confirm('¿Desea aprobar formalmente esta solicitud de permiso?')) return;
			
			const params = new URLSearchParams();
			params.append('accion', 'aprobar');
			params.append('id', id);

			try {
				const res = await fetch('http://localhost:8080/OnTimeBackend/PermisoServlet', { 
					method: 'POST', 
					credentials: 'include',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: params
				});
				if (!res.ok) throw new Error('Error al aprobar');
				await cargarPermisos();
			} catch (err) {
				console.error('Error aprobando solicitud:', err);
				alert('No se pudo aprobar la solicitud.');
			}
		}

		if (boton.classList.contains('bt-ejecucion-eliminar')) {
			if (!confirm('¿Desea rechazar esta solicitud de permiso?')) return;
			
			const params = new URLSearchParams();
			params.append('accion', 'rechazar');
			params.append('id', id);

			try {
				const res = await fetch('http://localhost:8080/OnTimeBackend/PermisoServlet', { 
					method: 'POST', 
					credentials: 'include',
					headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
					body: params
				});
				if (!res.ok) throw new Error('Error al rechazar');
				await cargarPermisos();
			} catch (err) {
				console.error('Error rechazando solicitud:', err);
				alert('No se pudo rechazar la solicitud.');
			}
		}
	});

	if (btnNueva) btnNueva.addEventListener('click', (e) => { e.preventDefault(); crearSolicitud(); });

	// Inicializar carga
	cargarPermisos();
}


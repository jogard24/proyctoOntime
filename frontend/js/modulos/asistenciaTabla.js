export function configurarAsistenciaTabla() {
    const tablaBody = document.getElementById('tabla-asistencia-body');
    const fechaDesde = document.getElementById('filtro-fecha-desde');
    const fechaHasta = document.getElementById('filtro-fecha-hasta');
    const filtroEmpleado = document.getElementById('filtro-empleado'); // Captura el nuevo buscador 
    const btnFiltrar = document.getElementById('btn-filtrar-asistencia');

    if (!tablaBody || !fechaDesde || !fechaHasta || !btnFiltrar) {
        console.warn('No se pudo inicializar Asistencia: faltan elementos del DOM.');
        return;
    }

    let listaAsistencias = [];

    btnFiltrar.addEventListener('click', (event) => {
        event.preventDefault();
        filtrarTabla();
    });

    cargarAsistencia();

    async function cargarAsistencia() {
        const url = 'http://localhost:8080/OnTimeBackend/AsistenciaServlet';

        try {
            const respuesta = await fetch(url, {
                method: 'GET',
                headers: { 'Accept': 'application/json' }
            });

            if (!respuesta.ok) throw new Error(`Error en el servidor: ${respuesta.status}`);

            const data = await respuesta.json();
            listaAsistencias = Array.isArray(data) ? data : [];
            renderizarTabla(listaAsistencias);
        } catch (error) {
            console.error('Error cargando el reporte:', error);
            // Se actualizó a 6 columnas para que coincida con el nuevo HTML
            tablaBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:red;">Error al cargar datos.</td></tr>`;
        }
    }

    function renderizarTabla(asistencias) {
        tablaBody.innerHTML = '';

        if (asistencias.length === 0) {
            tablaBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No hay registros coincidentes.</td></tr>`;
            return;
        }

        asistencias.forEach(reg => {
            const fila = document.createElement('tr');

            // Evaluamos la clase del badge para Entrada o Salida de forma estética
            const badgeClase = reg.tipoEvento === 'entrada' ? 'verde' : 'azul';

            // Inyectamos las 6 columnas exactas alineadas con la nueva BD Ontime3BD
            fila.innerHTML = `
                <td><strong>${reg.documentoIdentidad || 'N/A'}</strong></td>
                <td>${reg.nombreEmpleado || 'Desconocido'}</td>
                <td>${reg.fechaHora || '-'}</td>
                <td><span class="pill ${badgeClase}">${reg.tipoEvento.toUpperCase()}</span></td>
                <td>${reg.nombreJornada || 'General'}</td>
                <td>${reg.observacion || 'Ninguna'}</td>
            `;
            tablaBody.appendChild(fila);
        });
    }

    function filtrarTabla() {
        const desde = fechaDesde.value;
        const hasta = fechaHasta.value;
        const buscador = filtroEmpleado?.value.toLowerCase().trim() || '';

        // ---  ---
        if (desde && hasta && desde > hasta) {
            alert("Operación inválida: La 'Fecha Desde' no puede ser mayor o posterior a la 'Fecha Hasta'. Por favor, corrige las fechas.");
            return;
        }

        const resultados = listaAsistencias.filter(reg => {
            // 1. Filtrar por rango de fechas
            const fechaRegistro = reg.fechaHora ? reg.fechaHora.split(' ')[0] : '';
            if (desde && fechaRegistro < desde) return false;
            if (hasta && fechaRegistro > hasta) return false;

            // 2. Filtrar por buscador de empleado (Cédula o Nombre) (RF15)
            if (buscador) {
                const nombre = reg.nombreEmpleado ? reg.nombreEmpleado.toLowerCase() : '';
                const cedula = reg.documentoIdentidad ? reg.documentoIdentidad.toLowerCase() : '';
                if (!nombre.includes(buscador) && !cedula.includes(buscador)) {
                    return false;
                }
            }
            return true;
        });

        renderizarTabla(resultados);
    }
}

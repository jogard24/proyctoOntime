export function configurarAsistenciaTabla() {
    const tablaBody = document.getElementById('tabla-asistencia-body');
    const fechaDesde = document.getElementById('filtro-fecha-desde');
    const fechaHasta = document.getElementById('filtro-fecha-hasta');
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
            tablaBody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:red;">Error al cargar datos.</td></tr>`;
        }
    }



    function renderizarTabla(asistencias) {
        tablaBody.innerHTML = '';

        if (asistencias.length === 0) {
            tablaBody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No hay registros.</td></tr>`;
            return;
        }

        asistencias.forEach(reg => {
            // Separamos fecha y hora del string "YYYY-MM-DD HH:MM:SS"
            const [fecha, horaCompleta] = reg.fechaHora ? reg.fechaHora.split(' ') : ['', ''];

            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td>${reg.nombreEmpleado}</td>
                <td>${fecha}</td>
                <td>${reg.tipoEvento === 'entrada' ? horaCompleta : '-'}</td>
                <td>${reg.tipoEvento === 'salida' ? horaCompleta : '-'}</td>
                <td>${reg.observacion || '-'}</td>
                <td><span class="pill ${claseEstado(reg.tipoEvento)}">${reg.tipoEvento}</span></td>
            `;
            tablaBody.appendChild(fila);
        });
    }

    function filtrarTabla() {
        const desde = fechaDesde.value;
        const hasta = fechaHasta.value;

        const resultados = listaAsistencias.filter(reg => {
            const fechaRegistro = reg.fechaHora.split(' ')[0];
            if (desde && fechaRegistro < desde) return false;
            if (hasta && fechaRegistro > hasta) return false;
            return true;
        });

        renderizarTabla(resultados);
    }

    function claseEstado(tipo) {
        return tipo === 'entrada' ? 'verde' : 'amarillo';
    }
}

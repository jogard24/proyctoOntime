// 🚀 ASÍ DEBE QUEDAR TU ARCHIVO planilla.js EN NETBEANS
export function inicializarPlanilla() {
    const contenedorDias = document.getElementById('cabecera-planilla-dias');
    const cuerpoDatos = document.getElementById('cuerpo-planilla-datos');
    const filtroPeriodo = document.getElementById('filtro-periodo-planilla');
    const btnBuscar = document.getElementById('btn-buscar-planilla');

    // 1. Inyecta dinámicamente las cabeceras de los días del 1 al 31 en la tabla HTML
    // 🚀 ALINEACIÓN MILIMÉTRICA EN LA CABECERA (planilla.js)
    function generarCabeceraDias() {
        const thsExistentes = contenedorDias.querySelectorAll('.th-dia-dinamico');
        thsExistentes.forEach(th => th.remove());

        for (let dia = 1; dia <= 31; dia++) {
            const th = document.createElement('th');
            th.className = 'th-dia-dinamico';

            // 🛡️ REGLA DE ORO: Forzamos la misma caja de renderizado de abajo
            th.style.boxSizing = 'border-box';
            th.style.minWidth = '34px';
            th.style.width = '34px'; // Ancho estricto absoluto
            th.style.padding = '6px 0'; // Padding controlado simétrico
            th.style.margin = '0';
            th.style.textAlign = 'center';
            th.style.verticalAlign = 'middle';

            // Usamos un color de borde idéntico y limpio para la cuadrícula superior
            th.style.borderLeft = '1px solid #dee2e6';
            th.style.borderBottom = '2px solid #dee2e6';

            th.innerHTML = `<div style="font-size: 10px; text-transform: uppercase; font-weight: normal; opacity: 0.8; margin-bottom: 1px; line-height: 1;">Día</div><strong style="font-size: 15px; line-height: 1;">${dia}</strong>`;
            contenedorDias.appendChild(th);
        }
    }


    // 2. Procesa la matriz del JSON y pinta las celdas verdes (✔) o rojas (F)
    function renderizarMalla(empleados) {
        cuerpoDatos.innerHTML = '';

        if (!empleados || empleados.length === 0) {
            cuerpoDatos.innerHTML = `<tr><td colspan="34" style="padding:20px; text-align:center; font-family:sans-serif; font-weight:bold; color:#666;">No hay registros de asistencia para este periodo.</td></tr>`;
            return;
        }

        empleados.forEach((emp, index) => {
            const fila = document.createElement('tr');
            fila.style.borderBottom = '1px solid #dee2e6';

            //  Estilizado de las columnas de texto base
            let htmlFila = `
                <td style="padding: 15px 10px; font-weight: bold; background: #f8f9fa; color: #000000; border-right: 1px solid #ffffff;">${index + 1}</td>
                <td style="text-align: left; padding: 12px 15px; font-weight: bold; color: #000000; text-transform: uppercase; font-size: 12px; border-right: 1px solid #dee2e6; min-width: 180px;">${emp.nombre} ${emp.apellido}</td>
                <td style="color: #000000; font-weight: 600; font-size: 11px; text-transform: uppercase; padding: 12px 10px; border-right: 1px solid #dee2e6; min-width: 130px;">${emp.cargo}</td>
            `;

            // Bucle de renderizado para los 31 días calendarios
            //  AJUSTA EL BUCLE FOR ADENTRO DE TU FUNCIÓN renderizarMalla EN planilla.js
            //  BUCLE DE RENDERIZADO EN TU planilla.js
            for (let dia = 1; dia <= 31; dia++) {
                const estadoDia = emp.diasResumen[`dia_${dia}`] || 0;

                if (estadoDia === 1) {
                    // ✔ CASILLA VERDE: Asistencia Normal
                    htmlFila += `
                        <td style="box-sizing: border-box; background: #28a745; color: white; font-weight: bold; font-size: 13px; border-left: 1px solid #dee2e6; padding: 0; min-width: 35px; width: 35px; height: 38px; text-align: center; vertical-align: middle;">
                            <div style="display: flex; align-items: center; justify-content: center; height: 100%; width: 100%;">✔</div>
                        </td>`;
                } else if (estadoDia === 2) {
                    // 📅 CASILLA AMARILLA PREMIUM: Permiso Autorizado (P)
                    htmlFila += `
                        <td style="box-sizing: border-box; background: #ffc107; color: #212529; font-weight: bold; font-size: 12px; border-left: 1px solid #dee2e6; padding: 0; min-width: 35px; width: 35px; height: 38px; text-align: center; vertical-align: middle;" title="Permiso / Novedad Aprobada">
                            <div style="display: flex; align-items: center; justify-content: center; height: 100%; width: 100%;">P</div>
                        </td>`;
                } else {
                    // 🟥 CASILLA ROJA: Falta o Inasistencia Injustificada
                    htmlFila += `
                        <td style="box-sizing: border-box; background: #dc3545; color: white; font-weight: bold; font-size: 11px; border-left: 1px solid #dee2e6; padding: 0; min-width: 35px; width: 35px; height: 38px; text-align: center; vertical-align: middle;">
                            <div style="display: flex; align-items: center; justify-content: center; height: 100%; width: 100%;">F</div>
                        </td>`;
                }
            }



            fila.innerHTML = htmlFila;
            cuerpoDatos.appendChild(fila);
        });
    }


    // 3. Consulta asíncrona real hacia el PlanillaServlet
    async function consultarPlanilla() {
        generarCabeceraDias();
        const periodoSeleccionado = filtroPeriodo ? filtroPeriodo.value : '2026-06';

        try {
            cuerpoDatos.innerHTML = `<tr><td colspan="34" style="padding:20px; text-align:center; color:#666;">Procesando matriz de marcas de MySQL...</td></tr>`;

            const respuesta = await fetch(`http://localhost:8080/OnTimeBackend/PlanillaServlet?periodo=${periodoSeleccionado}`, {
                method: 'GET',
                credentials: 'include',
                headers: { 'Accept': 'application/json' }
            });

            if (!respuesta.ok) throw new Error(`Error: ${respuesta.status}`);

            const data = await respuesta.json();
            const empleados = Array.isArray(data) ? data : [];
            renderizarMalla(empleados);
        } catch (error) {
            console.error('Error al consultar planilla:', error);
            cuerpoDatos.innerHTML = `<tr><td colspan="34" style="text-align:center; color:red; font-weight:bold; padding:20px;">No se pudo conectar con el PlanillaServlet. Verifique que el Tomcat esté encendido.</td></tr>`;
        }
    }

    // Enlazamos el evento del botón de buscar que está adentro de tu planilla.html
    btnBuscar?.addEventListener('click', consultarPlanilla);
    consultarPlanilla(); // Carga automática inicial al abrir la sección
}



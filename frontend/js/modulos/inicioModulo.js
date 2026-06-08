const API_URL = "http://localhost:8080/OnTimeBackend";

export async function configurarInicioDashboard() {
    // 1. Personalizar el texto de bienvenida con el nombre real de la sesión (¡Excelente!)
    const sesionStr = sessionStorage.getItem("usuarioActual");
    if (sesionStr) {
        try {
            const usuario = JSON.parse(sesionStr);
            const txtBienvenida = document.getElementById("txt-bienvenida-admin");
            if (txtBienvenida && usuario.nombre) {
                txtBienvenida.textContent = `¡Bienvenido ${usuario.nombre}!`;
            }
        } catch (e) {
            console.warn("No se pudo parsear el usuario de la sesión.");
        }
    }

    // 2. Cargar los datos inmediatamente desde MySQL
    await consultarNovedadesInicio();

    // 3. Asignar el botón de actualizar manual de la sección de inicio
    const btnActualizar = document.getElementById("btn-actualizar-inicio");
    if (btnActualizar) {
        btnActualizar.addEventListener("click", (e) => {
            e.preventDefault();
            consultarNovedadesInicio();
        });
    }
}

async function consultarNovedadesInicio() {
    const tbody = document.getElementById("tbody-novedades-inicio");
    if (!tbody) return;

    try {
        // Apuntamos al Servlet modular que procesa el listado general del día
        const respuesta = await fetch(`${API_URL}/AsistenciaServlet?vista=novedadesHome`);

        if (!respuesta.ok) throw new Error("Error en la respuesta del servidor.");

        const registros = await respuesta.json();

        if (registros.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 20px;">No hay movimientos registrados el día de hoy.</td></tr>`;
            
            // Si no hay marcas, dejamos las tarjetas en cero
            document.getElementById("stat-total").textContent = "0";
            document.getElementById("stat-retardos").textContent = "0";
            document.getElementById("stat-salidas").textContent = "0";
            return;
        }

        // Tu lógica analítica estrella (¡No la pierdas!)
        let empleadosActivos = new Set();
        let totalRetardos = 0;
        let totalSalidas = 0;

        tbody.innerHTML = "";

        registros.forEach(reg => {
            const tipoEvento = reg.tipoEvento ? reg.tipoEvento.toLowerCase() : '';
            const observacion = reg.observacion ? reg.observacion.toLowerCase() : '';
            const nombreEmp = reg.nombreEmpleado || 'Desconocido';

            // Si el evento es una entrada, lo sumamos al Set único (RF09)
            if (tipoEvento === "entrada") {
                empleadosActivos.add(nombreEmp);
            }
            // Conteo de retardos
            if (observacion.includes("retardo")) {
                totalRetardos++;
            }
            // Conteo de salidas
            if (tipoEvento === "salida") {
                totalSalidas++;
            }

            const fila = document.createElement("tr");

            // Si tiene retardo, le aplicamos tu estilo sutil de alerta visual
            if (observacion.includes("retardo")) {
                fila.style.backgroundColor = "rgba(255, 77, 77, 0.08)";
            }

            // Sincronizado uno a uno con el nuevo HTML y Ontime3BD
            fila.innerHTML = `
                <td><strong>${reg.usuarioId || reg.documentoIdentidad || '-'}</strong></td>
                <td>${nombreEmp}</td>
                <td>
                    <span style="padding: 4px 8px; border-radius: 4px; font-size: 0.85rem; font-weight: bold;
                        background-color: ${tipoEvento === 'entrada' ? '#e8f5e9' : '#e3f2fd'};
                        color: ${tipoEvento === 'entrada' ? '#2e7d32' : '#1565c0'}; display: inline-block;">
                        ${tipoEvento.toUpperCase()}
                    </span>
                </td>
                <td>${reg.nombreJornada || 'General'}</td>
                <td>${reg.fechaHora || '-'}</td>
                <td style="font-weight: ${observacion.includes('retardo') ? 'bold' : 'normal'};
                           color: ${observacion.includes('retardo') ? '#ff4d4d' : '#ffffff'}">
                    ${reg.observacion || 'Ninguna'}
                </td>
            `;
            tbody.appendChild(fila);
        });

        // Tu lógica para pintar el resumen dinámico en las tarjetas de arriba
        const txtTotal = document.getElementById("stat-total");
        const txtRetardos = document.getElementById("stat-retardos");
        const txtSalidas = document.getElementById("stat-salidas");

        if (txtTotal) txtTotal.textContent = empleadosActivos.size;
        if (txtRetardos) txtRetardos.textContent = totalRetardos;
        if (txtSalidas) txtSalidas.textContent = totalSalidas;

    } catch (error) {
        console.error("Error cargando componentes en el Inicio:", error);
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red; padding: 20px;">✘ Error al conectar con el servidor OnTime.</td></tr>`;
    }
}

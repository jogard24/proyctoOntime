const API_URL = "http://localhost:8080/error";

export async function configurarInicioDashboard() {
    // 1. Personalizar el texto de bienvenida con el nombre real de la sesión
    const sesionStr = sessionStorage.getItem("usuarioActual");
    if (sesionStr) {
        const usuario = JSON.parse(sesionStr);
        const txtBienvenida = document.getElementById("txt-bienvenida-admin");
        if (txtBienvenida) {
            txtBienvenida.textContent = `¡Bienvenido ${usuario.nombre}!`;
        }
    }

    // 2. Cargar los datos inmediatamente desde MySQL
    await consultarNovedadesInicio();

    // 3. Asignar el botón de actualizar manual de la sección de inicio
    const btnActualizar = document.getElementById("btn-actualizar-inicio");
    if (btnActualizar) {
        btnActualizar.addEventListener("click", consultarNovedadesInicio);
    }
}

async function consultarNovedadesInicio() {
    const tbody = document.getElementById("tbody-novedades-inicio");
    if (!tbody) return;

    try {
        const respuesta = await fetch(`${API_URL}/novedades`);
        if (!respuesta.ok) throw new Error("Error en la respuesta del servidor.");

        const registros = await respuesta.json();

        if (registros.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; padding: 20px;">No hay movimientos registrados el día de hoy.</td></tr>`;
            return;
        }

        // --- AQUÍ ESTÁ EL CAMBIO CLAVE PARA EL PERSONAL ACTIVO ---
        // Usamos un Set para almacenar los nombres únicos de quienes marcaron entrada
        let empleadosActivos = new Set();
        let totalRetardos = 0;
        let totalSalidas = 0;

        tbody.innerHTML = "";

        registros.forEach(reg => {
            // Si el evento es una entrada, lo sumamos al Set de empleados activos
            if (reg.evento && reg.evento.toLowerCase() === "entrada") {
                empleadosActivos.add(reg.empleado); 
            }
            // Contadores dinámicos para las otras tarjetas
            if (reg.observacion && reg.observacion.toLowerCase().includes("retardo")) {
                totalRetardos++;
            }
            if (reg.evento && reg.evento.toLowerCase() === "salida") {
                totalSalidas++;
            }

            const fila = document.createElement("tr");
            
            // Si tiene retardo, le aplicamos un color de fondo sutil de alerta
            if (reg.observacion && reg.observacion.toLowerCase().includes("retardo")) {
                fila.style.backgroundColor = "rgba(255, 77, 77, 0.08)";
            }

            // Mapeamos las celdas usando tus estilos semánticos
            fila.innerHTML = `
                <td>${reg.id}</td>
                <td><strong>${reg.empleado}</strong></td>
                <td>
                    <span style="padding: 4px 8px; border-radius: 4px; font-size: 0.85rem; font-weight: bold;
                        background-color: ${reg.evento === 'entrada' ? '#e8f5e9' : '#fff3e0'};
                        color: ${reg.evento === 'entrada' ? '#2e7d32' : '#e65100'};">
                        ${reg.evento.toUpperCase()}
                    </span>
                </td>
                <td>${reg.turno}</td>
                <td>${reg.fecha_hora}</td>
                <td style="font-weight: ${reg.observacion.includes('Retardo') ? '500' : 'normal'}; 
                           color: ${reg.observacion.includes('Retardo') ? '#ff4d4d' : '#ffffff'}">
                    ${reg.observacion}
                </td>
            `;
            tbody.appendChild(fila);
        });

        // Actualizar los números de las tarjetas en la parte superior
        const txtTotal = document.getElementById("stat-total");
        const txtRetardos = document.getElementById("stat-retardos");
        const txtSalidas = document.getElementById("stat-salidas");

        // .size nos da el conteo final de elementos únicos dentro del Set
        if (txtTotal) txtTotal.textContent = empleadosActivos.size; 
        if (txtRetardos) txtRetardos.textContent = totalRetardos;
        if (txtSalidas) txtSalidas.textContent = totalSalidas;

    } catch (error) {
        console.error("Error cargando componentes en el Inicio:", error);
        tbody.innerHTML = `<tr><td colspan="6" style="text-align: center; color: red; padding: 20px;">✘ Error al conectar con el servidor OnTime.</td></tr>`;
    }
}
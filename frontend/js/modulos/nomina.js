export function configurarNomina() {
    console.log("Nomina cargada");

    let listaEmpleadosGlobal = [];

    cargarDatosNomina();

    const btnBuscar = document.getElementById("btn-nomina");
    if (btnBuscar) {
        btnBuscar.addEventListener("click", filtrarTablaNomina);
    }

    const inputBusqueda = document.querySelector(".label-modificador");
    if (inputBusqueda) {
        inputBusqueda.addEventListener("input", filtrarTablaNomina);
    }


    function cargarDatosNomina() {
        const url = 'http://localhost:8080/OnTimeBackend/NominaServlet';

        fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json'
            }
        })
        .then(response => {
            if (!response.ok) throw new Error("Error en el servidor");
            return response.json();
        })
        .then(data => {
            listaEmpleadosGlobal = data;
            renderizarTabla(listaEmpleadosGlobal);
        })
        .catch(error => {
            console.error("Error cargando la nómina en OnTime:", error);
            const tbody = document.querySelector(".tablaNomina tbody");
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:#e74c3c;">Error al conectar con el backend. Asegúrate de tener Tomcat encendido.</td></tr>`;
            }
        });
    }

    function renderizarTabla(empleados) {
        const tbody = document.querySelector(".tablaNomina tbody");
        if (!tbody) return;

        tbody.innerHTML = ""; 

        if (!Array.isArray(empleados) || empleados.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No se encontraron resultados coincidentes.</td></tr>`;
            return;
        }

        empleados.forEach(empleado => {
            const salarioBase = 1300000; 
            const valorPorRetardo = 15000; 
            const deducciones = (empleado.novedadesRetardos || 0) * valorPorRetardo;
            const valorPorExtra = 20000;
            const bonificaciones = (empleado.novedadesExtras || 0) * valorPorExtra;
            const salarioNeto = salarioBase - deducciones + bonificaciones;

            const formatoMoneda = new Intl.NumberFormat('es-CO', {
                style: 'currency',
                currency: 'COP',
                minimumFractionDigits: 0
            });

            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td><strong>${empleado.cedula}</strong></td>
                <td>${empleado.nombre}</td>
                <td>${formatoMoneda.format(salarioBase)}</td>
                <td style="${deducciones > 0 ? 'color: #e74c3c; font-weight: bold;' : ''}">
                    ${deducciones > 0 ? '-' : ''}${formatoMoneda.format(deducciones)}
                </td>
                <td style="${bonificaciones > 0 ? 'color: #2ecc71; font-weight: bold;' : ''}">
                    ${bonificaciones > 0 ? '+' : ''}${formatoMoneda.format(bonificaciones)}
                </td>
                <td style="font-weight: bold; background: rgba(255, 255, 255, 0.05);">${formatoMoneda.format(salarioNeto)}</td>
            `;
            tbody.appendChild(fila);
        });
    }

    function filtrarTablaNomina() {
        const inputBusqueda = document.querySelector(".label-modificador");
        if (!inputBusqueda) return;

        const textoBuscar = inputBusqueda.value.toLowerCase().trim();

        if (textoBuscar === "") {
            renderizarTabla(listaEmpleadosGlobal);
            return;
        }

        const empleadosFiltrados = listaEmpleadosGlobal.filter(empleado => {
            const coincideNombre = empleado.nombre && empleado.nombre.toString().toLowerCase().includes(textoBuscar);
            const coincideCedula = empleado.cedula && empleado.cedula.toString().toLowerCase().includes(textoBuscar);
            return coincideNombre || coincideCedula;
        });

        renderizarTabla(empleadosFiltrados);
    }
}

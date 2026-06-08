export function configurarNomina() {
    console.log("Nomina cargada correctamente");

    let listaEmpleadosGlobal = [];
    const tbody = document.querySelector(".tablaNomina tbody");
    const btnBuscar = document.getElementById("btn-nomina");
    const inputBusqueda = document.getElementById("busqueda-empleado-nomina");
    const filtroPeriodo = document.getElementById("filtro-periodo-nomina");
    const btnGenerar = document.getElementById("btn-generarNomina");
    const btnExportar = document.getElementById("btn-exportarNomina");

    // Formateador oficial de pesos colombianos
    const formatoMoneda = new Intl.NumberFormat('es-CO', { 
        style: 'currency', 
        currency: 'COP', 
        minimumFractionDigits: 0 
    });

    // Cargar datos al iniciar
    cargarDatosNomina();

    // Enlace de Eventos
    if (btnBuscar) btnBuscar.addEventListener("click", filtrarTablaNomina);
    if (inputBusqueda) inputBusqueda.addEventListener("input", filtrarTablaNomina);
    if (filtroPeriodo) filtroPeriodo.addEventListener("change", cargarDatosNomina);

    // Ajuste 3: Evento para simular o procesar el cierre financiero en la base de datos (RF17)
    if (btnGenerar) {
        btnGenerar.addEventListener("click", () => {
            if(confirm(`¿Está seguro de cerrar y guardar la nómina para el periodo ${filtroPeriodo.value}? Los datos se bloquearán.`)) {
                procesarCierreNomina();
            }
        });
    }

    // Evento para exportar/imprimir el informe de forma básica y escolar (RF19)
    if (btnExportar) {
        btnExportar.addEventListener("click", () => {
            window.print(); // Abre el asistente de PDF nativo del navegador, limpio y rápido
        });
    }

    function cargarDatosNomina() {
        const periodo = filtroPeriodo ? filtroPeriodo.value : '2026-06';
        // Enviamos el periodo seleccionado como parámetro GET hacia Java
        const url = `http://localhost:8080/OnTimeBackend/NominaServlet?periodo=${periodo}`;

        fetch(url, { method: 'GET' })
        .then(response => response.json())
        .then(data => {
            console.log("Datos de nómina recibidos:", data); 
            listaEmpleadosGlobal = Array.isArray(data) ? data : [];
            renderizarTabla(listaEmpleadosGlobal);
        })
        .catch(error => {
            console.error("Error conectando con NominaServlet:", error);
            if (tbody) tbody.innerHTML = `<tr><td colspan="6" style="text-align:center; color:red;">Error de conexión con el servidor.</td></tr>`;
        });
    }

    function renderizarTabla(empleados) {
        if (!tbody) return;
        tbody.innerHTML = ""; 

        if (empleados.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" style="text-align:center;">No hay registros de tiempo para este periodo.</td></tr>`;
            return;
        }

        empleados.forEach(empleado => {
            const cedulaMostrada = empleado.documento || empleado.documento_identidad || empleado.cedula || "N/A";
            
            // Ajuste 1: Extraer el salario real de la base de datos, con un respaldo básico por si viene nulo
            const salarioBase = empleado.salarioBase || empleado.salario_base || 1300000; 
            
            // Cálculos dinámicos provenientes del conteo analítico de tu Servlet/DAO (RF18)
            const deducciones = (empleado.totalRetardos || 0) * 15000; 
            const bonificaciones = (empleado.totalExtras || 0) * 20000; 
            const salarioNeto = salarioBase - deducciones + bonificaciones;

            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td><strong>${cedulaMostrada}</strong></td>
                <td>${empleado.nombre} ${empleado.apellido || ''}</td>
                <td>${formatoMoneda.format(salarioBase)}</td>
                <td style="${deducciones > 0 ? 'color: #e74c3c; font-weight: bold;' : ''}">${formatoMoneda.format(deducciones)}</td>
                <td style="${bonificaciones > 0 ? 'color: #2ecc71; font-weight: bold;' : ''}">${formatoMoneda.format(bonificaciones)}</td>
                <td><strong>${formatoMoneda.format(salarioNeto)}</strong></td>
            `;
            tbody.appendChild(fila);
        });
    }

    function filtrarTablaNomina() {
        const textoBuscar = inputBusqueda.value.toLowerCase().trim();
        if (!textoBuscar) {
            renderizarTabla(listaEmpleadosGlobal);
            return;
        }

        const filtrados = listaEmpleadosGlobal.filter(e => {
            const nombreCompleto = e.nombre ? e.nombre.toLowerCase() : '';
            const documento = (e.documento || e.cedula || '').toString();
            return nombreCompleto.includes(textoBuscar) || documento.includes(textoBuscar);
        });
        renderizarTabla(filtrados);
    }

    function procesarCierreNomina() {
        const url = 'http://localhost:8080/OnTimeBackend/NominaServlet';
        const params = new URLSearchParams();
        params.append('accion', 'guardarPeriodo');
        params.append('periodo', filtroPeriodo.value);

        fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(res => {
            if (res.ok) {
                alert("¡Nómina consolidada e integrada con éxito en las tablas históricas!");
                cargarDatosNomina();
            } else {
                alert("Error al intentar procesar el cierre en el servidor.");
            }
        })
        .catch(err => console.error("Error en el guardado de nómina:", err));
    }
}


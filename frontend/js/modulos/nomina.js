export function configurarNomina() {
    console.log("Nomina cargada correctamente con control de asistencia de 7 columnas");

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

    // Enlace de Eventos Seguros
    if (btnBuscar) btnBuscar.addEventListener("click", filtrarTablaNomina);
    if (inputBusqueda) inputBusqueda.addEventListener("input", filtrarTablaNomina);
    if (filtroPeriodo) filtroPeriodo.addEventListener("change", cargarDatosNomina);

    // Evento para procesar el cierre financiero en la base de datos (RF17)
    if (btnGenerar) {
        btnGenerar.addEventListener("click", () => {
            if(confirm(`¿Está seguro de cerrar y guardar la nómina para el periodo ${filtroPeriodo.value}? Los datos se bloquearán.`)) {
                procesarCierreNomina();
            }
        });
    }

    // Evento para exportar/imprimir el informe de forma básica (RF19)
    if (btnExportar) {
        btnExportar.addEventListener("click", () => {
            window.print(); 
        });
    }

    function cargarDatosNomina() {
        const periodo = filtroPeriodo ? filtroPeriodo.value : '2026-06';
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
            if (tbody) tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:red; font-weight:bold;">❌ Error de conexión con el servidor.</td></tr>`;
        });
    }

    function renderizarTabla(empleados) {
        if (!tbody) return;
        tbody.innerHTML = ""; 

        if (empleados.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" style="text-align:center;">No hay registros de tiempo para este periodo.</td></tr>`;
            return;
        }

        empleados.forEach(empleado => {
            const cedulaMostrada = empleado.documento || empleado.documento_identidad || empleado.cedula || "N/A";
            
            // 1. Salario base contractual de la base de datos
            const salarioBaseContractual = empleado.salarioBase || empleado.salario_base || 1400000; 
            
            // 2. Trazabilidad: totalExtras equivale a los días que asistió (marcas de salida)
            const diasTrabajados = empleado.totalExtras !== undefined ? empleado.totalExtras : 0;
            
            // 3. Conteo y cálculo monetario de las deducciones por retardos
            const numeroRetardos = empleado.totalRetardos || 0;
            const deducciones = numeroRetardos * 15000; 

            // 4. Cálculo de las bonificaciones de extras en dinero real ($)
            const bonificaciones = 0; 

            // =========================================================================
            // ALGORITMO FINANCIERO CON SOLUCIÓN DE PROPORCIONALIDAD
            // =========================================================================
            const sueldoProporcionalDias = (salarioBaseContractual / 30) * diasTrabajados;
            const salarioNetoFinal = sueldoProporcionalDias - deducciones + bonificaciones;
            // =========================================================================

            const fila = document.createElement("tr");
            
            // INYECCIÓN DE LAS 7 CELDAS PERFECTAMENTE CORREGIDAS
            fila.innerHTML = `
                <td><strong>${cedulaMostrada}</strong></td>
                <td>${empleado.nombre} ${empleado.apellido || ''}</td>
                <td>${formatoMoneda.format(salarioBaseContractual)}</td>
                <td style="${deducciones > 0 ? 'color: #e74c3c; font-weight: bold;' : ''}">
                     (${formatoMoneda.format(deducciones)})
                </td>
                <td style="color: #2ecc71; font-weight: bold; text-align: center;">
                    ${diasTrabajados} días
                </td>
                <td style="${bonificaciones > 0 ? 'color: #2ecc71; font-weight: bold;' : ''}">
                    ${formatoMoneda.format(bonificaciones)}
                </td>
                <td><strong>${formatoMoneda.format(salarioNetoFinal)}</strong></td>
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



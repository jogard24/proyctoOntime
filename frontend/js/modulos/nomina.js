export function configurarNomina() {
    console.log("Nomina cargada correctamente con control de asistencia");

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
            window.print(); // Abre el asistente de PDF nativo del navegador
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
            if (tbody) tbody.innerHTML = `<tr><td colspan="7" style="text-align:center; color:red;">Error de conexión con el servidor.</td></tr>`;
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
            
            // 1. Captura el salario base contractual real de la base de datos
            const salarioBaseContractual = empleado.salarioBase || empleado.salario_base || 1400000; 
            
            // 2. Trazabilidad: totalExtras equivale a los días que el empleado asistió y marcó salida
            const diasTrabajados = empleado.totalExtras !== undefined ? empleado.totalExtras : 0;
            
            // 3. Conteo de retardos (Mapea el número de retardos acumulados en el mes)
            const numeroRetardos = empleado.totalRetardos || 0;
            const deducciones = numeroRetardos * 15000; // Deducción fija por cada retardo

            // =========================================================================
            // 🧮 CÁLCULO PROPORCIONAL EXACTO (REGLA DE TRES LEGAL)
            // =========================================================================
            // Dividimos el sueldo del contrato en 30 días y lo multiplicamos por sus asistencias reales
            const sueldoPorDiasTrabajados = (salarioBaseContractual / 30) * diasTrabajados;
            
            // El Salario Neto final es el proporcional devengado menos las penalizaciones por retardo
            const salarioNetoFinal = sueldoPorDiasTrabajados - deducciones;
            // =========================================================================

            const fila = document.createElement("tr");
            
            // Mantenemos exactamente tus mismas 6 columnas del HTML para no desalinear la grilla
            fila.innerHTML = `
                <td><strong>${cedulaMostrada}</strong></td>
                <td>${empleado.nombre} ${empleado.apellido || ''}</td>
                <td>${formatoMoneda.format(salarioBaseContractual)}</td>
                
                <!-- Columna RETARDOS: Muestra cuántos retardos tuvo y el descuento acumulado -->
                <td style="${deducciones > 0 ? 'color: #e74c3c; font-weight: bold;' : ''}">
                    ${numeroRetardos} (${formatoMoneda.format(deducciones)})
                </td>
                
                <!-- Columna EXTRAS: Reutilizada estratégicamente para mostrar los Días Trabajados del Pinpad -->
                <td style="color: #2ecc71; font-weight: bold; text-align: center;">
                    ${diasTrabajados} días
                </td>
                
                <!-- Columna SALARIO NETO: Muestra el dinero real proporcional a pagar congelado -->
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


export function configurarNomina() {

    let listaEmpleadosGlobal = [];
    const tbody = document.querySelector(".tablaNomina tbody");
    const btnBuscar = document.getElementById("btn-nomina");
    const inputBusqueda = document.getElementById("busqueda-empleado-nomina");
    const filtroPeriodo = document.getElementById("filtro-periodo-nomina");
    const btnGenerar = document.getElementById("btn-generarNomina");
    const btnExportar = document.getElementById("btn-exportarNomina");

    // Formateador oficial de pesos colombianos (COP)
    const formatoMoneda = new Intl.NumberFormat('es-CO', { 
        style: 'currency', 
        currency: 'COP', 
        minimumFractionDigits: 0 
    });

    // Cargar datos al iniciar de forma automatizada
    cargarDatosNomina();

    // Enlace de Eventos del DOM
    if (btnBuscar) btnBuscar.addEventListener("click", filtrarTablaNomina);
    if (inputBusqueda) inputBusqueda.addEventListener("input", filtrarTablaNomina);
    if (filtroPeriodo) filtroPeriodo.addEventListener("change", cargarDatosNomina);

    // Evento para procesar el cierre financiero inmutable en la base de datos (RF17)
    if (btnGenerar) {
        btnGenerar.addEventListener("click", () => {
            if(confirm(`¿Está seguro de cerrar y guardar la nómina para el periodo ${filtroPeriodo.value}? Los datos se bloquearán.`)) {
                procesarCierreNomina();
            }
        });
    }

    // Evento para exportar/imprimir el informe masivo (RF19)
    if (btnExportar) {
        btnExportar.addEventListener("click", () => {
            window.print(); // Invoca el asistente de PDF nativo desacoplado por CSS
        });
    }

    function cargarDatosNomina() {
        const periodo = filtroPeriodo ? filtroPeriodo.value : '2026-06';
        const url = `http://localhost:8080/OnTimeBackend/NominaServlet?periodo=${periodo}`;

        fetch(url, { method: 'GET' })
        .then(response => response.json())
        .then(data => {
            console.log("Datos de nómina recibidos del backend:", data); 
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
            
            // 1. Salario base contractual extraído de la tabla contrato de Ontime3BD
            const salarioBaseContractual = empleado.salarioBase || empleado.salario_base || 1400000; 
            
            // 2. Trazabilidad de Días: Lee la propiedad asignada de marcas de salida
            const diasTrabajados = empleado.diasAsistidos !== undefined ? empleado.diasAsistidos : 30;
            
            // 3. Conteo y cálculo monetario de las deducciones por retardos ($10.000 por cada evento)
            const numeroRetardos = empleado.totalRetardos || 0;
            const deducciones = numeroRetardos * 1000; 

            // 4. Conteo y cálculo monetario de las horas extras reales ($20.000 por marca %extra%)
            const numeroHorasExtras = empleado.totalExtras !== undefined ? empleado.totalExtras : 0;
            const bonificaciones = numeroHorasExtras * 20000; 

            // =========================================================================
            // ALGORITMO FINANCIERO CON SOLUCIÓN DE PROPORCIONALIDAD
            // =========================================================================
            // Dividimos el salario contractual en 30 días y multiplicamos por sus asistencias reales
            const sueldoProporcionalDias = (salarioBaseContractual / 30) * diasTrabajados;
            
            // El Salario Neto final procesa los descuentos de retardos y suma las bonificaciones
            const salarioNetoFinal = sueldoProporcionalDias - deducciones + bonificaciones;
            // =========================================================================

            const fila = document.createElement("tr");
            
            // INYECCIÓN DE LAS 7 CELDAS EN  ORDEN SECUENCIAL
            fila.innerHTML = `
                <td><strong>${cedulaMostrada}</strong></td>
                <td>${empleado.nombre} ${empleado.apellido || ''}</td>
                <td>${formatoMoneda.format(salarioBaseContractual)}</td>
                
                <!-- 4. DEDUCCIONES: Cantidad de retardos y dinero en rojo si aplica -->
                <td style="${deducciones > 0 ? 'color: #e74c3c; font-weight: bold;' : ''}">
                  <!-- llegada tarde ${numeroRetardos}--> ${formatoMoneda.format(deducciones)}
                </td>
                
                <!-- 5. DIAS: Cantidad de días asistidos calculados en color verde -->
                <td style="color: #2ecc71; font-weight: bold; text-align: center;">
                    ${diasTrabajados} días
                </td>
                
                <!-- 6. EXTRAS: Bonificaciones monetarias reales en verde si tiene horas adicionales -->
                <td style="${bonificaciones > 0 ? 'color: #2ecc71; font-weight: bold;' : ''}">
                    ${formatoMoneda.format(bonificaciones)}
                </td>
                
                <!-- 7. SALARIO NETO: Balance total consolidado para el cierre -->
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



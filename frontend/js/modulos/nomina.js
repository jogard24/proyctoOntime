export function configurarNomina() {
    console.log("Nomina cargada correctamente");

    let listaEmpleadosGlobal = [];

    // Cargar datos al iniciar
    cargarDatosNomina();

    // Eventos
    const btnBuscar = document.getElementById("btn-nomina");
    if (btnBuscar) btnBuscar.addEventListener("click", filtrarTablaNomina);

    const inputBusqueda = document.querySelector(".label-modificador");
    if (inputBusqueda) inputBusqueda.addEventListener("input", filtrarTablaNomina);

    function cargarDatosNomina() {
        const url = 'http://localhost:8080/OnTimeBackend/NominaServlet';

        fetch(url, { method: 'GET' })
        .then(response => response.json())
        .then(data => {
            // DEBUG: Esto te dirá qué campos vienen del servidor
            console.log("Datos recibidos del backend:", data); 
            listaEmpleadosGlobal = data;
            renderizarTabla(listaEmpleadosGlobal);
        })
        .catch(error => {
            console.error("Error conectando con NominaServlet:", error);
        });
    }

    function renderizarTabla(empleados) {
        const tbody = document.querySelector(".tablaNomina tbody");
        if (!tbody) return;
        tbody.innerHTML = ""; 

        empleados.forEach(empleado => {
            // IMPORTANTE: Asegúrate de que empleado.cedula sea el nombre del campo 
            // que viene en el JSON. Si ves 'documento_identidad' en el console.log, cámbialo aquí.
            const cedulaMostrada = empleado.cedula || empleado.documento_identidad || "N/A";
            
            const salarioBase = 1300000; 
            const deducciones = (empleado.totalRetardos || 0) * 15000; // Ajustado a DAO
            const bonificaciones = (empleado.totalExtras || 0) * 20000; // Ajustado a DAO
            const salarioNeto = salarioBase - deducciones + bonificaciones;

            const formatoMoneda = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', minimumFractionDigits: 0 });

            const fila = document.createElement("tr");
            fila.innerHTML = `
                <td><strong>${cedulaMostrada}</strong></td>
                <td>${empleado.nombre}</td>
                <td>${formatoMoneda.format(salarioBase)}</td>
                <td style="${deducciones > 0 ? 'color: #e74c3c;' : ''}">${formatoMoneda.format(deducciones)}</td>
                <td style="${bonificaciones > 0 ? 'color: #2ecc71;' : ''}">${formatoMoneda.format(bonificaciones)}</td>
                <td>${formatoMoneda.format(salarioNeto)}</td>
            `;
            tbody.appendChild(fila);
        });
    }

    function filtrarTablaNomina() {
        const textoBuscar = document.querySelector(".label-modificador").value.toLowerCase();
        const filtrados = listaEmpleadosGlobal.filter(e => 
            e.nombre.toLowerCase().includes(textoBuscar) || 
            (e.cedula && e.cedula.toString().includes(textoBuscar))
        );
        renderizarTabla(filtrados);
    }
}

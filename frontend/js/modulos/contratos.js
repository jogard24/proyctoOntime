export function inicializarGestionContratos() {
    const contenedorCards = document.getElementById('contenedor-cards-contratos');
    const filtroPeriodo = document.getElementById('filtro-periodo-contratos');
    const btnBuscar = document.getElementById('btn-buscar-contratos');

    async function cargarContratos(mesFiltro = '2026-07') {

        if (!document.getElementById('css-contratos-dinamico')) {
            const linkCss = document.createElement('link');
            linkCss.id = 'css-contratos-dinamico';
            linkCss.rel = 'stylesheet';
            linkCss.href = '../css/contratos.css'; // Asegura la ruta exacta de tu archivo
            document.head.appendChild(linkCss); // Lo siembra directamente en el motor principal de Chrome
        }

        try {
           const respuesta = await fetch(`http://localhost:8080/OnTimeBackend/ContratoServlet?accion=listarTodos&periodo=${mesFiltro}`, {
                method: 'GET',
                credentials: 'include'
            });
            if (!respuesta.ok) throw new Error("Error de servidor");
            const data = await respuesta.json();
            renderizarCards(data);
        } catch (error) {
            console.error("Error cargando contratos:", error);
            contenedorCards.innerHTML = `<p style="color:red; font-weight:bold; font-family:sans-serif;">No se pudieron conectar las tarjetas al servidor.</p>`;
        }
    }

    function renderizarCards(contratos) {
        contenedorCards.innerHTML = '';

        contratos.forEach(con => {
            const esContratoInactivo = (con.estado === 'inactivo');
            const hoy = new Date();
            const fechaFin = new Date(con.fechaFin);
            const estaVencido = hoy > fechaFin;

            // Asignamos las clases dinámicas del CSS externo
            const claseSemaforo = esContratoInactivo ? 'card-clausurada' : (estaVencido ? 'card-vencido' : 'card-vigente');
            const claseBadge = esContratoInactivo ? 'badge-liquidado' : (estaVencido ? 'badge-vencido' : 'badge-vigente');
            const textoVigencia = esContratoInactivo ? '❌ LIQUIDADO' : (estaVencido ? '❌ VENCIDO' : '✔ ACTIVO');

            let propiedadBloqueo = esContratoInactivo ? 'disabled style="opacity:0.4; pointer-events:none; cursor:not-allowed;"' : '';
            const card = document.createElement('div');
            card.className = `card-contrato ${claseSemaforo}`;

            card.innerHTML = `
                <div style="display:flex; justify-content:space-between; align-items:center;">
                    <span class="badge-vigencia ${claseBadge}">${textoVigencia}</span>
                    <strong style="color:#ffff; font-size:12px;">N° ${con.contratoId}</strong>
                </div>
                
                <div>
                    <h3 style="margin:0 0 4px 0; color:#1e3d59; font-size:15px; text-transform:uppercase;">${con.empleadoNombre}</h3>
                    <p style="margin:0; color:#495057; font-size:13px; font-weight:500;">💼 ${con.cargo}</p>
                </div>

                <div class="card-info-box">
                    <div class="info-row"><span>💵 Salario Base:</span><strong>$ ${parseFloat(con.salario).toLocaleString()}</strong></div>
                    <div class="info-row"><span>📅 Periodo Pago:</span><strong style="color:#1e3d59;">${con.periodo_pago || 'Mensual'}</strong></div>
                    <div class="info-row"><span>📅 Vence el:</span><span style="font-weight:600;">${con.fechaFin}</span></div>
                </div>

                <div class="card-actions-grid">
                    <button class="btn-card btn-periodic" data-id="${con.contratoId}" data-nombre="${con.empleadoNombre}" data-salario="${con.salario}" data-periodo="${con.periodo_pago || 'Mensual'}">💰 Pagar Periodo</button>
                    <button class="btn-card btn-settle" data-id="${con.contratoId}" data-nombre="${con.empleadoNombre}" data-salario="${con.salario}">🧮 Retiro Definitivo</button>
                </div>
            `;

            contenedorCards.appendChild(card);
        });
    }

    // Escuchador centralizado de clics analíticos
    contenedorCards.addEventListener('click', (e) => {
        const target = e.target;
        if (!target.classList.contains('btn-card')) return;

        const id = target.dataset.id;
        const nombre = target.dataset.nombre;
        const salario = target.dataset.salario;

        if (target.classList.contains('btn-periodic')) {
            const periodoContractual = target.dataset.periodo; // 'Mensual' o 'Quincenal'

            // ◄ CAPTURA DINÁMICA: Extrae el año y mes elegido (Ej: "2026-07")
            const mesSeleccionado = filtroPeriodo ? filtroPeriodo.value : '2026-07';

            ejecutarLiquidacionPeriodica(id, nombre, salario, periodoContractual, mesSeleccionado);
        } else if (target.classList.contains('btn-settle')) {
            ejecutarDesvinculacionDefinitiva(id, nombre, salario, target);
        }
    });



    async function ejecutarLiquidacionPeriodica(contratoId, nombre, salario, periodo, mesSeleccionado) {
        const params = new URLSearchParams();
        params.append("accion", "calcularPagoPeriodo");
        params.append("contratoId", contratoId);
        params.append("periodoMes", mesSeleccionado); // ◄ VIAJA POR LA RED HACIA JAVA

        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/ContratoServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });

            if (!respuesta.ok) throw new Error("Error en el servidor");
            const res = await respuesta.json();

            // Dividimos el letrero de la alerta según el mes específico analizado
            alert(`💰 LIQUIDACIÓN DE NÓMINA ORDINARIA\n\n` +
                `👤 Colaborador: ${nombre}\n` +
                `📅 Período Auditado: ${mesSeleccionado} (${periodo.toUpperCase()})\n` +
                `💵 Salario Contractual Base: $ ${res.salarioMensual.toLocaleString()}\n` +
                `-----------------------------------------\n` +
                `打 Días Asistidos en este Mes (Pinpad): ${res.diasLaborados} Días\n` +
                `💰 Valor de la Jornada Diaria: $ ${Math.round(res.valorDia).toLocaleString()}/día\n` +
                `-----------------------------------------\n` +
                `💵 Sueldo Proporcional Causado: $ ${Math.round(res.sueldoProporcional).toLocaleString()}\n` +
                `🔸 Recargo por Horas Extras: $ ${res.extras.toLocaleString()}\n` +
                `🔸 Descuento por Retardos: - $ ${res.deducciones.toLocaleString()}\n` +
                `-----------------------------------------\n` +
                `💰 NETO DEFINITIVO A GIRAR: $ ${Math.round(res.netoGirar).toLocaleString()}\n\n` +
                `¿Desea asentar este pago del mes de ${mesSeleccionado} en el histórico contable?`);

        } catch (error) {
            alert("No se pudieron extraer las novedades de ese periodo.");
        }
    }

    // Enlazamos el botón de buscar para recargar las mallas si es necesario
     btnBuscar?.addEventListener('click', (e) => {
        e.preventDefault();
        const mesSeleccionado = filtroPeriodo ? filtroPeriodo.value : '2026-07';
        console.log(" Cargando novedades para el periodo:", mesSeleccionado);
        
        // Refrescamos las tarjetas inyectando el mes seleccionado en caliente
        cargarContratos(mesSeleccionado);
    });
      cargarContratos('2026-07');
}




//  ACCIÓN 2: Desvinculación y Liquidación Definitiva por Fin de Contrato
async function ejecutarDesvinculacionDefinitiva(contratoId, nombre, salario, botonPresionado) {
    const sueldoNum = parseFloat(salario);
    const prima = sueldoNum * 0.5;
    const cesantias = sueldoNum * 0.5;
    const vacaciones = sueldoNum * 0.25;
    const liquidacionTotal = prima + cesantias + vacaciones;

    if (confirm(` RETIRO DEFINITIVO DE EMPLEADO\n\n¿Está seguro de liquidar de forma final el contrato de: ${nombre}?\n\nEsto calculará sus prestaciones de ley y lo dará de baja en el sistema.`)) {

        // CAPTURA DINÁMICA DEL DOM: Buscamos la tarjeta padre del botón presionado
        const tarjetaContenedora = botonPresionado.closest('.card-contrato');

        //  Cambiamos el texto de '✔ ACTIVO' a ' LIQUIDADO'
        if (tarjetaContenedora) {
            const badgeEstado = tarjetaContenedora.querySelector('.badge-vigencia') || tarjetaContenedora.querySelector('span');
            if (badgeEstado) {
                badgeEstado.className = "badge-vigencia badge-liquidado";
                badgeEstado.innerHTML = "LIQUIDADO";
            }
            // Aplicamos la clase CSS de congelamiento gris
            tarjetaContenedora.classList.add('card-clausurada');
        }

        // DISPARAMOS LA PETICIÓN REAL POR RED HACIA EL SERVLET
        const params = new URLSearchParams();
        params.append("accion", "darDeBaja");
        params.append("contratoId", contratoId);

        try {
            const respuesta = await fetch('http://localhost:8080/OnTimeBackend/ContratoServlet', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: params
            });

            if (respuesta.ok) {
                // Alerta informativa con el desprendible de prestaciones de ley
                alert(`📊 DESPRENDIBLE DE RETIRO GENERADO\n\n` +
                    `🔸 Prima de Servicios: $ ${prima.toLocaleString()}\n` +
                    `🔸 Auxilio de Cesantías: $ ${cesantias.toLocaleString()}\n` +
                    `🔸 Vacaciones Proporcionales: $ ${vacaciones.toLocaleString()}\n` +
                    `-----------------------------------------\n` +
                    `💰 TOTAL LIQUIDACIÓN DE RETIRO: $ ${liquidacionTotal.toLocaleString()}\n\n` +
                    `¡El contrato N° ${contratoId} ha sido liquidado con éxito!`);

                // ⏳ EFECTO TEMPORAL DE CONGELAMIENTO: Esperamos 1.5 segundos para que se aprecie la tarjeta inactiva antes de borrarla
                setTimeout(() => {
                    if (tarjetaContenedora) {
                        tarjetaContenedora.style.opacity = "0";
                        tarjetaContenedora.style.transform = "scale(0.8)";
                    }
                    // Después de desvanecerla, refrescamos la lista para depurar la grilla por completo
                    setTimeout(() => {
                        cargarContratos();
                    }, 400);
                }, 1500);

            } else {
                alert("MySQL rechazó la baja del usuario. Verifique restricciones.");
                cargarContratos(); // Recarga para restaurar el estado si falla
            }
        } catch (error) {
            console.error("Error al dar de baja:", error);
            alert("No se pudo conectar con el servidor.");
            cargarContratos();
        }
    }
    cargarContratos();
}




export function configurarGestionEmpleados() {
    console.log('Gestión de empleados cargada con éxito.');

    const tablaBody = document.getElementById('tabla-empleados-body');
    const inputBusqueda = document.getElementById('input-busqueda-empleado') || document.querySelector('.input-busqueda');
    const btnBuscar = document.getElementById('btn-buscarEmpleado');

    // Elementos del modal de edición controlados de forma modular (RNF11)
    const modalEditar = document.getElementById('modalEditar');
    const btnCancelarEdicion = document.getElementById('btn-cancelar-edicion');
    const btnGuardarEdicion = document.getElementById('btn-guardar-edicion');

    if (!tablaBody || !inputBusqueda || !btnBuscar) {
        console.warn('No se pudo inicializar la Gestión: Faltan elementos esenciales en el DOM.');
        return;
    }

    let listaEmpleados = [];

    // --- 1. FUNCIÓN DE RENDERIZADO DINÁMICO (Sincronizada con Ontime3BD) ---
    function renderizarTabla(empleados) {
        tablaBody.innerHTML = '';
        if (!empleados || empleados.length === 0) {
            tablaBody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay empleados disponibles.</td></tr>';
            return;
        }

        empleados.forEach(emp => {
            const fila = document.createElement('tr');

            // Asignación estética del pill de estado (RF23)
            const pillClase = emp.estado === 'activo' ? 'verde' : 'amarillo';

            const baseUrlBackend = "http://localhost:8080/OnTimeBackend/";

            let fotoFinal = emp.fotoPerfilUrl;
            if (!fotoFinal || fotoFinal.trim() === "" || fotoFinal.trim().toUpperCase() === "N/A" || fotoFinal.trim().toUpperCase() == "NULL") {
                fotoFinal = "img/usuario-defecto.png";
            }
            // Armamos la URL absoluta definitiva apuntando a Tomcat
            const urlFotoCompleta = baseUrlBackend + fotoFinal;
            const urlFotoDefecto = baseUrlBackend + "img/usuario-defecto.png";

            fila.innerHTML = `
                
                <td>
                <img src="${urlFotoCompleta}" 
                class="avatar-tabla" 
                alt="Foto" 
                style="width:40px; height:40px; border-radius:50%; object-fit:cover;"
                onerror="this.onerror=null; this.src='${urlFotoDefecto}';">
                </td>
                <td><strong>${emp.documento || 'N/A'}</strong></td> 
                <td>${emp.nombre}</td>
                <td>${emp.telefonoCelular || 'N/A'}</td> 
                <td>${emp.direccion || 'N/A'}</td> 
                <td>${emp.cargo || 'Sin asignar'}</td>
                <td><span class="pill ${pillClase}">${emp.estado.toUpperCase()}</span></td>
                <td>
                    <button class="bt-menu-item bt-ejecucion-editar" data-id="${emp.id}">Editar</button>
                 <!--   <button class="bt-menu-item bt-ejecucion-eliminar" data-id="${emp.id}">Eliminar</button> -->
                </td>
            `;
            tablaBody.appendChild(fila);
        });
    }

    // --- 2. LOGICA DE DATOS: LECTURA DESDE EL SERVIDOR (doGet) ---
    function cargarEmpleados() {
        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', { method: 'GET' })
            .then(res => {
                if (!res.ok) throw new Error(`Error de red: ${res.status}`);
                return res.json();
            })
            .then(data => {
                listaEmpleados = Array.isArray(data) ? data : [];
                renderizarTabla(listaEmpleados);
            })
            .catch(err => {
                console.error('Error cargando empleados:', err);
                tablaBody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red; font-weight:bold;">❌ Error al conectar con el servidor para listar personal.</td></tr>';
            });
    }

    // --- 3. GESTIÓN INTRÍNSECA DEL MODAL (Refactorizado para el Select) ---
    // === ESTE ES TU BLOQUE ACTUAL REFACTORIZADO Y BLINDADO EN gestionEmpleados.js ===
    function abrirModal(id, nombre, cargo, estado, celular, direccion) {
        document.getElementById('editId').value = id;
        document.getElementById('editNombre').value = nombre || '';
        document.getElementById('editEstado').value = estado || 'activo';

        const selectCargo = document.getElementById('editCargo');
        const contenedorCredenciales = document.getElementById('contenedorCredencialesContador');

        if (selectCargo) {
            const cargoActual = cargo ? cargo.toLowerCase().trim() : 'empleado';
            if (cargoActual === 'administrador' || cargoActual === 'admin') {
                selectCargo.value = "1";
            } else if (cargoActual === 'contador' || cargoActual === 'conta') {
                selectCargo.value = "3";
            } else {
                selectCargo.value = "2"; // Por defecto Empleado (ID 2)
            }

            // CONTROL AUTOMÁTICO AL ABRIR: Si al cargar el modal ya es contador,
            // mostramos el contenedor de usuario/clave; si no, lo ocultamos.
            if (contenedorCredenciales) {
                if (selectCargo.value === "3") {
                    contenedorCredenciales.style.display = "flex";
                } else {
                    contenedorCredenciales.style.display = "none";
                }
            }
        }

        const inputCelular = document.getElementById('editCelular');
        const inputDireccion = document.getElementById('editDireccion');

        if (inputCelular) inputCelular.value = celular || '';
        if (inputDireccion) inputDireccion.value = direccion || '';

        if (modalEditar) modalEditar.style.display = 'flex';
    }


    window.cerrarModal = function () {
        if (modalEditar) modalEditar.style.display = 'none';
    };

    // ---  PERSISTENCIA EN PARÁMETROS PLANOS (Refactorizado para el Select) ---
    window.guardarEdicion = function () {
        const idVal = document.getElementById('editId').value;
        const nombreVal = document.getElementById('editNombre').value.trim();
        const estadoVal = document.getElementById('editEstado').value;
        const celularVal = document.getElementById('editCelular')?.value.trim() || '';
        const direccionVal = document.getElementById('editDireccion')?.value.trim() || '';

        // CORRECCIÓN INTERNA: Extraemos tanto el ID como el Texto del Select
        const selectCargo = document.getElementById('editCargo');
        if (!selectCargo) return;

        const rolIdVal = selectCargo.value; // Captura "1", "2" o "3"
        const cargoTextoVal = selectCargo.options[selectCargo.selectedIndex].text; // Captura "Administrador", "Empleado" o "Contador"

        //  CAPTURA DE LAS NUEVAS VARIABLES DE ACCESO WEB (BI-ROL)
        const usuarioWeb = document.getElementById('editUsuarioContador')?.value.trim() || '';
        const claveWeb = document.getElementById('editClaveContador')?.value || '';
        // =========================================================================

        if (!nombreVal) {
            alert("Operación denegada: El nombre es un campo obligatorio.");
            return;
        }

        const params = new URLSearchParams();
        params.append('accion', 'actualizar');
        params.append('id', idVal);
        params.append('nombre', nombreVal);
        params.append('estado', estadoVal);
        params.append('cargo', cargoTextoVal);  // Va para la tabla 'contrato' en Java
        params.append('rol_id', rolIdVal);     // Va para la tabla 'credenciales' en Java
        params.append('celular', celularVal);
        params.append('direccion', direccionVal);

        // VALIDACIÓN Y ANEXO DE CREDENCIALES HACIA EL BACKEND (Java Servlet)
        // Si el rol es Administrador (1) o Contador (3), forzamos la seguridad web
        if (rolIdVal === "1" || rolIdVal === "3") {
            if (!usuarioWeb || !claveWeb) {
                alert("Operación denegada. si deseas quitar los privilegios administrativos, cambia su rol");
                return;
            }
            // Inyectamos las credenciales al FormData plano de red
            params.append('usuario_web', usuarioWeb);
            params.append('clave_web', claveWeb);
        }
        // =========================================================================

        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
            .then(res => {
                if (res.ok) {
                    alert("¡Información del empleado modificada y guardada con éxito!");
                    window.cerrarModal();
                    cargarEmpleados();
                } else {
                    alert("Fallo del Servidor: MySQL rechazó la actualización por restricciones de integridad.");
                }
            })
            .catch(err => {
                console.error("Error al actualizar:", err);
                alert("Error de comunicación: No se pudo entablar conexión con OnTime Backend.");
            });
    };


    // --- 5. INACTIVACIÓN LÓGICA TRANSPARENTE (POST - RF25) ---
    function ejecutarEliminacion(id) {
        const params = new URLSearchParams();
        params.append('accion', 'eliminar');
        params.append('id', id);

        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
            .then(res => {
                if (res.ok) {
                    alert("¡El empleado y su historial han sido eliminados permanentemente de la base de datos!");
                    cargarEmpleados();
                } else {
                    alert("Error: El servidor rechazó la eliminación del usuario.");
                }
            })
            .catch(err => console.error("Error al eliminar:", err));
    }

    // --- 6. FILTRO DE BÚSQUEDA LOCAL ---
    function buscarEmpleados(termino) {
        if (!termino.trim()) {
            renderizarTabla(listaEmpleados);
            return;
        }
        const t = termino.toLowerCase().trim();
        const filtrados = listaEmpleados.filter(emp =>
            (emp.nombre && emp.nombre.toLowerCase().includes(t)) ||
            (emp.documento && emp.documento.toLowerCase().includes(t))
        );
        renderizarTabla(filtrados);
    }

    // --- 7. ESCUCHADORES DE EVENTOS EN EL DOM (Sincronizados) ---
    if (btnBuscar) {
        btnBuscar.addEventListener('click', (e) => {
            e.preventDefault();
            buscarEmpleados(inputBusqueda.value);
        });
    }

    if (inputBusqueda) {
        inputBusqueda.addEventListener('keyup', (e) => {
            if (e.key === 'Enter') {
                buscarEmpleados(inputBusqueda.value);
            }
        });
    }

    if (btnCancelarEdicion) {
        btnCancelarEdicion.addEventListener('click', (e) => {
            e.preventDefault();
            window.cerrarModal();
        });
    }

    if (btnGuardarEdicion) {
        btnGuardarEdicion.addEventListener('click', (e) => {
            e.preventDefault();
            window.guardarEdicion();
        });
    }

    tablaBody.addEventListener('click', (e) => {
        const boton = e.target.closest('button');
        if (!boton) return;
        const id = boton.dataset.id;

        if (boton.classList.contains('bt-ejecucion-editar')) {
            const emp = listaEmpleados.find(e => String(e.id) === String(id));
            if (emp) {
                abrirModal(emp.id, emp.nombre, emp.cargo, emp.estado, emp.telefonoCelular, emp.direccion);
            }
        } else if (boton.classList.contains('bt-ejecucion-eliminar')) {
            if (confirm(" ADVERTENCIA: ¿Está seguro de eliminar permanentemente a este empleado del sistema? Esta acción no se puede deshacer.")) {
                ejecutarEliminacion(id);
            }
        }
    });

    const selectCargoDOM = document.getElementById('editCargo');
    const contenedorCredencialesDOM = document.getElementById('contenedorCredencialesContador');

    if (selectCargoDOM && contenedorCredencialesDOM) {
        selectCargoDOM.addEventListener('change', function () {
            // Se enciende si es Administrador (1) O SI ES Contador (3)
            if (selectCargoDOM.value === "1" || selectCargoDOM.value === "3") {
                contenedorCredencialesDOM.style.display = "flex";

                const inputUser = document.getElementById('editUsuarioContador');
                const nombreActual = document.getElementById('editNombre')?.value.trim().split(" ")[0] || "user";
                if (inputUser && inputUser.value === "") {
                    // Auto-sugiere el nombre de usuario limpio en base al nuevo rol
                    const prefijo = selectCargoDOM.value === "1" ? "admin" : "conta";
                    inputUser.value = (prefijo + nombreActual).toLowerCase();
                }
            } else {
                // Si pasa a empleado convencional (ID 2), se ocultan los campos web
                contenedorCredencialesDOM.style.display = "none";
            }
        });
    }

    // Carga inicial automatizada de registros al inyectar la vista
    cargarEmpleados();
}


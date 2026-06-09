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

            fila.innerHTML = `
                <td><img src="${emp.fotoPerfilUrl || 'img/usuario-defecto.png'}" class="avatar-tabla" alt="Foto" style="width:40px; height:40px; border-radius:50%; object-fit:cover;"></td>
                <td>documento: <strong>${emp.documento || 'N/A'}</strong></td> 
                <td>${emp.nombre}</td>
                <td>${emp.telefonoCelular || 'N/A'}</td> <!-- Muestra el teléfono en la grilla -->
                <td>${emp.direccion || 'N/A'}</td> <!-- Muestra la dirección en la grilla -->
                <td>${emp.cargo || 'Sin asignar'}</td>
                <td><span class="pill ${pillClase}">${emp.estado.toUpperCase()}</span></td>
                <td>
                    <!-- Inyectamos los botones pasándole el ID único para las operaciones por delegación -->
                    <button class="bt-menu-item bt-ejecucion-editar" data-id="${emp.id}">Editar</button>
                    <button class="bt-menu-item bt-ejecucion-eliminar" data-id="${emp.id}">Inactivar</button>
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

    // --- 3. GESTIÓN INTRÍNSECA DEL MODAL ---
    function abrirModal(id, nombre, cargo, estado, celular, direccion) {
        document.getElementById('editId').value = id;
        document.getElementById('editNombre').value = nombre || '';
        document.getElementById('editCargo').value = cargo || '';
        document.getElementById('editEstado').value = estado || 'activo';
        
        // Inyección de las nuevas propiedades refactorizadas en el formulario (RF23)
        const inputCelular = document.getElementById('editCelular');
        const inputDireccion = document.getElementById('editDireccion');
        
        if (inputCelular) inputCelular.value = celular || '';
        if (inputDireccion) inputDireccion.value = direccion || '';
        
        if (modalEditar) modalEditar.style.display = 'flex';
    }

    window.cerrarModal = function() {
        if (modalEditar) modalEditar.style.display = 'none';
    };

    // --- 4. PERSISTENCIA MASIVA EN PARÁMETROS PLANOS (doPost - RF23 y RF25) ---
    window.guardarEdicion = function() {
        const idVal = document.getElementById('editId').value;
        const nombreVal = document.getElementById('editNombre').value.trim();
        const cargoVal = document.getElementById('editCargo').value.trim();
        const estadoVal = document.getElementById('editEstado').value;
        const celularVal = document.getElementById('editCelular')?.value.trim() || '';
        const direccionVal = document.getElementById('editDireccion')?.value.trim() || '';

        if (!nombreVal || !cargoVal) {
            alert("Operación denegada: El nombre y el cargo son campos obligatorios.");
            return;
        }

        // Serialización limpia mediante URLSearchParams para request.getParameter() de Java Puro
        const params = new URLSearchParams();
        params.append('accion', 'actualizar');
        params.append('id', idVal);
        params.append('nombre', nombreVal);
        params.append('estado', estadoVal);
        params.append('cargo', cargoVal);
        params.append('celular', celularVal);
        params.append('direccion', direccionVal);

        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(res => {
            if (res.ok) {
                alert("¡Información del empleado modificada y guardada con éxito!");
                window.cerrarModal();
                cargarEmpleados(); // Recarga analítica en caliente de la grilla
            } else {
                // Bloqueo de falsas alertas positivas ante respuestas de error 400/500
                alert(" Fallo del Servidor: MySQL rechazó la actualización por restricciones de integridad.");
            }
        })
        .catch(err => {
            console.error("Error al actualizar:", err);
            alert(" Error de comunicación: No se pudo entablar conexión con OnTime Backend.");
        });
    };

    // --- 5. INACTIVACIÓN LÓGICA TRANSPARENTE (POST - RF25) ---
    function ejecutarInactivacion(id) {
        const params = new URLSearchParams();
        params.append('accion', 'inactivar');
        params.append('id', id);

        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', { 
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: params
        })
        .then(res => {
            if (res.ok) {
                alert("El empleado ha sido marcado como INACTIVO de forma segura.");
                cargarEmpleados();
            } else {
                alert(" Error: No se pudo modificar el estado del usuario.");
            }
        })
        .catch(err => console.error("Error al inactivar:", err));
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

    // --- 7. ESCUCHADORES DE EVENTOS EN EL DOM (Elimina scripts redundantes) ---
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

    // Escuchadores internos del modal que blindan la navegación modular
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

    // Captura analítica de clics por delegación de eventos en la tabla
    tablaBody.addEventListener('click', (e) => {
        const boton = e.target.closest('button');
        if (!boton) return;
        const id = boton.dataset.id;
        
        if (boton.classList.contains('bt-ejecucion-editar')) {
            const emp = listaEmpleados.find(e => String(e.id) === String(id));
            if (emp) {
                // Desempaquetamos todos los atributos guardados en el objeto local hacia el modal
                abrirModal(emp.id, emp.nombre, emp.cargo, emp.estado, emp.telefonoCelular, emp.direccion);
            }
        } else if (boton.classList.contains('bt-ejecucion-eliminar')) {
            if (confirm("¿Está seguro de inactivar administrativamente a este empleado? Su histórico relacional se conservará.")) {
                ejecutarInactivacion(id);
            }
        }
    });

    // Carga inicial automatizada de registros al inyectar la vista
    cargarEmpleados();
}

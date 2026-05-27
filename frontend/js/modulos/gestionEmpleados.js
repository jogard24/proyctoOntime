export function configurarGestionEmpleados() {
    console.log('Gestión de empleados cargada con éxito.');

    const tablaBody = document.getElementById('tabla-empleados-body');
    const inputBusqueda = document.querySelector('.input-busqueda');
    const btnBuscar = document.getElementById('btn-buscarEmpleado');

    if (!tablaBody || !inputBusqueda || !btnBuscar) return;

    let listaEmpleados = [];

    // --- FUNCIONES DE RENDEREADO ---
    function renderizarTabla(empleados) {
        tablaBody.innerHTML = '';
        if (!empleados || empleados.length === 0) {
            tablaBody.innerHTML = '<tr><td colspan="6" style="text-align:center;">No hay empleados disponibles.</td></tr>';
            return;
        }
        empleados.forEach(emp => {
            const fila = document.createElement('tr');
            fila.innerHTML = `
                <td><img src="${emp.fotoPerfilUrl || '../img/avatar-default.png'}" class="avatar-tabla" alt="Foto"></td>
                <td>${emp.id}</td>
                <td>${emp.nombre}</td>
                <td>${emp.cargo}</td>
                <td>${emp.estado}</td>
                <td>
                    <button class="bt-menu-item bt-ejecucion-editar" data-id="${emp.id}">Editar</button>
                    <button class="bt-menu-item bt-ejecucion-eliminar" data-id="${emp.id}">Eliminar</button>
                </td>
            `;
            tablaBody.appendChild(fila);
        });
    }

    // --- LÓGICA DE DATOS ---
    function cargarEmpleados() {
        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', { method: 'GET' })
            .then(res => res.json())
            .then(data => { listaEmpleados = data; renderizarTabla(listaEmpleados); })
            .catch(err => console.error('Error:', err));
    }

    // --- FUNCIONES DEL MODAL ---
    function abrirModal(id, nombre, cargo, estado) {
        document.getElementById('modalEditar').style.display = 'block';
        document.getElementById('editId').value = id;
        document.getElementById('editNombre').value = nombre;
        document.getElementById('editCargo').value = cargo;
        document.getElementById('editEstado').value = estado.toLowerCase();
    }

    window.cerrarModal = function() {
        document.getElementById('modalEditar').style.display = 'none';
    };

    window.guardarEdicion = function() {
        const datos = {
            id: document.getElementById('editId').value,
            nombre: document.getElementById('editNombre').value,
            cargo: document.getElementById('editCargo').value,
            estado: document.getElementById('editEstado').value
        };

        // Aquí harás el POST a tu Servlet de edición
        console.log("Enviando a BD:", datos);
        
        // Simulación de éxito
        window.cerrarModal();
        alert("Empleado actualizado");
        cargarEmpleados();
    };

    // --- EVENTOS ---
    tablaBody.addEventListener('click', (e) => {
        const boton = e.target.closest('button');
        if (!boton) return;
        const id = boton.dataset.id;
        
        if (boton.classList.contains('bt-ejecucion-editar')) {
            const emp = listaEmpleados.find(e => String(e.id) === String(id));
            if (emp) abrirModal(emp.id, emp.nombre, emp.cargo, emp.estado);
        } else if (boton.classList.contains('bt-ejecucion-eliminar')) {
            if (confirm("¿Eliminar empleado?")) {
                console.log("Eliminando ID:", id);
                // Aquí iría el fetch con method: 'DELETE'
                cargarEmpleados();
            }
        }
    });

    btnBuscar.addEventListener('click', (e) => {
        e.preventDefault();
        const termino = inputBusqueda.value.toLowerCase();
        renderizarTabla(listaEmpleados.filter(e => e.nombre.toLowerCase().includes(termino)));
    });

    cargarEmpleados();
}
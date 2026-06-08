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
            // Mostrando el documento del empleado en lugar del ID interno
            fila.innerHTML = `
                <td><img src="${emp.foto || '../img/avatar-default.png'}" class="avatar-tabla" alt="Foto" style="width:40px; height:40px; border-radius:50%;"></td>
                <td>${emp.documento || emp.id}</td> 
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
        // Asegúrate de que el puerto coincida con tu Tomcat (usualmente 8080)
        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', { method: 'GET' })
            .then(res => res.json())
            .then(data => { 
                listaEmpleados = data; 
                renderizarTabla(listaEmpleados); 
            })
            .catch(err => console.error('Error cargando empleados:', err));
    }

    // --- FUNCIONES AUXILIARES ---
    function abrirModal(id, nombre, cargo, estado) {
        document.getElementById('editId').value = id;
        document.getElementById('editNombre').value = nombre;
        document.getElementById('editCargo').value = cargo;
        document.getElementById('editEstado').value = estado;
        document.getElementById('modalEditar').style.display = 'flex';
    }

    // --- LÓGICA DE BÚSQUEDA ---
    function buscarEmpleados(termino) {
        if (!termino.trim()) {
            renderizarTabla(listaEmpleados);
            return;
        }
        const filtrados = listaEmpleados.filter(emp => 
            emp.nombre.toLowerCase().includes(termino.toLowerCase()) ||
            (emp.documento && emp.documento.toLowerCase().includes(termino.toLowerCase())) ||
            emp.id.toString().includes(termino)
        );
        renderizarTabla(filtrados);
    }

    // --- LÓGICA DE ACTUALIZACIÓN ---
    window.guardarEdicion = function() {
        const datos = {
            id: document.getElementById('editId').value,
            nombre: document.getElementById('editNombre').value,
            cargo: document.getElementById('editCargo').value,
            estado: document.getElementById('editEstado').value
        };

        // Aquí usamos fetch para enviar los datos al Servlet (método POST o PUT)
        fetch('http://localhost:8080/OnTimeBackend/EmpleadoServlet', {
            method: 'POST', // O 'PUT'
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(datos)
        })
        .then(res => res.json())
        .then(data => {
            alert("Empleado actualizado correctamente");
            window.cerrarModal();
            cargarEmpleados();
        })
        .catch(err => console.error("Error al actualizar:", err));
    };

    // --- EVENTOS ---
    btnBuscar.addEventListener('click', () => {
        buscarEmpleados(inputBusqueda.value);
    });

    inputBusqueda.addEventListener('keyup', (e) => {
        if (e.key === 'Enter') {
            buscarEmpleados(inputBusqueda.value);
        }
    });

    tablaBody.addEventListener('click', (e) => {
        const boton = e.target.closest('button');
        if (!boton) return;
        const id = boton.dataset.id;
        
        if (boton.classList.contains('bt-ejecucion-editar')) {
            const emp = listaEmpleados.find(e => String(e.id) === String(id));
            if (emp) abrirModal(emp.id, emp.nombre, emp.cargo, emp.estado);
        } else if (boton.classList.contains('bt-ejecucion-eliminar')) {
            if (confirm("¿Eliminar empleado?")) {
                // Aquí el fetch para eliminar
                fetch(`http://localhost:8080/OnTimeBackend/EmpleadoServlet?id=${id}`, { method: 'DELETE' })
                .then(() => cargarEmpleados());
            }
        }
    });

    cargarEmpleados();
}
<!doctype html>
<html lang="es">

<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <link rel="stylesheet" href="../css/dashb.css" />
  <link rel="stylesheet" href="../css/gestionempleados.css" />
  <link rel="stylesheet" href="../css/asistenciaTabla.css" />
  <link rel="stylesheet" href="../css/permisos.css" />
  <link rel="stylesheet" href="../css/inicioDashboard.css" />
  <title>dashb</title>
</head>
<script>
  // Verificar si existe sesión
  const sesion = sessionStorage.getItem('usuarioActual');
  if (!sesion) {
    window.location.href = 'login.html'; // Si no hay sesión, al login
  } else {
    const usuario = JSON.parse(sesion);
    // Verificar si tiene el rol correcto para ESTA página específica
    if (usuario.rol !== 'contador') {
      alert("Acceso denegado");
      window.location.href = 'login.html';
    }
  }
</script>

<body>
  <section class="dash">
    <!-- conetenedor menu lateral -->
    <section class="contenedor-menu">
      <aside class="menu-lateral">
        <!-- foto perfil -->
        <div class="editar-perfil">
          <form id="form-foto" enctype="multipart/form-data">
            <div class="avatar-perfil">
              <img id="visualizar" src="../img/logo sinfondo.png" alt="vista-previa" />
            </div>

          </form>
        </div>
        <!-- botones menu lateral -->
        <nav class="menu">
          <button class="bt-home bt-menu-item" id="btn-home">
            🏛️ ​ HOME
          </button>

          <button type="button" class="bt-menu-item" id="btn-Asistencia">
            Asistencia
          </button>
          <button type="button" class="bt-menu-item" id="btn-Permisos">
            Permisos
          </button>
          <button type="button" class="bt-menu-item" id="btn-generarNomina">
            Nomina
          </button>

          <button type="button" class="bt-menu-item" id="btn-calendario">
            ------
          </button>

          <button type="button" class="bt-menu-item btn-Modificador-cerrar" id="btn-CerrarSesion">
            Cerrar sesión
          </button>



        </nav>
      </aside>
    </section>
    <!-- contenedor dashboard -->
    <div class="contenedor-dash" id="contenedor-dinamico">




  </section>
  </div>

  </section>

  <div id="modalEditar"
    style="display:none; position:fixed; top:0; left:0; width:100%; height:100%; background:rgba(0,0,0,0.5); z-index:1000;">
    <div
      style="background:white; width:min(90vw, 560px); max-width:560px; margin:80px auto; padding:24px; border-radius:12px; box-shadow:0 24px 48px rgba(0,0,0,0.28);">
      <h2 style="margin-top:0; font-size:1.45rem;">Editar Empleado</h2>
      <input type="hidden" id="editId">
      <div style="display:flex; gap:16px; align-items:flex-start; flex-wrap:wrap; margin-bottom:12px;">
        <div style="display:flex; flex-direction:column; align-items:center; gap:10px; min-width:120px;">
          <img id="previewEditFoto" src="../img/higuita.jpg" alt="vista previa"
            style="width:88px; height:88px; border-radius:50%; object-fit:cover; border:1px solid rgba(0,0,0,0.15);">
          <input type="file" id="editFoto" accept="image/png, image/jpeg, image/jpg" style="font-size:0.95rem;">
        </div>
        <div style="flex:1">
          <label>Nombre:</label><br>
          <input type="text" id="editNombre"><br>
          <label>Cargo:</label><br>
          <select id="editCargo">
            <option value="administrador">Administrador</option>
            <option value="contador">Contador</option>
            <option value="empleado">Empleado</option>
          </select><br>
          <label>Estado:</label><br>
          <select id="editEstado">
            <option value="activo">Activo</option>
            <option value="inactivo">Inactivo</option>
          </select>
        </div>
      </div>

      <div style="display:flex; gap:8px; justify-content:flex-end; margin-top:12px;">
        <button onclick="cerrarModal()" style="padding:8px 12px; border-radius:6px;">Cancelar</button>
        <button onclick="guardarEdicion()"
          style="padding:8px 12px; border-radius:6px; background:var(--blue); color:#fff; border:none;">Guardar
          Cambios</button>
      </div>
    </div>
  </div>

  <script type="module" src="../js/main.js"></script>

  <script>
    // Cerrar el modal
    function cerrarModal() {
      const modal = document.getElementById('modalEditar');
      if (modal) modal.style.display = 'none';
    }

    // Guardar cambios; actualiza la foto de perfil en la cabecera (id=visualizar)
    function guardarEdicion() {
      const fileInput = document.getElementById('editFoto');
      const visualizar = document.getElementById('visualizar');
      const preview = document.getElementById('previewEditFoto');

      if (fileInput && fileInput.files && fileInput.files[0]) {
        const archivo = fileInput.files[0];
        const lector = new FileReader();
        lector.onload = function (e) {
          if (visualizar) visualizar.src = e.target.result;
          if (preview) preview.src = e.target.result;
        };
        lector.readAsDataURL(archivo);
      }

      // Aquí puedes agregar lógica para enviar el archivo al backend junto a otros campos

      cerrarModal();
    }

    // Actualizar preview cuando se selecciona un archivo
    document.addEventListener('change', function (e) {
      if (e.target && e.target.id === 'editFoto') {
        const file = e.target.files && e.target.files[0];
        const preview = document.getElementById('previewEditFoto');
        if (file && preview) {
          const reader = new FileReader();
          reader.onload = function (ev) { preview.src = ev.target.result; };
          reader.readAsDataURL(file);
        }
      }
    });
  </script>

</body>

</html>
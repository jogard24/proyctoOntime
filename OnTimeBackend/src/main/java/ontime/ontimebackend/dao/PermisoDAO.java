package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.PermisoLaboral;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    /**
     * Recupera el listado uniendo la tabla usuario para pintar el nombre en el reporte (RF13).
     */
    // Método público que devuelve una lista con todos los permisos laborales registrados.
    public List<PermisoLaboral> listarPermisos() {
        //    // Instancia una lista vacía para almacenar los objetos de tipo PermisoLaboral que se recuperen.
        List<PermisoLaboral> lista = new ArrayList<>();

        //Utiliza alias (p para permiso_laboral, u para usuario) para simplificar el código.
        String sql = "SELECT p.id, p.usuario_id, p.tipo_permiso, p.fecha_asignacion, p.fecha_inicio, p.fecha_fin, p.estado, "
                   + "u.nombre, u.apellido "
                   + "FROM permiso_laboral p "
                // Vincula ambas tablas donde el ID del usuario coincida con el dueño del permiso.
                   + "INNER JOIN usuario u ON p.usuario_id = u.id "
                // Ordena los resultados para mostrar primero los permisos más recientes (por ID descendente).
                   + "ORDER BY p.id DESC";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql); 
                //Almacena todas las filas y columnas devueltas por la base de datos 
                //en la variable rs de tipo ResultSet.
             ResultSet rs = ps.executeQuery()) {//devuelve la consulta en el resulset
            
            // Recorre el ResultSet fila por fila mientras existan registros devueltos.
            while (rs.next()) {
               // Crea un nuevo objeto PermisoLaboral por cada fila encontrada en la base de datos.
                PermisoLaboral permiso = new PermisoLaboral();
            // Extrae y asigna los valores básicos del permiso desde las columnas de la tabla.
                permiso.setId(rs.getInt("id"));
                permiso.setUsuarioId(rs.getInt("usuario_id"));
                permiso.setTipoPermiso(rs.getString("tipo_permiso"));
                
                // Convierte el Timestamp de la base de datos a un formato String seguro para el frontend.
                permiso.setFechaAsignacion(rs.getTimestamp("fecha_asignacion").toString());
                permiso.setFechaInicio(rs.getString("fecha_inicio"));
                permiso.setFechaFin(rs.getString("fecha_fin"));
                permiso.setEstado(rs.getString("estado"));
                
                // Concatena el nombre y apellido del usuario para llenar un campo visual combinado en la interfaz.
                permiso.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));
                // Agrega el objeto totalmente lleno a la lista general.
                lista.add(permiso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Inserta un permiso buscando previamente el ID del empleado por su cédula (RF12).
     */
    public boolean crearPermiso(String documento, String tipoPermiso, String fechaInicio, String fechaFin) {
        String sqlBuscarId = "SELECT id FROM usuario WHERE documento_identidad = ?";
        String sqlInsertar = "INSERT INTO permiso_laboral (usuario_id, tipo_permiso, fecha_inicio, fecha_fin, estado) VALUES (?, ?, ?, ?, 'pendiente')";

        try (Connection con = Conexion.obtenerConexion()) {
            int usuarioId = -1;
            
            // 1. Encontrar el ID interno del usuario basándonos en la cédula digitada en el prompt
            try (PreparedStatement psB = con.prepareStatement(sqlBuscarId)) {
                psB.setString(1, documento);
                try (ResultSet rs = psB.executeQuery()) {
                    if (rs.next()) {
                        usuarioId = rs.getInt("id");
                    }
                }
            }

            // Si el documento de identidad no existe, abortamos la inserción de forma segura
            if (usuarioId == -1) {
                return false; 
            }

            // 2. Insertar el registro con el ID correcto en la tabla de la base de datos
            try (PreparedStatement psI = con.prepareStatement(sqlInsertar)) {
                psI.setInt(1, usuarioId);
                psI.setString(2, tipoPermiso);
                psI.setString(3, fechaInicio);
                psI.setString(4, fechaFin);
                return psI.executeUpdate() > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el estado de la solicitud en el flujo de aprobación del Administrador (RF13).
     */
    //Define un método público que devuelve un valor verdadero (true) si la actualización tuvo éxito, o falso (false)
    public boolean actualizarEstadoPermiso(int id, String nuevoEstado) {
        //Define la consulta SQL. Usa signos de interrogación (?) como marcadores de posición (placeholders) para evitar ataques de inyección
        String sql = "UPDATE permiso_laboral SET estado = ? WHERE id = ?";
        //where:  le estás diciendo a MySQL: "Busca únicamente la fila donde la columna 
        //id coincida con el número que te voy a dar, y cambia el estado solo en esa fila".
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstado.toLowerCase()); // 'aprobado' o 'rechazado' (ENUM de MySQL)
            //tolowercase convierte el estado a minúsculas para que 
            //coincida exactamente con las restricciones del tipo ENUM en MySQL.
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
            // devuelve el número de filas afectadas. Si el resultado es mayor a cero (> 0), 
            //significa que el registro se actualizó correctamente y el método retorna true
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

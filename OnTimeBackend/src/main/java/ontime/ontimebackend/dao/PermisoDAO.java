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
    public List<PermisoLaboral> listarPermisos() {
        List<PermisoLaboral> lista = new ArrayList<>();
        // SQL alineado al 100% con las columnas físicas de tu script Ontime3BD
        String sql = "SELECT p.id, p.usuario_id, p.tipo_permiso, p.fecha_asignacion, p.fecha_inicio, p.fecha_fin, p.estado, "
                   + "u.nombre, u.apellido "
                   + "FROM permiso_laboral p "
                   + "INNER JOIN usuario u ON p.usuario_id = u.id "
                   + "ORDER BY p.id DESC";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                PermisoLaboral permiso = new PermisoLaboral();
                permiso.setId(rs.getInt("id"));
                permiso.setUsuarioId(rs.getInt("usuario_id"));
                permiso.setTipoPermiso(rs.getString("tipo_permiso"));
                
                // Mapeo seguro de fechas/timestamps a String para el Frontend
                permiso.setFechaAsignacion(rs.getTimestamp("fecha_asignacion").toString());
                permiso.setFechaInicio(rs.getString("fecha_inicio"));
                permiso.setFechaFin(rs.getString("fecha_fin"));
                permiso.setEstado(rs.getString("estado"));
                
                // Atributo de transferencia analítico para tu grilla visual
                permiso.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));
                
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
    public boolean actualizarEstadoPermiso(int id, String nuevoEstado) {
        String sql = "UPDATE permiso_laboral SET estado = ? WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, nuevoEstado.toLowerCase()); // 'aprobado' o 'rechazado' (ENUM de MySQL)
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

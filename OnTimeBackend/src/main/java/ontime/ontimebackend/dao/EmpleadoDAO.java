package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    /**
     * Consulta el listado general uniendo las tablas para la grilla de Gestión (RF23).
     */
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        
        // SQL optimizado agregando el apellido al reporte de gestión
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, u.estado, u.fotoPerfil_url, "
                + "t.telefono_celular, e.email, con.cargo "
                + "FROM usuario u "
                + "LEFT JOIN telefono_personal t ON u.id = t.usuario_id "
                + "LEFT JOIN email_personal e ON u.id = e.usuario_id "
                + "LEFT JOIN contrato con ON u.id = con.usuario_id "
                + "ORDER BY u.id DESC";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();
                
                // Corrección 1: Asignación numérica del ID de Ontime3BD
                emp.setId(rs.getInt("id")); 
                
                // Unimos nombre y apellido para pintar en la columna 'Nombre Completo'
                String apellido = rs.getString("apellido") != null ? rs.getString("apellido") : "";
                emp.setNombre(rs.getString("nombre") + " " + apellido);
                
                emp.setDocumento(rs.getString("documento_identidad"));
                
                String cargo = rs.getString("cargo");
                emp.setCargo(cargo != null ? cargo : "Sin asignar");
                
                emp.setEstado(rs.getString("estado"));
                emp.setFoto(rs.getString("fotoPerfil_url"));
                emp.setTelefonoCelular(rs.getString("telefono_celular") != null ? rs.getString("telefono_celular") : "N/A");
                emp.setEmail(rs.getString("email") != null ? rs.getString("email") : "N/A");
                
                lista.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Modifica los datos permitidos de un usuario existente (RF23 y RF25).
     */
    public boolean actualizarEmpleado(Empleado emp) {
        String sql = "UPDATE usuario SET nombre = ?, estado = ? WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getEstado().toLowerCase()); // Forzamos minúsculas para el ENUM de MySQL
            ps.setInt(3, emp.getId()); // Corrección de tipo String a int

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Aplica la Inactivación Lógica del personal en lugar de un DELETE físico (RF25).
     * Conserva el histórico transaccional de asistencias y nóminas intacto.
     */
    public boolean inactivarEmpleado(int id) {
        String sql = "UPDATE usuario SET estado = 'inactivo' WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

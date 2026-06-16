package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    /**
     * Consulta el listado general uniendo las tablas para la grilla de Gestión
     * (RF23).
     */
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();

        // SQL optimizado agregando el apellido al reporte de gestión
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, u.estado, u.fotoPerfil_url, u.direccion, " // ¡AQUÍ: Agregado u.direccion
                + "t.telefono_celular, e.email, con.cargo "
                + "FROM usuario u "
                + "LEFT JOIN telefono_personal t ON u.id = t.usuario_id "
                + "LEFT JOIN email_personal e ON u.id = e.usuario_id "
                + "LEFT JOIN contrato con ON u.id = con.usuario_id "
                + "ORDER BY u.id DESC";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();

                // Corrección 1: Asignación numérica del ID de Ontime3BD
                emp.setId(rs.getInt("id"));

                // Unimos nombre y apellido para pintar en la columna 'Nombre Completo'
                String nombreBd = rs.getString("nombre");
                String apellidoBd = rs.getString("apellido");

// Validamos de forma : si el nombre ya contiene el apellido dentro de la BD, no lo volvemos a sumar
                if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase())) {
                    emp.setNombre(nombreBd);
                } else {
                    emp.setNombre(nombreBd + " " + apellidoBd);
                }

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

        String sqlUsuario = "UPDATE usuario SET nombre = ?, apellido = '', estado = ?, direccion = ? WHERE id = ?";
        String sqlTelefono = "UPDATE telefono_personal SET telefono_celular = ? WHERE usuario_id = ?";
        String sqlContrato = "UPDATE contrato SET cargo = ? WHERE usuario_id = ?";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false);

            try (PreparedStatement psU = con.prepareStatement(sqlUsuario)) {
                psU.setString(1, emp.getNombre());
                psU.setString(2, emp.getEstado().toLowerCase());
                psU.setString(3, emp.getDireccion());
                psU.setInt(4, emp.getId());
                psU.executeUpdate();
            }

            try (PreparedStatement psT = con.prepareStatement(sqlTelefono)) {
                psT.setString(1, emp.getTelefonoCelular());
                psT.setInt(2, emp.getId());
                psT.executeUpdate();
            }

            try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                psC.setString(1, emp.getCargo());
                psC.setInt(2, emp.getId());
                psC.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Elimina de forma física un empleado en cascada relacional (RF25). Borra
     * primero las tablas hijas y finalmente remueve al usuario padre.
     */
    public boolean eliminarEmpleado(int id) {
        String sqlCred = "DELETE FROM credenciales WHERE usuario_id = ?";
        String sqlCont = "DELETE FROM contrato WHERE usuario_id = ?";
        String sqlTel = "DELETE FROM telefono_personal WHERE usuario_id = ?";
        String sqlEmail = "DELETE FROM email_personal WHERE usuario_id = ?";
        String sqlContE = "DELETE FROM contacto_emergencia WHERE usuario_id = ?";
        String sqlAsis = "DELETE FROM asistencia WHERE usuario_id = ?";
        String sqlPerm = "DELETE FROM permiso_laboral WHERE usuario_id = ?";
        String sqlUser = "DELETE FROM usuario WHERE id = ?";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción limpia: borra todo o nada

            try (PreparedStatement ps = con.prepareStatement(sqlCred)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlCont)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlTel)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlEmail)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlContE)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlAsis)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlPerm)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Finalmente, borramos al usuario padre de la tabla central
            int filasAfectadas;
            try (PreparedStatement ps = con.prepareStatement(sqlUser)) {
                ps.setInt(1, id);
                filasAfectadas = ps.executeUpdate();
            }

            con.commit(); // Consolidamos los cambios en MySQL
            return filasAfectadas > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

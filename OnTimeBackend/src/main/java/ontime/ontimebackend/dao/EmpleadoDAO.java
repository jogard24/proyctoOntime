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

        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, u.estado, u.fotoPerfil_url, u.direccion, "
                + "t.telefono_celular, e.email, con.cargo "
                + "FROM usuario u "
                + "LEFT JOIN telefono_personal t ON u.id = t.usuario_id "
                + "LEFT JOIN email_personal e ON u.id = e.usuario_id "
                + "LEFT JOIN contrato con ON u.id = con.usuario_id "
                + "ORDER BY u.id DESC";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setId(rs.getInt("id"));

                String nombreBd = rs.getString("nombre");
                String apellidoBd = rs.getString("apellido");
                if (apellidoBd == null) apellidoBd = "";

                if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase())) {
                    emp.setNombre(nombreBd);
                } else {
                    emp.setNombre(nombreBd + " " + apellidoBd);
                }

                emp.setDocumento(rs.getString("documento_identidad"));
                emp.setCargo(rs.getString("cargo") != null ? rs.getString("cargo") : "Sin asignar");
                emp.setEstado(rs.getString("estado"));
                emp.setFoto(rs.getString("fotoPerfil_url"));
                emp.setDireccion(rs.getString("direccion") != null ? rs.getString("direccion") : "N/A");
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
     * CORRECCIÓN DEL JURADO: Modifica datos en cascada lógica transaccional.
     * Sincroniza el rol ID numérico en la tabla de credenciales (RF23).
     */
    public boolean actualizarEmpleadoConRol(Empleado emp, int rolId) {
        String sqlUsuario = "UPDATE usuario SET nombre = ?, estado = ?, direccion = ? WHERE id = ?";
        String sqlTelefono = "UPDATE telefono_personal SET telefono_celular = ? WHERE usuario_id = ?";
        String sqlContrato = "UPDATE contrato SET cargo = ? WHERE usuario_id = ?";
        String sqlCredenciales = "UPDATE credenciales SET rol_id = ? WHERE usuario_id = ?";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción atómica manual segura

            // 1. Actualizar tabla central usuario (Garantiza campos no nulos)
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario)) {
                psU.setString(1, emp.getNombre());
                psU.setString(2, emp.getEstado().toLowerCase());
                psU.setString(3, (emp.getDireccion() != null && !emp.getDireccion().trim().isEmpty()) ? emp.getDireccion() : "N/A");
                psU.setInt(4, emp.getId());
                psU.executeUpdate();
            }

            // 2. Actualizar teléfono celular relacional
            try (PreparedStatement psT = con.prepareStatement(sqlTelefono)) {
                psT.setString(1, (emp.getTelefonoCelular() != null) ? emp.getTelefonoCelular() : "");
                psT.setInt(2, emp.getId());
                psT.executeUpdate();
            }

            // 3. Actualizar cargo textual en contrato
            try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                psC.setString(1, emp.getCargo());
                psC.setInt(2, emp.getId());
                psC.executeUpdate();
            }

            // 4. Actualizar el ID del rol en la tabla de seguridad credenciales
            try (PreparedStatement psR = con.prepareStatement(sqlCredenciales)) {
                psR.setInt(1, rolId);
                psR.setInt(2, emp.getId());
                psR.executeUpdate();
            }

            con.commit(); // Consolidamos de forma inmutable todas las tablas
            System.out.println(" ÉXITO: Transacción de actualización completada para ID " + emp.getId());
            return true;
        } catch (SQLException e) {
            System.out.println(" ERROR TRANSACCIONAL EN DAO: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * CORRECCIÓN EXIGIDA POR EL JURADO: Se eliminó el ON DELETE CASCADE físico.
     * Aplica BORRADO LÓGICO: Inactiva el estado del usuario y bloquea su acceso web/pinpad,
     * protegiendo de forma estricta los históricos de asistencia y nómina (RF25).
     */
    public boolean eliminarEmpleado(int id) {
        String sqlUsuario = "UPDATE usuario SET estado = 'inactivo' WHERE id = ?";
        String sqlCredenciales = "UPDATE credenciales SET activo = 0 WHERE usuario_id = ?";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción segura abierta

            // 1. Inactivar el perfil del usuario (Borrado lógico contractual)
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario)) {
                psU.setInt(1, id);
                psU.executeUpdate();
            }

            // 2. Bloquear credenciales de inicio de sesión y pinpad
            try (PreparedStatement psC = con.prepareStatement(sqlCredenciales)) {
                psC.setInt(1, id);
                psC.executeUpdate();
            }

            con.commit(); // Confirmamos los cambios de forma consistente
            System.out.println("BORRADO LÓGICO: Usuario ID " + id + " inactivado correctamente.");
            return true;
        } catch (SQLException e) {
            System.out.println("ERROR EN BORRADO LÓGICO: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // Método viejo de respaldo compatible por si alguna clase interna lo invoca
    public boolean actualizarEmpleado(Empleado emp) {
        return actualizarEmpleadoConRol(emp, 2);
    }
}


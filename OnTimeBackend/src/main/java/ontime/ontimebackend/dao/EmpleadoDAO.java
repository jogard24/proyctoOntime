package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Usuario;
import ontime.ontimebackend.modelo.Contrato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    // Método existente para listar empleados
    public List<Empleado> listarTodos() {
        List<Empleado> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.estado, u.fotoPerfil_url, "
                + "t.telefono_celular, e.email, ct.nombre AS contacto_nombre, ct.telefono AS contacto_telefono, "
                + "con.cargo "
                + "FROM usuario u "
                + "LEFT JOIN telefono_personal t ON u.id = t.usuario_id "
                + "LEFT JOIN email_personal e ON u.id = e.usuario_id "
                + "LEFT JOIN contacto_emergencia ct ON u.id = ct.usuario_id "
                + "LEFT JOIN contrato con ON u.id = con.usuario_id";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Empleado emp = new Empleado();
                emp.setId(rs.getString("id"));
                emp.setNombre(rs.getString("nombre"));
                emp.setDocumento(rs.getString("documento_identidad"));
                String cargo = rs.getString("cargo");
                emp.setCargo(cargo != null ? cargo : "Sin asignar");
                emp.setEstado(rs.getString("estado"));
                emp.setFoto(rs.getString("fotoPerfil_url"));
                emp.setTelefonoCelular(rs.getString("telefono_celular") != null ? rs.getString("telefono_celular") : "N/A");
                emp.setEmail(rs.getString("email") != null ? rs.getString("email") : "N/A");
                emp.setContactoEmergenciaNombre(rs.getString("contacto_nombre") != null ? rs.getString("contacto_nombre") : "N/A");
                emp.setContactoEmergenciaTelefono(rs.getString("contacto_telefono") != null ? rs.getString("contacto_telefono") : "N/A");
                lista.add(emp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Nuevo método para registro transaccional de Usuario y Contrato
     */
    public boolean registrarEmpleadoCompleto(Usuario usuario, Contrato contrato) {
        String sqlUsuario = "INSERT INTO usuario (nombre) VALUES (?)";
        String sqlContrato = "INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id) VALUES (?, ?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Inicia transacción

            // 1. Insertar Usuario
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psU.setString(1, usuario.getNombre());
                psU.executeUpdate();

                // Obtener el ID que se acaba de generar para el usuario
                try (ResultSet rs = psU.getGeneratedKeys()) {
                    if (rs.next()) {
                        int nuevoId = rs.getInt(1);

                        // 2. Insertar Contrato usando ese nuevo ID
                        try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                            psC.setInt(1, nuevoId);
                            psC.setString(2, contrato.getTipoContrato());
                            psC.setString(3, contrato.getCargo());
                            psC.setBigDecimal(4, contrato.getSalarioBase());
                            psC.setInt(5, contrato.getJornadaId());
                            psC.executeUpdate();
                        }
                    }
                }
            }
            con.commit(); // Si todo fue bien, guardamos cambios en BD
            return true;
        } catch (SQLException e) {
            try {
                if (con != null) {
                    con.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}

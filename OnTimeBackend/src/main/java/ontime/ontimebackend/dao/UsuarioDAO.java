package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Usuario;
import java.sql.*;

public class UsuarioDAO {

    public Usuario autenticar(String user, String pass) {
        // SQL ajustado a tu tabla real: usamos 'nombre'
// Modifica la consulta en UsuarioDAO.java
        String sql = "SELECT u.id, u.nombre, r.Rol "
                + "FROM usuario u "
                + "JOIN credenciales c ON u.id = c.usuario_id "
                + "JOIN roles r ON c.rol_id = r.id "
                + // Asumiendo que tienes una tabla roles
                "WHERE c.usuario = ? AND c.clave = ?";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, user);
            ps.setString(2, pass);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario userObj = new Usuario();
                    userObj.setId(rs.getInt("id"));
                    userObj.setNombre(rs.getString("nombre"));
                    userObj.setRol(rs.getString("Rol"));
                    return userObj;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Esto saldrá en la consola de NetBeans si hay error
        }
        return null;
    }
}

package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Credencial;
import java.sql.*;

public class CredencialDAO {

    /**
     * Valida las credenciales planas del usuario contra la base de datos Ontime3BD.
     * Retorna un objeto Credencial con su respectivo Rol si existe y está activo.
     */
    public Credencial validarLogin(String usuario, String clave) {
        String sql = "SELECT c.id, c.usuario_id, c.usuario, c.activo, r.Rol " +
                     "FROM credenciales c " +
                     "INNER JOIN roles r ON c.rol_id = r.id " +
                     "WHERE c.usuario = ? AND c.clave = ?";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, usuario);
            ps.setString(2, clave); // Validación directa de clave plana

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Credencial cred = new Credencial();
                    cred.setId(rs.getInt("id"));
                    cred.setUsuarioId(rs.getInt("usuario_id"));
                    cred.setUsuario(rs.getString("usuario"));
                    cred.setActivo(rs.getBoolean("activo"));
                    cred.setNombreRol(rs.getString("Rol")); // Captura el ENUM en texto
                    return cred;
                }
            }
        } catch (SQLException e) {
            System.err.println(" Error de SQL en CredencialDAO al validar el login:");
            e.printStackTrace();
        }
        return null; // Credenciales inválidas o usuario inexistente
    }
}


package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Credencial;
import java.sql.*;

public class CredencialDAO {

    /**
     * Valida las credenciales planas del usuario contra la base de datos .
     * Retorna un objeto Credencial con su respectivo Rol si existe y está activo si no , no devuelve nada.
     */
    // Método que valida el acceso de un usuario al sistema mediante sus credenciales.
    public Credencial validarLogin(String usuario, String clave) {
        String sql = "SELECT c.id, c.usuario_id, c.usuario, c.activo, r.Rol " +
                     "FROM credenciales c " +
                     "INNER JOIN roles r ON c.rol_id = r.id " +
                     "WHERE c.usuario = ? AND c.clave = ?  ";

        // Abre la conexión y prepara la consulta usando 'try-with-resources' para un cierre automático.
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            // Reemplaza los comodines '?' con los valores enviados desde el formulario de login.
            ps.setString(1, usuario);
            ps.setString(2, clave); // Validación directa de clave plana
        // Ejecuta la consulta en la base de datos y obtiene el ResultSet con las filas resultantes.
            try (ResultSet rs = ps.executeQuery()) {
                // Evaluamos con 'if' porque el usuario es único; solo esperamos 0 o 1 fila.
                if (rs.next()) {
                    // Si hay coincidencia, se crea la instancia y se extraen los datos de la cuenta.
                    Credencial cred = new Credencial();
                    cred.setId(rs.getInt("id"));
                    cred.setUsuarioId(rs.getInt("usuario_id"));
                    cred.setUsuario(rs.getString("usuario"));
                    cred.setActivo(rs.getBoolean("activo"));
                    cred.setNombreRol(rs.getString("Rol")); // Captura el ENUM en texto
                    return cred;// Retorna el objeto de la sesión activa con sus privilegios.
                }
            }
        } catch (SQLException e) {
            // Captura e informa fallos técnicos (pérdida de conexión, errores de sintaxis en la BD, etc.).
            System.err.println(" Error de SQL en CredencialDAO al validar el login:");
            e.printStackTrace();
        }
        return null; // Credenciales inválidas o usuario inexistente
    }
}


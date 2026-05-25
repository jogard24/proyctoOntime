package ontime.ontimebackend;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "LoginServlet", urlPatterns = {"/LoginServlet", "/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configuración de CORS y respuesta JSON
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        
        // Capturar los parámetros enviados desde el JS
        String txtUsuario = request.getParameter("usuario");
        String txtClave = request.getParameter("clave");
        
        if (txtUsuario == null || txtUsuario.trim().isEmpty() || txtClave == null || txtClave.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"Usuario y contraseña son requeridos!!.\"}");
            return;
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // Consulta relacional: valida credenciales, verifica que esté activo y trae el nombre del rol string
        String sql = "SELECT u.nombre_completo, r.nombre_rol, c.activo "
                   + "FROM credenciales c "
                   + "INNER JOIN usuario u ON c.usuario_id = u.id "
                   + "INNER JOIN roles r ON c.rol_id = r.id "
                   + "WHERE c.usuario = ? AND c.clave = ?";

        try {
            con = Conexion.obtenerConexion();
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Error de conexión con la base de datos.\"}");
                return;
            }
            
            ps = con.prepareStatement(sql);
            ps.setString(1, txtUsuario.trim());
            ps.setString(2, txtClave.trim());
            rs = ps.executeQuery();
            
            if (rs.next()) {
                boolean estaActivo = rs.getBoolean("activo");
                
                if (!estaActivo) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print("{\"status\":\"error\",\"message\":\"Esta cuenta se encuentra desactivada. Contacta soporte.\"}");
                    return;
                }
                
                String nombreEmpleado = rs.getString("nombre_completo");
                String rol = rs.getString("nombre_rol"); // 'administrador', 'contador', etc.
                
                // Respuesta exitosa enviando los datos necesarios para armar la sesión en el frontend
                out.print("{\"status\":\"success\","
                        + "\"message\":\"¡Bienvenido al sistema! \","
                        + "\"nombre\":\"" + nombreEmpleado + "\","
                        + "\"rol\":\"" + rol + "\"}");
            } else {
                // Las credenciales no coinciden en la base de datos
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"status\":\"error\",\"message\":\"Usuario o contraseña incorrectos!!.\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorLimpio = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Error desconocido";
            out.print("{\"status\":\"error\",\"message\":\"Error interno: " + errorLimpio + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
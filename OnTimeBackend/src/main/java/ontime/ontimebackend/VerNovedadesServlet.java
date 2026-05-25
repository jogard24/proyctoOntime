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

@WebServlet(name = "VerNovedadesServlet", urlPatterns = {"/VerNovedadesServlet", "/novedades"})
public class VerNovedadesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configuración de CORS y JSON
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        // Query relacional para traer los datos clave de la marcación junto con el nombre del empleado
        String sql = "SELECT a.id, u.nombre_completo, a.tipo_evento, a.fecha_hora, a.observacion, a.tipo_turno "
                   + "FROM asistencia a "
                   + "INNER JOIN usuario u ON a.usuario_id = u.id "
                   + "ORDER BY a.fecha_hora DESC";

        try {
            con = Conexion.obtenerConexion();
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"No hay conexión con la BD.\"}");
                return;
            }

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            // Construcción manual del JSON Array para evitar dependencias externas pesadas
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            while (rs.next()) {
                json.append("{");
                json.append("\"id\":").append(rs.getInt("id")).append(",");
                json.append("\"empleado\":\"").append(rs.getString("nombre_completo")).append("\",");
                json.append("\"evento\":\"").append(rs.getString("tipo_evento")).append("\",");
                json.append("\"fecha_hora\":\"").append(rs.getTimestamp("fecha_hora").toString()).append("\",");
                json.append("\"observacion\":\"").append(rs.getString("observacion")).append("\",");
                json.append("\"turno\":\"").append(rs.getString("tipo_turno")).append("\"");
                json.append("},");
            }
            
            // Quitar la última coma sobrante si el array contiene elementos
            if (json.length() > 1) {
                json.deleteCharAt(json.length() - 1);
            }
            json.append("]");

            out.print(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorLimpio = e.getMessage() != null ? e.getMessage().replace("\"", "'") : "Error";
            out.print("{\"status\":\"error\",\"message\":\"" + errorLimpio + "\"}");
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
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

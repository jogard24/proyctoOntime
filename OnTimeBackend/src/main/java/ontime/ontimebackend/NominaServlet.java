package ontime.ontimebackend;

import ontime.ontimebackend.conexion.Conexion;
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

@WebServlet(name = "NominaServlet", urlPatterns = {"/NominaServlet", "/nomina"})
public class NominaServlet extends HttpServlet {

    // Método para agregar las cabeceras CORS en cada respuesta
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600"); // Cachea la respuesta CORS por 1 hora
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Aplicamos CORS al inicio
        configurarCORS(response);
        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

String sql = "SELECT u.id, u.documento_identidad, u.nombre_completo, "
           + "COALESCE(COUNT(DISTINCT DATE(a.fecha_hora)), 0) as dias_trabajados, "
           + "COALESCE(SUM(CASE WHEN a.observacion LIKE '%extras%' THEN 1 ELSE 0 END), 0) as total_extras, "
           + "COALESCE(SUM(CASE WHEN a.observacion LIKE '%Retardo%' THEN 1 ELSE 0 END), 0) as total_retardos "
           + "FROM usuario u "
           + "LEFT JOIN asistencia a ON u.id = a.usuario_id "
           + "GROUP BY u.id, u.documento_identidad, u.nombre_completo";

        try {
            con = Conexion.obtenerConexion();
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"Error de conexión con la base de datos.\"}");
                return;
            }

            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean esPrimero = true;
            while (rs.next()) {
                if (!esPrimero) {
                    json.append(",");
                }
                esPrimero = false;

                json.append("{")
                    .append("\"id\":").append(rs.getInt("id")).append(",")
                    .append("\"cedula\":\"").append(rs.getString("documento_identidad")).append("\",")
                    .append("\"nombre\":\"").append(rs.getString("nombre_completo")).append("\",")
                    .append("\"diasTrabajados\":").append(rs.getInt("dias_trabajados")).append(",")
                    .append("\"novedadesExtras\":").append(rs.getInt("total_extras")).append(",")
                    .append("\"novedadesRetardos\":").append(rs.getInt("total_retardos"))
                    .append("}");
            }
            json.append("]");

            response.setStatus(HttpServletResponse.SC_OK);
            out.print(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorLimpio = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Error desconocido";
            out.print("{\"status\":\"error\",\"message\":\"Error en servidor de nómina: " + errorLimpio + "\"}");
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (ps != null) ps.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Obligatorio para peticiones preflight del navegador
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
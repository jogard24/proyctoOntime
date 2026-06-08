package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO;
import ontime.ontimebackend.modelo.Asistencia;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "VerNovedadesServlet", urlPatterns = {"/VerNovedadesServlet", "/novedades"})
public class VerNovedadesServlet extends HttpServlet {

    // Inyectamos el DAO que ya tiene la lógica centralizada
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // CORS y Cabeceras
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();

        try {
            // Llamamos al método que creamos en el DAO
            List<Asistencia> lista = asistenciaDAO.listarNovedadesRecientes();
            
            // Construimos el JSON (puedes usar GSON si lo prefieres, o este método manual)
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                Asistencia a = lista.get(i);
                json.append("{");
                json.append("\"id\":").append(a.getId()).append(",");
                json.append("\"empleado\":\"").append(a.getNombreEmpleado()).append("\",");
                json.append("\"evento\":\"").append(a.getTipoEvento()).append("\",");
                json.append("\"fecha_hora\":\"").append(a.getFechaHora()).append("\",");
                json.append("\"observacion\":\"").append(a.getObservacion()).append("\",");
                json.append("\"nombre_turno\":\"").append(a.getTipoTurno()).append("\"");
                json.append("}");
                if (i < lista.size() - 1) json.append(",");
            }
            json.append("]");
            
            out.print(json.toString());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
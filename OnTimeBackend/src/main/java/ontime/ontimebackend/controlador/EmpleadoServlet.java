package ontime.ontimebackend.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.dao.EmpleadoDAO;

@WebServlet(name = "EmpleadoServlet", urlPatterns = {"/EmpleadoServlet"})
public class EmpleadoServlet extends HttpServlet {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    // Método auxiliar para evitar que caracteres especiales rompan el JSON
    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Empleado> empleados = empleadoDAO.listarTodos();

            StringBuilder json = new StringBuilder();
            json.append("[");
            
            for (int i = 0; i < empleados.size(); i++) {
                Empleado emp = empleados.get(i);
                
                json.append("{");
                json.append("\"id\":\"").append(escaparJson(emp.getId())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\",");
                json.append("\"documento\":\"").append(escaparJson(emp.getDocumento())).append("\",");
                json.append("\"cargo\":\"").append(escaparJson(emp.getCargo())).append("\",");
                json.append("\"estado\":\"").append(escaparJson(emp.getEstado())).append("\",");
                json.append("\"fotoPerfilUrl\":\"").append(escaparJson(emp.getFoto())).append("\"");
                json.append("}");
                
                if (i < empleados.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            out.print(json.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
            out.flush();
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

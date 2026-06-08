package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.NominaDAO;
import ontime.ontimebackend.modelo.Nomina;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "NominaServlet", urlPatterns = {"/NominaServlet"})
public class NominaServlet extends HttpServlet {

    private final NominaDAO nominaDAO = new NominaDAO();


    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Consulta y transmite la grilla financiera de tiempos filtrada por periodo (GET - RF17).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // Captura el periodo seleccionado en el input del frontend (ej: "2026-06")
        String periodo = request.getParameter("periodo");
        if (periodo == null || periodo.trim().isEmpty()) {
            periodo = "2026-06"; // Respaldo por defecto seguro
        }

        try {
            List<Nomina> listaNomina = nominaDAO.obtenerReporteNomina(periodo);
            
            // Renderizado de JSON nativo y directo en Java Puro básico sin frameworks
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < listaNomina.size(); i++) {
                Nomina n = listaNomina.get(i);
                json.append("{");
                json.append("\"id\":").append(n.getUsuarioId()).append(",");
                json.append("\"documento\":\"").append(escaparJson(n.getDocumento())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(n.getNombre())).append("\",");
                json.append("\"apellido\":\"").append(escaparJson(n.getApellido())).append("\",");
                json.append("\"salarioBase\":").append(n.getSalarioBasePeriodo()).append(",");
                json.append("\"totalRetardos\":").append(n.getTotalRetardos()).append(",");
                json.append("\"totalExtras\":").append(n.getTotalExtras());
                json.append("}");
                if (i < listaNomina.size() - 1) json.append(",");
            }
            json.append("]");
            
            out.print(json.toString());
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Procesa la consolidación final e inyección en las tablas de auditoría de nómina (POST - RF19).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        String periodo = request.getParameter("periodo");

        if ("guardarPeriodo".equals(accion) && periodo != null) {
            try {
                // Recupera la lista analítica del mes para congelar los registros de forma masiva
                List<Nomina> lista = nominaDAO.obtenerReporteNomina(periodo);
                boolean completado = true;

                for (Nomina n : lista) {
                    BigDecimal base = n.getSalarioBasePeriodo();
                    
                    // Aplicamos los mismos factores matemáticos pactados con tu frontend (RF17)
                    BigDecimal deducciones = BigDecimal.valueOf(n.getTotalRetardos() * 15000L);
                    BigDecimal bonificaciones = BigDecimal.valueOf(n.getTotalExtras() * 20000L);
                    BigDecimal neto = base.subtract(deducciones).add(bonificaciones);

                    // Inserta el registro en cascada relacional dentro de Ontime3BD
                    boolean r = nominaDAO.guardarNominaPeriodo(n.getUsuarioId(), base, n.getTotalExtras(), neto, periodo);
                    if (!r) completado = false;
                }

                if (completado) {
                    response.getWriter().print("{\"status\":\"success\",\"message\":\"Nómina cerrada con éxito.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("{\"status\":\"error\",\"message\":\"Algunos registros de usuario no pudieron congelarse.\"}");
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

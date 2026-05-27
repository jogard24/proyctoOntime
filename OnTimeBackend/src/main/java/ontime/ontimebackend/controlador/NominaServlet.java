package ontime.ontimebackend.controlador;

import com.google.gson.Gson;
import ontime.ontimebackend.dao.NominaDAO;
import ontime.ontimebackend.modelo.Nomina;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "NominaServlet", urlPatterns = {"/NominaServlet", "/nomina"})
public class NominaServlet extends HttpServlet {

    // Inyectamos el DAO. El Servlet no debe saber cómo se conecta a la BD, solo llama al DAO.
    private final NominaDAO nominaDAO = new NominaDAO();
    // Usamos Gson para convertir la lista de objetos a JSON automáticamente
    private final Gson gson = new Gson();

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        configurarCORS(response);
        response.setContentType("application/json;charset=UTF-8");

        try {
            // Llamamos al DAO para obtener los datos procesados
            List<Nomina> listaNomina = nominaDAO.obtenerReporteNomina();
            
            // Convertimos la lista a JSON y la enviamos
            String jsonRespuesta = gson.toJson(listaNomina);
            response.getWriter().write(jsonRespuesta);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
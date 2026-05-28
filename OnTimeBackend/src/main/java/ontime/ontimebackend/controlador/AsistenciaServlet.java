package ontime.ontimebackend.controlador;
// Librería GSON: fundamental para convertir objetos Java a formato JSON (el estándar de la web)
import com.google.gson.Gson;
import ontime.ontimebackend.dao.AsistenciaDAO;
import ontime.ontimebackend.modelo.Asistencia;
// IMPORTACIONES CORRECTAS PARA JAKARTA EE
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

// @WebServlet: Define la URL que el frontend debe invocar para acceder a este controlador
@WebServlet("/AsistenciaServlet")
public class AsistenciaServlet extends HttpServlet {
    
    // Instancia del DAO: El Servlet delega la lógica de acceso a datos a esta clase especialista
    private AsistenciaDAO asistenciaDAO = new AsistenciaDAO();
    // doGet: Maneja las peticiones HTTP GET (cuando el frontend pide datos, no cuando envía información privada)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 1. Solicitamos al DAO que consulte la BD y nos devuelva una lista de objetos 'Asistencia'
        List<Asistencia> lista = asistenciaDAO.listarAsistencia();
        // 2. Convertimos la lista de objetos Java a una cadena de texto en formato JSON
        // Esto permite que el JavaScript pueda leer la respuesta fácilmente
        String json = new Gson().toJson(lista);
        // 3. Configuramos la respuesta HTTP: informamos al navegador que lo que enviamos es un JSON
        response.setContentType("application/json");
        // 4. Aseguramos codificación UTF-8 para que las tildes y eñes se vean correctamente en la web
        response.setCharacterEncoding("UTF-8");
        // 5. Escribimos el JSON en el cuerpo de la respuesta para que el frontend lo reciba
        response.getWriter().write(json);
    }
}
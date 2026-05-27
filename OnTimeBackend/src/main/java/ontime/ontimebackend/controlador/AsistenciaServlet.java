package ontime.ontimebackend.controlador;

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

@WebServlet("/AsistenciaServlet")
public class AsistenciaServlet extends HttpServlet {
    private AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<Asistencia> lista = asistenciaDAO.listarAsistencia();
        
        // Convertimos la lista a JSON usando GSON
        String json = new Gson().toJson(lista);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(json);
    }
}
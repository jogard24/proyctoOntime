package ontime.ontimebackend.controlador;

import com.google.gson.Gson;
import ontime.ontimebackend.dao.PermisoDAO;
import ontime.ontimebackend.modelo.PermisoLaboral;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "PermisoServlet", urlPatterns = {"/PermisoServlet"})
public class PermisoServlet extends HttpServlet {
    private final PermisoDAO permisoDAO = new PermisoDAO();
    private final Gson gson = new Gson();

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        List<PermisoLaboral> permisos = permisoDAO.listarPermisos();
        response.getWriter().write(gson.toJson(permisos));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PermisoLaboral permiso = gson.fromJson(leerBody(request), PermisoLaboral.class);
        if (permiso == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Cuerpo JSON inválido\"}");
            return;
        }

        boolean creado = permisoDAO.crearPermiso(permiso);
        response.getWriter().write(gson.toJson(new Respuesta(creado ? "ok" : "error")));
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        if (idParam == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"ID requerido\"}");
            return;
        }

        try {
            boolean eliminado = permisoDAO.eliminarPermiso(Integer.parseInt(idParam));
            response.getWriter().write(gson.toJson(new Respuesta(eliminado ? "ok" : "error")));
        } catch (NumberFormatException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"ID inválido\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String leerBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                sb.append(linea);
            }
        }
        return sb.toString();
    }

    private static class Respuesta {
        private final String status;

        public Respuesta(String status) {
            this.status = status;
        }
    }
}

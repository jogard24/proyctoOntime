package ontime.ontimebackend.controlador;

import com.google.gson.Gson;
import ontime.ontimebackend.dao.HorarioDAO;
import ontime.ontimebackend.modelo.Horario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "HorarioServlet", urlPatterns = {"/HorarioServlet"})
public class HorarioServlet extends HttpServlet {

    private final HorarioDAO horarioDAO = new HorarioDAO();
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

        List<Horario> horarios = horarioDAO.listarHorarios();
        response.getWriter().write(gson.toJson(horarios));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Horario horario = gson.fromJson(leerBody(request), Horario.class);
        if (horario == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Cuerpo JSON inválido\"}");
            return;
        }

        boolean creado = horarioDAO.crearHorario(horario);
        response.getWriter().write(gson.toJson(new Respuesta(creado ? "ok" : "error")));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Horario horario = gson.fromJson(leerBody(request), Horario.class);
        if (horario == null || horario.getId() == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Horario inválido\"}");
            return;
        }

        boolean actualizado = horarioDAO.actualizarHorario(horario);
        response.getWriter().write(gson.toJson(new Respuesta(actualizado ? "ok" : "error")));
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

        boolean eliminado = horarioDAO.eliminarHorario(Integer.parseInt(idParam));
        response.getWriter().write(gson.toJson(new Respuesta(eliminado ? "ok" : "error")));
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

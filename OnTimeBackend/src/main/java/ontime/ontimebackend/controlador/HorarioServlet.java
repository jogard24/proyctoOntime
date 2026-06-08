package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.HorarioDAO;
import ontime.ontimebackend.modelo.Horario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "HorarioServlet", urlPatterns = {"/HorarioServlet"})
public class HorarioServlet extends HttpServlet {

    private final HorarioDAO horarioDAO = new HorarioDAO();


    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<Horario> horarios = horarioDAO.listarHorarios();
        
        // Renderizado de JSON manual sin librerías externas para máxima estabilidad académica
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < horarios.size(); i++) {
            Horario h = horarios.get(i);
            json.append("{");
            json.append("\"id\":").append(h.getId()).append(",");
            json.append("\"nombrejornada\":\"").append(escaparJson(h.getNombrejornada())).append("\",");
            json.append("\"horaEntrada\":\"").append(escaparJson(h.getHoraEntrada())).append("\",");
            json.append("\"horaSalida\":\"").append(escaparJson(h.getHoraSalida())).append("\"");
            json.append("}");
            if (i < horarios.size() - 1) json.append(",");
        }
        json.append("]");
        out.print(json.toString());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Captura el parámetro plano 'accion' enviado por tu horarios.js
        String accion = request.getParameter("accion");
        String idStr = request.getParameter("id");
        
        if (accion == null || idStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Parámetros faltantes\"}");
            return;
        }

        int id = Integer.parseInt(idStr.trim());
        boolean resultado = false;

        try {
            if ("eliminar".equals(accion)) {
                resultado = horarioDAO.eliminarHorario(id);
            } else {
                // Para 'crear' o 'actualizar', construimos el objeto Horario desde los parámetros planos
                Horario h = new Horario();
                h.setId(id);
                h.setNombrejornada(request.getParameter("nombrejornada"));
                h.setHoraEntrada(request.getParameter("horaEntrada"));
                h.setHoraSalida(request.getParameter("horaSalida"));

                if ("crear".equals(accion)) {
                    resultado = horarioDAO.crearHorario(h);
                } else if ("actualizar".equals(accion)) {
                    resultado = horarioDAO.actualizarHorario(h);
                }
            }

            if (resultado) {
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Operación rechazada por la Base de Datos.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

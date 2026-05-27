package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@WebServlet("/PinpadServlet")
public class PinpadServlet extends HttpServlet {
    private AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String docIdentidad = request.getParameter("usuarioId");
        
        if (docIdentidad == null || docIdentidad.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"exito\": false, \"message\": \"Documento requerido.\"}");
            return;
        }

        if (!asistenciaDAO.existeEmpleado(docIdentidad)) {
            response.getWriter().write("{\"exito\": false, \"message\": \"Documento no registrado en el sistema.\"}");
            return;
        }

        int usuarioId = asistenciaDAO.obtenerIdPorDocumento(docIdentidad);
        String nombreEmpleado = asistenciaDAO.obtenerNombrePorDocumento(docIdentidad);
        
        String ultimoEvento = asistenciaDAO.obtenerUltimoTipoEvento(usuarioId);
        String nuevoEvento = ultimoEvento.equals("entrada") ? "salida" : "entrada";
        
        // --- LÓGICA DE OBSERVACIÓN INTELIGENTE ---
        String observacion = "Registro desde Pinpad";
        LocalTime horaActual = LocalTime.now();
        LocalTime horaLimite = asistenciaDAO.obtenerHoraEntrada(usuarioId);
        if (nuevoEvento.equals("entrada")) {
            if (horaActual.isBefore(horaLimite)) {
                observacion = "Ingreso a tiempo.";
            } else {
                long minutosTarde = ChronoUnit.MINUTES.between(horaLimite, horaActual);
                observacion = "Retardo de " + minutosTarde + " minutos.";
            }
        } else {
            observacion = "Salida registrada.";
        }
        
        // --- REGISTRO ---
        // Se pasan los 5 parámetros que tu AsistenciaDAO espera
        boolean registrado = asistenciaDAO.registrarAsistencia(usuarioId, nuevoEvento, observacion, "General", 102);

        if (registrado) {
            String mensaje = "¡Hola, " + nombreEmpleado + "! Marcaje de " + nuevoEvento + " exitoso.";
            response.getWriter().write("{\"exito\": true, \"message\": \"" + mensaje + "\", \"evento\": \"" + nuevoEvento + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"exito\": false, \"message\": \"Error al guardar en base de datos.\"}");
        }
    }
}
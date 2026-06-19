package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter; 
import java.time.LocalTime;
import java.time.ZoneId; // Inyectado para el soporte regional de Colombia
import java.time.temporal.ChronoUnit;

@WebServlet("/PinpadServlet")
public class PinpadServlet extends HttpServlet {

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String docIdentidad = request.getParameter("documento");

        if (docIdentidad == null || docIdentidad.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"Documento requerido.\"}");
            out.flush();
            return;
        }

        if (!asistenciaDAO.existeEmpleado(docIdentidad)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"El documento ingresado no se encuentra registrado en OnTime.\"}");
            out.flush();
            return;
        }

        int usuarioId = asistenciaDAO.obtenerIdPorDocumento(docIdentidad);
        String nombreEmpleado = asistenciaDAO.obtenerNombrePorDocumento(docIdentidad);

        String ultimoEvento = asistenciaDAO.obtenerUltimoTipoEvento(usuarioId);
        String nuevoEvento = ultimoEvento.equals("entrada") ? "salida" : "entrada";

        String observacion = "Registro Pinpad";

        // =========================================================================
        // ⏰ CORRECCIÓN CRÍTICA DE ZONA HORARIA: COLOMBIA (BOGOTÁ)
        // =========================================================================
        // Forzamos la captura horaria exacta de la región de Colombia burlando el desfase UTC
        ZoneId zonaColombia = ZoneId.of("America/Bogota");
        LocalTime horaActual = LocalTime.now(zonaColombia); 
        // =========================================================================

        StringBuilder horaEntradaStr = new StringBuilder();
        int jornadaId = asistenciaDAO.obtenerJornadaIdYHoraEntrada(usuarioId, horaEntradaStr);
        LocalTime horaLimite = LocalTime.parse(horaEntradaStr.toString());

        if (nuevoEvento.equals("entrada")) {
            if (horaActual.isBefore(horaLimite)) {
                observacion = "Ingreso a tiempo.";
            } else {
                long minutosTarde = ChronoUnit.MINUTES.between(horaLimite, horaActual);
                observacion = "Retardo de " + minutosTarde + " minutos.";
            }
        } else {
            // MOTOR AUTOMÁTICO DE HORAS EXTRAS 
            LocalTime horaSalidaOficial = LocalTime.of(17, 0, 0); 

            if (horaActual.isAfter(horaSalidaOficial.plusMinutes(2))) {
                long minutosExtras = ChronoUnit.MINUTES.between(horaSalidaOficial, horaActual);
                long horasExtras = minutosExtras / 60;

                observacion = "Trabajo adicional. " + horasExtras + " hora(s) extra(s).";
            } else {
                observacion = "Salida registrada."; 
            }
        }

        boolean registrado = asistenciaDAO.registrarAsistencia(usuarioId, nuevoEvento, observacion, jornadaId);

        if (registrado) {
            response.setStatus(HttpServletResponse.SC_OK);

            String mensaje = "¡Hola, " + nombreEmpleado + "! Tu marcaje de " + nuevoEvento.toUpperCase() + " ha sido exitoso.";

            PrintWriter out = response.getWriter();
            out.print("{"
                    + "\"status\":\"success\","
                    + "\"message\":\"" + mensaje + "\","
                    + "\"evento\":\"" + nuevoEvento + "\""
                    + "}");
            out.flush(); 
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"Error interno en la base de datos al salvar la marca.\"}");
            out.flush();
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter; // Inyectado para el manejo correcto de flujos de red
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

@WebServlet("/PinpadServlet")
public class PinpadServlet extends HttpServlet {

    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Ajuste 1: Captura del documento según la clave unificada de pinpad.js
        String docIdentidad = request.getParameter("documento");

        // Validación básica de seguridad
        if (docIdentidad == null || docIdentidad.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"Documento requerido.\"}");
            out.flush();
            return;
        }

        // 2. Verificación: ¿Existe el empleado en la BD?
        if (!asistenciaDAO.existeEmpleado(docIdentidad)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"El documento ingresado no se encuentra registrado en OnTime.\"}");
            out.flush();
            return;
        }

        // 3. Obtención de datos: Buscamos ID y Nombre basados en el documento
        int usuarioId = asistenciaDAO.obtenerIdPorDocumento(docIdentidad);
        String nombreEmpleado = asistenciaDAO.obtenerNombrePorDocumento(docIdentidad);

        // 4. Lógica de Alternancia Automatizada (Entrada / Salida - RF10)
        String ultimoEvento = asistenciaDAO.obtenerUltimoTipoEvento(usuarioId);
        String nuevoEvento = ultimoEvento.equals("entrada") ? "salida" : "entrada";

        String observacion = "Registro Pinpad";
        
        // 5. Lógica Analítica de Observación: Calculamos si llega tarde o puntual 
        LocalTime horaActual = LocalTime.now(); // Captura la hora exacta del toque de pantalla
        
        // Extrae la jornada contractual real del empleado mediante el DAO de Ontime3BD
        StringBuilder horaEntradaStr = new StringBuilder();
        int jornadaId = asistenciaDAO.obtenerJornadaIdYHoraEntrada(usuarioId, horaEntradaStr);
        LocalTime horaLimite = LocalTime.parse(horaEntradaStr.toString());

        if (nuevoEvento.equals("entrada")) {
            if (horaActual.isBefore(horaLimite)) {
                observacion = "Ingreso a tiempo."; // Puntual
            } else {
                // Cálculo automático de los minutos de retardo
                long minutesTarde = ChronoUnit.MINUTES.between(horaLimite, horaActual);
                observacion = "Retardo de " + minutesTarde + " minutos.";
            }
        } else {
            observacion = "Salida registrada.";
        }

        // 6. Registro: Guardamos la asistencia pasando los parámetros limpios de Ontime3BD
        boolean registrado = asistenciaDAO.registrarAsistencia(usuarioId, nuevoEvento, observacion, jornadaId);

        // 7. Respuesta: Notificamos al usuario en el frontend con vaciado de buffer (flush)
        if (registrado) {
            response.setStatus(HttpServletResponse.SC_OK);
            
            String mensaje = "¡Hola, " + nombreEmpleado + "! Tu marcaje de " + nuevoEvento.toUpperCase() + " ha sido exitoso.";
            
            PrintWriter out = response.getWriter();
            out.print("{"
                    + "\"status\":\"success\","
                    + "\"message\":\"" + mensaje + "\","
                    + "\"evento\":\"" + nuevoEvento + "\""
                    + "}");
            out.flush(); // Empuja los bytes inmediatamente por el Wi-Fi hacia el celular
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


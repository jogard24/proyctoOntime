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

        // 1. Captura del ID: Obtenemos el documento ingresado en el pinpad
        String docIdentidad = request.getParameter("usuarioId");

        // Validación básica: Si no viene nada, devolvemos 
        if (docIdentidad == null || docIdentidad.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"exito\": false, \"message\": \"Documento requerido.\"}");
            return;
        }

        // 2. Verificación: ¿Existe el empleado en la BD?
        if (!asistenciaDAO.existeEmpleado(docIdentidad)) {
            response.getWriter().write("{\"exito\": false, \"message\": \"Documento no registrado en el sistema.\"}");
            return;
        }

        // 3. Obtención de datos: Buscamos ID y Nombre basados en el documento
        int usuarioId = asistenciaDAO.obtenerIdPorDocumento(docIdentidad);
        String nombreEmpleado = asistenciaDAO.obtenerNombrePorDocumento(docIdentidad);

        // 4. Lógica de Alternancia: Si el último evento fue "entrada", el nuevo será "salida"
        String ultimoEvento = asistenciaDAO.obtenerUltimoTipoEvento(usuarioId);
        String nuevoEvento = ultimoEvento.equals("entrada") ? "salida" : "entrada";

        //declaramos la variable 
        String observacion = "registro pinpad";
        
// 5. Lógica de Observación : Calculamos si llega tarde o no
        LocalTime horaActual = LocalTime.now();//Captura la hora, minuto y segundo exacto en el que el empleado está tocando la pantalla
        //Va a la base de datos a buscar el horario asignado a ese empleado 
        //(ej. su hora de entrada oficial es 08:00 AM).
        LocalTime horaLimite = asistenciaDAO.obtenerHoraEntrada(usuarioId);
        if (nuevoEvento.equals("entrada")) {//si la acción actual es una "entrada".
            if (horaActual.isBefore(horaLimite)) {//Significa "es antes de". Es el equivalente a usar el símbolo < pero especializado para horas.
                //Si la acción NO es una entrada
                // el código no calcula retardos. Simplemente escribe en la libreta: 
                //"Salida registrada." y termina.
                observacion = "Ingreso a tiempo.";
                //Si la horaActual (ej. 07:55 AM) es antes de su horaLimite 
                //(08:00 AM), ¡felicidades! El empleado fue puntual. La máquina anota: "Ingreso a tiempo."
            } else {
                long minutosTarde = ChronoUnit.MINUTES.between(horaLimite, horaActual);
                observacion = "Retardo de " + minutosTarde + " minutos.";
            }
        } else {
            observacion = "Salida registrada.";
        }

        // --- REGISTRO ---
        // Se pasan los 5 parámetros que tu AsistenciaDAO espera
        // 6. Registro: Guardamos la asistencia en la BD a través del DAO
        boolean registrado = asistenciaDAO.registrarAsistencia(usuarioId, nuevoEvento, observacion, "General", 102);

        // 7. Respuesta: Notificamos al usuario en el frontend
        if (registrado) {
            String mensaje = "¡Hola, " + nombreEmpleado + "! Marcaje de " + nuevoEvento + " exitoso.";
            response.getWriter().write("{\"exito\": true, \"message\": \"" + mensaje + "\", \"evento\": \"" + nuevoEvento + "\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"exito\": false, \"message\": \"Error al guardar en base de datos.\"}");
        }
    }
}

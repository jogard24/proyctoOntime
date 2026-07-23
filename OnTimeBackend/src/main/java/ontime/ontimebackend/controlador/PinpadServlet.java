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

    //Se crea un objeto DAO para interactuar con la base de datos de asistencias.
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    @Override//método se ejecuta cuando el cliente hace una petición POST al servlet
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");//Esto le dice al navegador o aplicación que debe interpretar el contenido como datos en formato JSON.
        response.setCharacterEncoding("UTF-8");
//se ejecuta request.getParameter "documento" para desempaquetar la cédula
        String docIdentidad = request.getParameter("documento");
// Valida  que el input de la cédula no viaje nulo ni con espacios vacíos
        if (docIdentidad == null || docIdentidad.trim().isEmpty()) {
        // Error 400 Bad Request (Petición incorrecta o mal estructurada por el cliente)
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"Documento requerido.\"}");
            out.flush();// Limpia los buffers de red y fuerza el envío inmediato de los bytes
            return;// Aborta la ejecución del hilo en el servidor para proteger la integridad
        }
//Consulta por base de datos si la cédula existe físicamente en la tabla 'usuario'
        if (!asistenciaDAO.existeEmpleado(docIdentidad)) {
            //si el dato no existe arroja error 
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"El documento ingresado no se encuentra registrado o esta inactivo.\"}");
            out.flush();
            return;// Interrumpe el flujo y expulsa la petición para evitar transacciones con datos fantasma
        }
//llamamos al metodo dao para consultar la bd 
        int usuarioId = asistenciaDAO.obtenerIdPorDocumento(docIdentidad);
//llamamos al metodo para reflejar el nombre del usuario 
        String nombreEmpleado = asistenciaDAO.obtenerNombrePorDocumento(docIdentidad);
//ultimo evento para saber si entro o salio 
        String ultimoEvento = asistenciaDAO.obtenerUltimoTipoEvento(usuarioId);
        //operador ternario simple
//estructura de control ej if else ,condicion ultimoevento es igual a entrada? si es true asigna salida de lo contrario entrada
        String nuevoEvento = ultimoEvento.equals("entrada") ? "salida" : "entrada";
//Si más adelante el empleado resulta ser perfectamente puntual y no entra en ninguna de las condiciones de 
//"llegó tarde" o "hizo horas extras", la variable ya tiene un contenido asignado de forma segura
        String observacion = "Registro Pinpad";

        
        // ZONA HORARIA ACTUAL
        // api java.time :Forzamos la captura horaria exacta de la región de Colombia 
        ZoneId zonaColombia = ZoneId.of("America/Bogota");
        LocalTime horaActual = LocalTime.now(zonaColombia); 

        //Almacenará por referencia la hora contractual de entrada extraída de MySQL
        StringBuilder horaEntradaStr = new StringBuilder();
        //Una vez definido el tipo de evento, Java extrae la jornada laboral contractual sujeta a ese empleado
        int jornadaId = asistenciaDAO.obtenerJornadaIdYHoraEntrada(usuarioId, horaEntradaStr);
// Transforma la cadena de texto de la base de datos en un objeto LocalTime comparable
        LocalTime horaLimite = LocalTime.parse(horaEntradaStr.toString());

        //FILTRO
    //Evaluación del cumplimiento horario en base al tipo de evento calculado
        if (nuevoEvento.equals("entrada")) { // EVALUACIÓN DE INGRESO LABORAL
       // Evalúa si el toque de pantalla actual ocurrió antes de la hora límite de su jornada laboral
            if (horaActual.isBefore(horaLimite)) {
                observacion = "Ingreso a tiempo.";
            } else {
                    //Cálculo Analítico de Minutos: Utiliza la función ChronoUnit.MINUTES.between() 
    //para restar la hora real frente a la hora contractual de su jornada
                long minutosTarde = ChronoUnit.MINUTES.between(horaLimite, horaActual);
                observacion = "Retardo de " + minutosTarde + " minutos.";
            }
        } else {
            // Se define la hora de salida oficial estándar (17:00:00)
            LocalTime horaSalidaOficial = LocalTime.of(17, 0, 0); 
            
            // Evalúa si el empleado marcó su salida después de superar el umbral de gracia de 60 minutos adicionales
            if (horaActual.isAfter(horaSalidaOficial.plusMinutes(60))) {
                // Métrica de Extras: Calcula la distancia temporal exacta entre la salida oficial y el marcaje real
                long minutosExtras = ChronoUnit.MINUTES.between(horaSalidaOficial, horaActual);
                long horasExtras = minutosExtras / 60;// División entera para consolidar horas cerradas

                observacion = "Trabajo adicional. " + horasExtras + " hora(s) extra(s).";
            } else {
                observacion = "Salida registrada."; // Egreso normal sin bonificaciones adicionales
            }
        }
// El DAO prepara el PreparedStatement y adiciona la marca física en la tabla 'asistencia'
        boolean registrado = asistenciaDAO.registrarAsistencia(usuarioId, nuevoEvento, observacion, jornadaId);
//Si la inserción en la tabla asistencia fue exitosa y MySQL consolidó la transacción 
        if (registrado) {
            response.setStatus(HttpServletResponse.SC_OK);
//se responde con un mensaje de exito si es exitosa el acceso
            String mensaje = "¡Hola, " + nombreEmpleado + "! Tu marcaje de " + nuevoEvento.toUpperCase() + " ha sido exitoso.";

            PrintWriter out = response.getWriter();//sale al usuario confirmando su marcacion 
            out.print("{"
                    + "\"status\":\"success\","
                    + "\"message\":\"" + mensaje + "\","
                    + "\"evento\":\"" + nuevoEvento + "\""
                    + "}");
            out.flush(); // Cierra el flujo de salida liberando la memoria caché del servidor
        } else {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            PrintWriter out = response.getWriter();
            out.print("{\"status\":\"error\", \"message\": \"Error interno en la base de datos al salvar la marca.\"}");
            out.flush();
        }
    }

    @Override //El método doOptions gestiona la verificación previa (Pre-flight) del protocolo CORS para habilitar accesos inalámbricos
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);// Responde HTTP 200 autorizando al navegador del celular a enviar el POST
    }
}

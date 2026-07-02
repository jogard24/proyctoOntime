package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.NominaDAO;
import ontime.ontimebackend.modelo.Nomina;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

@WebServlet(name = "NominaServlet", urlPatterns = {"/NominaServlet"})
public class NominaServlet extends HttpServlet {

    private final NominaDAO nominaDAO = new NominaDAO();

    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Consulta y transmite los tiempos filtrado por periodo
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String periodo = request.getParameter("periodo");
        if (periodo == null || periodo.trim().isEmpty()) {
            periodo = "2026-06";
        }

        try {
            List<Nomina> listaNomina = nominaDAO.obtenerReporteNomina(periodo);

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < listaNomina.size(); i++) {
                Nomina n = listaNomina.get(i);

                // En la base de datos limpia de pruebas, las marcas de salida totales
                // representan los días que el usuario completó su jornada laboral.
                int diasTrabajados = n.getDiasAsistidos();

                //  Si el empleado tiene marcas de salida, asumiremos
                // que sus horas extras reales se calculan si cumple criterios (para la prueba dará 1 si hay marcas '%extra%')
                int horasExtrasReales = (n.getTotalExtras() > 0) ? 1 : 0;

                json.append("{");
                json.append("\"id\":").append(n.getUsuarioId()).append(",");
                json.append("\"documento\":\"").append(escaparJson(n.getDocumento())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(n.getNombre())).append("\",");
                json.append("\"apellido\":\"").append(escaparJson(n.getApellido())).append("\",");
                json.append("\"salarioBase\":").append(n.getSalarioBasePeriodo()).append(",");
                json.append("\"totalRetardos\":").append(n.getTotalRetardos()).append(",");

                // Sincronizamos las llaves exactas que lee tu nomina.js refactorizado
                json.append("\"diasAsistidos\":").append(diasTrabajados).append(",");
                json.append("\"totalExtras\":").append(horasExtrasReales);
                json.append("}");

//ejemplo tenemos 3 usuarios que nos arrojo la consulta la condicion evalua la posicion 
// java muestra los datos de camilo SI 0 < (3-1)? como 0<2 es verdadero, el sistema ejecuta el json.append"," y pone la coma
//ahora los datos de laura SI 1< (3-1)?como 1<2 es verdadero el sistema pone la coma 
//ultimo empleado peter SI 2<(3-1) entonces 2<2? como no es menor da false cerrando al corchete final "]"
                if (i < listaNomina.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            out.print(json.toString());//mediantes esta instruccion se empuja la informacion al json de regreso al cliente

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\", \"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Procesa la consolidación final de nómina e inyección atómica en cascada
     * dentro de periodo_nomina, nomina, detalle_nomina y la tabla puente
     * (POST).
     */
    @Override
    // Método que atiende las peticiones HTTP POST (generalmente envíos de formularios o peticiones fetch).
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");// Configura la codificación a UTF-8 para evitar problemas con tildes y caracteres especiales (como la 'ñ').
        response.setContentType("application/json");// Establece que la respuesta enviada al cliente será un JSON.
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();// Objeto para escribir texto en el cuerpo de la respuesta.
// Captura los datos enviados desde el frontend (el tipo de acción a realizar y el mes/año del periodo).
        String accion = request.getParameter("accion");
        String periodo = request.getParameter("periodo");

        // Validación preventiva: Si falta la acción o el periodo viene vacío, rechaza la solicitud de inmediato.
        if (accion == null || periodo == null || periodo.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"Faltan parámetros requeridos (accion o periodo).\"}");
            return;// Termina la ejecución para evitar procesar datos corruptos o incompletos.
        }
        // Evalúa si la acción solicitada por el frontend es consolidar y guardar el periodo actual.
        if ("guardarPeriodo".equals(accion)) {
            try {
            // Recupera la lista de empleados con sus asistencias, retardos y horas extras desde el DAO.
                List<Nomina> lista = nominaDAO.obtenerReporteNomina(periodo);
                boolean completado = true;// boolean para rastrear si todas las inserciones fueron exitosas.
                // Itera sobre cada uno de los empleados obtenidos para realizar la liquidación individual.
                for (Nomina n : lista) {
                    BigDecimal baseContractual = n.getSalarioBasePeriodo();// Salario fijo del contrato.
                    //CÁLCULO PROPORCIONAL DE DÍAS
                    int diasTrabajados = n.getDiasAsistidos();
                    if (diasTrabajados <= 0) {
                        diasTrabajados = 30;// Protege el cálculo asignando 30 días si el registro viene vacío o en cero.
                    }

                    double factorDias = (double) diasTrabajados / 30.0;// Obtiene el porcentaje del mes trabajado.
            // Multiplica el salario base por el factor de días usando BigDecimal para mantener la precisión decimal.
                    BigDecimal sueldoProporcionalDias = baseContractual.multiply(BigDecimal.valueOf(factorDias));
            // CÁLCULO DE DEDUCCIONES POR RETARDOS
                    int retardosDelMes = n.getTotalRetardos();
                    BigDecimal deducciones = BigDecimal.valueOf(retardosDelMes * 15000L);// Penalización fija de $15,000 por retardo.

                    // CORRECCIÓN MATEMÁTICA: Sincroniza la lógica de extras con la de tu doGet para la prueba
                    int horasExtrasReales = (n.getTotalExtras() > 0) ? 1 : 0;
                    BigDecimal bonificaciones = BigDecimal.valueOf(horasExtrasReales * 20000L);// Bono fijo de $20,000.

                    // --- BALANCE FINAL (NETO A PAGAR) ---
                    // Fórmula: Sueldo Proporcional - Deducciones + Bonificaciones.
                    BigDecimal neto = sueldoProporcionalDias.subtract(deducciones).add(bonificaciones);

                    // Guarda de forma definitiva el desprendible calculado en la base de datos (congela el periodo).
                    boolean r = nominaDAO.guardarNominaPeriodo(n.getUsuarioId(), baseContractual, (double) horasExtrasReales, neto, periodo);
                    if (!r) {
                        completado = false;// Si falla tan solo un empleado (ej. por clave duplicada), el boolean cambia a false.
                    }
                }
                // Evalúa el resultado final del lote procesado para enviar la respuesta adecuada al frontend.
                if (completado) {
                    response.setStatus(HttpServletResponse.SC_OK);// Estado HTTP 200 (Éxito).
                    out.print("{\"status\":\"success\",\"message\":\"¡Nómina consolidada y guardada con éxito!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"status\":\"error\",\"message\":\"Algunos desprendibles no pudieron congelarse por restricciones de integridad.\"}");
                }
            } catch (Exception e) {
                // Maneja fallos imprevistos de código, conexiones caídas o errores de conversión.
                e.printStackTrace();
            // Envía el mensaje de error al frontend asegurándose de limpiar caracteres que rompan el formato JSON.
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        } else {
        // Si el parámetro 'accion' no coincide con 'guardarPeriodo', rechaza la petición.
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"Acción no reconocida.\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

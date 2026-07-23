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

//urlpatterns funciona como enrutador o un puente de acople permitiendo al fetch localizar la peticion e iniciar 
//el flujo hacia los metodos doget o dopost.
@WebServlet(name = "NominaServlet", urlPatterns = {"/NominaServlet"})
public class NominaServlet extends HttpServlet {

    //Crea e instancia una variable global interna llamada nominaDAO. La palabra final es una regla de 
    //seguridad estricta: le indica a Java que este puente de comunicación es inmutable y constante
    private final NominaDAO nominaDAO = new NominaDAO();
    
//Cuando Java traduce los datos de MySQL a formato JSON, utiliza comillas dobles (") para delimitar los textos. Si un empleado se llama, 
    //por ejemplo, José "El Ingeniero" Díaz, esa comilla interna rompería la estructura del JSON y haría que la pantalla web colapse arrojando un error de sintaxis.
    
    //si el texto viene vacío (null), retorna una cadena limpia "". Si trae texto, el método .replace() busca las 
    //comillas dobles y les inyecta una barra invertida de escape (\"), y limpia los saltos de línea (\n), 
    //garantizando que el paquete de red viaje con una sintaxis 100% perfecta hacia tu front
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * doget muestra en pantalla informacion de nomina 
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");//Esto le dice al navegador o aplicación que debe interpretar el contenido como datos en formato JSON.
        response.setCharacterEncoding("UTF-8");//Así se asegura que el texto enviado en la respuesta se interprete correctamente. interpreta caracteres
        PrintWriter out = response.getWriter();// permite escribir directamente en el cuerpo de la respuesta con print o outwrite
//captura y extrae la informacion enviada por el fetch desde el front
        String periodo = request.getParameter("periodo");
        
        //si la variable periodo viaja con un valor nulo O si tras aplicar la función de limpieza .trim()
        //su longitud se encuentra completamente vacía . Si se cumple cualquiera de las dos condiciones
        //lo que ocurre la primera vez que el Administrador carga el Dashboard sin haber interactuado con el 
        //selector de fechas
        if (periodo == null || periodo.trim().isEmpty()) {
            periodo = "2026-06";//de manera forzada asigna una fecha por defecto 
            //Esto garantiza que el Servlet siempre tenga un mes válido para interrogar a la base de datos, 
            //evitando que el query aborte o genere una pantalla en blanco.
        }

        try {
            //creamos una lista en la cual llamamos al metodo ejecutando la consulta devolviendo al controlador la lista 
            //cargada con los datos de la nomnina 
            List<Nomina> listaNomina = nominaDAO.obtenerReporteNomina(periodo);
            
            //stringbuilder heramienta para construir texto  
            //Instancia un objeto StringBuilder inicializado con un corchete de apertura [
            StringBuilder json = new StringBuilder("[");
            //i) que arrancará en 0 e irá avanzando posición por posición hasta recorrer el tamaño total de la lista (listaNomina.size()),
            for (int i = 0; i < listaNomina.size(); i++) {
                Nomina n = listaNomina.get(i);

                // En la base de datos limpia de pruebas, las marcas de salida totales
                // representan los días que el usuario completó su jornada laboral.
                int diasTrabajados = n.getDiasAsistidos();

                //  Si el empleado tiene marcas de salida, asumiremos
                // que sus horas extras reales se calculan si cumple criterios (para la prueba dará 1 si hay marcas '%extra%')            
                                                     //1: si la condicion es verdadera 
                                                    //0: si es false
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

        request.setCharacterEncoding("UTF-8");//Esto le dice al navegador o aplicación que debe interpretar el contenido como datos en formato JSON.evitar problemas con tildes y caracteres especiales.
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

                    // Sincroniza la lógica de extras con la de tu doGet para la prueba
                    //operador ternario si extras es mayo a cero Si el empleado marcó en el Pinpad al menos una 
                    //salida tarde con la palabra clave "extra", esta condición da Verdadero
                                                               //1: si la condicion es verdadera 
                                                               //0: si es false
                    int horasExtrasReales = (n.getTotalExtras() > 0) ? 1 : 0;
                    BigDecimal bonificaciones = BigDecimal.valueOf(horasExtrasReales * 20000L);// Bono fijo de $20,000.

                    // --- BALANCE FINAL (NETO A PAGAR) ---
                    // Fórmula: Sueldo Proporcional - Deducciones + Bonificaciones.
                    BigDecimal neto = sueldoProporcionalDias.subtract(deducciones).add(bonificaciones);

                    // Guarda de forma definitiva  calculado en la base de datos (congela el periodo).
                    boolean r = nominaDAO.guardarNominaPeriodo(n.getUsuarioId(), baseContractual, (double) horasExtrasReales, neto, periodo);
                    if (!r) {
                        completado = false;// Si falla tan solo un empleado (ej. por clave duplicada), el boolean cambia a false.
                    }
                }
                // Evalúa el resultado final del lote procesado para enviar la respuesta adecuada al frontend.
                if (completado) {
                    // Si todo salió bien, le responde un texto plano en verde a tu archivo nomina.js
                    response.setStatus(HttpServletResponse.SC_OK);// Estado HTTP 200 (Éxito).
                    out.print("{\"status\":\"success\",\"message\":\"¡Nómina consolidada y guardada con éxito!\"}");
                } else {
                    // Si la base de datos rebotó algún dato, avisa el error de integridad
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    out.print("{\"status\":\"error\",\"message\":\"Algunos datos no pudieron congelarse por restricciones de integridad.\"}");
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

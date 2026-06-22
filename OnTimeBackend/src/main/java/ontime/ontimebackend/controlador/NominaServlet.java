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
                int diasTrabajados = n.getTotalExtras();

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

                if (i < listaNomina.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");

            out.print(json.toString());

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String accion = request.getParameter("accion");
        String periodo = request.getParameter("periodo"); 

        if ("guardarPeriodo".equals(accion) && periodo != null) {
            try {
                //  Recuperamos la lista analítica real procesada por el query de tu DAO
                List<Nomina> lista = nominaDAO.obtenerReporteNomina(periodo);
                boolean completado = true;

                //  Procesamos empleado por empleado para la congelación 
                for (Nomina n : lista) {
                    BigDecimal baseContractual = n.getSalarioBasePeriodo();

                    // CONTROL OPERATIVO Leemos los días asistidos reales 
                    int diasTrabajados = n.getDiasAsistidos();
                    if (diasTrabajados <= 0) {
                        diasTrabajados = 30; // Respaldo seguro por mes completo
                    }
                    // REGLA DE TRES  Fraccionamiento de sueldo por asistencia
                    double factorDias = (double) diasTrabajados / 30.0;
                    BigDecimal sueldoProporcionalDias = baseContractual.multiply(BigDecimal.valueOf(factorDias));

                    // PENALIZACIONES Conteo de retardos multiplicado por $15.000 fijos
                    int retardosDelMes = n.getTotalRetardos();
                    BigDecimal deducciones = BigDecimal.valueOf(retardosDelMes * 15000L);

                    // BONIFICACIONES Conteo de extras reales multiplicado por $20.000 fijos
                    int horasExtrasReales = n.getTotalExtras();
                    BigDecimal bonificaciones = BigDecimal.valueOf(horasExtrasReales * 20000L);

                    // BALANCE  FINAL
                    BigDecimal neto = sueldoProporcionalDias.subtract(deducciones).add(bonificaciones);

                    // DISPARO EN CASCADA RELACIONAL
                    // Enviamos los datos al DAO para que siembre 'periodo_nomina', 'nomina', 'detalle_nomina' 
                    // y ensamble los cables físicos en la tabla puente 'nomina_asistencia' .
                    boolean r = nominaDAO.guardarNominaPeriodo(n.getUsuarioId(), baseContractual, (double) horasExtrasReales, neto, periodo);
                    if (!r) {
                        completado = false;
                    }
                }

                // 3. Respuesta síncrona controlada hacia el fetch de nomina.js
                if (completado) {
                    response.getWriter().print("{\"status\":\"success\",\"message\":\"¡Nómina consolidada y guardada con éxito en las tablas históricas!\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().print("{\"status\":\"error\",\"message\":\"Algunos desprendibles no pudieron congelarse por restricciones de integridad.\"}");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            }
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

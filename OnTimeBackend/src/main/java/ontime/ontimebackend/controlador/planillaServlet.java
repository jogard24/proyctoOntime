package ontime.ontimebackend.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import ontime.ontimebackend.dao.AsistenciaDAO;
import ontime.ontimebackend.modelo.EmpleadoPlanilla; // Clase DTO que crearemos abajo

/**
 * Capa de Control - Módulo de Auditoría Matricial de Asistencia (HTTP GET)
 * Procesa las peticiones de la malla de tiempos y serializa el resumen de los 31 días.
 */
@WebServlet(name = "PlanillaServlet", urlPatterns = {"/PlanillaServlet"})
public class planillaServlet extends HttpServlet {

    // Instanciamos el DAO de asistencias donde agregaremos el query de pivote relacional
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();

    /**
     * Sanitizador de cadenas para evitar que caracteres especiales rompan el JSON de la red
     */
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * 🧠 OPERACIÓN: READ (Consulta Asíncrona de Malla de Tiempos)
     * Captura el mes seleccionado y despacha la matriz horizontal día por día.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Configuración perimetral de cabeceras de red para formato JSON e interoperabilidad UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 📥 Capturamos el parámetro enviado por el fetch de planilla.js (Ej: "2026-06")
        String periodo = request.getParameter("periodo");
        
        // Filtro de Fallback: Si el parámetro viaja nulo o vacío por un retraso del DOM, forzamos un mes base
        if (periodo == null || periodo.trim().isEmpty()) {
            periodo = "2026-07"; // ◄ AJUSTADO AL MES REAL DE TUS PRUEBAS
        }
        
        try {
            // CONSULTA RELACIONAL: El DAO interroga a MySQL y nos trae la lista de empleados mapeados
            // Nota: Debes crear este método 'obtenerMallaAsistencia' en tu AsistenciaDAO.java con el query anterior
            List<EmpleadoPlanilla> lista = asistenciaDAO.obtenerMallaAsistencia(periodo);

            //️ SERIALIZACIÓN MATRICIAL MANUAL: Constructor dinámico de alta velocidad en memoria RAM
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < lista.size(); i++) {
                EmpleadoPlanilla emp = lista.get(i);
                
                json.append("{");
                json.append("\"id\":").append(emp.getId()).append(",");
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\",");
                json.append("\"apellido\":\"").append(escaparJson(emp.getApellido())).append("\",");
                json.append("\"cargo\":\"").append(escaparJson(emp.getCargo())).append("\",");
                
                // AQUÍ SE CONSTRUYE EL SUB-OBJETO DE LOS DÍAS EN FORMATO MATRICIAL
                json.append("\"diasResumen\":{");
                int[] dias = emp.getDiasArray(); // Recuperamos el arreglo interno de 31 posiciones del modelo
                for (int d = 1; d <= 31; d++) {
                    json.append("\"dia_").append(d).append("\":").append(dias[d - 1]);
                    if (d < 31) json.append(","); // Separador de días
                }
                json.append("}"); // Cierra diasResumen
                json.append("}"); // Cierra el objeto empleado
                
                if (i < lista.size() - 1) json.append(","); // Separador de empleados
            }
            json.append("]");

            // 📤 DESPACHO: Empujamos los bytes del string JSON estructurado de vuelta por el canal HTTP
            out.print(json.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

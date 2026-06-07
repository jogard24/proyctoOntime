package ontime.ontimebackend.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.dao.EmpleadoDAO;

// Define la ruta de acceso. Cuando navegues a /EmpleadoServlet, entrarás aquí.
@WebServlet(name = "EmpleadoServlet", urlPatterns = {"/EmpleadoServlet"})
public class EmpleadoServlet extends HttpServlet {

    // Instancia del DAO para interactuar con la base de datos
    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // Configura los permisos CORS para permitir que el navegador acceda desde cualquier origen
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
    }

// MÉTODO AUXILIAR: Evita que errores de formato en el JSON (ej. comillas en un nombre) 
    // rompan la estructura del JSON y causen un error en el frontend.
    private String escaparJson(String valor) {
        if (valor == null) return "";
        return valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        configurarCORS(response);
        response.setContentType("application/json");// Indicamos que el tipo de contenido es JSON
        response.setCharacterEncoding("UTF-8");// Soporte para caracteres especiales
        PrintWriter out = response.getWriter();// El objeto para escribir la respuesta

        try {
            // 1. Obtenemos la lista de objetos Empleado desde la base de datos vía DAO
            List<Empleado> empleados = empleadoDAO.listarTodos();

            // 2. Construcción MANUAL del JSON. 
            // En lugar de GSON, este código concatena cadenas para formar el arreglo [ { ... } ]
            StringBuilder json = new StringBuilder();
            json.append("[");
            
            //recorremos la lista empleados  El ciclo for toma el primer expediente, extrae la información 
            //de ese empleado (guardándola en la variable emp), hace el trabajo con él, y luego pasa al siguiente 
            //expediente hasta terminar la fila.
            for (int i = 0; i < empleados.size(); i++) {
                Empleado emp = empleados.get(i);
                
                json.append("{");
                json.append("\"id\":\"").append(escaparJson(emp.getId())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\",");
                json.append("\"documento\":\"").append(escaparJson(emp.getDocumento())).append("\",");
                json.append("\"cargo\":\"").append(escaparJson(emp.getCargo())).append("\",");
                json.append("\"estado\":\"").append(escaparJson(emp.getEstado())).append("\",");
                json.append("\"fotoPerfilUrl\":\"").append(escaparJson(emp.getFoto())).append("\"");
                json.append("}");
                //escaparJson= Si un empleado se llama Carlos "El Jefe" Pérez, esas comillas internas romperían 
                //la estructura del JSON y causarían un error. La función escaparJson se asegura de 
                //limpiar el texto para que el JSON final sea válido y seguro
                
                // Agrega una coma si no es el último elemento del arreglo
                if (i < empleados.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
// 3. Enviamos el JSON construido al navegador
            out.print(json.toString());
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
            out.flush();
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Maneja las peticiones de pre-verificación (CORS preflight)
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

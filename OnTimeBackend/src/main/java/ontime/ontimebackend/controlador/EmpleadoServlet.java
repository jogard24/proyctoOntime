package ontime.ontimebackend.controlador;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato; 
import ontime.ontimebackend.dao.EmpleadoDAO;

@WebServlet(name = "EmpleadoServlet", urlPatterns = {"/EmpleadoServlet"})
@MultipartConfig // Esto es suficiente para manejar archivos sin romper el flujo
public class EmpleadoServlet extends HttpServlet {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Empleado> empleados = empleadoDAO.listarTodos();
            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < empleados.size(); i++) {
                Empleado emp = empleados.get(i);
                json.append("{");
                json.append("\"id\":\"").append(escaparJson(emp.getId())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\",");
                json.append("\"fotoPerfilUrl\":\"").append(escaparJson(emp.getFoto())).append("\"");
                json.append("}");
                if (i < empleados.size() - 1) json.append(",");
            }
            json.append("]");
            out.print(json.toString());
        } catch (Exception e) {
            response.setStatus(500);
            out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        configurarCORS(response);
        response.setContentType("application/json");

        try {
            // 1. Obtener parámetros de texto de forma segura
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String documento = request.getParameter("documento_identidad");
            
            // 2. Procesar el archivo (si viene)
            Part filePart = request.getPart("fotoPerfil");
            String nombreArchivo = (filePart != null) ? filePart.getSubmittedFileName() : null;

            // 3. Mapear al objeto (ejemplo de registro)
            Empleado emp = new Empleado();
            emp.setNombre(nombre);
            emp.setApellido(apellido);
            emp.setDocumento(documento);
            // ... setea los demás campos ...

            // 4. Delegar al DAO
            // boolean exito = empleadoDAO.registrarEmpleadoCompleto(emp, objetoContrato);
            
            response.getWriter().print("{\"status\":\"success\"}");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}

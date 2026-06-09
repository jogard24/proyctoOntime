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
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.dao.EmpleadoDAO;

@WebServlet(name = "EmpleadoServlet", urlPatterns = {"/EmpleadoServlet"})
@MultipartConfig
public class EmpleadoServlet extends HttpServlet {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Alimenta la grilla general del módulo de Gestión (Sincronizado con
     * gestionEmpleados.js).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            List<Empleado> empleados = empleadoDAO.listarTodos();
            StringBuilder json = new StringBuilder("[");

            for (int i = 0; i < empleados.size(); i++) {
                Empleado emp = empleados.get(i);
                json.append("{");
                json.append("\"id\":").append(emp.getId()).append(",");
                json.append("\"documento\":\"").append(escaparJson(emp.getDocumento())).append("\",");
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\","); // Solo pasamos el campo nombre limpio
                json.append("\"telefonoCelular\":\"").append(escaparJson(emp.getTelefonoCelular())).append("\","); // ¡Nueva!
                json.append("\"direccion\":\"").append(escaparJson(emp.getDireccion())).append("\","); // ¡Nueva!
                json.append("\"cargo\":\"").append(escaparJson(emp.getCargo())).append("\",");
                json.append("\"estado\":\"").append(escaparJson(emp.getEstado())).append("\",");
                json.append("\"fotoPerfilUrl\":\"").append(escaparJson(emp.getFoto())).append("\"");
                json.append("}");
                if (i < empleados.size() - 1) {
                    json.append(",");
                }
            }

            json.append("]");
            out.print(json.toString());

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    /**
     * Procesa de forma unificada las acciones del modal de actualización y los
     * clics de borrado lógico (RF23 y RF25).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // Ajuste 3: Captura de los parámetros planos de URLSearchParams de tu javascript
        String accion = request.getParameter("accion");
        String idStr = request.getParameter("id");

        if (accion == null || idStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"Parámetros de control faltantes.\"}");
            return;
        }

        int id = Integer.parseInt(idStr.trim());
        boolean exito = false;

        try {
            if ("inactivar".equals(accion)) {
                // Ejecuta el UPDATE lógico modular que creamos en tu EmpleadoDAO
                exito = empleadoDAO.inactivarEmpleado(id);
            } else if ("actualizar".equals(accion)) {
                // Construye el objeto Empleado básico con los campos editados en el modal
                Empleado emp = new Empleado();
                emp.setId(id);
                emp.setNombre(request.getParameter("nombre"));
                emp.setEstado(request.getParameter("estado"));

                exito = empleadoDAO.actualizarEmpleado(emp);
            }

            if (exito) {
                response.getWriter().print("{\"status\":\"success\",\"message\":\"Operación completada con éxito.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"La base de datos rechazó la modificación.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

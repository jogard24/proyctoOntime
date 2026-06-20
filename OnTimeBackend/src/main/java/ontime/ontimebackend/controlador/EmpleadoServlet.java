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
     * Alimenta la tabla general del módulo de Gestión (Sincronizado con
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
                json.append("\"nombre\":\"").append(escaparJson(emp.getNombre())).append("\","); 
                json.append("\"telefonoCelular\":\"").append(escaparJson(emp.getTelefonoCelular())).append("\","); 
                json.append("\"direccion\":\"").append(escaparJson(emp.getDireccion())).append("\","); 
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

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
            if ("eliminar".equals(accion)) { 
                // Invoca el borrado lógico seguro (Modificado para cumplir con el jurado)
                exito = empleadoDAO.eliminarEmpleado(id);
            } else if ("actualizar".equals(accion)) {
                Empleado emp = new Empleado();
                emp.setId(id);
                emp.setNombre(request.getParameter("nombre"));
                emp.setEstado(request.getParameter("estado"));
                emp.setTelefonoCelular(request.getParameter("celular"));
                emp.setDireccion(request.getParameter("direccion"));
                emp.setCargo(request.getParameter("cargo"));

                // CAPTURA CRÍTICA: Extraemos el ID numérico del rol que envía el Select
                String rolIdStr = request.getParameter("rol_id");
                int rolId = (rolIdStr != null) ? Integer.parseInt(rolIdStr.trim()) : 2; // 2 = Empleado por defecto

                // Invocamos el nuevo método transaccional que actualiza todas las tablas acopladas
                exito = empleadoDAO.actualizarEmpleadoConRol(emp, rolId);
            }

            if (exito) {
                response.getWriter().print("{\"status\":\"success\",\"message\":\"Operación completada con éxito.\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"La base de datos rechazó la modificación por integridad.\"}");
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


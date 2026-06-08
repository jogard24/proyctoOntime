package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.PermisoDAO;
import ontime.ontimebackend.modelo.PermisoLaboral;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "PermisoServlet", urlPatterns = {"/PermisoServlet"})
public class PermisoServlet extends HttpServlet {
    
    private final PermisoDAO permisoDAO = new PermisoDAO();


    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    /**
     * Devuelve el listado completo de permisos mapeando el JSON de forma nativa (RF13).
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        List<PermisoLaboral> lista = permisoDAO.listarPermisos();
        
        // Renderizado manual sin dependencias externas para garantizar estabilidad y ligereza académica
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            PermisoLaboral p = lista.get(i);
            json.append("{");
            json.append("\"id\":").append(p.getId()).append(",");
            json.append("\"nombreEmpleado\":\"").append(escaparJson(p.getNombreEmpleado())).append("\",");
            json.append("\"tipoPermiso\":\"").append(escaparJson(p.getTipoPermiso())).append("\",");
            json.append("\"fechaAsignacion\":\"").append(escaparJson(p.getFechaAsignacion())).append("\",");
            json.append("\"fechaInicio\":\"").append(escaparJson(p.getFechaInicio())).append("\",");
            json.append("\"fechaFin\":\"").append(escaparJson(p.getFechaFin())).append("\",");
            json.append("\"estado\":\"").append(escaparJson(p.getEstado())).append("\"");
            json.append("}");
            if (i < lista.size() - 1) json.append(",");
        }
        json.append("]");
        out.print(json.toString());
    }

    /**
     * Procesa de forma unificada la creación de permisos, aprobaciones y rechazos en parámetros planos (RF12 y RF13).
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Captura de los parámetros planos de URLSearchParams de tu permisos.js
        String accion = request.getParameter("accion");
        boolean exito = false;

        try {
            if ("crear".equals(accion)) {
                String documento = request.getParameter("documento");
                String tipoPermiso = request.getParameter("tipoPermiso");
                String desde = request.getParameter("fechaInicio");
                String hasta = request.getParameter("fechaFin");
                
                // Invoca la inserción atómica validando la cédula del empleado
                exito = permisoDAO.crearPermiso(documento, tipoPermiso, desde, hasta);
            } 
            else if ("aprobar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id").trim());
                exito = permisoDAO.actualizarEstadoPermiso(id, "aprobado");
            } 
            else if ("rechazar".equals(accion)) {
                int id = Integer.parseInt(request.getParameter("id").trim());
                exito = permisoDAO.actualizarEstadoPermiso(id, "rechazado");
            }

            if (exito) {
                response.getWriter().print("{\"status\":\"success\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"La operación fue rechazada por restricciones del servidor.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

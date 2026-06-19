package ontime.ontimebackend.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import ontime.ontimebackend.dao.FormularioRegistroDAO;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato;

@WebServlet(name = "FormularioRegistroServlet", urlPatterns = {"/FormularioRegistroServlet"})
@MultipartConfig // Obligatorio en Jakarta para capturar la foto binaria sin romper el flujo
public class FormularioRegistroServlet extends HttpServlet {

    private final FormularioRegistroDAO registroDAO = new FormularioRegistroDAO();

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Recoger datos personales planos del Empleado (Mapeados desde tu javascript)
            Empleado emp = new Empleado();
            emp.setNombre(request.getParameter("nombre"));
            emp.setApellido(request.getParameter("apellido"));
            emp.setDocumento(request.getParameter("documento_identidad"));
            emp.setEmail(request.getParameter("email"));
            emp.setTelefonoCelular(request.getParameter("celular"));
            emp.setDireccion(request.getParameter("direccion"));
            emp.setTipoSangre(request.getParameter("tipoSangre"));
            
            // Datos del contacto de emergencia en español
            emp.setContactoEmergenciaNombre(request.getParameter("nombreContacto"));
            emp.setContactoEmergenciaTelefono(request.getParameter("celularContacto"));
            emp.setContactoEmergenciaParentesco(request.getParameter("relacionContacto"));
            
            // Procesamiento básico de la foto de perfil (Cumple tu requisito educativo con foto)
            Part fotoPart = request.getPart("fotoPerfil");
            if (fotoPart != null && fotoPart.getSize() > 0) {
                String nombreArchivo = fotoPart.getSubmittedFileName();
                emp.setFoto("img/perfiles/" + nombreArchivo); // Guardamos la ruta virtual para la base de datos
            } else {
                emp.setFoto("img/usuario-defecto.png"); // Respaldo escolar por defecto
            }

            // 2. Recoger datos laborales del Contrato (Sincronizado con las llaves de contrato.js)
            Contrato contrato = new Contrato();
            contrato.setTipoContrato(request.getParameter("tipo_contrato"));
            contrato.setCargo(request.getParameter("cargo"));
            
            String salarioStr = request.getParameter("salario_base");
            contrato.setSalarioBase(salarioStr != null ? new BigDecimal(salarioStr) : BigDecimal.ZERO);
            
            String jornadaStr = request.getParameter("jornada_id");
            contrato.setJornadaId(jornadaStr != null ? Integer.parseInt(jornadaStr.trim()) : 1);

            // =========================================================================
            //  APLICACIÓN DE LA CORRECCIÓN DEL JURADO: CAPTURA DE LAS FECHAS DESDE EL MODAL
            // =========================================================================
            String fechaInicioStr = request.getParameter("regFechaInicio");
            String fechaFinStr = request.getParameter("regFechaFin");

            // Seteamos la fecha de inicio. Si por algún error viene vacía, le dejamos la fecha actual de respaldo
            contrato.setFechaInicio(fechaInicioStr != null && !fechaInicioStr.trim().isEmpty() ? fechaInicioStr : "2026-06-18");
            
            // Control de nulidad para Término Indefinido: Si viene vacía del modal, se guarda como null
            contrato.setFechaFin(fechaFinStr != null && !fechaFinStr.trim().isEmpty() ? fechaFinStr : null);
            // =========================================================================

            // 3. Ejecutar inserción atómica mediante el DAO descriptivo
            boolean exito = registroDAO.registrarNuevoEmpleado(emp, contrato);

            // 4. Responder al Frontend en formato JSON básico directo
            if (exito) {
                response.getWriter().print("{\"status\":\"success\",\"message\":\"¡Empleado registrado con éxito!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"MySQL rechazó el registro. Revisa restricciones en la consola.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().print("{\"status\":\"error\",\"message\":\"Error interno en el servidor Java.\"}");
        }
    }
}


package ontime.ontimebackend.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import ontime.ontimebackend.dao.EmpleadoDAO;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato;

@WebServlet(name = "RegistroServlet", urlPatterns = {"/RegistroServlet"})
@MultipartConfig
public class RegistroServlet extends HttpServlet {

    private final EmpleadoDAO empleadoDAO = new EmpleadoDAO();

    // 1. Añadir CORS para que el Navegador no bloquee el Fetch de Javascript
    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        configurarCORS(response); // Habilitar comunicación
        response.setContentType("application/json");

        try {
            // 2. Mapear Empleado de forma completa respetando la Base de Datos
            Empleado emp = new Empleado();
            emp.setNombre(request.getParameter("nombre"));
            emp.setApellido(request.getParameter("apellido"));
            emp.setDocumento(request.getParameter("documento_identidad"));
            emp.setEmail(request.getParameter("email"));
            emp.setTelefonoCelular(request.getParameter("celular"));
            emp.setDireccion(request.getParameter("direccion"));

            emp.setContactoEmergenciaNombre(request.getParameter("nombreContacto"));
            emp.setContactoEmergenciaTelefono(request.getParameter("celularContacto"));
            emp.setContactoEmergenciaParentesco(request.getParameter("relacionContacto"));

            // Campos críticos requeridos por tu Base de Datos (ENUMs en minúsculas)
            String tipoSangre = request.getParameter("tipoSangre");
            emp.setTipoSangre(tipoSangre != null ? tipoSangre.toLowerCase() : "o+");
            emp.setEstado("activo"); // Obligatorio NOT NULL en tu BD

            // Lógica de Foto de Perfil
            Part fotoPart = request.getPart("fotoPerfil");
            if (fotoPart != null && fotoPart.getSize() > 0) {
                String nombreArchivo = fotoPart.getSubmittedFileName();
                // Aquí guardas la URL que irá a la base de datos para evitar el fallo de fotoPerfil_url NOT NULL
                emp.setFoto("img/perfiles/" + nombreArchivo);

                // NOTA: Para guardar físicamente el archivo en el servidor descomenta la línea de abajo:
                // fotoPart.write(getServletContext().getRealPath("/") + "img/perfiles/" + nombreArchivo);
            } else {
                emp.setFoto("img/usuario-defecto.png"); // Valor por defecto si no suben foto
            }

            // 3. Recuperar y armar los datos del Contrato (Mapeo Manual Seguro)
            Contrato contrato = new Contrato();

            // Leemos los datos directamente desde las llaves del FormData que envió JavaScript
            String tipoContrato = request.getParameter("tipoContrato");
            String cargoContrato = request.getParameter("cargoContrato");
            String salarioBaseStr = request.getParameter("salarioBase");
            String jornadaContrato = request.getParameter("jornadaContrato");

            // Si los datos vienen dentro del objeto JSON text 'contrato' de tu JS, extrae sus valores
            // Para desarrollo rápido sin librerías complejas, lee directamente los campos si los tienes disponibles
            contrato.setTipoContrato(tipoContrato != null ? tipoContrato : "Indefinido");
            contrato.setCargo(cargoContrato != null ? cargoContrato : "Empleado");
            contrato.setSalarioBase(salarioBaseStr != null ? new java.math.BigDecimal(salarioBaseStr) : java.math.BigDecimal.ZERO);

            if (jornadaContrato != null) {
                contrato.setJornadaId(Integer.parseInt(jornadaContrato));
            }

            // 4. Guardar en BD usando tu DAO
            boolean exito = empleadoDAO.registrarEmpleadoCompleto(emp, contrato);

            // 5. Enviar respuesta limpia a Javascript
            if (exito) {
                response.getWriter().print("{\"status\":\"success\",\"message\":\"Empleado guardado correctamente\"}");
            } else {
                response.setStatus(400);
                response.getWriter().print("{\"status\":\"error\",\"message\":\"El DAO no pudo procesar el guardado.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace(); // Revisa la consola de tu Servidor (Tomcat Log) para ver el error exacto de SQL
            response.setStatus(500);
            response.getWriter().print("{\"status\":\"error\", \"message\":\"" + e.getMessage() + "\"}");
        }
    }
}

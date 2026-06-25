package ontime.ontimebackend.controlador; // Sincronizado con tu paquete de controladores

import jakarta.servlet.ServletException;
import ontime.ontimebackend.dao.CredencialDAO;
import ontime.ontimebackend.modelo.Credencial;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {

    // Instancia del DAO refactorizado para conectar con Ontime3BD
    private final CredencialDAO credencialDAO = new CredencialDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Configuramos la respuesta como JSON para que el frontend pueda procesarla fácilmente
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Obtener parámetros... (continúa tu código normal)
        // Obtenemos los parámetros enviados desde el formulario HTML/JS (URLSearchParams)
        String user = request.getParameter("usuario");
        String pass = request.getParameter("clave");

        // Log de depuración para ver en consola qué está enviando el frontend
        System.out.println("DEBUG: Intentando login con usuario: " + user + " y pass: " + pass);

        // Llamamos al DAO para que valide las credenciales contra la base de datos Ontime3BD
        Credencial credencial = credencialDAO.validarLogin(user, pass);

        // Si el DAO retorna un objeto credencial, significa que las credenciales son válidas
        if (credencial != null) {

            // Verificamos si la cuenta está activa en el sistema escolar
            if (!credencial.isActivo()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("{\"exito\": false, \"message\": \" Cuenta inactiva. Contacte al administrador.\"}");
                return;
            }

            // Usamos el rol real que viene del INNER JOIN de la tabla roles
            String rolReal = credencial.getNombreRol().toLowerCase();
            System.out.println("DEBUG: Rol detectado en Java para " + credencial.getUsuario() + ": " + rolReal);

            // Creamos la sesión en el servidor para mantener el estado del usuario activo
            HttpSession session = request.getSession();
            session.setAttribute("usuarioLogueado", credencial);
            session.setAttribute("rolUsuario", rolReal);


            // Ajuste 2: Estructura JSON unificada con las llaves que lee tu login.js
            response.getWriter().write("{"
                    + "\"exito\": true, "
                    + "\"message\": \"¡Bienvenido, " + credencial.getUsuario() + "!\", "
                    + "\"nombre\": \"" + credencial.getUsuario() + "\", "
                    + "\"rol\": \"" + rolReal + "\""
                    + "}");
        } else {
            // Si el DAO retorna null, las credenciales son inválidas
            // Establecemos el estado HTTP 401 (Unauthorized) que ya tenías muy bien configurado
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\": false, \"message\": \" Usuario o contraseña incorrectos.\"}");
        }
    }

}

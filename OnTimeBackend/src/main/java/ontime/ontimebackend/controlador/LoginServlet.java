package ontime.ontimebackend;

import ontime.ontimebackend.dao.UsuarioDAO;
import ontime.ontimebackend.modelo.Usuario;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    // Instancia del DAO: El Servlet delega la gestión de datos y consultas SQL a esta clase .
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Configuramos la respuesta como JSON para que el frontend pueda procesarla fácilmente
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Obtenemos los parámetros enviados desde el formulario HTML/JS
        String user = request.getParameter("usuario");
        String pass = request.getParameter("clave");
        String rolElegido = request.getParameter("Rol");
        
        
        Usuario usuario = usuarioDAO.autenticar(user, pass);

        if (usuario != null && usuario.getRol().equalsIgnoreCase(rolElegido)) {
            response.getWriter().write("{\"exito\": true, \"message\": \"Bienvenido...\"}");
        }
        // Log de depuración para ver en consola qué está enviando el frontend
        System.out.println("DEBUG: Intentando login con usuario: " + user + " y pass: " + pass);
// Llamamos al DAO para que valide las credenciales contra la base de datos
// Si el DAO retorna un usuario, significa que las credenciales son válidas
        if (usuario != null) {
            // Enviamos una respuesta JSON positiva
            response.getWriter().write("{\"exito\": true, \"message\": \"Bienvenido, " + usuario.getNombre() + "\", \"nombre\": \"" + usuario.getNombre() + "\", \"rol\": \"administrador\"}");
        } else {
            // Si el DAO retorna null, las credenciales son inválidas
            // Establecemos el estado HTTP 401 (Unauthorized)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\": false, \"message\": \"Usuario o contraseña incorrectos.\"}");
        }
    }
}

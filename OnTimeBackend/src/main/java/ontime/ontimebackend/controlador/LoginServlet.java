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

        // ESTO PERMITE QUE EL FRONTEND HABLE CON EL BACKEND
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setHeader("Access-Control-Allow-Credentials", "true");
// Configuramos la respuesta como JSON para que el frontend pueda procesarla fácilmente
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Obtenemos los parámetros enviados desde el formulario HTML/JS
        String user = request.getParameter("usuario");
        String pass = request.getParameter("clave");

        // Log de depuración para ver en consola qué está enviando el frontend
        System.out.println("DEBUG: Intentando login con usuario: " + user + " y pass: " + pass);
// Llamamos al DAO para que valide las credenciales contra la base de datos
        Usuario usuario = usuarioDAO.autenticar(user, pass);
// Si el DAO retorna un usuario, significa que las credenciales son válidas
        if (usuario != null) {
// Usamos el rol real que viene del objeto usuario
            String rolReal = usuario.getRol();
            System.out.println("DEBUG: Rol detectado en Java para " + usuario.getNombre() + ": " + usuario.getRol());
            // Enviamos el rol real en el JSON
            response.getWriter().write("{\"exito\": true, \"message\": \"Bienvenido, " + usuario.getNombre()
                    + "\", \"nombre\": \"" + usuario.getNombre()
                    + "\", \"rol\": \"" + rolReal + "\"}");
        } else {
            // Si el DAO retorna null, las credenciales son inválidas
            // Establecemos el estado HTTP 401 (Unauthorized)
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\": false, \"message\": \"Usuario o contraseña incorrectos.\"}");
        }
    }
}

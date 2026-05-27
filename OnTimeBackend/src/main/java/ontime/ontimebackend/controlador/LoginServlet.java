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

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String user = request.getParameter("usuario");
        String pass = request.getParameter("clave");

        // ESTA ES LA LÍNEA QUE QUERÍAS AGREGAR
        // Va DENTRO del método doPost, justo antes de usar el DAO
        System.out.println("DEBUG: Intentando login con usuario: " + user + " y pass: " + pass);

        Usuario usuario = usuarioDAO.autenticar(user, pass);

        if (usuario != null) {
            // Asegúrate de enviar también el rol si lo tienes en el modelo Usuario
            response.getWriter().write("{\"exito\": true, \"message\": \"Bienvenido, " + usuario.getNombre() + "\", \"nombre\": \"" + usuario.getNombre() + "\", \"rol\": \"administrador\"}");
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"exito\": false, \"message\": \"Usuario o contraseña incorrectos.\"}");
        }
    }
}

package ontime.ontimebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
// @WebFilter("/*") le dice a Tomcat: interceptará todas las peticiones HTTP dirigidas a tu aplicación.
//Su función principal en un CorsFilter es habilitar CORS (Intercambio de Recursos de Origen Cruzado),
//permitiendo que tu backend reciba peticiones desde diferentes dominios
@WebFilter("/*")
public class CORSFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        // Convertimos el objeto genérico a HttpServletResponse para acceder a los headers HTTP
        HttpServletResponse response = (HttpServletResponse) res;
        // Permite que CUALQUIER sitio web haga peticiones. 
        response.setHeader("Access-Control-Allow-Origin", "*"); // Permite conexiones desde cualquier origen
        // Define qué acciones están permitidas (GET para leer, POST para enviar, etc.)
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        // Define qué tipos de encabezados permitimos. Content-Type es clave para enviar JSON
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");
        // ¡IMPORTANTE! Esto deja que la petición continúe su camino hacia el Servlet correcto
        chain.doFilter(req, res);//solicitud respuesta
    }
}
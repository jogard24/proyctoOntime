package ontime.ontimebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
// webfilter Intercepta absolutamente todas las peticiones que van hacia tu servidor de Tomcat 
//y les inyecta las cabeceras de seguridad de forma centralizada.
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Método de inicialización obligatorio en Jakarta EE (se deja vacío)
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 1. Especifica el origen exacto de tu frontend (Live Server de VS Code)
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        
        // 2. Habilita el uso seguro de cookies y sesiones entre dominios diferentes
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // 3. Define los métodos HTTP y cabeceras que permites en las peticiones asíncronas
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");

        // 4. Maneja de forma automática la petición pre-flight OPTIONS (Evita el bloqueo de CORS del navegador)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // Si la petición es válida, permite que continúe hacia su respectivo Servlet
        chain.doFilter(req, res);
    }

    @Override
    public void destroy() {
        // Método de destrucción de recursos obligatorio en Jakarta EE (se deja vacío)
    }
}

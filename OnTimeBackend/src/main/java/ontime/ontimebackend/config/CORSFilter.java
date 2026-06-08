package ontime.ontimebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebFilter("/*")
public class CORSFilter implements Filter {
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 1. Especifica el origen exacto de tu frontend
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        
        // 2. Habilita las credenciales
        response.setHeader("Access-Control-Allow-Credentials", "true");
        
        // 3. Define métodos y cabeceras permitidas
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");

        // 4. Maneja la petición pre-flight OPTIONS (esencial para evitar el error CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(req, res);
    }
}
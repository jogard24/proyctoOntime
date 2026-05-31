package ontime.ontimebackend.config;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getRequestURI();
        String pathLower = path.toLowerCase();

        // 1. Acceso libre (archivos, login, etc.)
        if (pathLower.contains("/login") || pathLower.contains("pinpad") || path.endsWith(".html") || path.endsWith(".css") || path.endsWith(".js")) {
            chain.doFilter(request, response);
            return;
        }

        // 2. DETECCIÓN DE AJAX: Si es una petición fetch/json, no redirigir, devolver error 401
        boolean isAjax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
                || (req.getHeader("Accept") != null && req.getHeader("Accept").contains("application/json"))
                || (req.getHeader("Content-Type") != null && req.getHeader("Content-Type").contains("application/json"));
        
        if (session == null || session.getAttribute("usuarioRole") == null) {
            if (isAjax) {
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                res.getWriter().write("{\"error\": \"Sesion expirada\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/login.html");
            }
            return;
        }

        String role = (String) session.getAttribute("usuarioRole");

        // 3. Control de Acceso por Roles
        if (role.equals("Administrador")) {
            chain.doFilter(request, response);
        } else if (role.equals("Contador") && (pathLower.contains("nomina") || pathLower.contains("permisos") || pathLower.contains("reporte"))) {
            chain.doFilter(request, response);
        } else {
            // Si intenta acceder a algo prohibido mediante AJAX
            if (isAjax) {
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.getWriter().write("{\"error\": \"Acceso denegado\"}");
            } else {
                res.sendRedirect(req.getContextPath() + "/acceso-denegado.html");
            }
        }
    }
}

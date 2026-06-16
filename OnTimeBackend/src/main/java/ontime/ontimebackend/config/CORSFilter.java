package ontime.ontimebackend.config;

import jakarta.servlet.*;//Importa las clases e interfaces básicas y genéricas de la API de Servlets.
import jakarta.servlet.annotation.WebFilter;//para decirle al contenedor web (como Apache Tomcat) exactamente qué rutas o URLs debe interceptar
import jakarta.servlet.http.HttpServletRequest;//Importa el objeto que representa la petición HTTP enviada por el cliente verifica si hay una sesion activa
import jakarta.servlet.http.HttpServletResponse;//represetan la respuesta Permite modificar la respuesta antes de que la reciba el usuario. Puedes usarlo para redirigir al usuario a otra página 
import java.io.IOException;//Importa una clase estándar de Java para el manejo de excepciones de entrada/salida.
//Los métodos obligatorios que procesan las peticiones en la API de servlets (como doFilter, doGet o doPost)

// webfilter Intercepta absolutamente todas las peticiones que van hacia tu servidor de Tomcat 
//y les inyecta las cabeceras de seguridad de forma centralizada.
@WebFilter("/*")
//Obliga a la clase a cumplir con el contrato de ciclo de vida de Jakarta EE 
//mediante los métodos init(), doFilter() y destroy().
public class CORSFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
//Transforma los parámetros genéricos de la red a objetos
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // Le preguntamos al navegador de dónde viene la petición web
        // Captura la URL exacta del navegador web que disparó el fetch o la IP del celular
        String origenFront = request.getHeader("Origin");

//        si la variable origenFront es igual a "http://127.0.0.1:5500" o a "http://localhost:5500".
//.       equals() compara cadenas en Java.
//
//      Si la condición se cumple, se agrega al response la cabecera Access-Control-Allow-Origin con el mismo valor de origenFront.
//      Esto significa que se autoriza ese origen específico para hacer peticiones al servidor.
        if ("http://127.0.0.1:5500".equals(origenFront) || "http://localhost:5500".equals(origenFront)) {
            // Si navegas desde la PC localmente, le da el pase
            response.setHeader("Access-Control-Allow-Origin", origenFront);
        } else {
            // Si navegas desde el celular o tablet, le da el pase con la IP fija de tu red local
            response.setHeader("Access-Control-Allow-Origin", "http://10.71.104.170:5500");
        }

        // 2. Habilita el uso seguro de cookies y sesiones entre dominios diferentes
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 3. Define los métodos HTTP y cabeceras que permites en las peticiones asíncronas
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS, PUT, DELETE");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, Accept");

//request.getMethod() devuelve el tipo de método de la petición (GET, POST, PUT, DELETE, OPTIONS, etc.).
//Aquí se compara con "OPTIONS", usando .equalsIgnoreCase() para que no importe si está en mayúsculas o minúsculas.
//
//Caso verdadero:
//Si la petición es de tipo OPTIONS, se responde inmediatamente con un estado 200 OK (HttpServletResponse.SC_OK equivale a 200).
//Luego se hace return; para salir del método y no ejecutar más lógica.
        
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

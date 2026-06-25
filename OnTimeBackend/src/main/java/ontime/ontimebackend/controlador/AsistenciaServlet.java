package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO; //importaciones conectan tu servlet
import ontime.ontimebackend.modelo.Asistencia;//importaciones conectan tu servlet
import jakarta.servlet.annotation.WebServlet; //anotación que registra tu servlet Tomcat. Define la URL que lo llama.
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;// representa la petición que llega del cliente 
import jakarta.servlet.http.HttpServletResponse;//representa la respuesta HTTP que tu servlet envía al cliente
import java.io.IOException;
import java.io.PrintWriter;//objeto que te permite escribir texto directamente en el cuerpo de la respuesta HTTP 
import java.util.List;// interfaz de Java para manejar listas de objetos (List<Asistencia>).
//util list :Se usa para almacenar objetos en memoria, en forma de lista


// @WebServlet: URL invocada por asistenciaTabla.js e inicioModulo.js de forma modular
@WebServlet("/AsistenciaServlet")
public class AsistenciaServlet extends HttpServlet {
    //Se crea un objeto de acceso a datos (DAO) para consultar la base de datos de asistencia.
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();


// Limpia cadenas de texto para que no rompan el formato JSON (si un nombre contien comillas y saltos de línea).
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override//se utiliza para indicar que un método está sobrescribiendo .
    //doGet se ejecuta automáticamente cuando el servidor recibe una petición HTTP de tipo GET (por ejemplo, cuando un navegador accede a una URL).
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");//Esto le dice al navegador o aplicación que debe interpretar el contenido como datos en formato JSON.
        response.setCharacterEncoding("UTF-8");//Así se asegura que el texto enviado en la respuesta se interprete correctamente. interpreta caracteres
        PrintWriter out = response.getWriter();// permite escribir directamente en el cuerpo de la respuesta con print o outwrite

      
        // Evaluamos si la petición viene del Dashboard de Novedades del Home o de la Tabla General de Reportes
        //Se lee el parámetro vista de la petición.
        String vista = request.getParameter("vista");
        //Si viene de la vista “novedadesHome”  se listan solo las últimas 10 asistencias.
        List<Asistencia> lista; 
        
        
        //INICIO NOVEDADES HOME.
        if ("novedadesHome".equals(vista)) {
            // Llama al método optimizado con LIMIT 10 que dejamos en tu AsistenciaDAO para el Dashboard
            lista = asistenciaDAO.listarNovedadesRecientes();
        } else {
            
            //Si no, se devuelve el listado completo de asistencia.
            lista = asistenciaDAO.listarAsistencia();
        }

        //  Renderizado manualmente el json
        // Se utiliza frecuentemente para construir manualmente arreglos o estructuras de datos en formato JSON
         //clase               //Crea una nueva instancia de la clase
        StringBuilder json = new StringBuilder("[");
        //Se recorre la lista de objetos Asistencia desde el primer elemento hasta el final.
        for (int i = 0; i < lista.size(); i++) {
            //Se construye un array JSON manualmente concatenando cadenas.
            //Cada objeto se convierte en un JSON con sus atributos
            
            //Extrae el objeto Asistencia que se encuentra en la posición actual i de la lista y lo guarda 
            //en la variable asis.
            Asistencia asis = lista.get(i);
            json.append("{");
            json.append("\"id\":").append(asis.getId()).append(",");
            //Agrega la propiedad "documentoIdentidad":, abre comillas ", llama a la función escaparJson() para limpiar caracteres especiales 
            //(como comillas o saltos de línea), añade el valor del documento, cierra las comillas y añade una coma.
            json.append("\"documentoIdentidad\":\"").append(escaparJson(asis.getDocumentoIdentidad())).append("\",");
            json.append("\"nombreEmpleado\":\"").append(escaparJson(asis.getNombreEmpleado())).append("\",");
            json.append("\"fechaHora\":\"").append(escaparJson(asis.getFechaHora())).append("\",");
            json.append("\"tipoEvento\":\"").append(escaparJson(asis.getTipoEvento())).append("\",");
            json.append("\"nombreJornada\":\"").append(escaparJson(asis.getNombreJornada())).append("\",");
            json.append("\"observacion\":\"").append(escaparJson(asis.getObservacion())).append("\"");
            json.append("}");
            
            //Verifica si el elemento actual no es el último de la lista. Si faltan más elementos por procesar, 
            //añade una coma , para separar los objetos JSON dentro del arreglo principal.
            if (i < lista.size() - 1) json.append(",");
        }
        json.append("]");

        //Se escribe el JSON final en el cuerpo de la respuesta HTTP.
        //El frontend lo recibe y lo interpreta para mostrar la tabla.
        out.print(json.toString());
    }

    @Override
    //Esto permite que navegadores confirmen que el servidor acepta solicitudes desde otros orígenes.
    //confirma que el servidor responde correctamente a las peticiones de verificación cors
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

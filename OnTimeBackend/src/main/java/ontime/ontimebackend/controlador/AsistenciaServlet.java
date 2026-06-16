package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.AsistenciaDAO;
import ontime.ontimebackend.modelo.Asistencia;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

// @WebServlet: URL invocada por asistenciaTabla.js e inicioModulo.js de forma modular
@WebServlet("/AsistenciaServlet")
public class AsistenciaServlet extends HttpServlet {
    //Se crea un objeto de acceso a datos (DAO) para consultar la base de datos de asistencia.
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();


// Limpia cadenas de texto para que no rompan el formato JSON (escapa comillas y saltos de línea).
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override//se utiliza para indicar que un método está sobrescribiendo .
    //doGet se ejecuta automáticamente cuando el servidor recibe una petición HTTP de tipo GET (por ejemplo, cuando un navegador accede a una URL).
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");//Esto le dice al navegador o aplicación que debe interpretar el contenido como datos en formato JSON.
        response.setCharacterEncoding("UTF-8");//Así se asegura que el texto enviado en la respuesta se interprete correctamente. interpreta caracteres
        PrintWriter out = response.getWriter();// permite escribir directamente en el cuerpo de la respuesta con print o outwrite

        //  LÓGICA DE DERIVACIÓN SEGURA:
        // Evaluamos si la petición viene del Dashboard de Novedades del Home o de la Tabla General de Reportes
        String vista = request.getParameter("vista");
        List<Asistencia> lista;
        
        
//Se lee el parámetro vista de la petición.
        if ("novedadesHome".equals(vista)) {
            // Llama al método optimizado con LIMIT 10 que dejamos en tu AsistenciaDAO para el Dashboard
            lista = asistenciaDAO.listarNovedadesRecientes();
        } else {
            // Llama al listado largo tradicional para el reporte analítico general (RF15)
            //Si no, se devuelve el listado completo de asistencia.
            lista = asistenciaDAO.listarAsistencia();
        }

        // 2. Renderizado de JSON nativo y directo mediante código Java Puro sin frameworks ni GSON
        StringBuilder json = new StringBuilder("[");
        //Se recorre la lista de objetos Asistencia.
        for (int i = 0; i < lista.size(); i++) {
            //Se construye un array JSON manualmente concatenando cadenas.
            //Cada objeto se convierte en un JSON con sus atributos 
            //Se usa escaparJson para evitar errores de formato.
            Asistencia asis = lista.get(i);
            json.append("{");
            json.append("\"id\":").append(asis.getId()).append(",");
            json.append("\"documentoIdentidad\":\"").append(escaparJson(asis.getDocumentoIdentidad())).append("\",");
            json.append("\"nombreEmpleado\":\"").append(escaparJson(asis.getNombreEmpleado())).append("\",");
            json.append("\"fechaHora\":\"").append(escaparJson(asis.getFechaHora())).append("\",");
            json.append("\"tipoEvento\":\"").append(escaparJson(asis.getTipoEvento())).append("\",");
            json.append("\"nombreJornada\":\"").append(escaparJson(asis.getNombreJornada())).append("\",");
            json.append("\"observacion\":\"").append(escaparJson(asis.getObservacion())).append("\"");
            json.append("}");
            if (i < lista.size() - 1) json.append(",");
        }
        json.append("]");

        // 3. Escribimos el string JSON resultante en el cuerpo de la respuesta HTTP
        out.print(json.toString());
    }

    @Override
    //Esto permite que navegadores confirmen que el servidor acepta solicitudes desde otros orígenes.
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

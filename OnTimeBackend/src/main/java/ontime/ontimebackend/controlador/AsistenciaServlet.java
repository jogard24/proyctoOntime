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
    
    private final AsistenciaDAO asistenciaDAO = new AsistenciaDAO();



    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        // 1. LÓGICA DE DERIVACIÓN SEGURA:
        // Evaluamos si la petición viene del Dashboard de Novedades del Home o de la Tabla General de Reportes
        String vista = request.getParameter("vista");
        List<Asistencia> lista;

        if ("novedadesHome".equals(vista)) {
            // Llama al método optimizado con LIMIT 10 que dejamos en tu AsistenciaDAO para el Dashboard
            lista = asistenciaDAO.listarNovedadesRecientes();
        } else {
            // Llama al listado largo tradicional para el reporte analítico general (RF15)
            lista = asistenciaDAO.listarAsistencia();
        }

        // 2. Renderizado de JSON nativo y directo mediante código Java Puro sin frameworks ni GSON
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
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
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}

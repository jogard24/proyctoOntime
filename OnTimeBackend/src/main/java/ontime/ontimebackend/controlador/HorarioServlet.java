package ontime.ontimebackend.controlador;

import ontime.ontimebackend.dao.HorarioDAO;
import ontime.ontimebackend.modelo.Horario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(name = "HorarioServlet", urlPatterns = {"/HorarioServlet"})
public class HorarioServlet extends HttpServlet {

    private final HorarioDAO horarioDAO = new HorarioDAO();

    //caracteres especiales para evitar que textos con comillas rompan la estructura del JSON resultante.
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    //Se ejecuta cuando la interfaz web solicita el listado de turnos para mostrar en pantalla
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        //llama al DAO para extraer la lista de objetos Horario desde MySQL
        List<Horario> horarios = horarioDAO.listarHorarios();
        
        //se construye en texto dinámico para empaquetar el JSON nativo
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < horarios.size(); i++) {
            Horario h = horarios.get(i);
            json.append("{");
            json.append("\"id\":").append(h.getId()).append(",");
            json.append("\"nombrejornada\":\"").append(escaparJson(h.getNombrejornada())).append("\",");
            json.append("\"horaEntrada\":\"").append(escaparJson(h.getHoraEntrada())).append("\",");
            json.append("\"horaSalida\":\"").append(escaparJson(h.getHoraSalida())).append("\"");
            json.append("}");
            if (i < horarios.size() - 1) json.append(",");//Inyecta una coma separadora solo si NO es el último elemento del arreglo
        }
        json.append("]");
        out.print(json.toString());//Envía los bytes del string JSON de vuelta por el canal Wi-Fi local hacia el Frontend
    }

    //Procesa de forma centralizada la creación, actualización o eliminación física de los turnos.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Captura el parámetro plano 'accion' enviado por tu horarios.js
        String accion = request.getParameter("accion");
        String idStr = request.getParameter("id");
        
        //Evalúa si la variable accion es nula O si la variable idStr es nula 
        if (accion == null || idStr == null) {
            //Si se cumple cualquiera de las dos condiciones, significa que el Frontend envió una petición incompleta 
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//el sistema detiene mostrando error
            response.getWriter().write("{\"status\":\"error\",\"message\":\"Parámetros faltantes\"}");
            return;//expulsa la solicitud mediante un return
        }

        int id = Integer.parseInt(idStr.trim());
        boolean resultado = false;
        
        //Convierte la cadena de texto de red en un entero limpio (INT) para base de datos
        try {//Evaluamos la variable accion utilizando un esquema de comparación
            if ("eliminar".equals(accion)) {// CASO: ELIMINACIÓN DE TURNOS
                // Delega de forma directa la eliminacion física al método especializado del DAO
                resultado = horarioDAO.eliminarHorario(id);
                
                //Si el texto de la acción es estrictamente igual a la palabra "eliminar"', 
                //el controlador toma el camino directo e invoca al método eliminarHorario(id)
            } else {
                //else determina que la solicitud no es una eliminación, por lo tanto, el software infiere que se trata de una inyección de datos
                // Para 'crear' o 'actualizar', construimos el objeto Horario desde los parámetros planos
                Horario h = new Horario();
                h.setNombrejornada(request.getParameter("nombrejornada"));
                h.setHoraEntrada(request.getParameter("horaEntrada"));
                h.setHoraSalida(request.getParameter("horaSalida"));

                //Si la variable accion es igual a 'crear', llama al metodo crearHorario
                if ("crear".equals(accion)) {
                    // 🚀 CONTROL DE AUTOINCREMENTAL: Si el id es mayor a 0 (edición) se inyecta, 
                    // si viene en 0 (nuevo) se omite para dejar que MySQL asigne la llave primaria sola.
                    if (id > 0) {
                        h.setId(id);
                    }
                    resultado = horarioDAO.crearHorario(h);
                    //de lo contrario si la accion es actualizar el flujo desvia a actualizarHorario
                } else if ("actualizar".equals(accion)) {
                    h.setId(id); // En actualización el ID sí es estrictamente obligatorio para el WHERE id = ?
                    resultado = horarioDAO.actualizarHorario(h);
                }
            }
            
            //si resultado es verdadero que se haya modificado el horario de forma exitosa 
            if (resultado) {
                //el servlet responde de forma exitosa alineado al token 'success' de horarios.js
                response.getWriter().write("{\"status\":\"success\"}");
            } else {
                //de lo contrario falla 
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Operación rechazada por la Base de Datos.\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
    }
}


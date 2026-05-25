package ontime.ontimebackend;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.Duration;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "AsistenciaServlet", urlPatterns = {"/AsistenciaServlet", "/asistencia"})
public class AsistenciaServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // Configuración de CORS y respuesta JSON
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setContentType("application/json;charset=UTF-8");
        
        PrintWriter out = response.getWriter();
        String cedula = request.getParameter("documento_identidad");
        
        if (cedula == null || cedula.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"status\":\"error\",\"message\":\"La cédula no puede estar vacía.\"}");
            return;
        }

        Connection con = null;
        PreparedStatement psUsuario = null;
        PreparedStatement psAsistenciaHoy = null;
        PreparedStatement psJornada = null;
        PreparedStatement psInsert = null;
        ResultSet rsUsuario = null;
        ResultSet rsAsistencia = null;
        ResultSet rsJornada = null;

        try {
            con = Conexion.obtenerConexion();
            if (con == null) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"No se pudo conectar con el servidor de Base de Datos.\"}");
                return;
            }
            
            // 1. Validar si la cédula existe en la tabla 'usuario' y traer su nombre
            String sqlBuscarUsuario = "SELECT id, nombre_completo FROM usuario WHERE documento_identidad = ?";
            psUsuario = con.prepareStatement(sqlBuscarUsuario);
            psUsuario.setString(1, cedula.trim());
            rsUsuario = psUsuario.executeQuery();
            
            if (rsUsuario.next()) {
                int usuarioId = rsUsuario.getInt("id");
                String nombreEmpleado = rsUsuario.getString("nombre_completo");
                
                // 2. DETECCIÓN AUTOMÁTICA DEL HORARIO
                LocalTime horaActual = LocalTime.now();
                int jornadaId = 101; // Mañana Completa
                String tipoTurno = "Mañana";
                
                if (horaActual.isAfter(LocalTime.of(13, 0))) {
                    jornadaId = 102; // Tarde Completa
                    tipoTurno = "Tarde";
                }
                
                // 3. CONSULTAR LOS HORARIOS TEÓRICOS DE LA JORNADA DESDE LA BD
                String sqlJornada = "SELECT hora_entrada, hora_salida FROM jornadaLaboral WHERE id = ?";
                psJornada = con.prepareStatement(sqlJornada);
                psJornada.setInt(1, jornadaId);
                rsJornada = psJornada.executeQuery();
                
                LocalTime horaEntradaTeorica = LocalTime.of(8, 0);
                LocalTime horaSalidaTeorica = LocalTime.of(17, 0);
                
                if (rsJornada.next()) {
                    if (rsJornada.getTime("hora_entrada") != null) {
                        horaEntradaTeorica = rsJornada.getTime("hora_entrada").toLocalTime();
                    }
                    if (rsJornada.getTime("hora_salida") != null) {
                        horaSalidaTeorica = rsJornada.getTime("hora_salida").toLocalTime();
                    }
                }
                
                // 4. DETECCIÓN AUTOMÁTICA DE EVENTO (ENTRADA O SALIDA)
                String sqlAsistenciaHoy = "SELECT tipo_evento FROM asistencia "
                                        + "WHERE usuario_id = ? AND DATE(fecha_hora) = CURRENT_DATE() "
                                        + "ORDER BY fecha_hora DESC LIMIT 1";
                
                psAsistenciaHoy = con.prepareStatement(sqlAsistenciaHoy);
                psAsistenciaHoy.setInt(1, usuarioId);
                rsAsistencia = psAsistenciaHoy.executeQuery();
                
                String proximoEvento = "entrada";
                String observacion = "";
                String mensajeExito = "";
                
                if (rsAsistencia.next()) {
                    String ultimoEvento = rsAsistencia.getString("tipo_evento");
                    
                    if ("entrada".equals(ultimoEvento)) {
                        proximoEvento = "salida";
                        
                        // CALCULAR HORAS EXTRAS
                        if (horaActual.isAfter(horaSalidaTeorica)) {
                            long minutosExtras = Duration.between(horaSalidaTeorica, horaActual).toMinutes();
                            double horasExtras = Math.round((minutosExtras / 60.0) * 100.0) / 100.0;
                            observacion = "Salida con " + horasExtras + " horas extras.";
                            mensajeExito = "¡Salida registrada, " + nombreEmpleado + "! Calculadas " + horasExtras + " hrs extras.";
                        } else {
                            observacion = "Salida regular a tiempo.";
                            mensajeExito = "¡Salida registrada correctamente! Que tengas un excelente descanso, " + nombreEmpleado + ".";
                        }
                    } else {
                        proximoEvento = "entrada";
                    }
                }
                
                // Si es una entrada (ya sea la primera del día o un reingreso)
                if ("entrada".equals(proximoEvento)) {
                    // CALCULAR RETARDO (Damos un margen de 5 minutos de gracia)
                    if (horaActual.isAfter(horaEntradaTeorica.plusMinutes(5))) {
                        long minutosRetardo = Duration.between(horaEntradaTeorica, horaActual).toMinutes();
                        observacion = "Retardo de " + minutosRetardo + " minutos.";
                        mensajeExito = "¡Ingreso registrado con retardo (" + minutosRetardo + " min), " + nombreEmpleado + ".";
                    } else {
                        observacion = "Ingreso a tiempo.";
                        mensajeExito = "¡Ingreso registrado a tiempo! Bienvenido a OnTime, " + nombreEmpleado + ".";
                    }
                }
                
                // 5. Insertar el registro definitivo con la novedad calculada
                String sqlInsert = "INSERT INTO asistencia (usuario_id, tipo_evento, fecha_hora, observacion, tipo_turno, jornada_id) "
                                 + "VALUES (?, ?, NOW(), ?, ?, ?)";
                
                psInsert = con.prepareStatement(sqlInsert);
                psInsert.setInt(1, usuarioId);
                psInsert.setString(2, proximoEvento);
                psInsert.setString(3, observacion);
                psInsert.setString(4, tipoTurno);
                psInsert.setInt(5, jornadaId);
                psInsert.executeUpdate();
                
                out.print("{\"status\":\"success\",\"message\":\"" + mensajeExito + "\"}");
                
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"status\":\"error\",\"message\":\"La cédula no coincide con ningún empleado registrado.\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorLimpio = e.getMessage() != null ? e.getMessage().replace("\"", "'").replace("\n", " ") : "Error desconocido";
            out.print("{\"status\":\"error\",\"message\":\"Error interno en la BD: " + errorLimpio + "\"}");
        } finally {
            // Cierre ordenado de los objetos JDBC
            try { if (rsJornada != null) rsJornada.close(); } catch (Exception e) {}
            try { if (rsAsistencia != null) rsAsistencia.close(); } catch (Exception e) {}
            try { if (rsUsuario != null) rsUsuario.close(); } catch (Exception e) {}
            try { if (psJornada != null) psJornada.close(); } catch (Exception e) {}
            try { if (psAsistenciaHoy != null) psAsistenciaHoy.close(); } catch (Exception e) {}
            try { if (psUsuario != null) psUsuario.close(); } catch (Exception e) {}
            try { if (psInsert != null) psInsert.close(); } catch (Exception e) {}
            try { if (con != null) con.close(); } catch (Exception e) {}
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        response.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
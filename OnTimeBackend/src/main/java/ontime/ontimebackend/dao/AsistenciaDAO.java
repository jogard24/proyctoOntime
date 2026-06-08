package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    // Recupera todo el historial de asistencias (usando un JOIN para traer el nombre del usuario)
    public List<Asistencia> listarAsistencia() {
        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT a.id, u.nombre AS nombre_empleado, a.fecha_hora, a.tipo_evento, a.observacion, a.tipo_turno "
                + "FROM asistencia a JOIN usuario u ON a.usuario_id = u.id ORDER BY a.fecha_hora DESC";
        // El bloque try-with-resources cierra automáticamente la conexión y el statement
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Asistencia asis = new Asistencia();
                asis.setId(rs.getInt("id"));
                asis.setNombreEmpleado(rs.getString("nombre_empleado"));
                asis.setFechaHora(rs.getString("fecha_hora"));
                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setObservacion(rs.getString("observacion"));
                asis.setTipoTurno(rs.getString("tipo_turno"));
                lista.add(asis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Valida si un usuario existe en el sistema basándose en su documento
    public boolean existeEmpleado(String docIdentidad) {
        String sql = "SELECT id FROM usuario WHERE documento_identidad = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Métodos de consulta rápida (Id, Nombre, Último evento)
    // Estos son usados por el PinpadServlet para saber "quién es" y "qué le toca hacer"
    public int obtenerIdPorDocumento(String docIdentidad) {
        String sql = "SELECT id FROM usuario WHERE documento_identidad = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public String obtenerNombrePorDocumento(String docIdentidad) {
        String sql = "SELECT nombre FROM usuario WHERE documento_identidad = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nombre");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Usuario";
    }
// Determina si el empleado debe marcar entrada o salida según su último registro

    public String obtenerUltimoTipoEvento(int usuarioId) {
        String sql = "SELECT tipo_evento FROM asistencia WHERE usuario_id = ? ORDER BY fecha_hora DESC LIMIT 1";
        // ... (lógica de consulta)
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("tipo_evento");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "salida";// Por defecto, si nunca ha marcado, asumimos que su próximo evento es entrada
    }
// Inserta un nuevo registro en la tabla asistencia

    public boolean registrarAsistencia(int usuarioId, String tipoEvento, String observacion, String tipoTurno, int jornadaId) {
        String sql = "INSERT INTO asistencia (usuario_id, tipo_evento, observacion, tipo_turno, jornada_id, fecha_hora) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tipoEvento);
            ps.setString(3, observacion);
            ps.setString(4, tipoTurno);
            ps.setInt(5, jornadaId);
            return ps.executeUpdate() > 0;// Retorna true si se insertó al menos una fila
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
// Trae la hora de entrada de la jornada asignada al usuario

    public LocalTime obtenerHoraEntrada(int usuarioId) {
        String sql = "SELECT j.hora_entrada FROM jornadaLaboral j "
                + "JOIN contrato c ON j.id = c.jornada_id "
                + "WHERE c.usuario_id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTime("hora_entrada").toLocalTime();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return LocalTime.of(8, 0); // Hora por defecto si algo falla

    }

    public List<Asistencia> listarNovedadesRecientes() {
        List<Asistencia> lista = new ArrayList<>();
        // Usamos el SQL corregido aquí
//    String sql = "SELECT a.id, u.nombre, a.tipo_evento, a.fecha_hora, a.observacion, a.tipo_turno "
//               + "FROM asistencia a "
//               + "INNER JOIN usuario u ON a.usuario_id = u.id "
//               + "ORDER BY a.fecha_hora DESC LIMIT 10"; // LIMIT 10 para que no cargue todo el historial

        String sql = "SELECT a.id, u.nombre, a.tipo_evento, a.fecha_hora, a.observacion, j.nombrejornada AS nombre_turno "
                + "FROM asistencia a "
                + "INNER JOIN usuario u ON a.usuario_id = u.id "
                + "LEFT JOIN jornadaLaboral j ON a.jornada_id = j.id "
                + "ORDER BY a.fecha_hora DESC LIMIT 10";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Asistencia asis = new Asistencia();
                asis.setId(rs.getInt("id"));
                asis.setNombreEmpleado(rs.getString("nombre"));
                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
                asis.setObservacion(rs.getString("observacion"));
                asis.setTipoTurno(rs.getString("nombre_turno"));
                lista.add(asis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

}

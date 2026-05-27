package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    public List<Asistencia> listarAsistencia() {
        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT a.id, u.nombre AS nombre_empleado, a.fecha_hora, a.tipo_evento, a.observacion, a.tipo_turno "
                + "FROM asistencia a JOIN usuario u ON a.usuario_id = u.id ORDER BY a.fecha_hora DESC";
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

    public String obtenerUltimoTipoEvento(int usuarioId) {
        String sql = "SELECT tipo_evento FROM asistencia WHERE usuario_id = ? ORDER BY fecha_hora DESC LIMIT 1";
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
        return "salida";
    }

    public boolean registrarAsistencia(int usuarioId, String tipoEvento, String observacion, String tipoTurno, int jornadaId) {
        String sql = "INSERT INTO asistencia (usuario_id, tipo_evento, observacion, tipo_turno, jornada_id, fecha_hora) VALUES (?, ?, ?, ?, ?, NOW())";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tipoEvento);
            ps.setString(3, observacion);
            ps.setString(4, tipoTurno);
            ps.setInt(5, jornadaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

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
    String sql = "SELECT a.id, u.nombre, a.tipo_evento, a.fecha_hora, a.observacion, a.tipo_turno "
               + "FROM asistencia a "
               + "INNER JOIN usuario u ON a.usuario_id = u.id "
               + "ORDER BY a.fecha_hora DESC LIMIT 10"; // LIMIT 10 para que no cargue todo el historial

    try (Connection con = Conexion.obtenerConexion();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {
        
        while (rs.next()) {
            Asistencia asis = new Asistencia();
            asis.setId(rs.getInt("id"));
            asis.setNombreEmpleado(rs.getString("nombre")); // Mapeo correcto
            asis.setTipoEvento(rs.getString("tipo_evento"));
            asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
            asis.setObservacion(rs.getString("observacion"));
            asis.setTipoTurno(rs.getString("tipo_turno"));
            lista.add(asis);
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return lista;
}
    
}

package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AsistenciaDAO {

    /**
     * Recupera todo el historial de asistencias uniendo las tablas para el
     * reporte analítico.
     */
    //Declaramos que el método devuelve una "lista" de objetos de tipo Asistencia
    public List<Asistencia> listarAsistencia() {
//Inicializamos una lista vacía. Aquí es donde iremos guardando 
//cada registro que extraigamos de la base de datos.        
        List<Asistencia> lista = new ArrayList<>();
        //  
        String sql = "SELECT a.id, u.documento_identidad, u.nombre, u.apellido, a.fecha_hora, a.tipo_evento, a.observacion, j.nombrejornada "
                + "FROM asistencia a "
                + "INNER JOIN usuario u ON a.usuario_id = u.id "
                + "LEFT JOIN jornadaLaboral j ON a.jornada_id = j.id "
                + "ORDER BY a.fecha_hora DESC";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Asistencia asis = new Asistencia();
                asis.setId(rs.getInt("id"));
                asis.setDocumentoIdentidad(rs.getString("documento_identidad"));
                asis.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));

                // Formateamos la fecha a String limpio para JavaScript
                asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setObservacion(rs.getString("observacion"));
                asis.setNombreJornada(rs.getString("nombrejornada")); // Sincronizado con asistenciaTabla.js
                lista.add(asis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Valida si un usuario existe en el sistema basándose en su documento (RF09)
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

    // Métodos de consulta rápida requeridos por tu Pinpad
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

    public String obtenerNombrePorDocumento(String documento) {
        String sql = "SELECT nombre, apellido FROM usuario WHERE documento_identidad = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String nombreBd = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
                    String apellidoBd = rs.getString("apellido") != null ? rs.getString("apellido").trim() : "";

                    // BLINDAJE: Si la columna nombre ya contiene el apellido físicamente,
                    // retornamos solo el campo nombre para que no se duplique en el celular.
                    if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase()) || apellidoBd.isEmpty()) {
                        return nombreBd;
                    } else {
                        return nombreBd + " " + apellidoBd;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Usuario";
    }

    /**
     * Determina de forma automática el próximo evento del empleado (RF10). Si
     * su última marca fue 'entrada', le toca marcar 'salida'. De lo contrario,
     * toca 'entrada'.
     */
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
        return "salida"; // Si nunca ha marcado, retorna 'salida' para que la lógica lo asigne como una 'entrada'
    }

    /**
     * Inserta la marca física inyectando de manera estricta la hora del
     * servidor MySQL con NOW() (RF10).
     */
    public boolean registrarAsistencia(int usuarioId, String tipoEvento, String observacion, int jornadaId) {
        // Corrección 2: Removido el campo obsoleto tipo_turno
        String sql = "INSERT INTO asistencia (usuario_id, tipo_evento, observacion, jornada_id, fecha_hora) VALUES (?, ?, ?, ?, NOW())";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            ps.setString(2, tipoEvento);
            ps.setString(3, observacion);
            ps.setInt(4, jornadaId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trae la jornada horaria contractual del empleado para el cálculo
     * automático de puntualidad.
     */
    public int obtenerJornadaIdYHoraEntrada(int usuarioId, StringBuilder horaEntradaOut) {
        String sql = "SELECT j.id, j.hora_entrada FROM jornadaLaboral j "
                + "INNER JOIN contrato c ON j.id = c.jornada_id "
                + "WHERE c.usuario_id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    horaEntradaOut.append(rs.getTime("hora_entrada").toString());
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        horaEntradaOut.append("08:00:00");
        return 1; // Retorna la jornada 1 por defecto
    }

    /**
     * Método estrella del Feed de Novedades del Home que mapea las asistencias
     * y datos del usuario (Alimenta tu homeInicio.js).
     *
     */
    public List<Asistencia> listarNovedadesRecientes() {
        List<Asistencia> lista = new ArrayList<>();
        String sql = "SELECT a.id, u.documento_identidad, u.nombre, u.apellido, a.tipo_evento, a.fecha_hora, a.observacion, j.nombrejornada "
                + "FROM asistencia a "
                + "INNER JOIN usuario u ON a.usuario_id = u.id "
                + "LEFT JOIN jornadaLaboral j ON a.jornada_id = j.id "
                + "ORDER BY a.fecha_hora DESC LIMIT 10";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Asistencia asis = new Asistencia();
                asis.setId(rs.getInt("id"));
                asis.setDocumentoIdentidad(rs.getString("documento_identidad"));

                // BLINDAJE DE NOMBRE DUPLICADO: Extraemos y limpiamos las cadenas
                String nombreBd = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
                String apellidoBd = rs.getString("apellido") != null ? rs.getString("apellido").trim() : "";

                // Si la columna 'nombre' ya contiene el apellido (o si el apellido viene vacío), 
                // inyectamos solo el nombre para evitar el efecto espejo.
                if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase()) || apellidoBd.isEmpty()) {
                    asis.setNombreEmpleado(nombreBd);
                } else {
                    asis.setNombreEmpleado(nombreBd + " " + apellidoBd);
                }

                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
                asis.setObservacion(rs.getString("observacion"));
                asis.setNombreJornada(rs.getString("nombrejornada"));
                lista.add(asis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

}

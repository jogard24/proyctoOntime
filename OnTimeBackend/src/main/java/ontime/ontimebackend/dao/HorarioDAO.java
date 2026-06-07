package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Horario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    public List<Horario> listarHorarios() {
        List<Horario> lista = new ArrayList<>();
        String sql = "SELECT id, nombrejornada, hora_entrada, hora_salida FROM jornadaLaboral";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Horario horario = new Horario();
                horario.setId(rs.getInt("id"));
                horario.setTurno(rs.getString("nombrejornada"));
                horario.setEntrada(rs.getString("hora_entrada"));
                horario.setSalida(rs.getString("hora_salida"));
                lista.add(horario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean crearHorario(Horario horario) {
        String sql = "INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.obtenerConexion()) {
            int nextId = obtenerSiguienteId(con);
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, nextId);
                ps.setString(2, horario.getTurno());
                ps.setString(3, horario.getEntrada());
                ps.setString(4, horario.getSalida());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarHorario(Horario horario) {
        String sql = "UPDATE jornadaLaboral SET nombrejornada = ?, hora_entrada = ?, hora_salida = ? WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, horario.getTurno());
            ps.setString(2, horario.getEntrada());
            ps.setString(3, horario.getSalida());
            ps.setInt(4, horario.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarHorario(int id) {
        String sql = "DELETE FROM jornadaLaboral WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private int obtenerSiguienteId(Connection con) throws SQLException {
        String sql = "SELECT COALESCE(MAX(id), 100) + 1 AS next_id FROM jornadaLaboral";
        try (Statement stmt = con.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("next_id");
            }
        }
        return 101;
    }
}

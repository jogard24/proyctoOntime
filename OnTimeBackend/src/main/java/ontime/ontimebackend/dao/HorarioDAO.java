package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Horario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    public List<Horario> listarHorarios() {
        List<Horario> lista = new ArrayList<>();
        String sql = "SELECT id, nombrejornada, hora_entrada, hora_salida FROM jornadaLaboral";

        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Horario horario = new Horario();
                horario.setId(rs.getInt("id"));
                horario.setNombrejornada(rs.getString("nombrejornada"));
                horario.setHoraEntrada(rs.getString("hora_entrada"));
                horario.setHoraSalida(rs.getString("hora_salida"));
                lista.add(horario);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean crearHorario(Horario horario) {
        String sql = "INSERT INTO jornadaLaboral (id, nombrejornada, hora_entrada, hora_salida) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, horario.getId()); // Inserción directa del ID ingresado por el Administrador
            ps.setString(2, horario.getNombrejornada());
            ps.setString(3, horario.getHoraEntrada());
            ps.setString(4, horario.getHoraSalida());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarHorario(Horario horario) {
        String sql = "UPDATE jornadaLaboral SET nombrejornada = ?, hora_entrada = ?, hora_salida = ? WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, horario.getNombrejornada());
            ps.setString(2, horario.getHoraEntrada());
            ps.setString(3, horario.getHoraSalida());
            ps.setInt(4, horario.getId());
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarHorario(int id) {
        String sql = "DELETE FROM jornadaLaboral WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}


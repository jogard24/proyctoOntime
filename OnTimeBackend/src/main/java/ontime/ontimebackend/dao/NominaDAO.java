package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Nomina;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NominaDAO {
    public List<Nomina> obtenerReporteNomina() {
        List<Nomina> lista = new ArrayList<>();
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, " +
                     "COALESCE(COUNT(DISTINCT DATE(a.fecha_hora)), 0) as dias_trabajados, " +
                     "COALESCE(SUM(CASE WHEN a.observacion LIKE '%extras%' THEN 1 ELSE 0 END), 0) as total_extras, " +
                     "COALESCE(SUM(CASE WHEN a.observacion LIKE '%Retardo%' THEN 1 ELSE 0 END), 0) as total_retardos " +
                     "FROM usuario u LEFT JOIN asistencia a ON u.id = a.usuario_id " +
                     "GROUP BY u.id, u.documento_identidad, u.nombre";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                Nomina n = new Nomina();
                n.setId(rs.getInt("id"));
                n.setCedula(rs.getString("documento_identidad"));
                n.setNombre(rs.getString("nombre"));
                n.setDiasTrabajados(rs.getInt("dias_trabajados"));
                n.setTotalExtras(rs.getInt("total_extras"));
                n.setTotalRetardos(rs.getInt("total_retardos"));
                lista.add(n);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }
}
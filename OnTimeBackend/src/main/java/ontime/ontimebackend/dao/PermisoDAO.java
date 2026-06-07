package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.PermisoLaboral;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermisoDAO {

    public List<PermisoLaboral> listarPermisos() {
        List<PermisoLaboral> lista = new ArrayList<>();
        String sql = "SELECT id, permiso_id, tipo_permiso, fecha_solicitud, desde, hasta, estado, aprobado FROM permiso_laboral";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                PermisoLaboral permiso = new PermisoLaboral();
                permiso.setId(rs.getInt("id"));
                permiso.setPermisoId(rs.getInt("permiso_id"));
                permiso.setTipoPermiso(rs.getString("tipo_permiso"));
                permiso.setFechaSolicitud(rs.getString("fecha_solicitud"));
                permiso.setDesde(rs.getString("desde"));
                permiso.setHasta(rs.getString("hasta"));
                permiso.setEstado(rs.getString("estado"));
                permiso.setAprobado(rs.getBoolean("aprobado"));
                lista.add(permiso);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean crearPermiso(PermisoLaboral permiso) {
        String sql = "INSERT INTO permiso_laboral (permiso_id, tipo_permiso, fecha_solicitud, desde, hasta, estado, aprobado) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, permiso.getPermisoId());
            ps.setString(2, permiso.getTipoPermiso());
            ps.setString(3, permiso.getFechaSolicitud() == null ? null : permiso.getFechaSolicitud());
            ps.setString(4, permiso.getDesde());
            ps.setString(5, permiso.getHasta());
            ps.setString(6, permiso.getEstado() == null ? "pendiente" : permiso.getEstado());
            ps.setBoolean(7, permiso.isAprobado());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean aprobarPermiso(int id) {
        String sql = "UPDATE permiso_laboral SET estado = 'aprobado', aprobado = TRUE WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarPermiso(int id) {
        String sql = "DELETE FROM permiso_laboral WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

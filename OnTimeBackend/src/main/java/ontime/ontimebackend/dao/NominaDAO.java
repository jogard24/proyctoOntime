package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Nomina;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NominaDAO {

    /**
     * Consulta analítica por periodos que extrae el salario contractual de cada empleado
     * y cuenta dinámicamente sus extras y retardos del mes (RF17 y RF18).
     */
    public List<Nomina> obtenerReporteNomina(String periodo) {
        List<Nomina> lista = new ArrayList<>();
        
        // SQL refinado alineado a Ontime3BD: Lee salario_base y filtra las asistencias por año-mes
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base, " +
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'entrada' AND a.observacion LIKE '%retardo%' THEN 1 ELSE 0 END), 0) as total_retardos, " +
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'salida' THEN 1 ELSE 0 END), 0) as total_extras " +
                     "FROM usuario u " +
                     "INNER JOIN contrato con ON u.id = con.usuario_id " +
                     "LEFT JOIN asistencia a ON u.id = a.usuario_id AND DATE_FORMAT(a.fecha_hora, '%Y-%m') = ? " +
                     "GROUP BY u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, periodo); // Filtro dinámico del mes (ej: "2026-06") enviado por el frontend
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Nomina n = new Nomina();
                    n.setUsuarioId(rs.getInt("id"));
                    n.setDocumento(rs.getString("documento_identidad"));
                    n.setNombre(rs.getString("nombre"));
                    n.setApellido(rs.getString("apellido"));
                    
                    // Extrae el salario real de la tabla contrato de Ontime3BD (RF26)
                    n.setSalarioBasePeriodo(rs.getBigDecimal("salario_base"));
                    
                    // Contadores dinámicos que alimentarán las operaciones de nomina.js
                    n.setTotalRetardos(rs.getInt("total_retardos"));
                    n.setTotalExtras(rs.getInt("total_extras"));
                    
                    lista.add(n);
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return lista;
    }

    /**
     * Registra y congela de forma permanente el histórico financiero en tus tablas relacionales (RF17 y RF19).
     */
    public boolean guardarNominaPeriodo(int usuarioId, java.math.BigDecimal salarioBase, double extras, java.math.BigDecimal neto, String periodoStr) {
        String sqlPeriodo = "INSERT INTO periodo_nomina (fecha_inicio, fecha_fin, estado) VALUES (?, ?, 'cerrado')";
        String sqlNomina = "INSERT INTO nomina (usuario_id, periodo_id, salario_base_periodo, total_horas_extras, total_neto) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_nomina (nomina_id, concepto_id, valor, observacion) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción limpia: Guarda todo o nada

            int periodoId = 1;
            // 1. Crear el registro del periodo (Simulación escolar básica usando el string del mes)
            try (PreparedStatement psP = con.prepareStatement(sqlPeriodo, Statement.RETURN_GENERATED_KEYS)) {
                psP.setString(1, periodoStr + "-01");
                psP.setString(2, periodoStr + "-30");
                psP.executeUpdate();
                try (ResultSet rs = psP.getGeneratedKeys()) {
                    if (rs.next()) periodoId = rs.getInt(1);
                }
            }

            int nominaId = 0;
            // 2. Insertar en la tabla central 'nomina'
            try (PreparedStatement psN = con.prepareStatement(sqlNomina, Statement.RETURN_GENERATED_KEYS)) {
                psN.setInt(1, usuarioId);
                psN.setInt(2, periodoId);
                psN.setBigDecimal(3, salarioBase);
                psN.setDouble(4, extras);
                psN.setBigDecimal(5, neto);
                psN.executeUpdate();
                try (ResultSet rs = psN.getGeneratedKeys()) {
                    if (rs.next()) nominaId = rs.getInt(1);
                }
            }

            // 3. Insertar el renglón analítico en 'detalle_nomina' para registrar el histórico de la liquidación
            try (PreparedStatement psD = con.prepareStatement(sqlDetalle)) {
                psD.setInt(1, nominaId);
                psD.setInt(2, 1); // Concepto ID 1 (Liquidación general)
                psD.setBigDecimal(3, neto);
                psD.setString(4, "Nómina consolidada para el periodo " + periodoStr);
                psD.executeUpdate();
            }

            con.commit(); // Consolidar cambios de forma atómica en MySQL
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}

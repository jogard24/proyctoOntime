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
        
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base, " +
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'entrada' AND a.observacion LIKE '%retardo%' THEN 1 ELSE 0 END), 0) as total_retardos, " +
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'salida' THEN 1 ELSE 0 END), 0) as total_extras " +
                     "FROM usuario u " +
                     "INNER JOIN contrato con ON u.id = con.usuario_id " +
                     "LEFT JOIN asistencia a ON u.id = a.usuario_id AND DATE_FORMAT(a.fecha_hora, '%Y-%m') = ? " +
                     "GROUP BY u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, periodo); 
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Nomina n = new Nomina();
                    n.setUsuarioId(rs.getInt("id"));
                    n.setDocumento(rs.getString("documento_identidad"));
                    n.setNombre(rs.getString("nombre"));
                    n.setApellido(rs.getString("apellido"));
                    n.setSalarioBasePeriodo(rs.getBigDecimal("salario_base"));
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

  
     // Registra y congela el histórico financiero en tus tablas relacionales,
     // alimentando la tabla puente 'nomina_asistencia' para amarrar físicamente el pago a las marcas operativas (RF17 y RF19).

    public boolean guardarNominaPeriodo(int usuarioId, java.math.BigDecimal salarioBase, double extras, java.math.BigDecimal neto, String periodoStr) {
        String sqlPeriodo = "INSERT INTO periodo_nomina (fecha_inicio, fecha_fin, estado) VALUES (?, ?, 'cerrado')";
        String sqlNomina = "INSERT INTO nomina (usuario_id, periodo_id, salario_base_periodo, total_horas_extras, total_neto) VALUES (?, ?, ?, ?, ?)";
        String sqlDetalle = "INSERT INTO detalle_nomina (nomina_id, concepto_id, valor, observacion) VALUES (?, ?, ?, ?)";
        
        // Query para extraer las asistencias reales del mes que alimentaron la nómina
        String sqlBuscarAsistencias = "SELECT id FROM asistencia WHERE usuario_id = ? AND DATE_FORMAT(fecha_hora, '%Y-%m') = ?";
        // Query para inyectar la relación física en la tabla puente de quiebre
        String sqlInsertPuente = "INSERT INTO nomina_asistencia (nomina_id, asistencia_id) VALUES (?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción limpia abierta: Guarda todo o nada

            int periodoId = 1;
            //  Crear el registro del periodo el string del mes
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

     
            // ALIMENTAR TABLA PUENTE
    
            List<Integer> listAsistenciasIds = new ArrayList<>();
            try (PreparedStatement psBuscar = con.prepareStatement(sqlBuscarAsistencias)) {
                psBuscar.setInt(1, usuarioId);
                psBuscar.setString(2, periodoStr);
                try (ResultSet rs = psBuscar.executeQuery()) {
                    while (rs.next()) {
                        listAsistenciasIds.add(rs.getInt("id"));
                    }
                }
            }

            // Inyectamos las relaciones en la tabla 'nomina_asistencia' uno a uno en lote
            if (!listAsistenciasIds.isEmpty()) {
                try (PreparedStatement psPuente = con.prepareStatement(sqlInsertPuente)) {
                    for (int asistenciaId : listAsistenciasIds) {
                        psPuente.setInt(1, nominaId);
                        psPuente.setInt(2, asistenciaId);
                        psPuente.addBatch(); // Empaqueta para inserción masiva veloz
                    }
                    psPuente.executeBatch(); // Corre todas las uniones físicas en un solo ciclo
                }
            }
            // =========================================================================

            con.commit(); // Consolidar todos los cambios de forma 100% atómica en MySQL
            System.out.println(" ÉXITO: Nómina #" + nominaId + " guardada y amarrada a sus marcas de asistencia.");
            return true;
        } catch (SQLException e) {
            System.out.println(" ERROR TRANSACCIONAL EN NOMINADAO: " + e.getMessage());
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


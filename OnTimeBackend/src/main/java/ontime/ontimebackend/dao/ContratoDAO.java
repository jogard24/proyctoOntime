package ontime.ontimebackend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Contrato;

public class ContratoDAO {

    public List<Contrato> listarContratosTodos() {
        List<Contrato> lista = new ArrayList<>();
        // AJUSTE 1: Agregamos 'c.estado' al SELECT para saber si el contrato está activo o inactivo
        String sql = "SELECT DISTINCT c.id AS contrato_id,u.id AS usuario_id, u.nombre, u.apellido, c.cargo, c.salario_base, c.fecha_inicio, c.fecha_fin, c.periodo_pago, c.estado " +
                     "FROM contrato c " +
                     "INNER JOIN usuario u ON c.usuario_id = u.id " +
                     "ORDER BY u.nombre ASC";

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Contrato c = new Contrato();
                c.setId(rs.getInt("contrato_id"));
                c.setUsuarioId(rs.getInt("usuario_id"));
                c.setEmpleadoNombre(rs.getString("nombre") + " " + rs.getString("apellido"));
                c.setCargo(rs.getString("cargo"));
                c.setSalarioBase(rs.getBigDecimal("salario_base")); 
                c.setFechaInicio(rs.getString("fecha_inicio"));
                c.setFechaFin(rs.getString("fecha_fin"));
                
                // AJUSTE 2: Mapeamos los datos de periodicidad y el estado real de la BD
                c.setPeriodoPago(rs.getString("periodo_pago")); 
                
                // Como necesitamos enviar el estado 'activo/inactivo' al JS, 
                // usaremos de forma temporal el campo tipoContrato de tu clase para transportar esta palabra
                c.setTipoContrato(rs.getString("estado")); 
                
                lista.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean prorrogarContratoMeses(int contratoId, int meses) {
        String sql = "UPDATE contrato SET fecha_fin = DATE_ADD(fecha_fin, INTERVAL ? MONTH) WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, meses);
            ps.setInt(2, contratoId);
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * AJUSTE 3: EL MÉTODO TRANSACCIONAL QUE FALTA EN TU CÓDIGO
     * Cambia de forma simultánea el estado del contrato y del usuario a 'inactivo' (RF29).
     */
    public boolean clausurarContratoYEmpleado(int contratoId) {
        // Sentencia A: Apaga el contrato en MySQL
        String sqlContrato = "UPDATE contrato SET estado = 'inactivo' WHERE id = ?";
        
        // Sentencia B: Apaga al usuario en la tabla de personal cruzando las llaves
        String sqlUsuario = "UPDATE usuario u " +
                            "INNER JOIN contrato c ON u.id = c.usuario_id " +
                            "SET u.estado = 'inactivo' " + // ◄ Masculino exacto alineado a tu BD
                            "WHERE c.id = ?";

        try (Connection con = Conexion.obtenerConexion()) {
            // Ponemos el freno de mano de seguridad (Transacción Atómica)
            con.setAutoCommit(false);

            try (PreparedStatement psC = con.prepareStatement(sqlContrato);
                 PreparedStatement psU = con.prepareStatement(sqlUsuario)) {
                
                psC.setInt(1, contratoId);
                psU.setInt(1, contratoId);

                int filasC = psC.executeUpdate();
                int filasU = psU.executeUpdate();

                // Si ambas tablas aceptaron la inactivación, guardamos de forma inmutable
                if (filasC > 0 && filasU > 0) {
                    con.commit();
                    return true;
                } else {
                    con.rollback(); // Deshace los cambios si uno falla para no romper la integridad
                    return false;
                }
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    

    public Contrato buscarContratoPorId(int contratoId) {
        // Query paramétrico que extrae los datos del contrato junto con el nombre del usuario
        String sql = "SELECT c.id AS contrato_id,c.usuario_id, u.nombre, u.apellido, c.cargo, c.salario_base, c.fecha_inicio, c.fecha_fin, c.periodo_pago, c.estado " +
                     "FROM contrato c " +
                     "INNER JOIN usuario u ON c.usuario_id = u.id " +
                     "WHERE c.id = ?"; // ◄ Filtro atómico por ID

        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, contratoId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Contrato c = new Contrato();
                    c.setId(rs.getInt("contrato_id"));
                    c.setUsuarioId(rs.getInt("usuario_id"));
                    c.setEmpleadoNombre(rs.getString("nombre") + " " + rs.getString("apellido"));
                    c.setCargo(rs.getString("cargo"));
                    c.setSalarioBase(rs.getBigDecimal("salario_base")); // Sincronizado con tu BigDecimal
                    c.setFechaInicio(rs.getString("fecha_inicio"));
                    c.setFechaFin(rs.getString("fecha_fin"));
                    c.setPeriodoPago(rs.getString("periodo_pago")); 
                    c.setTipoContrato(rs.getString("estado")); // Almacena el estado activo/inactivo
                    return c; // Retorna el objeto completamente poblado
                }
            }
        } catch (SQLException e) {
            System.err.println(" ERROR AL BUSCAR CONTRATO POR ID EN EL DAO: " + e.getMessage());
            e.printStackTrace();
        }
        return null; // Retorna null si el contrato no existe en la base de datos
    }

}

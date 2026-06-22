package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Nomina;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NominaDAO {

    /**
     * Consulta analítica por periodos que extrae el salario contractual de cada empleado
     * y cuenta dinámicamente sus extras y retardos del mes 
     */
    //se define y Se declara un método público que devuelve una lista de objetos Nomina
    public List<Nomina> obtenerReporteNomina(String periodo) {
        //Se crea una lista vacía donde se almacenarán los resultados de la consulta.
        List<Nomina> lista = new ArrayList<>();
        
        /**
         * Consulta analítica sincronizada de 7 columnas que separa el control de días asistidos
         * de los retardos y horas extras verdaderas calculadas en el Pinpad.
         */
        // Separamos el conteo de marcas de salida (días) del conteo de salidas tarde (extras)
        // en esta consulta se arma un string que traerá datos de usuarios, contratos y asistencias.
        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base, " +
                     // consulta funcion de agregacion total_retardos: Cuenta las marcas de entrada tarde para las deducciones
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'entrada' AND a.observacion LIKE '%retardo%' THEN 1 ELSE 0 END), 0) as total_retardos, " +
                     // funcion de agregacion total_extras: Cuenta exclusivamente las salidas que registran tiempo adicional de trabajo
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'salida' AND a.observacion LIKE '%extra%' THEN 1 ELSE 0 END), 0) as total_extras, " +
                     // dias_asistidos: Cuenta todas las marcas de salida como jornadas de asistencia completadas
                     "COALESCE(SUM(CASE WHEN a.tipo_evento = 'salida' THEN 1 ELSE 0 END), 0) as dias_asistidos_reales " +
                     "FROM usuario u " +
                     "INNER JOIN contrato con ON u.id = con.usuario_id " + //Une usuario con su contrato.
                     // left join Une asistencias filtradas por el periodo (? será reemplazado).
                     "LEFT JOIN asistencia a ON u.id = a.usuario_id AND DATE_FORMAT(a.fecha_hora, '%Y-%m') = ? " +
                     "GROUP BY u.id, u.documento_identidad, u.nombre, u.apellido, con.salario_base";

        // Try-with-resources: Garantiza cierre automático de conexión y statement.
        // Se obtiene conexión y se prepara la consulta.
        try (Connection con = Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, periodo); // Se reemplaza el parámetro ? en la consulta con el valor de periodo
            
            // Ejecuta la consulta (ps.executeQuery()) y guarda el resultado en un objeto llamado ResultSet (rs).
            try (ResultSet rs = ps.executeQuery()) { // ResultSet es como un enrutador que apunta a los resultados fila por fila.
                while (rs.next()) { // mueve ese cursor a la siguiente fila. Si hay datos, devuelve true; si se acaban los resultados, devuelve false y el bucle termina
                    Nomina n = new Nomina(); // Crea una nueva instancia de tu clase Nomina. Cada fila representa un objeto diferente.
                    n.setUsuarioId(rs.getInt("id"));
                    n.setDocumento(rs.getString("documento_identidad"));
                    n.setNombre(rs.getString("nombre"));
                    n.setApellido(rs.getString("apellido"));
                    n.setSalarioBasePeriodo(rs.getBigDecimal("salario_base"));
                    
                    // SINCRONIZADO CON NOMINA.JS Y SERVLET:
                    n.setTotalRetardos(rs.getInt("total_retardos")); // Viaja a la columna Deducciones
                    n.setTotalExtras(rs.getInt("total_extras"));     // Viaja a la columna Extras del front (extras reales)
                    
                    // SEPARACIÓN DE DÍAS OFICIAL: Seteamos los días en su propiedad desacoplada real
                    n.setDiasAsistidos(rs.getInt("dias_asistidos_reales")); // Viaja a la columna Días del front
                                     
                    lista.add(n); // en el momento que se obtienen los datos añade a la lista nomina 
                }
            }
        } catch (SQLException e) { 
            e.printStackTrace(); 
        }
        return lista; // Devuelve la lista completa de objetos Nomina con la información del periodo solicitado.
    }

    /**
     * Registra y congela el histórico financiero en tus tablas relacionales,
     * alimentando la tabla puente 'nomina_asistencia' para amarrar físicamente el pago a las marcas operativas (RF17 y RF19).
     */
    public boolean guardarNominaPeriodo(int usuarioId, java.math.BigDecimal salarioBase, double extras, java.math.BigDecimal neto, String periodoStr) {
        // Crea un nuevo periodo de nómina.
        String sqlPeriodo = "INSERT INTO periodo_nomina (fecha_inicio, fecha_fin, estado) VALUES (?, ?, 'cerrado')";
        // registra la nomina del usuario 
        String sqlNomina = "INSERT INTO nomina (usuario_id, periodo_id, salario_base_periodo, total_horas_extras, total_neto) VALUES (?, ?, ?, ?, ?)";
        // registra datos de pago del usuario 
        String sqlDetalle = "INSERT INTO detalle_nomina (nomina_id, concepto_id, valor, observacion) VALUES (?, ?, ?, ?)";
        // extrae las asistencias reales del mes que alimentaron la nómina
        String sqlBuscarAsistencias = "SELECT id FROM asistencia WHERE usuario_id = ? AND DATE_FORMAT(fecha_hora, '%Y-%m') = ?";
        // inserta a travez de la tabla puente datos relacionando nomnina con asistencia
        String sqlInsertPuente = "INSERT INTO nomina_asistencia (nomina_id, asistencia_id) VALUES (?, ?)";

        Connection con = null;
        try { // se abre conexion a bd y se comitea para guardar todo si no false
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); 

            int periodoId = 1;
            // Crear el registro del periodo el string del mes
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
                // RETURN_GENERATED_KEYS Sirve para recuperar el ID autogenerado después de un INSERT.
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

            //  ALIMENTAR TABLA PUENTE EN LA MEMORIA TRANSACCIONAL DE MYSQL
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

            con.commit(); // Consolidar todos los cambios de forma 100% en MySQL
            System.out.println(" ÉXITO Nómina #" + nominaId + " guardada y amarrada a sus marcas de asistencia.");
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



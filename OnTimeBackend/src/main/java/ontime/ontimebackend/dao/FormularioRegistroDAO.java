package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FormularioRegistroDAO {

    /**
     * Registra un empleado completo en la base de datos Ontime3BD. Inserta en
     * usuario, contrato, email_personal, telefono_personal y
     * contacto_emergencia bajo una única transacción
     * 
     */
    public boolean registrarNuevoEmpleado(Empleado emp, Contrato contrato) {

        String sqlUsuario = "INSERT INTO usuario (documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        // Sincronización explícita de las columnas fecha_inicio y fecha_fin en el query relacional
        String sqlContrato = "INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id, fecha_inicio, fecha_fin) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        String sqlEmail = "INSERT INTO email_personal (usuario_id, email) VALUES (?, ?)";
        String sqlTel = "INSERT INTO telefono_personal (usuario_id, telefono_celular) VALUES (?, ?)";
        String sqlContacto = "INSERT INTO contacto_emergencia (usuario_id, nombre, telefono, parentesco) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción atómica manual: Evita que guarde datos a medias si falla un canal

            int nuevoUsuarioId = 0;

            // 1. Insertar en la tabla central padre 'usuario'
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psU.setString(1, emp.getDocumento());
                psU.setString(2, emp.getNombre());
                psU.setString(3, emp.getApellido());
                psU.setString(4, emp.getDireccion() != null ? emp.getDireccion() : "N/A");
                psU.setString(5, "activo"); // Estado inicial operativo por defecto
                psU.setString(6, emp.getTipoSangre() != null ? emp.getTipoSangre().toLowerCase() : "o+");
                psU.setString(7, emp.getFoto() != null ? emp.getFoto() : "img/usuario-defecto.png");

                psU.executeUpdate();

                // Recuperamos el ID autogenerado asignado por el motor de indexación de MySQL
                try (ResultSet rs = psU.getGeneratedKeys()) {
                    if (rs.next()) {
                        nuevoUsuarioId = rs.getInt(1);
                    }
                }
            }

            // Validación de control de infraestructura por si falla la generación del ID primario
            if (nuevoUsuarioId == 0) {
                throw new SQLException("No se pudo obtener el ID autogenerado del usuario.");
            }

            // 2. Insertar en la tabla 'contrato' amarrado al ID del usuario y a las fechas reales de vigencia
            try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                psC.setInt(1, nuevoUsuarioId);
                psC.setString(2, contrato.getTipoContrato());
                psC.setString(3, contrato.getCargo());
                psC.setBigDecimal(4, contrato.getSalarioBase()); 
                psC.setInt(5, contrato.getJornadaId()); 
                
                // INYECCIÓN DE VIGENCIA TEMPORAL CORREGIDA
                psC.setString(6, contrato.getFechaInicio()); // Sobrescribe el default '2026-01-01'
                
                // BLINDAJE DE VENCIMIENTO CONTRACTUAL: Control lógico robusto basado en longitud de caracteres
                if (contrato.getFechaFin() != null && contrato.getFechaFin().trim().length() >= 4) {
                    psC.setString(7, contrato.getFechaFin().trim()); // Almacena la fecha real elegida en el modal flotante
                    System.out.println("  Sembrando fecha de vencimiento contractual: " + contrato.getFechaFin());
                } else {
                    psC.setNull(7, java.sql.Types.DATE); // Si el input viaja vacío, inyecta NULL legal (Contrato Indefinido)
                    System.out.println("  Contrato configurado como Término Indefinido (Inyectando NULL).");
                }
                
                psC.executeUpdate();
            }

            // 3. Insertar en la tabla relacional 'email_personal' (3FN)
            try (PreparedStatement psE = con.prepareStatement(sqlEmail)) {
                psE.setInt(1, nuevoUsuarioId);
                psE.setString(2, emp.getEmail());
                psE.executeUpdate();
            }

            // 4. Insertar en la tabla relacional 'telefono_personal' (3FN)
            try (PreparedStatement psT = con.prepareStatement(sqlTel)) {
                psT.setInt(1, nuevoUsuarioId);
                psT.setString(2, emp.getTelefonoCelular());
                psT.executeUpdate();
            }

            // 5. Insertar en la tabla relacional de auditoría 'contacto_emergencia' (3FN)
            try (PreparedStatement psCE = con.prepareStatement(sqlContacto)) {
                psCE.setInt(1, nuevoUsuarioId);
                psCE.setString(2, emp.getContactoEmergenciaNombre() != null ? emp.getContactoEmergenciaNombre() : "N/A");
                psCE.setString(3, emp.getContactoEmergenciaTelefono() != null ? emp.getContactoEmergenciaTelefono() : "N/A");
                psCE.setString(4, emp.getContactoEmergenciaParentesco() != null ? emp.getContactoEmergenciaParentesco() : "N/A");
                psCE.executeUpdate();
            }

            // Si las 5 inserciones en cadena relacional se ejecutaron sin errores, consolidamos en MySQL
            con.commit();
            System.out.println("-> ¡Empleado " + emp.getNombre() + " registrado con éxito en Ontime3BD!");
            return true;

        } catch (SQLException e) {
            System.err.println(" ERROR: Falla detectada en la transacción de registro. Ejecutando Rollback de seguridad...");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback(); // Deshace los inserts parciales en memoria para evitar corrupción de datos
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Cerramos de forma estricta el canal físico de red para optimizar el rendimiento (Garantiza el RNF08)
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato;
import java.sql.*;

public class FormularioRegistroDAO {

    /**
     * Registra un empleado completo en la base de datos Ontime3BD.
     * Inserta en usuario, contrato, email_personal, telefono_personal y contacto_emergencia
     * bajo una única transacción segura 
     */
    public boolean registrarNuevoEmpleado(Empleado emp, Contrato contrato) {
        // Sentencias SQL nativas alineadas al 100% con tu script Ontime3BD
        String sqlUsuario = "INSERT INTO usuario (documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlContrato = "INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id) VALUES (?, ?, ?, ?, ?)";
        String sqlEmail = "INSERT INTO email_personal (usuario_id, email) VALUES (?, ?)";
        String sqlTel = "INSERT INTO telefono_personal (usuario_id, telefono_celular) VALUES (?, ?)";
        String sqlContacto = "INSERT INTO contacto_emergencia (usuario_id, nombre, telefono, parentesco) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Transacción escolar limpia: Evita que guarde datos a medias

            int nuevoUsuarioId = 0;

            // 1. Insertar en la tabla central 'usuario'
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psU.setString(1, emp.getDocumento());
                psU.setString(2, emp.getNombre());
                psU.setString(3, emp.getApellido());
                psU.setString(4, emp.getDireccion());
                psU.setString(5, "activo"); // Estado inicial plano por defecto
                psU.setString(6, emp.getTipoSangre() != null ? emp.getTipoSangre().toLowerCase() : "o+");
                psU.setString(7, emp.getFoto() != null ? emp.getFoto() : "img/usuario-defecto.png");
                
                psU.executeUpdate();

                // Recuperamos el ID autogenerado por MySQL
                try (ResultSet rs = psU.getGeneratedKeys()) {
                    if (rs.next()) {
                        nuevoUsuarioId = rs.getInt(1);
                    }
                }
            }

            // Validación de seguridad por si falla la generación del ID primario
            if (nuevoUsuarioId == 0) {
                throw new SQLException("No se pudo obtener el ID autogenerado del usuario.");
            }

            // 2. Insertar en la tabla 'contrato' usando el ID capturado
            try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                psC.setInt(1, nuevoUsuarioId);
                psC.setString(2, contrato.getTipoContrato());
                psC.setString(3, contrato.getCargo());
                psC.setBigDecimal(4, contrato.getSalarioBase()); // Compatible con BigDecimal de tu modelo
                psC.setInt(5, contrato.getJornadaId()); // ID de la jornada laboral foránea
                psC.executeUpdate();
            }

            // 3. Insertar en la tabla 'email_personal'
            try (PreparedStatement psE = con.prepareStatement(sqlEmail)) {
                psE.setInt(1, nuevoUsuarioId);
                psE.setString(2, emp.getEmail());
                psE.executeUpdate();
            }

            // 4. Insertar en la tabla 'telefono_personal'
            try (PreparedStatement psT = con.prepareStatement(sqlTel)) {
                psT.setInt(1, nuevoUsuarioId);
                psT.setString(2, emp.getTelefonoCelular());
                psT.executeUpdate();
            }

            // 5. Insertar en la tabla 'contacto_emergencia'
            try (PreparedStatement psCE = con.prepareStatement(sqlContacto)) {
                psCE.setInt(1, nuevoUsuarioId);
                psCE.setString(2, emp.getContactoEmergenciaNombre() != null ? emp.getContactoEmergenciaNombre() : "N/A");
                psCE.setString(3, emp.getContactoEmergenciaTelefono() != null ? emp.getContactoEmergenciaTelefono() : "N/A");
                psCE.setString(4, emp.getContactoEmergenciaParentesco() != null ? emp.getContactoEmergenciaParentesco() : "N/A");
                psCE.executeUpdate();
            }

            // Si todas las inserciones se ejecutaron sin errores, consolidamos de forma permanente en MySQL
            con.commit(); 
            System.out.println("-> ¡Empleado " + emp.getNombre() + " registrado con éxito en Ontime3BD!");
            return true;

        } catch (SQLException e) {
            // Si cualquiera de los 5 queries falló (ej: cédula duplicada o jornada inválida), revertimos todo
            System.err.println(" Error en la transacción de registro. Aplicando Rollback...");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback(); // Limpia la memoria de MySQL para no dejar datos huérfanos
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            // Cerramos la conexión de forma segura para cumplir el requerimiento de rendimiento RNF08
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

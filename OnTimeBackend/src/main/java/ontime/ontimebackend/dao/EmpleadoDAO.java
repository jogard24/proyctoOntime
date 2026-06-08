package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Empleado;
import ontime.ontimebackend.modelo.Contrato;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmpleadoDAO {

    // ... (Tu método listarTodos sigue igual) ...
    // Método existente para listar empleados
    public List<Empleado> listarTodos() {

        List<Empleado> lista = new ArrayList<>();

        String sql = "SELECT u.id, u.documento_identidad, u.nombre, u.estado, u.fotoPerfil_url, "
                + "t.telefono_celular, e.email, ct.nombre AS contacto_nombre, ct.telefono AS contacto_telefono, "
                + "con.cargo "
                + "FROM usuario u "
                + "LEFT JOIN telefono_personal t ON u.id = t.usuario_id "
                + "LEFT JOIN email_personal e ON u.id = e.usuario_id "
                + "LEFT JOIN contacto_emergencia ct ON u.id = ct.usuario_id "
                + "LEFT JOIN contrato con ON u.id = con.usuario_id";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Empleado emp = new Empleado();
                emp.setId(rs.getString("id"));
                emp.setNombre(rs.getString("nombre"));
                emp.setDocumento(rs.getString("documento_identidad"));
                String cargo = rs.getString("cargo");
                emp.setCargo(cargo != null ? cargo : "Sin asignar");
                emp.setEstado(rs.getString("estado"));
                emp.setFoto(rs.getString("fotoPerfil_url"));
                emp.setTelefonoCelular(rs.getString("telefono_celular") != null ? rs.getString("telefono_celular") : "N/A");
                emp.setEmail(rs.getString("email") != null ? rs.getString("email") : "N/A");
                emp.setContactoEmergenciaNombre(rs.getString("contacto_nombre") != null ? rs.getString("contacto_nombre") : "N/A");
                emp.setContactoEmergenciaTelefono(rs.getString("contacto_telefono") != null ? rs.getString("contacto_telefono") : "N/A");
                lista.add(emp);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;

    }

    public boolean actualizarEmpleado(Empleado emp) {

        String sql = "UPDATE usuario SET nombre = ?, estado = ? WHERE id = ?";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, emp.getNombre());
            ps.setString(2, emp.getEstado());
            ps.setString(3, emp.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {

            e.printStackTrace();

            return false;

        }

    }

    /**
     * Registro usando Empleado.java como fuente principal de datos personales
     */
    /**
     * Registro usando Empleado.java como fuente principal de datos personales
     */
    public boolean registrarEmpleadoCompleto(Empleado emp, Contrato contrato) {
        // SQLs: Corregidos para cumplir estrictamente con el diseño de tus tablas de MySQL
        String sqlUsuario = "INSERT INTO usuario (documento_identidad, nombre, apellido, direccion, estado, tipo_sangre, fotoPerfil_url) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String sqlContrato = "INSERT INTO contrato (usuario_id, tipo_contrato, cargo, salario_base, jornada_id) VALUES (?, ?, ?, ?, ?)";
        String sqlEmail = "INSERT INTO email_personal (usuario_id, email) VALUES (?, ?)";
        String sqlTel = "INSERT INTO telefono_personal (usuario_id, telefono_celular) VALUES (?, ?)";
        String sqlContacto = "INSERT INTO contacto_emergencia (usuario_id, nombre, telefono, parentesco) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = Conexion.obtenerConexion();
            con.setAutoCommit(false); // Iniciamos transacción modular

            int nuevoId = 0;

            // 1. Insertar en tabla 'usuario' (Respetando restricciones NOT NULL y ENUMs)
            try (PreparedStatement psU = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                psU.setString(1, emp.getDocumento());
                psU.setString(2, emp.getNombre());
                psU.setString(3, emp.getApellido());
                psU.setString(4, emp.getDireccion());

                // Forzamos minúsculas para que coincida exactamente con los ENUM de la base de datos
                psU.setString(5, emp.getEstado() != null ? emp.getEstado().toLowerCase() : "activo");
                psU.setString(6, emp.getTipoSangre() != null ? emp.getTipoSangre().toLowerCase() : "o+");

                // Evitamos el fallo de fotoPerfil_url NOT NULL pasándole la ruta de la foto o una por defecto
                psU.setString(7, emp.getFoto() != null ? emp.getFoto() : "img/usuario-defecto.png");

                psU.executeUpdate();

                try (ResultSet rs = psU.getGeneratedKeys()) {
                    if (rs.next()) {
                        nuevoId = rs.getInt(1);
                    }
                }
            }

            if (nuevoId == 0) {
                throw new SQLException("No se pudo obtener el ID generado para el usuario.");
            }

            // 2. Insertar en 'contrato' (Se cambió a setDouble o se castea a BigDecimal según tu modelo de datos)
            try (PreparedStatement psC = con.prepareStatement(sqlContrato)) {
                psC.setInt(1, nuevoId);
                psC.setString(2, contrato.getTipoContrato());
                psC.setString(3, contrato.getCargo());

                // Si getSalarioBase() en tu clase Contrato retorna double:
                psC.setBigDecimal(4, contrato.getSalarioBase());
                // NOTA: Si tu modelo requiere estrictamente BigDecimal descomenta la de abajo y borra la de arriba:
                // psC.setBigDecimal(4, java.math.BigDecimal.valueOf(contrato.getSalarioBase()));

                // Validamos que el ID de la jornada exista en tu tabla 'jornadaLaboral' (Debe ser 1, 2, etc.)
                psC.setInt(5, contrato.getJornadaId());
                psC.executeUpdate();
            }

            // 3. Insertar en 'email_personal' (La tabla que sí existe en tu BD)
            try (PreparedStatement psE = con.prepareStatement(sqlEmail)) {
                psE.setInt(1, nuevoId);
                psE.setString(2, emp.getEmail());
                psE.executeUpdate();
            }

            // 4. Insertar en 'telefono_personal'
            try (PreparedStatement psT = con.prepareStatement(sqlTel)) {
                psT.setInt(1, nuevoId);
                psT.setString(2, emp.getTelefonoCelular());
                psT.executeUpdate();
            }

            // 5. Insertar en 'contacto_emergencia' (¡Te faltaba esta inserción crucial!)
            try (PreparedStatement psCE = con.prepareStatement(sqlContacto)) {
                psCE.setInt(1, nuevoId);
                // Si no tienes estos getters en tu clase Empleado, puedes agregarlos o mapear las variables
                psCE.setString(2, emp.getContactoEmergenciaNombre() != null ? emp.getContactoEmergenciaNombre() : "N/A");
                psCE.setString(3, emp.getContactoEmergenciaTelefono() != null ? emp.getContactoEmergenciaTelefono() : "N/A");
                psCE.setString(4, emp.getContactoEmergenciaParentesco() != null ? emp.getContactoEmergenciaParentesco() : "N/A");
                psCE.executeUpdate();
            }

            // Si llegamos aquí sin errores, consolidamos de forma permanente en MySQL
            con.commit();
            return true;

        } catch (SQLException e) {
            // Si cualquier inserción falló, revertimos todo para evitar datos corruptos
            System.err.println("¡Error en la transacción del DAO! Aplicando Rollback...");
            e.printStackTrace();
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
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

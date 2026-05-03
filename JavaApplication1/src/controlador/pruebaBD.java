package controlador;

import conexion.conexionBD;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class pruebaBD {

    public static void main(String[] args) {
        conexionBD conexion = new conexionBD();

        try (Connection conn = conexion.getConnection()) {

            // 1️⃣ INSERTAR un empleado
            String sql = "INSERT INTO datosPersonales (nombre_completo, documento_identidad, salario_base) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "Carlos Pérez");
            ps.setString(2, "123456779");
            ps.setDouble(3, 1500000.0);
            ps.executeUpdate();
            System.out.println("Empleado insertado correctamente.");

            // 2️⃣ CONSULTAR empleados
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM datosPersonales");

            while (rs.next()) {
//                int id = rs.getInt("id");
                String documento = rs.getString("documento_identidad");
                String nombre = rs.getString("nombre_completo");
                String celular = rs.getString("celular");
                String direccion = rs.getString("direccion");
                String tipoSangre = rs.getString("tipo_sangre");
                String contactoEmergencia = rs.getString("contacto_emergencia");
                String contactoTelefono = rs.getString("contacto_telefono");
                String fechaRegistro = rs.getString("fecha_registro");
//                int jornadaId = rs.getInt("jornada_id");
                double salarioBase = rs.getDouble("salario_base");
                String tipoContrato = rs.getString("tipoDcontrato");
                String cargoEmpleado = rs.getString("cargoempleado");

                System.out.println(
//                        "ID: " + id
                         " | Documento: " + documento +
                         " | Nombre: " + nombre +
                         " | Celular: " + celular +
                         " | Dirección: " + direccion +
                         " | Tipo sangre: " + tipoSangre +
                         " | Contacto emergencia: " + contactoEmergencia +
                         " | Teléfono contacto: " + contactoTelefono +
                         " | Fecha registro: " + fechaRegistro +
//                         " | Jornada ID: " + jornadaId +
                         " | Salario base: " + salarioBase +
                         " | Tipo contrato: " + tipoContrato +
                         " | Cargo: " + cargoEmpleado 
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}

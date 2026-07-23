package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Horario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HorarioDAO {

    public List<Horario> listarHorarios() {
        // se crea una lista 
        List<Horario> lista = new ArrayList<>();
        // se consulta a la base de datos la tabla jornadalaboral
        String sql = "SELECT id, nombrejornada, hora_entrada, hora_salida FROM jornadaLaboral";
        // se abre la conexion a la base de datos y se prepara la consulta 
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql); 
             ResultSet rs = ps.executeQuery()) { // Ejecuta la consulta de lectura y abre el cursor relacional
            
            // Bucle iterativo que desplaza el cursor fila por fila mientras existan registros en el ResultSet.            
            while (rs.next()) {
                // se instancia horario por cada regisgtro de la bd 
                Horario horario = new Horario();
                // Encapsulamiento: Extrae los valores de las celdas de MySQL y los inyecta en el objeto vía Setters                
                horario.setId(rs.getInt("id")); 
                horario.setNombrejornada(rs.getString("nombrejornada"));
                horario.setHoraEntrada(rs.getString("hora_entrada"));
                horario.setHoraSalida(rs.getString("hora_salida"));
                lista.add(horario); // Agrega el objeto completamente poblado a la lista
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista; // Despacha la colección llena hacia el HorarioServlet
    }

    public boolean crearHorario(Horario horario) {
        // 🚀 CORRECCIÓN DEFINITIVA: Se remueve la columna 'id' de la inserción.
        // MySQL asumirá de forma transparente el AUTO_INCREMENT asignando la llave primaria sola.
        String sql = "INSERT INTO jornadaLaboral (nombrejornada, hora_entrada, hora_salida) VALUES (?, ?, ?)";
        
        // se abre la base de datos y se prepara
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            // Reordenamiento indexado de los marcadores '?'
            ps.setString(1, horario.getNombrejornada()); // Caja de texto libre (Ej: Turno Tarde, Dominical, Rotativo)
            ps.setString(2, horario.getHoraEntrada());
            ps.setString(3, horario.getHoraSalida());
            
            return ps.executeUpdate() > 0; // Retorna true si las filas afectadas en MySQL son mayores a cero
            
        } catch (SQLException e) {
            System.err.println(" ERROR AL INJECTAR HORARIO LIBRE EN MYSQL: " + e.getMessage());
            e.printStackTrace();
            return false; // retorna si el sistema rechaza el horario programado 
        }
    }

    // se modifican horarios metodo boolean
    public boolean actualizarHorario(Horario horario) {
        // sentencia de modificacion , se actualiza la base de datos 
        String sql = "UPDATE jornadaLaboral SET nombrejornada = ?, hora_entrada = ?, hora_salida = ? WHERE id = ?";
        // se abre la base de datos y se prepara 
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, horario.getNombrejornada());
            ps.setString(2, horario.getHoraEntrada());
            ps.setString(3, horario.getHoraSalida());
            ps.setInt(4, horario.getId()); // Filtro del ID que direcciona la actualización en caliente
            
            return ps.executeUpdate() > 0; // retorna true si se ha modificado 
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // eliminar horario , metodo que me reotrna true o false 
    public boolean eliminarHorario(int id) {
        // Sentencia de moficacion sujeta a restricciones de Integridad Referencial por Llaves Foráneas
        String sql = "DELETE FROM jornadaLaboral WHERE id = ?";
        try (Connection con = Conexion.obtenerConexion(); 
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id); // Amarra el ID del turno que se desea destruir
            return ps.executeUpdate() > 0; // retorna :Ejecuta el borrado físico directo en MySQL
            
        } catch (SQLException e) {
            // CAPTURA DE INTEGRIDAD: Si un contrato o asistencia ya usa este ID, MySQL arroja un código de error y entra aquí
            System.err.println(" VIOLACIÓN DE INTEGRIDAD REFERENCIAL (ELIMINAR HORARIO): " + e.getMessage());
            e.printStackTrace();
            return false; // Bloquea la eliminación y retorna falso para salvar la consistencia de los históricos
        }
    }
}



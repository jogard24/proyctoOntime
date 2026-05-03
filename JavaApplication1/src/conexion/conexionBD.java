package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class conexionBD {

    private Connection con;

    String url = "jdbc:mysql://localhost:3306/OntimeBD";
    String user = "root";
    String pass = "#Aprendiz2024";
    String driver = "com.mysql.cj.jdbc.Driver";

    public Connection getConnection() {
        try {
            Class.forName(driver); // Cargamos el driver
            con = DriverManager.getConnection(url, user, pass); // Intento de conexión
            System.out.println("Conexión exitosa");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Error de conexión: " + e);
        }
        return con;
    }
}

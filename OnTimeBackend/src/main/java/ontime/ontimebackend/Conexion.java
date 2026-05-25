package ontime.ontimebackend;

import java.sql.Connection;
import java.sql.DriverManager;

public class Conexion {
    // Definimos los datos de conexión de manera clara y ordenada
    private static final String URL = "jdbc:mysql://localhost:3306/ontime2BD?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "94052413960"; // Escribe tu clave aquí

    public static Connection obtenerConexion() {
        Connection con = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("-> ¡OnTime BD Conectada con éxito!");
        } catch (Exception e) {
            System.out.println("-> Error al conectar MySQL: " + e.getMessage());
        }
        return con;
    }
}

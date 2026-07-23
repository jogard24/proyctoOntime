package ontime.ontimebackend.dao;

import ontime.ontimebackend.conexion.Conexion;
import ontime.ontimebackend.modelo.Asistencia;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import ontime.ontimebackend.modelo.EmpleadoPlanilla;

public class AsistenciaDAO {

    /**
     * Recupera todo el historial de asistencias uniendo las tablas para el
     * reporte analítico.
     */
    //Declaramos que el método devuelve una "lista" de objetos de tipo Asistencia
    //No recibe parámetros, simplemente consulta todos los registros de asistencia.
    public List<Asistencia> listarAsistencia() {
//Inicializamos una lista vacía. Aquí es donde iremos guardando 
//cada registro que extraigamos de la base de datos.        
        List<Asistencia> lista = new ArrayList<>();
        //  Selecciona los campos principales de la tabla asistencia.
        String sql = "SELECT DISTINCT  a.id, u.documento_identidad, u.nombre, u.apellido, a.fecha_hora, a.tipo_evento, a.observacion, j.nombrejornada "
                + "FROM asistencia a "//Une con la tabla usuario para obtener datos personales.
                + "INNER JOIN usuario u ON a.usuario_id = u.id "
                //Usa LEFT JOIN con jornadaLaboral para traer el nombre de la jornada
                + "LEFT JOIN jornadaLaboral j ON a.jornada_id = j.id "
                + "ORDER BY a.fecha_hora DESC";//Ordena por fecha_hora descendente
        //no hay limite en este metodo y devulve todos los registros
//El try-with-resources asegura que todo se cierre automáticamente.
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {//Recorre cada fila del resultado.
                Asistencia asis = new Asistencia();//Crea un objeto Asistencia y asigna los valores de las columnas.
                asis.setId(rs.getInt("id"));
                asis.setDocumentoIdentidad(rs.getString("documento_identidad"));
                //Aquí concatenas directamente nombre + apellido sin lógica adicional para evitar duplicados.
                asis.setNombreEmpleado(rs.getString("nombre") + " " + rs.getString("apellido"));

                //Convierte la fecha/hora a String.
                asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setObservacion(rs.getString("observacion"));
                asis.setNombreJornada(rs.getString("nombrejornada")); // Sincronizado con asistenciaTabla.js
                lista.add(asis);//Se completan los demás atributos del objeto Asistencia.
                //Se añade a la lista final.
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;//Devuelve la lista con todos los registros de asistencia, ordenados por fecha.
    }

    // Valida si un usuario existe en el sistema basándose en su documento (RF09)
    public boolean existeEmpleado(String docIdentidad) {
        String sql = "SELECT id FROM usuario WHERE documento_identidad = ? AND estado = 'activo'";
        //try-with-resources asegura que la conexión y el statement se cierren automáticamente.
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Métodos de consulta rápida requeridos por tu Pinpad
    public int obtenerIdPorDocumento(String docIdentidad) {
        //Se define la consulta para obtener el campo id de la tabla usuario.
        String sql = "SELECT id FROM usuario WHERE documento_identidad = ?";//El ? es un placeholder que luego se reemplaza con el valor real
        //Conexion.obtenerConexion(): abre la conexión a la base de datos.
        //prepara la consulta SQL para ejecutarla de forma segura (evita inyección SQL).
        //El try (...) con recursos asegura que la conexión y el statement se cierren automáticamente al terminar.
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, docIdentidad);//Reemplaza el primer ? de la consulta con el valor de docIdentidad.
            try (ResultSet rs = ps.executeQuery()) {//ejecuta la consulta y devuelve los resultados en un ResultSet.
                if (rs.next()) {// avanza a la primera fila (si existe).
                    return rs.getInt("id");// obtiene el valor de la columna id. Si encuentra un usuario, lo devuelve inmediatamente.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //Este método busca un usuario por su documento de identidad y devuelve su id.
        //Si no lo encuentra, devuelve -1.
        return -1;
    }

    public String obtenerNombrePorDocumento(String documento) {
        //Busca las columnas nombre y apellido en la tabla usuario. 
        //Filtra por el documento de identidad recibido.
        String sql = "SELECT nombre, apellido FROM usuario WHERE documento_identidad = ?";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //Evalúa si el casillero de la base de datos tiene un texto real y no está vacío con un valor nulo 
                    // Si la celda sí contiene un nombre, ejecuta rs.getString("nombre").trim(). El método .trim() es una función 
                    //de limpieza: borra de forma automática cualquier espacio en blanco oculto que el usuario haya metido por error 
                    //al principio o al final del texto al registrarse
                    String nombreBd = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
                    String apellidoBd = rs.getString("apellido") != null ? rs.getString("apellido").trim() : "";

                    // Si la columna nombre ya contiene el apellido físicamente,
                    // retornamos solo el campo nombre para que no se duplique 
                    if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase()) || apellidoBd.isEmpty()) {
                        return nombreBd;
                    } else {
                        return nombreBd + " " + apellidoBd;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Usuario";//Si no se encuentra ningún registro o ocurre un error, devuelve "Usuario" como valor por defecto.
    }

    /**
     * Determina de forma automática el próximo evento del empleado (RF10). Si
     * su última marca fue 'entrada', le toca marcar 'salida'. De lo contrario,
     * toca 'entrada'.
     */
    //recibe el identificador del usuario que queremos consultar.
    public String obtenerUltimoTipoEvento(int usuarioId) {
        //Busca el campo tipo_evento en la tabla asistencia.
        //Filtra por el usuario_id recibido.
        //Ordena los registros por fecha_hora en orden descendente (del más reciente al más antiguo).
        //LIMIT 1 asegura que solo se devuelva el último evento.
        String sql = "SELECT tipo_evento FROM asistencia WHERE usuario_id = ? ORDER BY fecha_hora DESC LIMIT 1";
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);//reemplaza el primer ? con el valor real del usuario.
            try (ResultSet rs = ps.executeQuery()) {//Ejecuta la consulta y obtiene los resultados en un ResultSet.
                if (rs.next()) {//rs.next() avanza a la primera fila (si existe).
                    return rs.getString("tipo_evento");//retornamos el valor de la columna tipo_evento.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "salida"; // Si nunca ha marcado, retorna 'salida' para que la lógica lo asigne como una 'entrada'
    }

    /**
     * Inserta la marca física inyectando de manera estricta la hora del
     * servidor MySQL con NOW() (RF10).
     */
    //devuelve true si la inserción fue exitosa, false si falló.
    public boolean registrarAsistencia(int usuarioId, String tipoEvento, String observacion, int jornadaId) {
        // NOW() asigna automáticamente la fecha y hora actual del sistema.
        //sentencia de modificacion : Inserta un nuevo registro en la tabla asistencia.
        String sql = "INSERT INTO asistencia (usuario_id, tipo_evento, observacion, jornada_id, fecha_hora) VALUES (?, ?, ?, ?, NOW())";
        //try-with-resources asegura que la conexión y el statement se cierren automáticamente.
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            //Cada set reemplaza un ? en la consulta:
            ps.setInt(1, usuarioId);
            ps.setString(2, tipoEvento);
            ps.setString(3, observacion);
            ps.setInt(4, jornadaId);
            //executeUpdate() ejecuta la inserción y devuelve el número de filas afectadas.
            //Si es mayor que 0, significa que se insertó correctamente → retorna true.
            return ps.executeUpdate() > 0;

            //Si no, retorna false.
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Trae la jornada horaria contractual del empleado para el cálculo
     * automático de puntualidad.
     */
    //horaEntradaOut: un StringBuilder que se usa como “salida adicional” para devolver la hora de entrada.
    public int obtenerJornadaIdYHoraEntrada(int usuarioId, StringBuilder horaEntradaOut) {
        //consulta :Se selecciona el id y la hora de entrada de la tabla jornadaLaboral.
        String sql = "SELECT j.id, j.hora_entrada FROM jornadaLaboral j "
                //Se hace un INNER JOIN con la tabla contrato para obtener la jornada asociada al usuario.
                + "INNER JOIN contrato c ON j.id = c.jornada_id "
                //Se filtra por usuario_id.
                + "WHERE c.usuario_id = ?";
        //try :Abre la conexión a la base de datos.
        //Prepara la consulta SQL.
        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, usuarioId);
            try (ResultSet rs = ps.executeQuery()) {//avanza a la primera fila (si existe).
                if (rs.next()) {
                    //obtiene la hora de entrada y la añade al StringBuilder pasado como parámetro
                    horaEntradaOut.append(rs.getTime("hora_entrada").toString());
                    return rs.getInt("id");//Devuelve el id de la jornada laboral.
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //si no se encuentra ninguna jornada, se asigna por defecto
        horaEntradaOut.append("08:00:00");
        return 1; // Retorna la jornada 1 por defecto
    }

    /**
     * Método estrella del Feed de Novedades del Home que mapea las asistencias
     * y datos del usuario (Alimenta tu homeInicio.js).
     *
     */
    //devuelve una lista de objetos Asistencia
    public List<Asistencia> listarNovedadesRecientes() {
        List<Asistencia> lista = new ArrayList<>();
        //consulta
        String sql = "SELECT a.id, u.documento_identidad, u.nombre, u.apellido, a.tipo_evento, a.fecha_hora, a.observacion, j.nombrejornada "
                //Selecciona los 10 registros más recientes de la tabla asistencia.
                //Une con la tabla usuario para obtener datos personales.
                + "FROM asistencia a "
                + "INNER JOIN usuario u ON a.usuario_id = u.id "
                //Usa LEFT JOIN con jornadaLaboral para traer el nombre de la jornada.
                + "LEFT JOIN jornadaLaboral j ON a.jornada_id = j.id "
                //Ordena por fecha_hora descendente
                + "ORDER BY a.fecha_hora DESC LIMIT 10";

        try (Connection con = Conexion.obtenerConexion(); PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            //Recorre cada fila del resultado.
            //Crea un objeto Asistencia y asigna los valores de las columnas.
            while (rs.next()) {
                Asistencia asis = new Asistencia();
                asis.setId(rs.getInt("id"));
                asis.setDocumentoIdentidad(rs.getString("documento_identidad"));

                //Se limpian espacios y se evita null.
                String nombreBd = rs.getString("nombre") != null ? rs.getString("nombre").trim() : "";
                String apellidoBd = rs.getString("apellido") != null ? rs.getString("apellido").trim() : "";

                // Si la columna 'nombre' ya contiene el apellido (o si el apellido viene vacío), 
                // inyectamos solo el nombre para duplicado.
                if (nombreBd.toLowerCase().contains(apellidoBd.toLowerCase()) || apellidoBd.isEmpty()) {
                    asis.setNombreEmpleado(nombreBd);
                } else {//Si no, se concatena nombre + apellido.
                    asis.setNombreEmpleado(nombreBd + " " + apellidoBd);
                }

                //Se completan los demás atributos del objeto Asistencia.
                //Se añade a la lista final.
                asis.setTipoEvento(rs.getString("tipo_evento"));
                asis.setFechaHora(rs.getTimestamp("fecha_hora").toString());
                asis.setObservacion(rs.getString("observacion"));
                asis.setNombreJornada(rs.getString("nombrejornada"));
                lista.add(asis);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;//Devuelve la lista con los 10 registros más recientes de asistencia.
    }


 
    public List<EmpleadoPlanilla> obtenerMallaAsistencia(String periodoStr) {
        List<EmpleadoPlanilla> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        
        sql.append("SELECT u.id, u.nombre, u.apellido, con.cargo ");
        
        // EL EMBLEMA MATRICIAL: Evaluamos de forma directa las marcas usando funciones nativas
        for (int dia = 1; dia <= 31; dia++) {
            sql.append(", CASE ")
               // Regla 1: Si el empleado marcó salida en el Pinpad en este día, registra Asistencia (1)
               .append("    WHEN MAX(CASE WHEN DAY(a.fecha_hora) = ").append(dia).append(" AND a.tipo_evento = 'salida' THEN 1 ELSE 0 END) = 1 THEN 1 ")
               // Regla 2: Si el día cae dentro del rango de un permiso aprobado (Evaluado dinámicamente en MySQL)
               .append("    WHEN MAX(CASE WHEN p.id IS NOT NULL AND ").append(dia).append(" BETWEEN DAY(p.fecha_inicio) AND DAY(p.fecha_fin) THEN 1 ELSE 0 END) = 1 THEN 2 ")
               .append("    ELSE 0 ")
               .append("END AS dia_").append(dia);
        }
        
        sql.append(" FROM usuario u ")
           .append(" INNER JOIN contrato con ON u.id = con.usuario_id ")
           // LEFT JOIN 1: Trae los marcajes del Pinpad que coincidan con el mes consultado
           .append(" LEFT JOIN asistencia a ON u.id = a.usuario_id AND DATE_FORMAT(a.fecha_hora, '%Y-%m') = ? ")
           // LEFT JOIN 2: Trae los permisos aprobados que coincidan con el mes consultado
           .append(" LEFT JOIN permiso_laboral p ON u.id = p.usuario_id AND p.estado = 'aprobado' AND DATE_FORMAT(p.fecha_inicio, '%Y-%m') = ? ")
           .append(" GROUP BY u.id, u.nombre, u.apellido, con.cargo ")
           .append(" ORDER BY u.nombre ASC");

        try (Connection con = ontime.ontimebackend.conexion.Conexion.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            
            // 🛡️ SINCRO DE ALTA INGENIERÍA: 
            // Eliminamos las 32 iteraciones confusas y pasamos solo los 2 parámetros estrictos que el motor requiere
            ps.setString(1, periodoStr); // Alimenta el ? de las asistencias
            ps.setString(2, periodoStr); // Alimenta el ? de los permisos
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    EmpleadoPlanilla emp = new EmpleadoPlanilla();
                    emp.setId(rs.getInt("id"));
                    emp.setNombre(rs.getString("nombre"));
                    emp.setApellido(rs.getString("apellido"));
                    emp.setCargo(rs.getString("cargo"));
                    
                    // Extraemos los estados (0, 1 o 2) de cada columna y los inyectamos en la RAM
                    for (int dia = 1; dia <= 31; dia++) {
                        int valorDia = rs.getInt("dia_" + dia);
                        emp.setDiaValor(dia, valorDia);
                    }
                    lista.add(emp);
                }
            }
        } catch (SQLException e) {
            System.err.println(" EXCEPCIÓN CRÍTICA EN EL PIVOTE UNIFICADO DE ASISTENCIAS: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

}
package ontime.ontimebackend.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import ontime.ontimebackend.dao.ContratoDAO; //  SE INCLUYE EL DAO DE FORMA DESACOPLADA
import ontime.ontimebackend.modelo.Contrato; //  USA TU MODELO ORIGINAL REUTILIZADO

/**
 * Capa de Control - Módulo de Gestión de Contratos y Liquidaciones Orquesta la
 * extracción de vigencias contractuales y procesa las prórrogas/renovaciones.
 */
@WebServlet(name = "ContratoServlet", urlPatterns = {"/ContratoServlet"})
public class ContratoServlet extends HttpServlet {

    // Instanciamos el DAO global inmutable para delegar la persistencia relacional
    private final ContratoDAO contratoDAO = new ContratoDAO();

    /**
     * Sanitizador de cadenas para evitar que caracteres especiales rompan el
     * formato JSON
     */
    private String escaparJson(String valor) {
        return (valor == null) ? "" : valor.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String accion = request.getParameter("accion");

        if ("listarTodos".equals(accion)) {
            // DELEGACIÓN FORMAL MVC: El DAO interroga a MySQL y nos retorna los JavaBeans cargados
            List<Contrato> listaContratos = contratoDAO.listarContratosTodos();

            StringBuilder json = new StringBuilder("[");
            for (int i = 0; i < listaContratos.size(); i++) {
                Contrato c = listaContratos.get(i);

                json.append("{");
                json.append("\"contratoId\":").append(c.getId()).append(",");
                json.append("\"empleadoNombre\":\"").append(escaparJson(c.getEmpleadoNombre())).append("\",");
                json.append("\"cargo\":\"").append(escaparJson(c.getCargo())).append("\",");
                json.append("\"salario\":").append(c.getSalarioBase()).append(","); // Sincronizado con BigDecimal
                json.append("\"fechaInicio\":\"").append(escaparJson(c.getFechaInicio())).append("\",");
                json.append("\"fechaFin\":\"").append(escaparJson(c.getFechaFin())).append("\",");
                json.append("\"periodo_pago\":\"").append(escaparJson(c.getPeriodoPago())).append("\",");
                // LÍNEA INDISPENSABLE: Captura el estado 'activo/inactivo' transportado en el modelo (tipoContrato)
                json.append("\"estado\":\"").append(escaparJson(c.getTipoContrato())).append("\"");
                json.append("}");

                if (i < listaContratos.size() - 1) {
                    json.append(","); // Inyecta la coma separadora de objetos
                }
            }
            json.append("]");

            out.print(json.toString()); // Envía el flujo de texto JSON por la red local hacia contratos.js
            out.flush();
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String accion = request.getParameter("accion");

        // BLOQUE A: ACCIÓN RENOVAR
        if ("renovar".equals(accion)) {
            String contratoIdStr = request.getParameter("contratoId");
            String mesesStr = request.getParameter("mesesProrroga");

            if (contratoIdStr == null || mesesStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"Parámetros incompletos.\"}");
                out.flush();
                return;
            }

            //  BLOQUE TRY GENERAL DE LA ACCIÓN
            try {
                int idRecibido = Integer.parseInt(contratoIdStr.trim());
                int meses = Integer.parseInt(mesesStr.trim());
                int contratoIdReal = idRecibido;

                System.out.println(" INFO INTERNA SERVLET - ID RECIBIDO DESDE FRONTEND: " + idRecibido);

                java.util.List<ontime.ontimebackend.modelo.Contrato> todos = contratoDAO.listarContratosTodos();
                for (ontime.ontimebackend.modelo.Contrato conBase : todos) {
                    if (conBase.getUsuarioId() == idRecibido || conBase.getId() == idRecibido) {
                        contratoIdReal = conBase.getId();
                        break;
                    }
                }
                System.out.println(" INFO INTERNA SERVLET - ID DE CONTRATO FINAL A ACTUALIZAR EN MYSQL: " + contratoIdReal);

                String sqlProrroga = "UPDATE contrato SET fecha_fin = DATE_ADD(fecha_fin, INTERVAL ? MONTH) WHERE id = ?";
                String sqlActivarContrato = "UPDATE contrato SET estado = 'activo' WHERE id = ?";
                String sqlActivarUsuario = "UPDATE usuario SET estado = 'activo' WHERE id = (SELECT usuario_id FROM contrato WHERE id = ?)";

                try (Connection con = ontime.ontimebackend.conexion.Conexion.obtenerConexion()) {
                    con.setAutoCommit(false); // Freno de mano de seguridad activo

                    try (PreparedStatement psP = con.prepareStatement(sqlProrroga); PreparedStatement psC = con.prepareStatement(sqlActivarContrato); PreparedStatement psU = con.prepareStatement(sqlActivarUsuario)) {

                        // PLANTILLA 1: Tiene 2 parámetros secuenciales (1 y 2)
                        psP.setInt(1, meses);
                        psP.setInt(2, contratoIdReal);

                        // PLANTILLA 2: Tiene un único parámetro aislado. DEBE ARRANCAR EN 1
                        psC.setInt(1, contratoIdReal);

                        //  PLANTILLA 3: Tiene un único parámetro aislado. DEBE ARRANCAR EN 1
                        psU.setInt(1, contratoIdReal);

                        // Ejecutamos la tripleta de impacto directo en el disco duro
                        psP.executeUpdate();
                        psC.executeUpdate();
                        psU.executeUpdate();

                        con.commit(); // ÉXITO ABSOLUTO: Forzamos el guardado inmutable en MySQL Workbench
                        out.print("{\"status\":\"success\",\"message\":\"¡Contrato prorrogado y colaborador reactivado con éxito en MySQL!\"}");
                        out.flush();
                    } catch (SQLException e) {
                        con.rollback(); // Deshace los cambios si ocurre un error físico real
                        throw e;
                    }
                } //CIERRA CONEXIÓN JDBC

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
                out.flush();
            } // 

            // LA COMPUERTA EXTRA CONECTADA CON ÉXITO AL NUEVO MÉTODO TRANSACCIONAL DEL DAO:
        } else if ("darDeBaja".equals(accion)) {
            String contratoIdStr = request.getParameter("contratoId");

            if (contratoIdStr == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"status\":\"error\",\"message\":\"ID de contrato ausente.\"}");
                return;
            }

            try {
                int contratoId = Integer.parseInt(contratoIdStr.trim());

                // Invocamos al método transaccional del ContratoDAO (El Ajuste 3)
                boolean exito = contratoDAO.clausurarContratoYEmpleado(contratoId);

                if (exito) {
                    // El Servlet responde con éxito alineado al token 'success' que espera contratos.js
                    out.print("{\"status\":\"success\",\"message\":\"Colaborador desvinculado de forma definitiva en MySQL.\"}");
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"status\":\"error\",\"message\":\"Operación rechazada. Verifique restricciones en la BD.\"}");
                }
                out.flush();

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        } else if ("calcularPagoPeriodo".equals(accion)) {
            String contratoIdStr = request.getParameter("contratoId");

            // CAPTURA DINÁMICA: Java recibe el mes exacto que el Contador picó en la web
            String periodoMes = request.getParameter("periodoMes");

            if (periodoMes == null || periodoMes.trim().isEmpty()) {
                periodoMes = "2026-07"; // Fallback de seguridad si viaja nulo
            }

            try {
                int contratoId = Integer.parseInt(contratoIdStr.trim());
                ontime.ontimebackend.modelo.Contrato c = contratoDAO.buscarContratoPorId(contratoId);

                double salarioMensual = c.getSalarioBase().doubleValue();
                double valorDia = salarioMensual / 30.0;

                ontime.ontimebackend.dao.NominaDAO nominaDAO = new ontime.ontimebackend.dao.NominaDAO();

                // LA VICTORIA COMPLETA: Le pasamos la variable 'periodoMes' real al query
                java.util.List<ontime.ontimebackend.modelo.Nomina> registrosNomina = nominaDAO.obtenerReporteNomina(periodoMes);

                int diasAsistidosReales = 0; // Inicializamos los acumuladores en cero
                double dineroExtras = 0.0;
                double dineroDeducciones = 0.0;

                // Buscamos dentro de la lista al empleado que le pertenece este contrato
                if (registrosNomina != null) {
                    System.out.println("🔀 TOTAL DE REGISTROS DE NÓMINA ENCONTRADOS: " + registrosNomina.size());
                    for (ontime.ontimebackend.modelo.Nomina n : registrosNomina) {

                        // FILTRO DE ALTA INGENIERÍA:
                        // Convertimos ambos IDs a texto plano limpio de espacios en la RAM.
                        // Esto rompe cualquier diferencia de mayúsculas, tipos de datos o referencias de memoria.
                        String idNominaStr = String.valueOf(n.getUsuarioId()).trim();
                        String idContratoStr = String.valueOf(c.getUsuarioId()).trim();

                        System.out.println(" COMPARANDO: ID Nómina [" + idNominaStr + "] contra ID Contrato [" + idContratoStr + "]");

                        if (idNominaStr.equals(idContratoStr)) {
                            // Al coincidir las cadenas, succionamos los datos reales del Pinpad
                            diasAsistidosReales = n.getDiasAsistidos();
                            dineroExtras = n.getTotalHorasExtras();
                            dineroDeducciones = (double) n.getTotalRetardos();
                            System.out.println(" MATCH EXITOSO PARA: " + idContratoStr + " | Días: " + diasAsistidosReales);
                            
                            // Multiplicamos la cantidad entera de retardos (ej: 2) por la constante de $1.000 COP
                            int cantidadRetardos = n.getTotalRetardos(); 
                            dineroDeducciones = (double) (cantidadRetardos * 1000); // ◄ Ahora 2 retardos se transforman en $2.000 reales
                            
                            break; // Rompemos el ciclo al consolidar al trabajador
                        }
                    }
                }

                // LA MATEMÁTICA EN LA RAM DEL SERVIDOR:
                // Multiplica el valor del día por los días reales que el empleado marcó en el Pinpad
                double sueldoProporcionalCausado = valorDia * diasAsistidosReales;
                double netoFinalGirar = (sueldoProporcionalCausado + dineroExtras) - dineroDeducciones;

                // PAYLOAD INMUTABLE DE RED: Despachamos los resultados en JSON
                out.print("{");
                out.print("\"status\":\"success\",");
                out.print("\"salarioMensual\":" + salarioMensual + ",");
                out.print("\"diasLaborados\":" + diasAsistidosReales + ",");
                out.print("\"valorDia\":" + valorDia + ",");
                out.print("\"sueldoProporcional\":" + sueldoProporcionalCausado + ",");
                out.print("\"extras\":" + dineroExtras + ",");
                out.print("\"deducciones\":" + dineroDeducciones + ",");
                out.print("\"netoGirar\":" + netoFinalGirar);
                out.print("}");
                out.flush();

            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.print("{\"status\":\"error\",\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
            }
        }

    }

}
